package com.autocare.vehicleservice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autocare.vehicleservice.entity.Booking;
import com.autocare.vehicleservice.entity.BookingItem;
import com.autocare.vehicleservice.entity.Customer;
import com.autocare.vehicleservice.entity.Invoice;
import com.autocare.vehicleservice.entity.Payment;
import com.autocare.vehicleservice.entity.ServiceItem;
import com.autocare.vehicleservice.entity.Vehicle;

import com.autocare.vehicleservice.repository.BookingRepository;
import com.autocare.vehicleservice.repository.CustomerRepository;
import com.autocare.vehicleservice.repository.InvoiceRepository;
import com.autocare.vehicleservice.repository.PaymentRepository;
import com.autocare.vehicleservice.repository.ServiceItemRepository;
import com.autocare.vehicleservice.repository.VehicleRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public BookingService(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            ServiceItemRepository serviceItemRepository,
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {

        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public List<Booking> findRecentBookings(int limit) {
        return bookingRepository.findAllByOrderByIdDesc(
                PageRequest.of(0, limit));
    }

    public long countAllBookings() {
        return bookingRepository.count();
    }

    public long countBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    public List<Booking> findCustomerBookings(Long customerId) {
        return bookingRepository
                .findByCustomerIdOrderByServiceDateDesc(customerId);
    }

    public List<Booking> findCompletedCustomerBookings(Long customerId) {
        return bookingRepository
                .findByCustomerIdAndStatusOrderByServiceDateDesc(
                        customerId,
                        Booking.BookingStatus.COMPLETED);
    }

    public List<Booking> findBookingsByStatus(
            Booking.BookingStatus status) {

        return bookingRepository.findByStatusOrderByServiceDateDesc(status);
    }

    public Booking findCustomerBookingById(
            Long bookingId,
            Long customerId) {

        return bookingRepository.findById(bookingId)
                .filter(booking ->
                        booking.getCustomer().getId().equals(customerId))
                .orElseThrow();
    }

    @Transactional
    public Booking create(
            Long customerId,
            Long vehicleId,
            List<Long> serviceIds,
            LocalDate date,
            LocalTime time,
            String notes) {

        if (customerId == null) {
            throw new IllegalArgumentException("Customer is required.");
        }

        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle is required.");
        }

        if (date == null) {
            throw new IllegalArgumentException("Service date is required.");
        }

        if (time == null) {
            throw new IllegalArgumentException("Service time is required.");
        }

        if (serviceIds == null || serviceIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Select at least one service.");
        }

        Set<Long> uniqueServiceIds = new LinkedHashSet<>(serviceIds);

        if (uniqueServiceIds.contains(null)) {
            throw new IllegalArgumentException(
                    "Invalid service selection.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found."));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Vehicle not found."));

        if (vehicle.getCustomer() == null
                || !vehicle.getCustomer().getId().equals(customerId)) {

            throw new IllegalArgumentException(
                    "This vehicle does not belong to the customer.");
        }

        List<ServiceItem> selectedServices = uniqueServiceIds.stream()
                .map(serviceId ->
                        serviceItemRepository.findById(serviceId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Service not found: "
                                                        + serviceId)))
                .collect(Collectors.toList());

        List<Booking.BookingStatus> eligibleStatuses = List.of(
                Booking.BookingStatus.PENDING);

        Booking existingBooking = bookingRepository
                .findFirstByCustomerIdAndVehicleIdAndServiceDateAndStatusInOrderByIdDesc(
                        customerId,
                        vehicleId,
                        date,
                        eligibleStatuses)
                .orElse(null);

        if (existingBooking != null) {

            Set<Long> existingServiceIds = new HashSet<>();

            for (BookingItem item : existingBooking.getBookingItems()) {
                if (item.getService() != null) {
                    existingServiceIds.add(item.getService().getId());
                }
            }

            if (existingBooking.getService() != null) {
                existingServiceIds.add(
                        existingBooking.getService().getId());
            }

            for (ServiceItem service : selectedServices) {
                if (existingServiceIds.contains(service.getId())) {
                    throw new IllegalArgumentException(
                            "This service is already booked for this vehicle "
                                    + "on this date. Please select a different service.");
                }
            }

            BigDecimal subtotal = BigDecimal.ZERO;

            for (BookingItem existingItem
                    : existingBooking.getBookingItems()) {

                subtotal = subtotal.add(
                        BigDecimal.valueOf(existingItem.getServicePrice()));
            }

            for (ServiceItem service : selectedServices) {

                BookingItem item = new BookingItem();

                item.setService(service);
                item.setServicePrice(service.getBasePrice());

                existingBooking.addBookingItem(item);

                subtotal = subtotal.add(
                        BigDecimal.valueOf(service.getBasePrice()));
            }

            existingBooking.setEstimatedAmount(subtotal);

            return bookingRepository.save(existingBooking);
        }

        Booking booking = new Booking();

        booking.setCustomer(customer);
        booking.setVehicle(vehicle);
        booking.setServiceDate(date);
        booking.setServiceTime(time);
        booking.setNotes(notes);
        booking.setStatus(Booking.BookingStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        boolean firstService = true;

        for (ServiceItem service : selectedServices) {

            if (firstService) {
                booking.setService(service);
                firstService = false;
            }

            BookingItem item = new BookingItem();

            item.setService(service);
            item.setServicePrice(service.getBasePrice());

            booking.addBookingItem(item);

            subtotal = subtotal.add(
                    BigDecimal.valueOf(service.getBasePrice()));
        }

        booking.setEstimatedAmount(subtotal);

        return bookingRepository.save(booking);
    }

    @Transactional
    public void updateStatus(
            Long bookingId,
            Booking.BookingStatus status) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow();

        booking.setStatus(status);

        bookingRepository.save(booking);

        if (status == Booking.BookingStatus.COMPLETED) {
            createPaymentAndInvoice(booking);
        }
    }

    private void createPaymentAndInvoice(Booking booking) {

        double subtotal = booking.getEstimatedAmount().doubleValue();
        double tax = subtotal * 0.18;
        double totalAmount = subtotal + tax;

        /*
         * Create a payment only if one does not already exist.
         *
         * Completing a service booking does not mean payment
         * has been received.
         */
        Payment payment = paymentRepository
                .findByBookingId(booking.getId())
                .orElse(null);

        if (payment == null) {

            payment = new Payment();

            payment.setBooking(booking);
            payment.setAmount(totalAmount);
            payment.setMethod(null);
            payment.setStatus("Pending");
            payment.setPaidAt(null);

            paymentRepository.save(payment);
        }

        /*
         * Create the invoice only if it does not already exist.
         *
         * The invoice status reflects the actual payment status.
         */
        Invoice invoice = invoiceRepository
                .findByBookingId(booking.getId())
                .orElse(null);

        if (invoice == null) {

            invoice = new Invoice();

            invoice.setBooking(booking);
            invoice.setInvoiceNumber(
                    "INV-" + System.currentTimeMillis());

            invoice.setInvoiceDate(LocalDate.now());
            invoice.setSubtotal(subtotal);
            invoice.setTax(tax);
            invoice.setTotalAmount(totalAmount);

            invoice.setStatus(
                    isPaymentPaid(payment) ? "Paid" : "Pending");

            invoiceRepository.save(invoice);
        }
    }

    private boolean isPaymentPaid(Payment payment) {
        return payment != null
                && payment.getStatus() != null
                && payment.getStatus().equalsIgnoreCase("Paid");
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElseThrow();
    }

    public Invoice findInvoice(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId).orElse(null);
    }

    public Payment findPayment(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).orElse(null);
    }
}
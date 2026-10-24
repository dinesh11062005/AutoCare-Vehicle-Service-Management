
package com.autocare.vehicleservice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.autocare.vehicleservice.entity.Booking;
import com.autocare.vehicleservice.entity.Payment;
import com.autocare.vehicleservice.repository.CustomerRepository;
import com.autocare.vehicleservice.repository.InvoiceRepository;
import com.autocare.vehicleservice.repository.PaymentRepository;
import com.autocare.vehicleservice.repository.ServiceItemRepository;
import com.autocare.vehicleservice.repository.VehicleRepository;
import com.autocare.vehicleservice.service.BookingService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookingService bookingService;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    private static final Path CUSTOMER_UPLOAD_DIR =
            Paths.get("data", "uploads", "customers")
                    .toAbsolutePath()
                    .normalize();

    private static final Path VEHICLE_UPLOAD_DIR =
            Paths.get("data", "uploads", "vehicles")
                    .toAbsolutePath()
                    .normalize();

    public AdminController(
            BookingService bookingService,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            ServiceItemRepository serviceItemRepository,
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository) {

        this.bookingService = bookingService;
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // ==================== ADMIN DASHBOARD ====================

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {

        model.addAttribute(
                "totalBookings",
                bookingService.countAllBookings());

        model.addAttribute(
                "pendingCount",
                bookingService.countBookingsByStatus(
                        Booking.BookingStatus.PENDING));

        model.addAttribute(
                "inProgressCount",
                bookingService.countBookingsByStatus(
                        Booking.BookingStatus.IN_PROGRESS));

        model.addAttribute(
                "completedCount",
                bookingService.countBookingsByStatus(
                        Booking.BookingStatus.COMPLETED));

        model.addAttribute(
                "cancelledCount",
                bookingService.countBookingsByStatus(
                        Booking.BookingStatus.CANCELLED));

        model.addAttribute(
                "recentBookings",
                bookingService.findRecentBookings(5));

        // Display active customers only.
        model.addAttribute(
                "customers",
                customerRepository.findByActiveTrue());

        model.addAttribute(
                "vehicles",
                vehicleRepository.findAll());

        model.addAttribute(
                "services",
                serviceItemRepository.findAll());

        return "admin/dashboard";
    }

    // ==================== SERVICE REQUESTS ====================

    @GetMapping("/service-requests")
    public String serviceRequests(Model model) {

        model.addAttribute(
                "bookings",
                bookingService.findAll());

        return "admin/service-requests";
    }

    // ==================== WORK IN PROGRESS ====================

    @GetMapping("/work-in-progress")
    public String workInProgress(Model model) {

        model.addAttribute(
                "bookings",
                bookingService.findBookingsByStatus(
                        Booking.BookingStatus.IN_PROGRESS));

        return "admin/work-in-progress";
    }

    // ==================== COMPLETED SERVICES ====================

    @GetMapping("/completed")
    public String completed(Model model) {

        model.addAttribute(
                "bookings",
                bookingService.findBookingsByStatus(
                        Booking.BookingStatus.COMPLETED));

        return "admin/completed";
    }

    // ==================== CUSTOMERS ====================

    @GetMapping("/customers")
    public String customers(Model model) {

        // Deactivated customers are excluded.
        model.addAttribute(
                "customers",
                customerRepository.findByActiveTrue());

        return "admin/customers";
    }

    // ==================== CUSTOMER PROFILE PHOTO ====================

    @GetMapping("/customer-photo/{id}")
    public ResponseEntity<Resource> getCustomerPhoto(
            @PathVariable("id") Long id) {

        var customer = customerRepository.findById(id).orElse(null);

        if (customer == null
                || customer.getProfilePhoto() == null
                || customer.getProfilePhoto().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        String filename;

        try {
            filename = Paths.get(customer.getProfilePhoto())
                    .getFileName()
                    .toString();

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }

        return serveImage(CUSTOMER_UPLOAD_DIR, filename);
    }

    // ==================== VEHICLE IMAGE ====================

    @GetMapping("/vehicle-image/{id}")
    public ResponseEntity<Resource> getVehicleImage(
            @PathVariable("id") Long id) {

        var vehicle = vehicleRepository.findById(id).orElse(null);

        if (vehicle == null
                || vehicle.getImage() == null
                || vehicle.getImage().isBlank()) {

            return ResponseEntity.notFound().build();
        }

        String filename;

        try {
            filename = Paths.get(vehicle.getImage())
                    .getFileName()
                    .toString();

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }

        return serveImage(VEHICLE_UPLOAD_DIR, filename);
    }

    // ==================== SHARED IMAGE RESPONSE ====================

    private ResponseEntity<Resource> serveImage(
            Path uploadDirectory,
            String filename) {

        try {
            Path imagePath = uploadDirectory.resolve(filename)
                    .normalize();

            if (!imagePath.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.isRegularFile(imagePath)
                    || !Files.isReadable(imagePath)) {

                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imagePath);

            if (contentType == null
                    || !contentType.startsWith("image/")) {

                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(imagePath.toUri());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException | RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== VEHICLES ====================

    @GetMapping("/vehicles")
    public String vehicles(Model model) {

        model.addAttribute(
                "vehicles",
                vehicleRepository.findAll());

        return "admin/vehicles";
    }

    // ==================== PAYMENTS ====================

    @GetMapping("/payments")
    public String payments(Model model) {

        List<Payment> payments = paymentRepository.findAll();

        long totalPayments = payments.size();

        double totalAmount = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        double paidAmount = payments.stream()
                .filter(payment ->
                        payment.getStatus() != null
                                && payment.getStatus()
                                        .equalsIgnoreCase("Paid"))
                .mapToDouble(Payment::getAmount)
                .sum();

        double pendingAmount = payments.stream()
                .filter(payment ->
                        payment.getStatus() != null
                                && payment.getStatus()
                                        .equalsIgnoreCase("Pending"))
                .mapToDouble(Payment::getAmount)
                .sum();

        model.addAttribute("payments", payments);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("pendingAmount", pendingAmount);

        return "admin/payments";
    }

    // ==================== UPDATE PAYMENT METHOD / STATUS ====================

    @PostMapping("/payments/{id}/update")
    public String updatePayment(
            @PathVariable("id") Long id,
            @RequestParam("method") String method,
            @RequestParam("action") String action,
            RedirectAttributes redirectAttributes) {

        Payment payment = paymentRepository.findById(id).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Payment record not found.");

            return "redirect:/admin/payments";
        }

        if ("Paid".equalsIgnoreCase(payment.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This payment is already marked as paid.");

            return "redirect:/admin/payments";
        }

        Set<String> allowedMethods = Set.of(
                "Cash",
                "Google Pay",
                "PhonePe",
                "Paytm",
                "UPI",
                "Debit Card",
                "Credit Card");

        if (method == null || !allowedMethods.contains(method)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a valid payment method.");

            return "redirect:/admin/payments";
        }

        if ("save-method".equals(action)) {

            payment.setMethod(method);
            payment.setStatus("Pending");
            payment.setPaidAt(null);
            paymentRepository.save(payment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment method saved. Payment remains pending.");

            return "redirect:/admin/payments";
        }

        if ("confirm-cash".equals(action)) {

            if (!"Cash".equals(method)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Only cash payments can be manually confirmed.");

                return "redirect:/admin/payments";
            }

            payment.setMethod("Cash");
            payment.setStatus("Paid");
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cash payment confirmed successfully.");

            return "redirect:/admin/payments";
        }

        if ("demo-payment".equals(action)) {

            if ("Cash".equals(method)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Use Confirm Cash for cash payments.");

                return "redirect:/admin/payments";
            }

            payment.setMethod(method);
            payment.setStatus("Pending");
            payment.setPaidAt(null);
            paymentRepository.save(payment);

            return "redirect:/admin/payments/"
                    + id
                    + "/demo-confirmation";
        }

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Invalid payment action.");

        return "redirect:/admin/payments";
    }

    // ==================== DEMO PAYMENT CONFIRMATION ====================

    @GetMapping("/payments/{id}/demo-confirmation")
    public String demoPaymentConfirmation(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Payment payment = paymentRepository.findById(id).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Payment record not found.");

            return "redirect:/admin/payments";
        }

        if ("Paid".equalsIgnoreCase(payment.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This payment is already completed.");

            return "redirect:/admin/payments";
        }

        Set<String> demoMethods = Set.of(
                "Google Pay",
                "PhonePe",
                "Paytm",
                "UPI",
                "Debit Card",
                "Credit Card");

        if (payment.getMethod() == null
                || !demoMethods.contains(payment.getMethod())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Select an online payment method before continuing.");

            return "redirect:/admin/payments";
        }

        model.addAttribute("payment", payment);
        model.addAttribute("demoPayment", true);

        return "admin/demo-payment-confirmation";
    }

    @PostMapping("/payments/{id}/demo-complete")
    public String completeDemoPayment(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        Payment payment = paymentRepository.findById(id).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Payment record not found.");

            return "redirect:/admin/payments";
        }

        if ("Paid".equalsIgnoreCase(payment.getStatus())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This payment is already completed.");

            return "redirect:/admin/payments";
        }

        Set<String> demoMethods = Set.of(
                "Google Pay",
                "PhonePe",
                "Paytm",
                "UPI",
                "Debit Card",
                "Credit Card");

        if (payment.getMethod() == null
                || !demoMethods.contains(payment.getMethod())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid demo payment method.");

            return "redirect:/admin/payments";
        }

        // DEMO ONLY: This simulates success.
        // It does not transfer or verify real money.
        payment.setStatus("Paid");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "DEMO PAYMENT completed successfully.");

        return "redirect:/admin/payments";
    }

    // ==================== REPORTS ====================

    @GetMapping("/reports")
    public String reports(Model model) {

        model.addAttribute(
                "bookings",
                bookingService.findAll());

        // Exclude deactivated customers from reports.
        model.addAttribute(
                "customers",
                customerRepository.findByActiveTrue());

        model.addAttribute(
                "vehicles",
                vehicleRepository.findAll());

        model.addAttribute(
                "payments",
                paymentRepository.findAll());

        model.addAttribute(
                "invoices",
                invoiceRepository.findAll());

        return "admin/reports";
    }

    // ==================== UPDATE BOOKING STATUS ====================

    @PostMapping("/booking/{id}/status")
    public String updateBookingStatus(
            @PathVariable("id") Long id,
            @RequestParam("value") Booking.BookingStatus value,
            @RequestParam(
                    value = "redirectTo",
                    defaultValue = "dashboard") String redirectTo) {

        bookingService.updateStatus(id, value);

        switch (redirectTo) {

            case "work-in-progress":
                return "redirect:/admin/work-in-progress";

            case "completed":
                return "redirect:/admin/completed";

            case "service-requests":
                return "redirect:/admin/service-requests";

            default:
                return "redirect:/admin/dashboard";
        }
    }

    // ==================== INVOICE ====================

    @GetMapping("/invoice/{bookingId}")
    public String invoice(
            @PathVariable("bookingId") Long bookingId,
            Model model) {

        Booking booking = bookingService.findById(bookingId);

        model.addAttribute("booking", booking);

        model.addAttribute(
                "invoice",
                bookingService.findInvoice(bookingId));

        model.addAttribute(
                "payment",
                bookingService.findPayment(bookingId));

        return "customer/invoice";
    }
}


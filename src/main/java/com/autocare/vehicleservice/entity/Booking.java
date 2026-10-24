package com.autocare.vehicleservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Vehicle is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Retained for compatibility with existing bookings and pages.
    @NotNull(message = "Service is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem service;

    @OneToMany(
        mappedBy = "booking",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<BookingItem> bookingItems = new ArrayList<>();

    @NotNull(message = "Service date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate serviceDate;

    @NotNull(message = "Service time is required")
    @Column(name = "booking_time", nullable = false)
    private LocalTime serviceTime;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @DecimalMin(value = "0.00", message = "Amount cannot be negative")
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal estimatedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.PENDING;

    public enum BookingStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public Booking() {
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ServiceItem getService() {
        return service;
    }

    public void setService(ServiceItem service) {
        this.service = service;
    }

    public List<BookingItem> getBookingItems() {
        return bookingItems;
    }

    public void setBookingItems(List<BookingItem> bookingItems) {
        this.bookingItems.clear();

        if (bookingItems != null) {
            for (BookingItem item : bookingItems) {
                addBookingItem(item);
            }
        }
    }

    public void addBookingItem(BookingItem item) {
        if (item != null) {
            item.setBooking(this);
            this.bookingItems.add(item);
        }
    }

    public void removeBookingItem(BookingItem item) {
        if (item != null && this.bookingItems.remove(item)) {
            item.setBooking(null);
        }
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public LocalTime getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(LocalTime serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount != null
                ? estimatedAmount
                : BigDecimal.ZERO;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount != null
                ? estimatedAmount
                : BigDecimal.ZERO;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
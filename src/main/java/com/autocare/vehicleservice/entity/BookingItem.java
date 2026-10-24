package com.autocare.vehicleservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_items")
public class BookingItem {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "booking_id", nullable = false)
private Booking booking;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "service_id", nullable = false)
private ServiceItem service;

@Column(name = "service_price", nullable = false)
private double servicePrice;

public BookingItem() {
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public Booking getBooking() {
    return booking;
}

public void setBooking(Booking booking) {
    this.booking = booking;
}

public ServiceItem getService() {
    return service;
}

public void setService(ServiceItem service) {
    this.service = service;
}

public double getServicePrice() {
    return servicePrice;
}

public void setServicePrice(double servicePrice) {
    this.servicePrice = servicePrice;
}

}

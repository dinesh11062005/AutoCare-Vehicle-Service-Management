package com.autocare.vehicleservice.repository;

import com.autocare.vehicleservice.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.autocare.vehicleservice.repository.BookingRepository;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
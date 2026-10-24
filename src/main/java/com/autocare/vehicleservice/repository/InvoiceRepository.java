package com.autocare.vehicleservice.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.autocare.vehicleservice.entity.Invoice;
import com.autocare.vehicleservice.repository.BookingRepository;
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByBookingId(Long bookingId);
}

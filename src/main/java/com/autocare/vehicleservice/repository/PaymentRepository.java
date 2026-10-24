package com.autocare.vehicleservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.autocare.vehicleservice.entity.Payment;
import com.autocare.vehicleservice.repository.BookingRepository;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByBookingId(Long bookingId);

	List<Payment> findByBookingCustomerIdOrderByIdDesc(Long customerId);
	
	@Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
		       "WHERE p.booking.customer.id = :customerId " +
		       "AND LOWER(p.status) = 'paid'")
		double calculateTotalSpentByCustomerId(@Param("customerId") Long customerId);
}
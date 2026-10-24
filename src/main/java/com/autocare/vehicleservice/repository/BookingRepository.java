package com.autocare.vehicleservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.autocare.vehicleservice.entity.Booking;
import com.autocare.vehicleservice.entity.Booking.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByVehicleIdAndStatusIn(
            Long vehicleId,
            List<BookingStatus> statuses
    );

    List<Booking> findByCustomerIdOrderByServiceDateDesc(Long customerId);

    List<Booking> findByCustomerIdAndStatusOrderByServiceDateDesc(
            Long customerId,
            BookingStatus status
    );

    List<Booking> findByStatusOrderByServiceDateDesc(
            BookingStatus status
    );

    List<Booking> findAllByOrderByIdDesc(Pageable pageable);

    long countByStatus(BookingStatus status);

    boolean existsByVehicleId(Long vehicleId);

    // Find an existing booking for the same customer, vehicle, date and status.
    Optional<Booking> findFirstByCustomerIdAndVehicleIdAndServiceDateAndStatusInOrderByIdDesc(
            Long customerId,
            Long vehicleId,
            LocalDate serviceDate,
            List<BookingStatus> statuses
    );
}
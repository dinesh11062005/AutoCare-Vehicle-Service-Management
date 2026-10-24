package com.autocare.vehicleservice.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autocare.vehicleservice.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
	Optional<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);

	boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    // Display only vehicles that have not been deleted.
    List<Vehicle> findByCustomerIdAndDeletedFalse(Long customerId);

    // Find a vehicle belonging to the customer if it is not deleted.
    Optional<Vehicle> findByIdAndCustomerIdAndDeletedFalse(
            Long id,
            Long customerId
    );
}
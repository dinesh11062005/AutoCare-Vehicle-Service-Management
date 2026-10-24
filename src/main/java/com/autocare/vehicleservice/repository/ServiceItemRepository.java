package com.autocare.vehicleservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.autocare.vehicleservice.entity.ServiceItem;
import com.autocare.vehicleservice.repository.BookingRepository;
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
}

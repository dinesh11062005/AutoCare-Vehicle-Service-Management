package com.autocare.vehicleservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.autocare.vehicleservice.entity.Customer;
import com.autocare.vehicleservice.entity.ServiceItem;
import com.autocare.vehicleservice.repository.CustomerRepository;
import com.autocare.vehicleservice.repository.ServiceItemRepository;

@SpringBootApplication
public class VehicleServiceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceManagementApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner seedData(
            CustomerRepository customerRepository,
            ServiceItemRepository serviceItemRepository) {

        return args -> {

            if (customerRepository.count() == 0) {
                Customer customer = new Customer();
                customer.setName("Dinesh");
                customer.setEmail("dinesh@autocare.com");
                customer.setPhone("9876543210");
                customer.setAddress("Chennai, Tamil Nadu");

                // Preserve the original demo password for now.
                // Registration and login must be updated together
                // before switching existing passwords to BCrypt.
                customer.setPassword("demo123");

                customerRepository.save(customer);
            }

            if (serviceItemRepository.count() == 0) {
                createService(
                        serviceItemRepository,
                        "General Service",
                        "Complete inspection and routine maintenance",
                        1500,
                        "general-service.svg");

                createService(
                        serviceItemRepository,
                        "Oil Change",
                        "Engine oil and filter replacement",
                        800,
                        "oil-change.svg");

                createService(
                        serviceItemRepository,
                        "Brake Service",
                        "Brake inspection and maintenance",
                        1200,
                        "brake-service.svg");

                createService(
                        serviceItemRepository,
                        "Tyre Service",
                        "Tyre inspection, rotation and balancing",
                        800,
                        "tyre-service.svg");

                createService(
                        serviceItemRepository,
                        "Battery Replacement",
                        "Battery health check and replacement",
                        1000,
                        "battery-service.svg");

                createService(
                        serviceItemRepository,
                        "AC Service",
                        "Air-conditioning inspection and cleaning",
                        1800,
                        "ac-service.svg");

                createService(
                        serviceItemRepository,
                        "Engine Repair",
                        "Engine diagnostics and repair",
                        2500,
                        "engine-service.svg");

                createService(
                        serviceItemRepository,
                        "Electrical Service",
                        "Electrical diagnostics and repair",
                        900,
                        "electrical-service.svg");
            }
        };
    }

    private void createService(
            ServiceItemRepository repo,
            String name,
            String description,
            double price,
            String image) {

        ServiceItem service = new ServiceItem();
        service.setName(name);
        service.setDescription(description);
        service.setBasePrice(price);
        service.setImage(image);

        repo.save(service);
    }
}
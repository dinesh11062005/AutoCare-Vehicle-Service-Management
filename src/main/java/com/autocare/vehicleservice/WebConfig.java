package com.autocare.vehicleservice;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Vehicle images
        String vehicleUploadPath = Paths.get("data", "uploads", "vehicles")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        if (!vehicleUploadPath.endsWith("/")) {
            vehicleUploadPath += "/";
        }

        registry.addResourceHandler("/uploads/vehicles/**")
                .addResourceLocations(vehicleUploadPath);

        // Customer profile photos
        String customerUploadPath = Paths.get("data", "uploads", "customers")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        if (!customerUploadPath.endsWith("/")) {
            customerUploadPath += "/";
        }

        registry.addResourceHandler("/uploads/customers/**")
                .addResourceLocations(customerUploadPath);
    }
}
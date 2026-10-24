# AutoCare - Vehicle Service Management System

A complete starter application for the Vehicle Service Management project.

## Stack
- Java 17
- Spring Boot 3.5.6
- Spring MVC
- Thymeleaf
- Spring Data JPA / Hibernate
- H2 demo database
- MySQL connector included
- HTML, CSS, JavaScript

## Run
1. Import as an Existing Maven Project in Spring Tool Suite 4 / Eclipse.
2. Wait for Maven dependencies to finish.
3. Run `VehicleServiceManagementApplication.java` as Spring Boot App.
4. Open http://localhost:8080

## Demo login
Email: dinesh@autocare.com
Password: demo123

## Customer flow
Home -> Login -> Dashboard -> My Vehicles -> Book Service -> My Bookings -> Invoice

## Service-center flow
Open http://localhost:8080/admin/dashboard

The project currently uses an H2 file database so it can start without requiring MySQL setup. The MySQL connector is already included and the MySQL properties are commented in `application.properties`.

## Important
This is the complete functional baseline. We can change the UI, database design, roles, validation, security, image assets, invoice layout, booking workflow, and other requirements after you review it.

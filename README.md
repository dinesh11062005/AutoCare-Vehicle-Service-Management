# 🚗 AutoCare – Vehicle Service Management System

A web-based Vehicle Service Management System developed to simplify vehicle management, service booking, and service-center operations. AutoCare provides a user-friendly interface for customers and a dedicated dashboard for service-center administration.

**Created by Dinesh J. | Java Full Stack Developer**

---

## 📌 Project Overview

AutoCare is a Java-based web application built using Spring Boot, Thymeleaf, and database technologies. It allows customers to manage their vehicles, book services, view bookings, and access invoices. Administrators can manage customers, vehicles, service requests, and payments through a dedicated dashboard.

## 🛠️ Technology Stack

* **Java 17** – Backend programming
* **Spring Boot 3.5.6** – Application framework
* **Spring MVC** – Request handling and application flow
* **Thymeleaf** – Dynamic web pages
* **Spring Data JPA / Hibernate** – Database operations
* **H2 Database** – File-based demo database
* **MySQL Connector** – MySQL database connectivity
* **HTML5** – Web page structure
* **CSS3** – Styling and responsive layouts
* **JavaScript** – Frontend interactions
* **Maven** – Dependency management and project build

## ✨ Key Features

### 👤 Customer Features

* Customer registration and login
* Customer dashboard
* Vehicle registration and management
* Vehicle service booking
* Booking history
* Service history
* Invoice viewing
* Customer profile management

### ⚙️ Service-Center Features

* Dedicated administration dashboard
* Customer management
* Vehicle management
* Service request management
* Booking and service-status tracking
* Payment management
* Service-center operations overview

### 🎨 User Interface

* Clean and user-friendly design
* Dedicated customer and administrator pages
* Service information pages
* Vehicle service images and visual elements
* Organized navigation and dashboard layouts

## 🔄 Application Workflow

**Customer Flow**

Home → Login → Dashboard → My Vehicles → Book Service → My Bookings → Invoice

**Service-Center Flow**

Admin Dashboard → Manage Customers → Manage Vehicles → Manage Service Requests → Track Payments

## 📂 Project Structure

```text
AutoCare/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/autocare/vehicleservice/
│       │       ├── controller/
│       │       ├── entity/
│       │       ├── repository/
│       │       ├── service/
│       │       ├── VehicleServiceManagementApplication.java
│       │       └── WebConfig.java
│       └── resources/
│           ├── static/
│           │   ├── css/
│           │   ├── js/
│           │   └── images/
│           ├── templates/
│           └── application.properties
├── pom.xml
└── README.md
```

## 💻 Getting Started

### Prerequisites

Make sure you have the following installed:

* Java JDK 17
* Spring Tool Suite 4 or Eclipse
* Maven dependencies configured
* A web browser

### Installation and Setup

**1. Clone the repository**

```bash
git clone https://github.com/dinesh11062005/AutoCare-Vehicle-Service-Management.git
```

**2. Import the project**

Import the cloned folder into Spring Tool Suite 4 or Eclipse as an **Existing Maven Project**.

**3. Wait for dependencies**

Allow Maven to download and configure the required dependencies.

**4. Run the application**

Run `VehicleServiceManagementApplication.java` as a Spring Boot application.

**5. Open the application**

Visit the following address in your browser:

http://localhost:8080

## 🔑 Admin Dashboard

Access the AutoCare Service-Center Admin Dashboard using the link below:

**Admin Dashboard:** http://localhost:8080/admin/dashboard

**Note:** Start the Spring Boot application before opening this link. This is a local URL that works on your computer, not a publicly accessible website.


## 🗄️ Database Configuration

The application currently uses an H2 file-based database, allowing the demo application to start without requiring a MySQL installation.

The MySQL connector is included in the project, and the MySQL configuration properties are commented out in `application.properties`.

MySQL can be configured later if required.

## 🔐 Demo Login

The project provides the following demo login credentials:

* **Email:** `dinesh@autocare.com`
* **Password:** `demo123`

Use these credentials only if the demo account is configured in your local application. Do not use demo credentials for a publicly deployed production application.

## 🎯 Project Objectives

* Simplify vehicle service booking and tracking.
* Organize customer and vehicle information.
* Provide a centralized service-center dashboard.
* Demonstrate Java web application development using Spring Boot.
* Apply MVC architecture and database integration in a practical project.

## 🚀 Future Enhancements

* Online payment gateway integration
* Email notifications for service bookings
* Advanced analytics and service reports
* Enhanced authentication and authorization
* Cloud deployment
* Automated testing

*Future enhancements are planned improvements and may not be implemented in the current version.*

## 👨‍💻 About the Developer

**Dinesh J.**

BCA Graduate | Aspiring Java Full Stack Developer

AutoCare was developed as a practical project to apply Java, Spring Boot, database integration, and frontend web development concepts in a vehicle service management application.

## 📄 License

This project is intended for learning and portfolio demonstration. Contact the author for permission before redistributing the project if no separate license is provided.

---

⭐ **Thank you for visiting AutoCare!**

If you find this project interesting, feel free to explore the source code and learn from the implementation.

**Created with dedication by Dinesh J.**

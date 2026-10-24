
package com.autocare.vehicleservice.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import com.autocare.vehicleservice.entity.Customer;
import com.autocare.vehicleservice.repository.CustomerRepository;

@Controller
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Customer customer = customerRepository
                .findByEmail(email.trim())
                .orElse(null);

        if (customer == null
                || !passwordMatches(customer.getPassword(), password)) {

            model.addAttribute("error", "Invalid email or password");
            return "login";
        }

        // Deactivated customers cannot log in.
        if (!customer.isActive()) {
            model.addAttribute(
                    "error",
                    "Your account has been deactivated. Please register again to reactivate your account."
            );
            return "login";
        }

        // Upgrade legacy plain-text passwords to BCrypt.
        if (!isBCryptPassword(customer.getPassword())) {
            customer.setPassword(passwordEncoder.encode(password));
            customerRepository.save(customer);
        }

        session.setAttribute("customerId", customer.getId());
        session.setAttribute("customerName", customer.getName());

        return "redirect:/customer/dashboard";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            Customer submittedCustomer,
            Model model) {

        String email = submittedCustomer.getEmail() == null
                ? ""
                : submittedCustomer.getEmail().trim();

        if (email.isEmpty()) {
            model.addAttribute("error", "Email address is required");
            return "register";
        }

        Customer existingCustomer = customerRepository
                .findByEmail(email)
                .orElse(null);

        if (existingCustomer != null) {

            // Do not allow duplicate registration for active accounts.
            if (existingCustomer.isActive()) {
                model.addAttribute("error", "Email already registered");
                return "register";
            }

            // Reactivate the existing deactivated account.
            existingCustomer.setName(submittedCustomer.getName());
            existingCustomer.setEmail(email);
            existingCustomer.setPhone(submittedCustomer.getPhone());
            existingCustomer.setAddress(submittedCustomer.getAddress());
            existingCustomer.setPassword(
                    passwordEncoder.encode(submittedCustomer.getPassword())
            );
            existingCustomer.setActive(true);

            customerRepository.save(existingCustomer);

            return "redirect:/login?reactivated";
        }

        // Register a completely new customer.
        submittedCustomer.setEmail(email);
        submittedCustomer.setPassword(
                passwordEncoder.encode(submittedCustomer.getPassword())
        );
        submittedCustomer.setActive(true);

        customerRepository.save(submittedCustomer);

        return "redirect:/login?registered";
    }

    private boolean passwordMatches(
            String storedPassword,
            String submittedPassword) {

        if (storedPassword == null || submittedPassword == null) {
            return false;
        }

        if (isBCryptPassword(storedPassword)) {
            return passwordEncoder.matches(submittedPassword, storedPassword);
        }

        // Compatibility with existing plain-text passwords.
        return storedPassword.equals(submittedPassword);
    }

    private boolean isBCryptPassword(String password) {
        return password != null
                && password.matches("^\\$2[aby]\\$\\d{2}\\$.*");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}


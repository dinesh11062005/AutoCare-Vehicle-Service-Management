package com.autocare.vehicleservice.controller;

import java.io.IOException;

import java.io.InputStream;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.nio.file.StandardCopyOption;

import java.time.LocalDate;

import java.time.LocalTime;

import java.util.List;

import java.util.Set;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autocare.vehicleservice.entity.Booking;

import com.autocare.vehicleservice.entity.Customer;

import com.autocare.vehicleservice.entity.Vehicle;

import com.autocare.vehicleservice.repository.BookingRepository;

import com.autocare.vehicleservice.repository.CustomerRepository;

import com.autocare.vehicleservice.repository.PaymentRepository;

import com.autocare.vehicleservice.repository.ServiceItemRepository;

import com.autocare.vehicleservice.repository.VehicleRepository;

import com.autocare.vehicleservice.service.BookingService;

import jakarta.servlet.http.HttpSession;

@Controller

@RequestMapping("/customer")

public class CustomerController {

	private static final Path VEHICLE_UPLOAD_DIR =

			Paths.get("data", "uploads", "vehicles")

					.toAbsolutePath()

					.normalize();

	private static final Path CUSTOMER_UPLOAD_DIR =

			Paths.get("data", "uploads", "customers")

					.toAbsolutePath()

					.normalize();

	private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

	private static final Set<String> ALLOWED_IMAGE_TYPES =

			Set.of("image/jpeg", "image/png", "image/webp");

	private final CustomerRepository customerRepository;

	private final VehicleRepository vehicleRepository;

	private final ServiceItemRepository serviceItemRepository;

	private final BookingService bookingService;

	private final PaymentRepository paymentRepository;

	private final BookingRepository bookingRepository;

	private final PasswordEncoder passwordEncoder;

	public CustomerController(

			CustomerRepository customerRepository,

			VehicleRepository vehicleRepository,

			ServiceItemRepository serviceItemRepository,

			BookingService bookingService,

			PaymentRepository paymentRepository,

			BookingRepository bookingRepository,

			PasswordEncoder passwordEncoder) {

		this.customerRepository = customerRepository;

		this.vehicleRepository = vehicleRepository;

		this.serviceItemRepository = serviceItemRepository;

		this.bookingService = bookingService;

		this.paymentRepository = paymentRepository;

		this.bookingRepository = bookingRepository;

		this.passwordEncoder = passwordEncoder;

	}

	// ==================== SESSION ====================

	private Long customerId(HttpSession session) {

		Object id = session.getAttribute("customerId");

		return id == null ? null : (Long) id;

	}

	// ==================== IMAGE STORAGE ====================

	private String storeImage(

			MultipartFile image,

			Path uploadDirectory,

			boolean required) throws IOException {

		if (image == null || image.isEmpty()) {

			if (required) {

				throw new IllegalArgumentException(

						"Please select an image.");

			}

			return null;

		}

		if (image.getSize() > MAX_IMAGE_SIZE) {

			throw new IllegalArgumentException(

					"The image must be 5 MB or smaller.");

		}

		String contentType = image.getContentType();

		if (contentType == null

				|| !ALLOWED_IMAGE_TYPES.contains(contentType)) {

			throw new IllegalArgumentException(

					"Please upload a JPG, PNG, or WEBP image.");

		}

		String extension;

		switch (contentType) {

		case "image/jpeg":

			extension = ".jpg";

			break;

		case "image/png":

			extension = ".png";

			break;

		case "image/webp":

			extension = ".webp";

			break;

		default:

			throw new IllegalArgumentException(

					"Unsupported image format.");

		}

		Files.createDirectories(uploadDirectory);

		String filename = UUID.randomUUID() + extension;

		Path destination = uploadDirectory

				.resolve(filename)

				.normalize();

		if (!destination.startsWith(uploadDirectory)) {

			throw new IllegalArgumentException(

					"Invalid image destination.");

		}

		try (InputStream input = image.getInputStream()) {

			Files.copy(

					input,

					destination,

					StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {

			Files.deleteIfExists(destination);

			throw e;

		}

		return filename;

	}

	private void deleteImage(String filename, Path uploadDirectory) {

		if (filename == null || filename.isBlank()) {

			return;

		}

		try {

			Path imagePath = uploadDirectory

					.resolve(filename)

					.normalize();

			if (imagePath.startsWith(uploadDirectory)) {

				Files.deleteIfExists(imagePath);

			}

		} catch (IOException e) {

			// File cleanup failure must not break other operations.

		}

	}

	private String storeVehicleImage(MultipartFile image)

			throws IOException {

		return storeImage(image, VEHICLE_UPLOAD_DIR, true);

	}

	private void deleteVehicleImage(String filename) {

		deleteImage(filename, VEHICLE_UPLOAD_DIR);

	}

	private String storeCustomerPhoto(MultipartFile image)

			throws IOException {

		return storeImage(image, CUSTOMER_UPLOAD_DIR, true);

	}

	private void deleteCustomerPhoto(String filename) {

		deleteImage(filename, CUSTOMER_UPLOAD_DIR);

	}

	// ==================== DASHBOARD ====================

	@GetMapping("/dashboard")
	public String dashboard(HttpSession session, Model model) {

	    Long id = customerId(session);

	    if (id == null) {
	        return "redirect:/login";
	    }

	    Customer customer = customerRepository.findById(id).orElseThrow();

	    List<Booking> bookings = bookingService.findCustomerBookings(id);

	    model.addAttribute("customer", customer);
	    model.addAttribute("vehicles",
	            vehicleRepository.findByCustomerIdAndDeletedFalse(id));
	    model.addAttribute("bookings", bookings);
	    model.addAttribute("services", serviceItemRepository.findAll());

	    double totalSpent = paymentRepository.calculateTotalSpentByCustomerId(id);
	    model.addAttribute("totalSpent", totalSpent);

	    return "customer/dashboard";
	}

	// ==================== VEHICLES LIST ====================

	@GetMapping("/vehicles")

	public String vehicles(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		model.addAttribute(

				"vehicles",

				vehicleRepository.findByCustomerIdAndDeletedFalse(id));

		return "customer/vehicles";

	}

	// ==================== ADD VEHICLE FORM ====================

	@GetMapping("/vehicles/add")

	public String addVehicle(HttpSession session, Model model) {

		if (customerId(session) == null) {

			return "redirect:/login";

		}

		model.addAttribute("vehicle", new Vehicle());

		model.addAttribute("editMode", false);

		return "customer/vehicle-form";

	}

	// ==================== SAVE NEW VEHICLE ====================

	@PostMapping("/vehicles/save")

	public String saveVehicle(

			Vehicle vehicle,

			@RequestParam("vehicleImage") MultipartFile vehicleImage,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		String filename = null;

		try {

			String registrationNumber = vehicle.getRegistrationNumber();

			if (registrationNumber == null || registrationNumber.isBlank()) {

				redirectAttributes.addFlashAttribute(

						"errorMessage",

						"Vehicle registration number is required.");

				return "redirect:/customer/vehicles/add";

			}

			registrationNumber = registrationNumber.trim().toUpperCase();

			Customer customer = customerRepository.findById(id).orElse(null);

			if (customer == null) {

				session.invalidate();

				return "redirect:/login";

			}

			Vehicle existingVehicle = vehicleRepository

					.findByRegistrationNumberIgnoreCase(registrationNumber)

					.orElse(null);

			if (existingVehicle != null) {

				// Do not allow an active registration number to be added again.

				if (!existingVehicle.isDeleted()) {

					redirectAttributes.addFlashAttribute(

							"errorMessage",

							"This registration number is already registered.");

					return "redirect:/customer/vehicles/add";

				}

				// A deleted vehicle can only be restored by its original owner.

				if (existingVehicle.getCustomer() == null

						|| !existingVehicle.getCustomer().getId().equals(id)) {

					redirectAttributes.addFlashAttribute(

							"errorMessage",

							"This registration number is already registered.");

					return "redirect:/customer/vehicles/add";

				}

				// Restore the same row so its ID and booking history remain unchanged.

				existingVehicle.setMake(vehicle.getMake());

				existingVehicle.setModel(vehicle.getModel());

				existingVehicle.setRegistrationNumber(registrationNumber);

				existingVehicle.setVehicleType(vehicle.getVehicleType());

				existingVehicle.setYear(vehicle.getYear());

				existingVehicle.setMileage(vehicle.getMileage());

				existingVehicle.setDeleted(false);

				// On restore, keep the old image if no new image was selected.

				if (vehicleImage != null && !vehicleImage.isEmpty()) {

					filename = storeVehicleImage(vehicleImage);

					String oldImage = existingVehicle.getImage();

					existingVehicle.setImage(filename);

					vehicleRepository.save(existingVehicle);

					deleteVehicleImage(oldImage);

					filename = null; // The saved image must not be deleted by a later catch block.

				} else {

					vehicleRepository.save(existingVehicle);

				}

				redirectAttributes.addFlashAttribute(

						"successMessage",

						"Previously registered vehicle restored successfully.");

				return "redirect:/customer/vehicles";

			}

			// No previous record exists, so create a new vehicle.

			filename = storeVehicleImage(vehicleImage);

			vehicle.setCustomer(customer);

			vehicle.setRegistrationNumber(registrationNumber);

			vehicle.setImage(filename);

			vehicle.setDeleted(false);

			vehicleRepository.save(vehicle);

			filename = null; // The image is now associated with the saved vehicle.

			redirectAttributes.addFlashAttribute(

					"successMessage",

					"Vehicle added successfully.");

			return "redirect:/customer/vehicles";

		} catch (IllegalArgumentException e) {

			deleteVehicleImage(filename);

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					e.getMessage());

			return "redirect:/customer/vehicles/add";

		} catch (IOException e) {

			deleteVehicleImage(filename);

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"Could not save the vehicle image. Please try again.");

			return "redirect:/customer/vehicles/add";

		}

	}

	// ==================== EDIT VEHICLE FORM ====================

	@GetMapping("/vehicles/edit/{id}")

	public String editVehicle(

			@PathVariable Long id,

			HttpSession session,

			Model model,

			RedirectAttributes redirectAttributes) {

		Long customerId = customerId(session);

		if (customerId == null) {

			return "redirect:/login";

		}

		Vehicle vehicle = vehicleRepository

				.findByIdAndCustomerIdAndDeletedFalse(id, customerId)

				.orElse(null);

		if (vehicle == null) {

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"Vehicle not found.");

			return "redirect:/customer/vehicles";

		}

		model.addAttribute("vehicle", vehicle);

		model.addAttribute("editMode", true);

		return "customer/vehicle-form";

	}

	// ==================== UPDATE VEHICLE ====================

	@PostMapping("/vehicles/update/{id}")

	public String updateVehicle(

			@PathVariable Long id,

			Vehicle form,

			@RequestParam(value = "vehicleImage", required = false)

			MultipartFile vehicleImage,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long customerId = customerId(session);

		if (customerId == null) {

			return "redirect:/login";

		}

		Vehicle vehicle = vehicleRepository

				.findByIdAndCustomerIdAndDeletedFalse(id, customerId)

				.orElse(null);

		if (vehicle == null) {

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"Vehicle not found.");

			return "redirect:/customer/vehicles";

		}

		String oldImage = vehicle.getImage();

		String newImage = null;

		try {

			vehicle.setMake(form.getMake());

			vehicle.setModel(form.getModel());

			vehicle.setRegistrationNumber(form.getRegistrationNumber());

			vehicle.setVehicleType(form.getVehicleType());

			vehicle.setYear(form.getYear());

			vehicle.setMileage(form.getMileage());

			if (vehicleImage != null && !vehicleImage.isEmpty()) {

				newImage = storeVehicleImage(vehicleImage);

				vehicle.setImage(newImage);

			}

			vehicleRepository.save(vehicle);

			if (newImage != null) {

				deleteVehicleImage(oldImage);

			}

			redirectAttributes.addFlashAttribute(

					"successMessage",

					"Vehicle updated successfully.");

			return "redirect:/customer/vehicles";

		} catch (IllegalArgumentException e) {

			deleteVehicleImage(newImage);

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					e.getMessage());

			return "redirect:/customer/vehicles/edit/" + id;

		} catch (IOException e) {

			deleteVehicleImage(newImage);

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"Could not upload the new image. Please try again.");

			return "redirect:/customer/vehicles/edit/" + id;

		}

	}

	// ==================== DELETE VEHICLE ====================

	@PostMapping("/vehicles/delete/{id}")

	public String deleteVehicle(

			@PathVariable Long id,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long customerId = customerId(session);

		if (customerId == null) {

			return "redirect:/login";

		}

		Vehicle vehicle = vehicleRepository

				.findByIdAndCustomerIdAndDeletedFalse(id, customerId)

				.orElse(null);

		if (vehicle == null) {

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"Vehicle not found.");

			return "redirect:/customer/vehicles";

		}

		boolean hasActiveBookings =

				bookingRepository.existsByVehicleIdAndStatusIn(

						id,

						List.of(

								Booking.BookingStatus.PENDING,

								Booking.BookingStatus.IN_PROGRESS));

		if (hasActiveBookings) {

			redirectAttributes.addFlashAttribute(

					"errorMessage",

					"This vehicle cannot be deleted because it has pending or ongoing bookings.");

			return "redirect:/customer/vehicles";

		}

		vehicle.setDeleted(true);

		vehicleRepository.save(vehicle);

		redirectAttributes.addFlashAttribute(

				"successMessage",

				"Vehicle deleted successfully.");

		return "redirect:/customer/vehicles";

	}

	// ==================== BOOK SERVICE FORM ====================

	@GetMapping("/book-service")
	public String bookService(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {
			return "redirect:/login";
		}

		model.addAttribute("vehicles", vehicleRepository.findByCustomerIdAndDeletedFalse(id));

		model.addAttribute("services", serviceItemRepository.findAll());

		return "customer/book-service";
	}

	// ==================== SAVE BOOKING ====================

	@PostMapping("/book-service")
	public String saveBooking(@RequestParam("vehicleId") Long vehicleId,
			@RequestParam(value = "serviceIds", required = false) List<Long> serviceIds,
			@RequestParam("serviceDate") String serviceDate, @RequestParam("serviceTime") String serviceTime,
			@RequestParam(value = "notes", required = false) String notes, HttpSession session, Model model,
			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {
			return "redirect:/login";
		}

		// Validate selected services.
		if (serviceIds == null || serviceIds.isEmpty()) {
			return showBookingFormWithError(id, model, "Please select at least one service.");
		}

		// Confirm that this vehicle belongs to the logged-in customer.
		Vehicle vehicle = vehicleRepository.findByIdAndCustomerIdAndDeletedFalse(vehicleId, id).orElse(null);

		if (vehicle == null) {
			return showBookingFormWithError(id, model, "Please select a valid vehicle.");
		}

		try {
			LocalDate parsedDate = LocalDate.parse(serviceDate);
			LocalTime parsedTime = LocalTime.parse(serviceTime);

			if (parsedDate.isBefore(LocalDate.now())) {
				return showBookingFormWithError(id, model, "Please select today or a future date.");
			}

			// BookingService.create() performs the duplicate check
			// and merges different services into an eligible PENDING booking.
			Booking savedBooking = bookingService.create(id, vehicle.getId(), serviceIds, parsedDate, parsedTime,
					notes);

			// A successful booking redirects to the bookings page.
			redirectAttributes.addFlashAttribute("successMessage", "Your booking has been saved successfully.");

			return "redirect:/customer/bookings";

		} catch (IllegalArgumentException e) {

			// Return the form directly so the error is available in the model.
			return showBookingFormWithError(id, model,
					e.getMessage() == null ? "Unable to create the booking." : e.getMessage());

		} catch (Exception e) {

			// Log the real exception in the Spring Boot console.
			e.printStackTrace();

			return showBookingFormWithError(id, model, "Unable to create your booking. Please try again.");
		}
	}

	// ==================== SHOW BOOKING FORM WITH ERROR ====================

	private String showBookingFormWithError(Long customerId, Model model, String message) {

		model.addAttribute("vehicles", vehicleRepository.findByCustomerIdAndDeletedFalse(customerId));

		model.addAttribute("services", serviceItemRepository.findAll());

		model.addAttribute("errorMessage", message);

		return "customer/book-service";
	}
	// ==================== BOOKINGS ====================

	@GetMapping("/bookings")

	public String bookings(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		model.addAttribute(

				"bookings",

				bookingService.findCustomerBookings(id));

		return "customer/bookings";

	}

	// ==================== SERVICE HISTORY ====================

	@GetMapping("/service-history")

	public String serviceHistory(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		model.addAttribute(

				"bookings",

				bookingService.findCompletedCustomerBookings(id));

		return "customer/service-history";

	}

	// ==================== PAYMENTS ====================

	@GetMapping("/payments")

	public String payments(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		model.addAttribute(

				"payments",

				paymentRepository.findByBookingCustomerIdOrderByIdDesc(id));

		return "customer/payments";

	}

	// ==================== INVOICE ====================

	@GetMapping("/invoice/{bookingId}")
	public String invoice(@PathVariable Long bookingId, HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {
			return "redirect:/login";
		}

		Booking booking = bookingService.findCustomerBookingById(bookingId, id);

		if (booking == null) {
			return "redirect:/customer/bookings";
		}

		model.addAttribute("booking", booking);
		model.addAttribute("bookingItems", booking.getBookingItems());
		model.addAttribute("invoice", bookingService.findInvoice(bookingId));
		model.addAttribute("payment", bookingService.findPayment(bookingId));

		return "customer/invoice";
	}

	// ==================== PROFILE ====================

	@GetMapping("/profile")

	public String profile(HttpSession session, Model model) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		Customer customer =

				customerRepository.findById(id).orElseThrow();

		model.addAttribute("customer", customer);

		return "customer/profile";

	}

	// ==================== UPDATE PROFILE ====================

	@PostMapping("/profile")

	public String updateProfile(

			@RequestParam String name,

			@RequestParam(required = false) String phone,

			@RequestParam(required = false) String address,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		Customer customer =

				customerRepository.findById(id).orElseThrow();

		if (name == null || name.isBlank()) {

			redirectAttributes.addFlashAttribute(

					"profileError",

					"Full name is required.");

			return "redirect:/customer/profile";

		}

		customer.setName(name.trim());

		customer.setPhone(phone == null ? null : phone.trim());

		customer.setAddress(address == null ? null : address.trim());

		customerRepository.save(customer);

		session.setAttribute("customerName", customer.getName());

		return "redirect:/customer/profile?saved";

	}

	// ==================== UPLOAD / CHANGE PROFILE PHOTO ====================

	@PostMapping("/profile/photo")

	public String uploadProfilePhoto(

			@RequestParam("profilePhoto") MultipartFile profilePhoto,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		Customer customer =

				customerRepository.findById(id).orElse(null);

		if (customer == null) {

			session.invalidate();

			return "redirect:/login";

		}

		String newFilename = null;

		try {

			newFilename = storeCustomerPhoto(profilePhoto);

			String oldFilename = customer.getProfilePhoto();

			customer.setProfilePhoto(newFilename);

			customerRepository.save(customer);

			if (oldFilename != null && !oldFilename.equals(newFilename)) {

				deleteCustomerPhoto(oldFilename);

			}

			redirectAttributes.addFlashAttribute(

					"photoSuccess",

					"Profile photo updated successfully.");

			return "redirect:/customer/profile";

		} catch (IllegalArgumentException e) {

			deleteCustomerPhoto(newFilename);

			redirectAttributes.addFlashAttribute(

					"photoError",

					e.getMessage());

			return "redirect:/customer/profile";

		} catch (IOException e) {

			deleteCustomerPhoto(newFilename);

			redirectAttributes.addFlashAttribute(

					"photoError",

					"Could not upload the profile photo. Please try again.");

			return "redirect:/customer/profile";

		}

	}

	// ==================== REMOVE PROFILE PHOTO ====================

	@PostMapping("/profile/photo/remove")

	public String removeProfilePhoto(

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		Customer customer =

				customerRepository.findById(id).orElse(null);

		if (customer == null) {

			session.invalidate();

			return "redirect:/login";

		}

		String oldFilename = customer.getProfilePhoto();

		customer.setProfilePhoto(null);

		customerRepository.save(customer);

		deleteCustomerPhoto(oldFilename);

		redirectAttributes.addFlashAttribute(

				"photoSuccess",

				"Profile photo removed successfully.");

		return "redirect:/customer/profile";

	}

	// ==================== CHANGE PASSWORD ====================

	@PostMapping("/profile/change-password")

	public String changePassword(

			@RequestParam String currentPassword,

			@RequestParam String newPassword,

			@RequestParam String confirmPassword,

			HttpSession session,

			RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {

			return "redirect:/login";

		}

		Customer customer =

				customerRepository.findById(id).orElse(null);

		if (customer == null) {

			session.invalidate();

			return "redirect:/login";

		}

		String storedPassword = customer.getPassword();

		if (!matchesStoredPassword(currentPassword, storedPassword)) {

			redirectAttributes.addFlashAttribute(

					"passwordError",

					"Current password is incorrect.");

			return "redirect:/customer/profile";

		}

		if (newPassword == null || newPassword.length() < 8) {

			redirectAttributes.addFlashAttribute(

					"passwordError",

					"New password must contain at least 8 characters.");

			return "redirect:/customer/profile";

		}

		if (!newPassword.equals(confirmPassword)) {

			redirectAttributes.addFlashAttribute(

					"passwordError",

					"New password and confirmation do not match.");

			return "redirect:/customer/profile";

		}

		if (matchesStoredPassword(newPassword, storedPassword)) {

			redirectAttributes.addFlashAttribute(

					"passwordError",

					"Choose a password different from your current password.");

			return "redirect:/customer/profile";

		}

		customer.setPassword(passwordEncoder.encode(newPassword));

		customerRepository.save(customer);

		redirectAttributes.addFlashAttribute(

				"passwordSuccess",

				"Password changed successfully.");

		return "redirect:/customer/profile";

	}

	private boolean matchesStoredPassword(

			String submittedPassword,

			String storedPassword) {

		if (submittedPassword == null || storedPassword == null) {

			return false;

		}

		if (isBCryptPassword(storedPassword)) {

			return passwordEncoder.matches(

					submittedPassword,

					storedPassword);

		}

		return storedPassword.equals(submittedPassword);

	}

	private boolean isBCryptPassword(String password) {

		return password != null

				&& (password.startsWith("$2a$")

						|| password.startsWith("$2b$")

						|| password.startsWith("$2y$"));

	}

	// ==================== DEACTIVATE ACCOUNT ====================

	@PostMapping("/profile/deactivate")
	public String deactivateAccount(HttpSession session, RedirectAttributes redirectAttributes) {

		Long id = customerId(session);

		if (id == null) {
			return "redirect:/login";
		}

		Customer customer = customerRepository.findById(id).orElse(null);

		if (customer == null) {
			session.invalidate();
			return "redirect:/login";
		}

		// Deactivate the account without deleting its historical records.
		customer.setActive(false);
		customerRepository.save(customer);

		// Log the customer out immediately.
		session.invalidate();

		redirectAttributes.addFlashAttribute("successMessage", "Your account has been deactivated successfully.");

		return "redirect:/";
	}

}
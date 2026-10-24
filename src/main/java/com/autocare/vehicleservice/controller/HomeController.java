package com.autocare.vehicleservice.controller;

import com.autocare.vehicleservice.entity.ContactMessage;
import com.autocare.vehicleservice.service.ContactMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
	private final ContactMessageService contactMessageService;

	public HomeController(ContactMessageService contactMessageService) {
		this.contactMessageService = contactMessageService;
	}

	@GetMapping("/")
	public String home() {
		return "index";
	}

	@GetMapping("/services")
	public String services() {
		return "services";
	}

	@GetMapping("/about")
	public String about() {
		return "about";
	}

	@GetMapping("/contact")
	public String contact(Model model) {
		if (!model.containsAttribute("contactMessage")) {
			model.addAttribute("contactMessage", new ContactMessage());
		}
		return "contact";
	}

	@PostMapping("/contact")
	public String submitContact(@ModelAttribute("contactMessage") ContactMessage contactMessage,
			RedirectAttributes redirectAttributes) {
		try {
			contactMessageService.saveMessage(contactMessage);
			redirectAttributes.addFlashAttribute("successMessage",
					"Thank you for contacting AutoCare! Your message has been submitted successfully.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("contactMessage", contactMessage);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "We could not submit your message. Please try again.");
		}
		return "redirect:/contact";
	}
}
package com.autocare.vehicleservice.service;

import com.autocare.vehicleservice.entity.ContactMessage;
import com.autocare.vehicleservice.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactMessageService {
	private final ContactMessageRepository contactMessageRepository;

	public ContactMessageService(ContactMessageRepository contactMessageRepository) {
		this.contactMessageRepository = contactMessageRepository;
	}

	@Transactional
	public ContactMessage saveMessage(ContactMessage contactMessage) {
		contactMessage.setFullName(contactMessage.getFullName().trim());
		contactMessage.setEmail(contactMessage.getEmail().trim());
		contactMessage.setSubject(contactMessage.getSubject().trim());
		contactMessage.setMessage(contactMessage.getMessage().trim());
		if (contactMessage.getPhoneNumber() != null) {
			contactMessage.setPhoneNumber(contactMessage.getPhoneNumber().trim());
		}
		if (contactMessage.getFullName().isBlank() || contactMessage.getEmail().isBlank()
				|| contactMessage.getSubject().isBlank() || contactMessage.getMessage().isBlank()) {
			throw new IllegalArgumentException("Please complete all required fields.");
		}
		if (!contactMessage.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
			throw new IllegalArgumentException("Please enter a valid email address.");
		}
		return contactMessageRepository.save(contactMessage);
	}
}
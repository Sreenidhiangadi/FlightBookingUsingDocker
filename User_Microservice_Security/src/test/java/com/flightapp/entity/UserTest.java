package com.flightapp.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UserTest {

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@Test
	void testUserCreationAndGettersSetters() {
		User user = new User();
		user.setId("1");
		user.setName("Sreenidhi");
		user.setEmail("sreenidhi@gmail.com");
		user.setAge(22);
		user.setGender("Female");
		user.setPassword("securePassword");
		user.setRole(Role.USER);

		assertEquals("1", user.getId());
		assertEquals("Sreenidhi", user.getName());
		assertEquals("sreenidhi@gmail.com", user.getEmail());
		assertEquals(22, user.getAge());
		assertEquals("Female", user.getGender());
		assertEquals("securePassword", user.getPassword());
		assertEquals(Role.USER, user.getRole());
	}

	@Test
	void testUserValidationConstraints() {
	    User user = new User(); 

	    Set<ConstraintViolation<User>> violations = validator.validate(user);

	    assertFalse(violations.isEmpty());
	    Set<String> invalidFields = violations.stream()
	            .map(v -> v.getPropertyPath().toString())
	            .collect(Collectors.toSet());

	    assertTrue(invalidFields.contains("name"));
	    assertTrue(invalidFields.contains("email"));
	    assertTrue(invalidFields.contains("age"));
	    assertTrue(invalidFields.contains("gender"));
	}
}
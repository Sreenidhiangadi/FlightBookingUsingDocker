package com.flightapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document
public class User {

	@Id
	private String id;

	@NotBlank(message = "User name is required")
	private String name;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotNull(message = "Age is required")
	@Min(value = 1, message = "Age must be positive")
	private Integer age;

	@NotBlank(message = "Password is required")
	private String password;

	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	@NotNull(message = "User role is required")
	private Role role;
}

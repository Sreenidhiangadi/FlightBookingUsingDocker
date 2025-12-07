package com.flightapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document
public class Passenger {

	@Id
	private String id;

	@NotBlank(message = "User name is required")
	private String name;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotNull(message = "Age is required")
	@Min(value = 1, message = "Age must be positive")
	private Integer age;

	@NotBlank(message = "Seat number is required")
	private String seatNumber;

	private String mealPreference;

	private String ticketId;
}

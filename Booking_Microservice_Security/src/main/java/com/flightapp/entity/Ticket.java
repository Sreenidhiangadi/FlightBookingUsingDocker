package com.flightapp.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document(collection = "tickets")
public class Ticket {

	@Id
	private String id;

	@NotBlank(message = "PNR cannot be blank")
	private String pnr;

	@NotBlank(message = "User ID is required")
	private String userId;

	@NotBlank(message = "Departure flight is required")
	private String departureFlightId;

	private String returnFlightId;

	@NotNull(message = "Trip type is required")
	private FLIGHTTYPE tripType;

	@NotNull(message = "Booking time is required")
	private LocalDateTime bookingTime;

	private String seatsBooked;

	@NotBlank
	private String userEmail;

	private String mealType;

	@Min(value = 0, message = "Total price must be positive")
	private Double totalPrice;

	private boolean canceled;
	@Transient
	private List<Passenger> passengers;
}

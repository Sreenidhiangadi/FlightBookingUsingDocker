package com.flightapp.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FlightSearchRequest {
	private String fromPlace;
	private String toPlace;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}

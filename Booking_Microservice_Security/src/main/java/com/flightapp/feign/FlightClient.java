package com.flightapp.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.flightapp.dto.FlightDto;

@FeignClient(name = "flight-microservice")
public interface FlightClient {

	@GetMapping("/api/flight/{id}")
	FlightDto getFlight(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable("id") String id);

	@PutMapping("/api/flight/internal/{id}/reserve/{seatCount}")
	void reserveSeats(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable("id") String id,
			@PathVariable("seatCount") int seatCount);

	@PutMapping("/api/flight/internal/{id}/release/{seatCount}")
	void releaseSeats(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable("id") String id,
			@PathVariable("seatCount") int seatCount);
}

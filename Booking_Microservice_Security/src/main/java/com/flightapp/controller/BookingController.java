package com.flightapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;
import com.flightapp.service.BookingService;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flight")
public class BookingController {

	private final BookingService bookingService;

	@Data
	public static class BookingRequest {
		private String userEmail;
		private String returnFlightId;
		private FLIGHTTYPE tripType;
		private List<@Valid Passenger> passengers;
	}

//	@PostMapping("/booking/{flightId}")
//	public Mono<ResponseEntity<String>> bookTicket(@PathVariable("flightId") String departureFlightId,
//			@RequestBody BookingRequest request) {
//		return bookingService.bookTicket(request.getUserEmail(), departureFlightId, request.getReturnFlightId(),
//				request.getPassengers(), request.getTripType()).map(pnr -> ResponseEntity.ok("PNR: " + pnr));
//	}
	  @PostMapping("/booking/{flightId}")
	    public Mono<ResponseEntity<String>> bookTicket(@PathVariable("flightId") String departureFlightId,
	                                                   @RequestBody BookingRequest request,
	                                                   JwtAuthenticationToken auth) {
	        String callerEmail = auth.getToken().getSubject();

	        return bookingService.bookTicket(callerEmail, departureFlightId, request.getReturnFlightId(),
	                        request.getPassengers(), request.getTripType())
	                .map(pnr -> ResponseEntity.ok("PNR: " + pnr));
	    }
	@GetMapping("/ticket/{pnr}")
	public Mono<Ticket> getTicket(@PathVariable String pnr) {
		return bookingService.getByPnr(pnr);
	}

//	@GetMapping("/booking/history/{emailId}")
//	public Flux<Ticket> history(@PathVariable String emailId) {
//		return bookingService.historyByEmail(emailId);
//	}
	   @GetMapping("/booking/history")
    public Flux<Ticket> myHistory(JwtAuthenticationToken auth) {
        return bookingService.historyByEmail(auth.getToken().getSubject());
    }
	   @PreAuthorize("hasRole('ADMIN')")
	    @GetMapping("/admin/booking/history/{emailId}")
	    public Flux<Ticket> historyByEmailForAdmin(@PathVariable String emailId) {
	        return bookingService.historyByEmail(emailId);
	    }
	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<String> cancel(@PathVariable String pnr) {
		return bookingService.cancelByPnr(pnr);
	}
}

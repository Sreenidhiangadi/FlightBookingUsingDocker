package com.flightapp.service;

import java.time.LocalDateTime;

import com.flightapp.entity.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {

	Mono<Flight> addFlight(Flight flight);

	Flux<Flight> getAllFlights();

	Mono<Flight> searchFlightById(String id);

	Flux<Flight> searchFlights(String fromPlace, String toPlace, LocalDateTime start, LocalDateTime end);

	Flux<Flight> searchFlightsByAirline(String fromPlace, String toPlace, String airline);

	Mono<Flight> reserveSeats(String flightId, int seatCount);

	Mono<Flight> releaseSeats(String flightId, int seatCount);
}

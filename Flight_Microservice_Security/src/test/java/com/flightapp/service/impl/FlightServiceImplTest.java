package com.flightapp.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.entity.Flight;
import com.flightapp.repository.FlightRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

	@Mock
	private FlightRepository flightRepository;

	@InjectMocks
	private FlightServiceImpl flightService;

	private Flight flight;

	@BeforeEach
	void setup() {
		flight = new Flight();
		flight.setId("F1");
		flight.setAirline("Indigo");
		flight.setFromPlace("BLR");
		flight.setToPlace("DEL");
		flight.setDepartureTime(LocalDateTime.now());
		flight.setArrivalTime(LocalDateTime.now().plusHours(2));
		flight.setTotalSeats(100);
		flight.setAvailableSeats(50);
		flight.setPrice(3000);
	}

	@Test
	void addFlight_shouldSaveAndReturnFlight() {
		when(flightRepository.save(flight)).thenReturn(Mono.just(flight));

		StepVerifier.create(flightService.addFlight(flight)).expectNext(flight).verifyComplete();

		verify(flightRepository).save(flight);
	}

	@Test
	void getAllFlights_shouldReturnFluxFromRepo() {
		when(flightRepository.findAll()).thenReturn(Flux.just(flight));

		StepVerifier.create(flightService.getAllFlights()).expectNext(flight).verifyComplete();

		verify(flightRepository).findAll();
	}

	@Test
	void searchFlightById_whenPresent_returnsFlight() {
		when(flightRepository.findById("F1")).thenReturn(Mono.just(flight));

		StepVerifier.create(flightService.searchFlightById("F1")).expectNext(flight).verifyComplete();
	}

	@Test
	void searchFlightById_whenNotPresent_returnsError() {
		when(flightRepository.findById("F1")).thenReturn(Mono.empty());

		StepVerifier.create(flightService.searchFlightById("F1"))
				.expectErrorMatches(ex -> ex.getMessage().contains("Flight with this ID is not present")).verify();
	}

	@Test
	void reserveSeats_successfulReservation() {
		when(flightRepository.findById("F1")).thenReturn(Mono.just(flight));
		when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

		StepVerifier.create(flightService.reserveSeats("F1", 10)).expectNextMatches(f -> f.getAvailableSeats() == 40)
				.verifyComplete();
	}

	@Test
	void reserveSeats_notEnoughSeats_shouldReturnError() {
		when(flightRepository.findById("F1")).thenReturn(Mono.just(flight));

		StepVerifier.create(flightService.reserveSeats("F1", 1000))
				.expectErrorMatches(ex -> ex.getMessage().contains("Not enough seats")).verify();
	}

	@Test
	void reserveSeats_flightNotFound_returnsError() {
		when(flightRepository.findById("F1")).thenReturn(Mono.empty());

		StepVerifier.create(flightService.reserveSeats("F1", 10))
				.expectErrorMatches(ex -> ex.getMessage().contains("Flight not found")).verify();
	}

	@Test
	void releaseSeats_shouldIncreaseSeatCount() {
		when(flightRepository.findById("F1")).thenReturn(Mono.just(flight));
		when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

		StepVerifier.create(flightService.releaseSeats("F1", 5)).expectNextMatches(f -> f.getAvailableSeats() == 55)
				.verifyComplete();
	}

	@Test
	void releaseSeats_flightNotFound_returnsError() {
		when(flightRepository.findById("F1")).thenReturn(Mono.empty());

		StepVerifier.create(flightService.releaseSeats("F1", 5))
				.expectErrorMatches(ex -> ex.getMessage().contains("Flight not found")).verify();
	}

	@Test
	void searchFlights_shouldDelegateToRepository() {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusHours(5);

		when(flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween("BLR", "DEL", start, end))
				.thenReturn(Flux.just(flight));

		StepVerifier.create(flightService.searchFlights("BLR", "DEL", start, end)).expectNext(flight).verifyComplete();
	}

	@Test
	void searchFlightsByAirline_shouldDelegateToRepository() {
		when(flightRepository.findByFromPlaceAndToPlaceAndAirline("BLR", "DEL", "Indigo"))
				.thenReturn(Flux.just(flight));

		StepVerifier.create(flightService.searchFlightsByAirline("BLR", "DEL", "Indigo")).expectNext(flight)
				.verifyComplete();
	}
}

package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;
import com.flightapp.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BookingControllerTest {

	private BookingService bookingService;
	private BookingController bookingController;

	@BeforeEach
	void setUp() {
		bookingService = Mockito.mock(BookingService.class);
		bookingController = new BookingController(bookingService);
	}

	@Test
	void testGetTicket() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setUserEmail("pooja@gmail.com");
		ticket.setDepartureFlightId("DEP123");
		ticket.setTripType(FLIGHTTYPE.ONE_WAY);
		ticket.setBookingTime(LocalDateTime.now());

		when(bookingService.getByPnr("PNR123")).thenReturn(Mono.just(ticket));

		StepVerifier.create(bookingController.getTicket("PNR123")).expectNext(ticket).verifyComplete();

		verify(bookingService).getByPnr("PNR123");
	}


	@Test
	void testCancel() {

		when(bookingService.cancelByPnr("PNR123")).thenReturn(Mono.just("Cancelled"));
		StepVerifier.create(bookingController.cancel("PNR123")).expectNext("Cancelled")
				.verifyComplete();
		verify(bookingService).cancelByPnr("PNR123");
	}
}
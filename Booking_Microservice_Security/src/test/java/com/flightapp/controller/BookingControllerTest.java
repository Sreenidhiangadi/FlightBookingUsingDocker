package com.flightapp.controller;

import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;
import com.flightapp.service.BookingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookingService bookingService;

    @Test
    void bookTicket_shouldCallServiceAndReturnPnrWrappedInResponseEntity() {
        BookingController.BookingRequest request = new BookingController.BookingRequest();
        request.setUserEmail("sreenidhi@gmail.com");
        request.setReturnFlightId("RET123");
        request.setTripType(FLIGHTTYPE.ROUND_TRIP);

        Passenger passenger = new Passenger();
        passenger.setName("John Doe");
        passenger.setAge(30);
        passenger.setGender("Male");
        passenger.setSeatNumber("A1");
        request.setPassengers(List.of(passenger));

        when(bookingService.bookTicket(
                "sreenidhi@gmail.com",
                "DEP123",
                "RET123",
                request.getPassengers(),
                FLIGHTTYPE.ROUND_TRIP
        )).thenReturn(Mono.just("PNR123"));

        webTestClient.post()
                .uri("/api/flight/booking/{flightId}", "DEP123")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("PNR: PNR123");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> depIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> retIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> passengersCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<FLIGHTTYPE> typeCaptor = ArgumentCaptor.forClass(FLIGHTTYPE.class);

        verify(bookingService).bookTicket(
                emailCaptor.capture(),
                depIdCaptor.capture(),
                retIdCaptor.capture(),
                passengersCaptor.capture(),
                typeCaptor.capture()
        );

        assertEquals("sreenidhi@gmail.com", emailCaptor.getValue());
        assertEquals("DEP123", depIdCaptor.getValue());
        assertEquals("RET123", retIdCaptor.getValue());
        assertEquals(1, passengersCaptor.getValue().size());
        assertEquals(FLIGHTTYPE.ROUND_TRIP, typeCaptor.getValue());
    }

    @Test
    void getTicket_shouldReturnTicketFromService() {
        Ticket ticket = new Ticket();
        ticket.setPnr("PNR123");
        ticket.setUserEmail("sreenidhi@gmail.com");

        when(bookingService.getByPnr("PNR123")).thenReturn(Mono.just(ticket));

        webTestClient.get()
                .uri("/api/flight/ticket/{pnr}", "PNR123")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("PNR123")
                .jsonPath("$.userEmail").isEqualTo("sreenidhi@gmail.com");

        verify(bookingService).getByPnr("PNR123");
    }

    @Test
    void history_shouldReturnFluxOfTickets() {
        Ticket ticket1 = new Ticket();
        ticket1.setPnr("PNR1");
        ticket1.setUserEmail("sreenidhi@gmail.com");

        Ticket ticket2 = new Ticket();
        ticket2.setPnr("PNR2");
        ticket2.setUserEmail("sreenidhi@gmail.com");

        when(bookingService.historyByEmail("sreenidhi@gmail.com"))
                .thenReturn(Flux.just(ticket1, ticket2));

        webTestClient.get()
                .uri("/api/flight/booking/history/{emailId}", "sreenidhi@gmail.com")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Ticket.class)
                .value(tickets -> {
                    assertEquals(2, tickets.size());
                    assertEquals("PNR1", tickets.get(0).getPnr());
                    assertEquals("PNR2", tickets.get(1).getPnr());
                });

        verify(bookingService).historyByEmail("sreenidhi@gmail.com");
    }

    @Test
    void cancel_shouldDelegateToServiceAndReturnResult() {
        when(bookingService.cancelByPnr("PNR123"))
                .thenReturn(Mono.just("Cancelled"));

        webTestClient.delete()
                .uri("/api/flight/booking/cancel/{pnr}", "PNR123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Cancelled");

        verify(bookingService).cancelByPnr("PNR123");
    }
}

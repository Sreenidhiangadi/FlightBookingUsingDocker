//package com.flightapp.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//import com.flightapp.entity.FLIGHTTYPE;
//import com.flightapp.entity.Passenger;
//import com.flightapp.entity.Ticket;
//import com.flightapp.service.BookingService;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@WebFluxTest(controllers = BookingController.class)
//class BookingControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private BookingService bookingService;
//
//    @Test
//    void bookTicket() {
//        Passenger passenger = new Passenger();
//        passenger.setName("sreenidhi");
//
//        BookingController.BookingRequest request = new BookingController.BookingRequest();
//        request.setUserEmail("sreenidhi@gmail.com");
//        request.setTripType(FLIGHTTYPE.ONE_WAY);
//        request.setPassengers(List.of(passenger));
//
//        Mockito.when(bookingService.bookTicket(
//                eq("sreenidhi@gmail.com"),
//                eq("FL001"),
//                any(),
//                any(),
//                eq(FLIGHTTYPE.ONE_WAY)
//        )).thenReturn(Mono.just("PNR123"));
//
//        webTestClient
//                .mutateWith(mockJwt().jwt(jwt -> jwt.subject("sreenidhi@gmail.com")))
//                .post()
//                .uri("/api/flight/booking/FL001")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(String.class).isEqualTo("PNR: PNR123");
//    }
//
//    @Test
//    void getTicketByPnr() {
//        Ticket ticket = new Ticket();
//        ticket.setPnr("PNR123");
//
//        Mockito.when(bookingService.getByPnr("PNR123"))
//                .thenReturn(Mono.just(ticket));
//
//        webTestClient
//                .mutateWith(mockJwt())
//                .get()
//                .uri("/api/flight/ticket/PNR123")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.pnr").isEqualTo("PNR123");
//    }
//
//    @Test
//    void bookingHistoryForUser() {
//        Ticket ticket = new Ticket();
//        ticket.setPnr("PNR123");
//
//        Mockito.when(bookingService.historyByEmail("sreenidhi@gmail.com"))
//                .thenReturn(Flux.just(ticket));
//
//        webTestClient
//                .mutateWith(mockJwt().jwt(jwt -> jwt.subject("sreenidhi@gmail.com")))
//                .get()
//                .uri("/api/flight/booking/history")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Ticket.class).hasSize(1);
//    }
//
//    @Test
//    void bookingHistoryForAdmin() {
//        Ticket ticket = new Ticket();
//        ticket.setPnr("PNR123");
//
//        Mockito.when(bookingService.historyByEmail("sreenidhi@gmail.com"))
//                .thenReturn(Flux.just(ticket));
//
//        webTestClient
//                .mutateWith(mockJwt().authorities(() -> "ROLE_ADMIN"))
//                .get()
//                .uri("/api/flight/admin/booking/history/sreenidhi@gmail.com")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(Ticket.class).hasSize(1);
//    }
//}

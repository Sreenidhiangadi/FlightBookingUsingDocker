package com.flightapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.flightapp.dto.FlightDto;
import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;
import com.flightapp.feign.FlightClient;
import com.flightapp.messaging.BookingEvent;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.repository.TicketRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

class BookingServiceImplTest {

    TicketRepository ticketRepository = Mockito.mock(TicketRepository.class);
    PassengerRepository passengerRepository = Mockito.mock(PassengerRepository.class);
    FlightClient flightClient = Mockito.mock(FlightClient.class);
    KafkaTemplate<String, BookingEvent> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

    BookingServiceImpl service;

    @BeforeEach
    void setup() {
        service = new BookingServiceImpl(
                ticketRepository,
                passengerRepository,
                flightClient,
                kafkaTemplate
        );
    }

    private Context securityContext(String email) {
        Jwt jwt = Jwt.withTokenValue("token")
                .subject(email)
                .header("alg", "none")
                .claim("scope", "ROLE_USER")
                .build();

        JwtAuthenticationToken auth = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return ReactiveSecurityContextHolder.withSecurityContext(
                Mono.just(new SecurityContextImpl(auth))
        );
    }



    @Test
    void getTicketByPnrSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId("1");
        ticket.setPnr("PNR123");
        ticket.setUserEmail("sreenidhi@gmail.com");

        Mockito.when(ticketRepository.findByPnr("PNR123"))
                .thenReturn(Mono.just(ticket));

        Mockito.when(passengerRepository.findByTicketId("1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(
                service.getByPnr("PNR123")
                        .contextWrite(securityContext("sreenidhi@gmail.com"))
        )
        .assertNext(t -> assertEquals("PNR123", t.getPnr()))
        .verifyComplete();
    }

    @Test
    void historyByEmailSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId("1");
        ticket.setUserEmail("sreenidhi@gmail.com");

        Mockito.when(ticketRepository.findByUserEmail("sreenidhi@gmail.com"))
                .thenReturn(Flux.just(ticket));

        Mockito.when(passengerRepository.findByTicketId("1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(
                service.historyByEmail("sreenidhi@gmail.com")
                        .contextWrite(securityContext("sreenidhi@gmail.com"))
        )
        .expectNextCount(1)
        .verifyComplete();
    }

    @Test
    void cancelTicketSuccess() {
        Ticket ticket = new Ticket();
        ticket.setId("1");
        ticket.setPnr("PNR123");
        ticket.setUserEmail("sreenidhi@gmail.com");
        ticket.setDepartureFlightId("FL1");
        ticket.setSeatsBooked("A1");
        ticket.setCanceled(false);

        FlightDto flight = new FlightDto();
        flight.setDepartureTime(LocalDateTime.now().plusDays(2));

        Mockito.when(ticketRepository.findByPnr("PNR123"))
                .thenReturn(Mono.just(ticket));

        Mockito.when(flightClient.getFlight(any(), any()))
                .thenReturn(flight);

        Mockito.when(ticketRepository.save(any()))
                .thenReturn(Mono.just(ticket));

        StepVerifier.create(
                service.cancelByPnr("PNR123")
                        .contextWrite(securityContext("sreenidhi@gmail.com"))
        )
        .assertNext(resp -> assertEquals("Cancelled Successfully", resp.getBody()))
        .verifyComplete();
    }
}

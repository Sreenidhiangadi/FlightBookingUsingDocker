package com.flightapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

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

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private FlightClient flightClient;

    @Mock
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private JwtAuthenticationToken jwtAuth() {
        Jwt jwt = new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "user@example.com")
        );
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void bookTicket_oneWay_success_withKafkaException() {
        Passenger passenger = new Passenger();
        passenger.setSeatNumber("B2");

        FlightDto depFlight = new FlightDto();
        depFlight.setId("DEP2");
        depFlight.setAvailableSeats(5);
        depFlight.setPrice(200.0);

        when(flightClient.getFlight(anyString(), eq("DEP2"))).thenReturn(depFlight);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId("TICKET_ID2");
            return Mono.just(t);
        });

        when(passengerRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Passenger> passengers = invocation.getArgument(0);
            return Flux.fromIterable(passengers);
        });

        doThrow(new RuntimeException("Kafka down"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(BookingEvent.class));

        StepVerifier.create(
                        bookingService
                                .bookTicket("user@example.com", "DEP2", null, List.of(passenger), FLIGHTTYPE.ONE_WAY)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .assertNext(pnr -> assertEquals(8, pnr.length()))
                .verifyComplete();

        verify(ticketRepository).save(any(Ticket.class));
        verify(passengerRepository).saveAll(anyList());
        verify(kafkaTemplate).send(anyString(), anyString(), any(BookingEvent.class));
    }

    @Test
    void bookTicket_departureFlightNotFound_shouldError() {
        Passenger passenger = new Passenger();

        when(flightClient.getFlight(anyString(), eq("DEP_MISSING"))).thenReturn(null);

        StepVerifier.create(
                        bookingService
                                .bookTicket("user@example.com", "DEP_MISSING", null, List.of(passenger),
                                        FLIGHTTYPE.ONE_WAY)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectErrorMatches(
                        ex -> ex instanceof RuntimeException && ex.getMessage().equals("Departure flight not found"))
                .verify();
    }

    @Test
    void bookTicket_notEnoughSeatsInDeparture_shouldError() {
        Passenger p1 = new Passenger();
        Passenger p2 = new Passenger();
        List<Passenger> passengers = List.of(p1, p2);

        FlightDto depFlight = new FlightDto();
        depFlight.setAvailableSeats(1);

        when(flightClient.getFlight(anyString(), eq("DEP3"))).thenReturn(depFlight);

        StepVerifier.create(
                        bookingService
                                .bookTicket("user@example.com", "DEP3", null, passengers, FLIGHTTYPE.ONE_WAY)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Not enough seats in departure flight"))
                .verify();
    }

    @Test
    void bookTicket_returnFlightNotFound_shouldError() {
        Passenger passenger = new Passenger();

        FlightDto depFlight = new FlightDto();
        depFlight.setAvailableSeats(10);

        when(flightClient.getFlight(anyString(), eq("DEP4"))).thenReturn(depFlight);
        when(flightClient.getFlight(anyString(), eq("RET_MISSING"))).thenReturn(null);

        StepVerifier.create(
                        bookingService
                                .bookTicket("user@example.com", "DEP4", "RET_MISSING", List.of(passenger),
                                        FLIGHTTYPE.ROUND_TRIP)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectErrorMatches(
                        ex -> ex instanceof RuntimeException && ex.getMessage().equals("Return flight not found"))
                .verify();
    }

    @Test
    void bookTicket_notEnoughSeatsInReturn_shouldError() {
        Passenger passenger = new Passenger();

        FlightDto depFlight = new FlightDto();
        depFlight.setAvailableSeats(10);

        FlightDto retFlight = new FlightDto();
        retFlight.setAvailableSeats(0);

        when(flightClient.getFlight(anyString(), eq("DEP5"))).thenReturn(depFlight);
        when(flightClient.getFlight(anyString(), eq("RET5"))).thenReturn(retFlight);

        StepVerifier.create(
                        bookingService
                                .bookTicket("user@example.com", "DEP5", "RET5", List.of(passenger),
                                        FLIGHTTYPE.ROUND_TRIP)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Not enough seats in return flight"))
                .verify();
    }

    @Test
    void bookTicketFallback_shouldReturnResponseStatusException() throws Exception {
        Method method = BookingServiceImpl.class.getDeclaredMethod("bookTicketFallback", String.class, String.class,
                String.class, List.class, FLIGHTTYPE.class, Throwable.class);
        method.setAccessible(true);

        Mono<String> result = (Mono<String>) method.invoke(bookingService, "user@example.com", "DEP1", "RET1",
                List.of(new Passenger()), FLIGHTTYPE.ROUND_TRIP, new RuntimeException("original"));

        StepVerifier.create(result).expectErrorSatisfies(ex -> {
            assertTrue(ex instanceof ResponseStatusException);
            ResponseStatusException rse = (ResponseStatusException) ex;
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, rse.getStatusCode());
            assertTrue(rse.getReason().contains("Booking service is temporarily unavailable"));
        }).verify();
    }

    @Test
    void getByPnr_shouldReturnTicket() {
        Ticket ticket = new Ticket();
        ticket.setPnr("PNR123");
        ticket.setUserEmail("sreenidhi@gmail.com");

        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

        StepVerifier.create(bookingService.getByPnr("PNR123")).expectNext(ticket).verifyComplete();

        verify(ticketRepository).findByPnr("PNR123");
    }

    @Test
    void historyByEmail_shouldReturnTickets() {
        Ticket t1 = new Ticket();
        t1.setPnr("P1");
        Ticket t2 = new Ticket();
        t2.setPnr("P2");

        when(ticketRepository.findByUserEmail("sreenidhi@gmail.com")).thenReturn(Flux.just(t1, t2));

        StepVerifier.create(bookingService.historyByEmail("sreenidhi@gmail.com")).expectNext(t1).expectNext(t2)
                .verifyComplete();

        verify(ticketRepository).findByUserEmail("sreenidhi@gmail.com");
    }

    @Test
    void cancelByPnr_pnrNotFound_shouldReturn404Error() {
        when(ticketRepository.findByPnr("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(bookingService.cancelByPnr("UNKNOWN")).expectErrorSatisfies(ex -> {
            assertTrue(ex instanceof ResponseStatusException);
            ResponseStatusException rse = (ResponseStatusException) ex;
            assertEquals(HttpStatus.NOT_FOUND, rse.getStatusCode());
            assertEquals("PNR not found", rse.getReason());
        }).verify();

        verify(ticketRepository).findByPnr("UNKNOWN");
    }

    @Test
    void cancelByPnr_alreadyCancelled_shouldReturnMessage() {
        Ticket ticket = new Ticket();
        ticket.setPnr("PNR123");
        ticket.setCanceled(true);
        ticket.setSeatsBooked("A1,A2");
        ticket.setDepartureFlightId("DEP");
        ticket.setReturnFlightId("RET");

        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

        StepVerifier.create(bookingService.cancelByPnr("PNR123")).expectNext("Ticket already cancelled")
                .verifyComplete();

        verify(ticketRepository).findByPnr("PNR123");
        verifyNoInteractions(flightClient);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelByPnr_success_oneWay_withSeatsBookedNull_shouldUseSeatCountOne() {
        Ticket ticket = new Ticket();
        ticket.setPnr("PNR_ONEWAY");
        ticket.setCanceled(false);
        ticket.setSeatsBooked(null);
        ticket.setDepartureFlightId("DEPX");
        ticket.setReturnFlightId(null);

        when(ticketRepository.findByPnr("PNR_ONEWAY")).thenReturn(Mono.just(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            return Mono.just(t);
        });

        StepVerifier.create(
                        bookingService
                                .cancelByPnr("PNR_ONEWAY")
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectNext("Cancelled Successfully")
                .verifyComplete();

        verify(flightClient).releaseSeats(anyString(), eq("DEPX"), eq(1));
        verify(ticketRepository).save(argThat(Ticket::isCanceled));
    }

    @Test
    void cancelByPnr_success_roundTrip_withSeatsBooked_shouldReleaseBoth() {
        Ticket ticket = new Ticket();
        ticket.setPnr("PNR_RT");
        ticket.setCanceled(false);
        ticket.setSeatsBooked("A1,A2,A3");
        ticket.setDepartureFlightId("DEPY");
        ticket.setReturnFlightId("RETY");

        when(ticketRepository.findByPnr("PNR_RT")).thenReturn(Mono.just(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setBookingTime(LocalDateTime.now());
            return Mono.just(t);
        });

        StepVerifier.create(
                        bookingService
                                .cancelByPnr("PNR_RT")
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(jwtAuth()))
                )
                .expectNext("Cancelled Successfully")
                .verifyComplete();

        verify(flightClient).releaseSeats(anyString(), eq("DEPY"), eq(3));
        verify(flightClient).releaseSeats(anyString(), eq("RETY"), eq(3));
        verify(ticketRepository).save(argThat(Ticket::isCanceled));
        verify(kafkaTemplate).send(eq("booking-events"), eq("PNR_RT"), any(BookingEvent.class));
    }
}

//package com.flightapp.service.impl;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.server.ResponseStatusException;
//
//import com.flightapp.dto.FlightDto;
//import com.flightapp.entity.FLIGHTTYPE;
//import com.flightapp.entity.Passenger;
//import com.flightapp.entity.Ticket;
//import com.flightapp.feign.FlightClient;
//import com.flightapp.messaging.BookingEvent;
//import com.flightapp.repository.PassengerRepository;
//import com.flightapp.repository.TicketRepository;
//
//import org.springframework.kafka.core.KafkaTemplate;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//import java.time.ZonedDateTime;
//@ExtendWith(MockitoExtension.class)
//class BookingServiceImplTest {
//
//    @Mock private TicketRepository ticketRepository;
//    @Mock private PassengerRepository passengerRepository;
//    @Mock private FlightClient flightClient;
//    @Mock private KafkaTemplate<String, BookingEvent> kafkaTemplate;
//
//    @InjectMocks
//    private BookingServiceImpl bookingService;
//
//    private Passenger passenger;
//
//    @BeforeEach
//    void setup() {
//        passenger = new Passenger();
//        passenger.setName("John");
//        passenger.setAge(30);
//        passenger.setGender("M");
//        passenger.setSeatNumber("A1");
//    }
//
//    private JwtAuthenticationToken userAuth(String email) {
//        Jwt jwt = new Jwt(
//                "token-value-123",
//                Instant.now(),
//                Instant.now().plusSeconds(3600),
//                Map.of("alg", "HS256"),
//                Map.of("sub", email)
//        );
//        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
//    }
//
//    private JwtAuthenticationToken adminAuth(String email) {
//        Jwt jwt = new Jwt(
//                "token-value-admin-456",
//                Instant.now(),
//                Instant.now().plusSeconds(3600),
//                Map.of("alg", "HS256"),
//                Map.of("sub", email)
//        );
//        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
//    }
//
//    private <T> reactor.util.context.ContextView authCtx(String email, boolean admin) {
//        return ReactiveSecurityContextHolder.withAuthentication(admin ? adminAuth(email) : userAuth(email));
//    }
//
//    private FlightDto flight(String id, int availableSeats, double price, ZonedDateTime ZonedDateTime) {
//        FlightDto f = new FlightDto();
//        f.setId(id);
//        f.setAvailableSeats(availableSeats);
//        f.setPrice(price);
//        f.setDepartureTime(ZonedDateTime);
//        return f;
//    }
//
//    
//    @Test
//    void bookTicket_forDifferentEmail_nonAdmin_should403() {
//        StepVerifier.create(
//                bookingService.bookTicket("other@example.com", "DEP1", null, List.of(passenger), FLIGHTTYPE.ONE_WAY)
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .expectErrorSatisfies(ex -> {
//            assertTrue(ex instanceof ResponseStatusException);
//            ResponseStatusException rse = (ResponseStatusException) ex;
//            assertEquals(HttpStatus.FORBIDDEN, rse.getStatusCode());
//            assertEquals("Access denied", rse.getReason());
//        })
//        .verify();
//
//        verifyNoInteractions(flightClient);
//        verifyNoInteractions(ticketRepository);
//    }
//
//  
//
// 
//
//    @Test
//    void getByPnr_owner_success() {
//        Ticket ticket = new Ticket();
//        ticket.setId("T1");
//        ticket.setPnr("PNR123");
//        ticket.setUserEmail("user@example.com");
//
//        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));
//        when(passengerRepository.findByTicketId("T1")).thenReturn(Flux.just(passenger));
//
//        StepVerifier.create(
//                bookingService.getByPnr("PNR123")
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .assertNext(t -> {
//            assertEquals("PNR123", t.getPnr());
//            assertEquals("user@example.com", t.getUserEmail());
//            assertNotNull(t.getPassengers());
//            assertEquals(1, t.getPassengers().size());
//        })
//        .verifyComplete();
//    }
//
//    @Test
//    void getByPnr_nonOwner_nonAdmin_should403() {
//        Ticket ticket = new Ticket();
//        ticket.setId("T1");
//        ticket.setPnr("PNR123");
//        ticket.setUserEmail("owner@example.com");
//
//        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));
//
//        StepVerifier.create(
//                bookingService.getByPnr("PNR123")
//                        .contextWrite(authCtx("other@example.com", false))
//        )
//        .expectErrorSatisfies(ex -> {
//            assertTrue(ex instanceof ResponseStatusException);
//            ResponseStatusException rse = (ResponseStatusException) ex;
//            assertEquals(HttpStatus.FORBIDDEN, rse.getStatusCode());
//            assertEquals("Access denied", rse.getReason());
//        })
//        .verify();
//    }
//
//
//    @Test
//    void historyByEmail_otherEmail_nonAdmin_should403() {
//        StepVerifier.create(
//                bookingService.historyByEmail("someone@else.com")
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .expectErrorSatisfies(ex -> {
//            assertTrue(ex instanceof ResponseStatusException);
//            ResponseStatusException rse = (ResponseStatusException) ex;
//            assertEquals(HttpStatus.FORBIDDEN, rse.getStatusCode());
//            assertEquals("Access denied", rse.getReason());
//        })
//        .verify();
//    }
//
// 
//    @Test
//    void cancelByPnr_notFound_should404() {
//        when(ticketRepository.findByPnr("UNKNOWN")).thenReturn(Mono.empty());
//
//        StepVerifier.create(
//                bookingService.cancelByPnr("UNKNOWN")
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .expectErrorSatisfies(ex -> {
//            assertTrue(ex instanceof ResponseStatusException);
//            ResponseStatusException rse = (ResponseStatusException) ex;
//            assertEquals(HttpStatus.NOT_FOUND, rse.getStatusCode());
//            assertEquals("PNR not found", rse.getReason());
//        })
//        .verify();
//    }
//
//    @Test
//    void cancelByPnr_alreadyCancelled_shouldReturnMessage() {
//        Ticket ticket = new Ticket();
//        ticket.setId("T1");
//        ticket.setPnr("PNR123");
//        ticket.setUserEmail("user@example.com");
//        ticket.setCanceled(true);
//
//        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));
//
//        StepVerifier.create(
//                bookingService.cancelByPnr("PNR123")
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .expectNext("Ticket already cancelled")
//        .verifyComplete();
//
//        verify(ticketRepository, never()).save(any());
//        verifyNoInteractions(flightClient);
//    }
//
//    @Test
//    void cancelByPnr_within24Hours_shouldReturnMessage_andNotSave() {
//        Ticket ticket = new Ticket();
//        ticket.setId("T1");
//        ticket.setPnr("PNR_SOON");
//        ticket.setUserEmail("user@example.com");
//        ticket.setCanceled(false);
//        ticket.setSeatsBooked("A1");
//        ticket.setDepartureFlightId("DEP1");
//        ticket.setReturnFlightId(null);
//
//       
//        FlightDto dep = flight("DEP1", 10, 100.0, ZonedDateTime.now().plusHours(10));
//
//        when(ticketRepository.findByPnr("PNR_SOON")).thenReturn(Mono.just(ticket));
//        when(flightClient.getFlight(anyString(), eq("DEP1"))).thenReturn(dep);
//
//        StepVerifier.create(
//                bookingService.cancelByPnr("PNR_SOON")
//                        .contextWrite(authCtx("user@example.com", false))
//        )
//        .expectNext("Cannot cancel ticket within 24 hours of departure")
//        .verifyComplete();
//
//        verify(ticketRepository, never()).save(any());
//        verify(flightClient, never()).releaseSeats(anyString(), anyString(), anyInt());
//    }
//
//
//    @Test
//    void cancelByPnr_nonOwner_nonAdmin_should403() {
//        Ticket ticket = new Ticket();
//        ticket.setId("T1");
//        ticket.setPnr("PNR123");
//        ticket.setUserEmail("owner@example.com");
//        ticket.setCanceled(false);
//        ticket.setDepartureFlightId("DEP1");
//        ticket.setSeatsBooked("A1");
//
//        when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));
//
//        StepVerifier.create(
//                bookingService.cancelByPnr("PNR123")
//                        .contextWrite(authCtx("other@example.com", false))
//        )
//        .expectErrorSatisfies(ex -> {
//            assertTrue(ex instanceof ResponseStatusException);
//            ResponseStatusException rse = (ResponseStatusException) ex;
//            assertEquals(HttpStatus.FORBIDDEN, rse.getStatusCode());
//            assertEquals("Access denied", rse.getReason());
//        })
//        .verify();
//    }
//}

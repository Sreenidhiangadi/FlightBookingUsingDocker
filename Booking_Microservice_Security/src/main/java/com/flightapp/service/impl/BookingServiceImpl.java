package com.flightapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.dto.FlightDto;
import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;
import com.flightapp.feign.FlightClient;
import com.flightapp.messaging.BookingEvent;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.repository.TicketRepository;
import com.flightapp.service.BookingService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TicketRepository ticketRepository;
    private final PassengerRepository passengerRepository;
    private final FlightClient flightClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    private static final String TOPIC = "booking-events";

    private record Caller(String email, boolean isAdmin, String bearer) {}

    private Mono<Caller> currentCaller() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .cast(Authentication.class)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    String email = jwtAuth.getToken().getSubject();
                    boolean isAdmin = jwtAuth.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                    String bearer = "Bearer " + jwtAuth.getToken().getTokenValue();
                    return new Caller(email, isAdmin, bearer);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")));
    }

    private Mono<Void> ensureCanActForEmail(String requestedEmail, Caller caller) {
        if (caller.isAdmin()) return Mono.empty();
        if (requestedEmail != null && requestedEmail.equalsIgnoreCase(caller.email())) return Mono.empty();
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
    }

    private Mono<Ticket> ensureTicketOwnerOrAdmin(Ticket ticket, Caller caller) {
        if (caller.isAdmin()) return Mono.just(ticket);
        if (ticket.getUserEmail() != null && ticket.getUserEmail().equalsIgnoreCase(caller.email())) {
            return Mono.just(ticket);
        }
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
    }

    @Override
    @CircuitBreaker(name = "bookingServiceCircuitBreaker", fallbackMethod = "bookTicketFallback")
    public Mono<String> bookTicket(String userEmail, String departureFlightId, String returnFlightId,
                                   List<Passenger> passengers, FLIGHTTYPE tripType) {

        int seatCount = passengers.size();

        return currentCaller()
                .flatMap(caller -> ensureCanActForEmail(userEmail, caller).thenReturn(caller))
                .flatMap(caller -> Mono.fromCallable(() -> {
                    FlightDto depFlight = flightClient.getFlight(caller.bearer(), departureFlightId);
                    if (depFlight == null) throw new RuntimeException("Departure flight not found");
                    if (depFlight.getAvailableSeats() < seatCount) throw new RuntimeException("Not enough seats in departure flight");

                    FlightDto retFlight = null;
                    if (tripType == FLIGHTTYPE.ROUND_TRIP && returnFlightId != null) {
                        retFlight = flightClient.getFlight(caller.bearer(), returnFlightId);
                        if (retFlight == null) throw new RuntimeException("Return flight not found");
                        if (retFlight.getAvailableSeats() < seatCount) throw new RuntimeException("Not enough seats in return flight");
                    }

                    flightClient.reserveSeats(caller.bearer(), departureFlightId, seatCount);

                    if (retFlight != null) {
                        try {
                            flightClient.reserveSeats(caller.bearer(), returnFlightId, seatCount);
                        } catch (Exception e) {
                            flightClient.releaseSeats(caller.bearer(), departureFlightId, seatCount);
                            throw new RuntimeException("Failed to reserve return flight, rolled back departure");
                        }
                    }

                    return new CheckedFlights(depFlight, retFlight);
                }).subscribeOn(Schedulers.boundedElastic())
                        .flatMap(checked -> createTicket(userEmail, departureFlightId, returnFlightId,
                                passengers, tripType, checked.dep(), checked.ret())));
    }

    private Mono<String> bookTicketFallback(String userEmail, String departureFlightId, String returnFlightId,
                                           List<Passenger> passengers, FLIGHTTYPE tripType, Throwable throwable) {
        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Booking service is temporarily unavailable, please try again later", throwable));
    }

    private Mono<String> createTicket(String userEmail, String departureFlightId, String returnFlightId,
                                     List<Passenger> passengers, FLIGHTTYPE tripType,
                                     FlightDto depFlight, FlightDto retFlight) {

        Ticket ticket = new Ticket();
        ticket.setPnr(UUID.randomUUID().toString().substring(0, 8));
        ticket.setUserEmail(userEmail);
        ticket.setDepartureFlightId(departureFlightId);
        ticket.setReturnFlightId(returnFlightId);
        ticket.setTripType(tripType);
        ticket.setBookingTime(LocalDateTime.now());
        ticket.setSeatsBooked(passengers.stream().map(Passenger::getSeatNumber).collect(Collectors.joining(",")));

        int seatCount = passengers.size();
        double total = depFlight.getPrice() * seatCount;
        if (retFlight != null) total += retFlight.getPrice() * seatCount;

        ticket.setTotalPrice(total);
        ticket.setCanceled(false);

        return ticketRepository.save(ticket)
                .flatMap(saved -> {
                    passengers.forEach(p -> p.setTicketId(saved.getId()));
                    return passengerRepository.saveAll(passengers).then(Mono.just(saved));
                })
                .doOnSuccess(saved -> sendEvent("BOOKING_CONFIRMED", saved))
                .map(Ticket::getPnr);
    }

    @Override
    public Mono<Ticket> getByPnr(String pnr) {
        return currentCaller()
                .flatMap(caller ->
                        ticketRepository.findByPnr(pnr)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "PNR not found")))
                                .flatMap(ticket -> ensureTicketOwnerOrAdmin(ticket, caller))
                                .flatMap(ticket -> passengerRepository.findByTicketId(ticket.getId())
                                        .collectList()
                                        .map(passengers -> {
                                            ticket.setPassengers(passengers);
                                            return ticket;
                                        }))
                );
    }

    @Override
    public Flux<Ticket> historyByEmail(String email) {
        return currentCaller()
                .flatMapMany(caller -> {
                    if (caller.isAdmin()) {
                        return ticketRepository.findByUserEmail(email);
                    }
                    // USER can only read own history
                    if (!caller.email().equalsIgnoreCase(email)) {
                        return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
                    }
                    return ticketRepository.findByUserEmail(email);
                });
    }

    @Override
    public Mono<String> cancelByPnr(String pnr) {
        return currentCaller()
                .flatMap(caller ->
                        ticketRepository.findByPnr(pnr)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "PNR not found")))
                                .flatMap(ticket -> ensureTicketOwnerOrAdmin(ticket, caller))
                                .flatMap(ticket -> cancelTicketInternal(ticket, caller.bearer()))
                );
    }

    private Mono<String> cancelTicketInternal(Ticket ticket, String bearer) {
        if (ticket.isCanceled()) {
            return Mono.just("Ticket already cancelled");
        }

        int seatCount = (ticket.getSeatsBooked() != null && !ticket.getSeatsBooked().isEmpty())
                ? ticket.getSeatsBooked().split(",").length
                : 1;

        return Mono.fromCallable(() -> {
            FlightDto depFlight = flightClient.getFlight(bearer, ticket.getDepartureFlightId());
            LocalDateTime departureTime = depFlight.getDepartureTime();
            LocalDateTime now = LocalDateTime.now();

            if (!departureTime.minusHours(24).isAfter(now)) {
                return (Object) "Cannot cancel ticket within 24 hours of departure";
            }

            flightClient.releaseSeats(bearer, ticket.getDepartureFlightId(), seatCount);
            if (ticket.getReturnFlightId() != null) {
                flightClient.releaseSeats(bearer, ticket.getReturnFlightId(), seatCount);
            }

            return (Object) ticket;
        }).subscribeOn(Schedulers.boundedElastic())
          .flatMap(result -> {
              if (result instanceof String msg) return Mono.just(msg);
              return updateCancellation((Ticket) result);
          });
    }

    private Mono<String> updateCancellation(Ticket ticket) {
        ticket.setCanceled(true);
        return ticketRepository.save(ticket)
                .doOnSuccess(saved -> sendEvent("BOOKING_CANCELLED", saved))
                .thenReturn("Cancelled Successfully");
    }

    private void sendEvent(String eventType, Ticket ticket) {
        BookingEvent event = BookingEvent.builder()
                .eventType(eventType)
                .pnr(ticket.getPnr())
                .userEmail(ticket.getUserEmail())
                .totalPrice(ticket.getTotalPrice())
                .build();

        try {
            kafkaTemplate.send(TOPIC, ticket.getPnr(), event);
        } catch (Exception ignored) {}
    }

    private record CheckedFlights(FlightDto dep, FlightDto ret) {}
}

package com.flightapp.service;

import com.flightapp.entity.FLIGHTTYPE;
import com.flightapp.entity.Passenger;
import com.flightapp.entity.Ticket;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface BookingService {

    Mono<String> bookTicket(String userEmail,
                            String departureFlightId,
                            String returnFlightId,
                            List<Passenger> passengers,
                            FLIGHTTYPE tripType);

    Mono<Ticket> getByPnr(String pnr);

    Flux<Ticket> historyByEmail(String email);

//    Mono<String> cancelByPnr(String pnr);
    Mono<ResponseEntity<String>> cancelByPnr(String pnr);
}
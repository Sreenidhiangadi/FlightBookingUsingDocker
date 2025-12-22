package com.flightapp.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {
	Flux<Flight> getFightByFromPlaceAndToPlace(String fromPlace, String toPlace);

	Flux<Flight> findByFromPlaceAndToPlaceAndAirline(String fromPlace, String toPlace, String airline);
	Mono<Boolean> existsByAirlineAndFromPlaceAndToPlaceAndDepartureTime(
	        String airline,
	        String fromPlace,
	        String toPlace,
	        LocalDateTime departureTime
	);

}
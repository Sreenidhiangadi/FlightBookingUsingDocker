package com.flightapp.service.impl;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Flight;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.ZonedDateTime;
@Service
public class FlightServiceImpl implements FlightService {

	private final FlightRepository flightRepository;

	public FlightServiceImpl(FlightRepository flightRepository) {
		this.flightRepository = flightRepository;
	}

	@Override
	public Mono<Flight> addFlight(Flight flight) {
		 if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
		        throw new ResponseStatusException(
		                HttpStatus.BAD_REQUEST,
		                "Cannot add flight with past departure date"
		        );
		    }
		 if (flight.getArrivalTime().isBefore(flight.getDepartureTime())) {
		        throw new ResponseStatusException(
		                HttpStatus.BAD_REQUEST,
		                "Arrival time must be after departure time"
		        );
		    }
		 return flightRepository
		            .existsByAirlineAndFromPlaceAndToPlaceAndDepartureTime(
		                    flight.getAirline(),
		                    flight.getFromPlace(),
		                    flight.getToPlace(),
		                    flight.getDepartureTime()
		            )
		            .flatMap(exists -> {
		                if (exists) {
		                    return Mono.error(new ResponseStatusException(
		                            HttpStatus.CONFLICT,
		                            "Flight already exists"
		                    ));
		                }

		                return flightRepository.save(flight);
		            });
	}
    
    @Override
    public Mono<Flight> updateFlight(String flightId, Flight updatedFlight) {

        return flightRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
                .flatMap(existingFlight -> {

                    existingFlight.setFromPlace(updatedFlight.getFromPlace());
                    existingFlight.setToPlace(updatedFlight.getToPlace());
                    existingFlight.setAirline(updatedFlight.getAirline());
                    existingFlight.setPrice(updatedFlight.getPrice());
                    existingFlight.setDepartureTime(updatedFlight.getDepartureTime());
                    existingFlight.setArrivalTime(updatedFlight.getArrivalTime());
                    existingFlight.setAvailableSeats(updatedFlight.getAvailableSeats());

                    return flightRepository.save(existingFlight);
                });
    }

    @Override
    public Mono<Void> deleteFlight(String flightId) {

        return flightRepository.findById(flightId)
                .switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
                .flatMap(flightRepository::delete);
    }
	@Override
	public Flux<Flight> getAllFlights() {
		return flightRepository.findAll();
	}
	@Override
	public Mono<Flight> searchFlightById(String id) {
	    return flightRepository.findById(id)
	        .switchIfEmpty(
	            Mono.error(new ResponseStatusException(
	                HttpStatus.NOT_FOUND,
	                "Flight with this ID is not present"
	            ))
	        );
	}


	@Override
	public Mono<Flight> reserveSeats(String flightId, int seatCount) {
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
				.flatMap(flight -> {
					if (flight.getAvailableSeats() < seatCount) {
						return Mono.error(new RuntimeException("Not enough seats"));
					}
					flight.setAvailableSeats(flight.getAvailableSeats() - seatCount);
					return flightRepository.save(flight);
				});
	}

	@Override
	public Mono<Flight> releaseSeats(String flightId, int seatCount) {
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new RuntimeException("Flight not found")))
				.flatMap(flight -> {
					flight.setAvailableSeats(flight.getAvailableSeats() + seatCount);
					return flightRepository.save(flight);
				});
	}

	@Override
	public Mono<List<Flight>> search(FlightSearchRequest flightSearchRequest) {

	    return flightRepository
	            .getFightByFromPlaceAndToPlace(
	                    flightSearchRequest.getFromPlace(),
	                    flightSearchRequest.getToPlace()
	            )
	            .filter(flight ->
	                    flight.getArrivalTime()
	                          .toLocalDate()
	                          .equals(flightSearchRequest.getDate())
	            )
	            .collectList();
	}


	@Override
	public Flux<Flight> searchFlightsByAirline(String fromPlace, String toPlace, String airline) {
		return flightRepository.findByFromPlaceAndToPlaceAndAirline(fromPlace, toPlace, airline);
	}
}

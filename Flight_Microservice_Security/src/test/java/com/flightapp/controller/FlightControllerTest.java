package com.flightapp.controller;

import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Flight;
import com.flightapp.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    private FlightController flightController;

    @BeforeEach
    void setUp() {
        flightController = new FlightController(flightService);
    }

    @Test
    void addFlight_shouldDelegateToServiceAndReturnSuccessMessage() {
        LocalDateTime dep = LocalDateTime.parse("2025-12-01T10:00");
        LocalDateTime arr = LocalDateTime.parse("2025-12-01T11:30");

        Flight flight = new Flight();
        flight.setAirline("Indigo");
        flight.setFromPlace("BLR");
        flight.setToPlace("HYD");
        flight.setDepartureTime(dep);
        flight.setArrivalTime(arr);
        flight.setTotalSeats(100);
        flight.setPrice(2500.0f);

        Flight saved = new Flight();
        saved.setId("flight-123");

        when(flightService.addFlight(any(Flight.class)))
                .thenReturn(Mono.just(saved));

        Mono<String> result = flightController.addFlight(flight);

        StepVerifier.create(result)
                .expectNext("Flight added successfully with id: flight-123")
                .verifyComplete();

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightService, times(1)).addFlight(captor.capture());

        Flight passed = captor.getValue();
        assertThat(passed).isSameAs(flight);
        assertThat(passed.getAirline()).isEqualTo("Indigo");
        assertThat(passed.getFromPlace()).isEqualTo("BLR");
        assertThat(passed.getToPlace()).isEqualTo("HYD");
        assertThat(passed.getDepartureTime()).isEqualTo(dep);
        assertThat(passed.getArrivalTime()).isEqualTo(arr);
        assertThat(passed.getTotalSeats()).isEqualTo(100);
        assertThat(passed.getPrice()).isEqualTo(2500.0f);
    }

    @Test
    void searchFlights_shouldDelegateToService() {
        LocalDateTime start = LocalDateTime.parse("2025-12-01T10:00");
        LocalDateTime end   = LocalDateTime.parse("2025-12-01T20:00");

        FlightSearchRequest request = new FlightSearchRequest();
        request.setFromPlace("BLR");
        request.setToPlace("DEL");
        request.setStartTime(start);
        request.setEndTime(end);

        Flight f1 = new Flight();
        f1.setId("f1");
        Flight f2 = new Flight();
        f2.setId("f2");

        when(flightService.searchFlights(
                "BLR",
                "DEL",
                start,
                end
        )).thenReturn(Flux.just(f1, f2));


        Flux<Flight> result = flightController.searchFlights(request);

        StepVerifier.create(result)
                .expectNext(f1)
                .expectNext(f2)
                .verifyComplete();

        verify(flightService).searchFlights("BLR", "DEL", start, end);

    }

    @Test
    void reserveSeats_shouldCallServiceAndReturnMono() {
        Flight flight = new Flight();
        flight.setId("f1");

        when(flightService.reserveSeats("f1", 2))
                .thenReturn(Mono.just(flight));

        Mono<Flight> result = flightController.reserveSeats("f1", 2);

        StepVerifier.create(result)
                .expectNext(flight)
                .verifyComplete();

        verify(flightService).reserveSeats("f1", 2);
    }

    @Test
    void releaseSeats_shouldCallServiceAndReturnMono() {
        Flight flight = new Flight();
        flight.setId("f1");

        when(flightService.releaseSeats("f1", 2))
                .thenReturn(Mono.just(flight));

        Mono<Flight> result = flightController.releaseSeats("f1", 2);

        StepVerifier.create(result)
                .expectNext(flight)
                .verifyComplete();

        verify(flightService).releaseSeats("f1", 2);
    }

    @Test
    void getAllFlights_shouldReturnFluxFromService() {
        Flight f1 = new Flight();
        f1.setId("f1");
        Flight f2 = new Flight();
        f2.setId("f2");

        when(flightService.getAllFlights())
                .thenReturn(Flux.just(f1, f2));

        Flux<Flight> result = flightController.getAllFlights();

        StepVerifier.create(result)
                .expectNext(f1)
                .expectNext(f2)
                .verifyComplete();

        verify(flightService).getAllFlights();
    }

    @Test
    void searchByAirline_shouldUseRequestBodyMap() {
        Flight f1 = new Flight();
        f1.setId("f1");

        when(flightService.searchFlightsByAirline("BLR", "DEL", "Indigo"))
                .thenReturn(Flux.just(f1));

        Map<String, String> body = Map.of(
                "fromPlace", "BLR",
                "toPlace", "DEL",
                "airline", "Indigo"
        );

        Flux<Flight> result = flightController.searchByAirline(body);

        StepVerifier.create(result)
                .expectNext(f1)
                .verifyComplete();

        verify(flightService).searchFlightsByAirline("BLR", "DEL", "Indigo");
    }

    @Test
    void getFlightById_shouldDelegateToService() {
        Flight f = new Flight();
        f.setId("f1");

        when(flightService.searchFlightById("f1"))
                .thenReturn(Mono.just(f));

        Mono<Flight> result = flightController.getFlightById("f1");

        StepVerifier.create(result)
                .expectNext(f)
                .verifyComplete();

        verify(flightService).searchFlightById("f1");
    }
}

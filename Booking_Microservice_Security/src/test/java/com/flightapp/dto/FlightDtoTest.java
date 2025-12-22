package com.flightapp.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class FlightDtoTest {

    @Test
    void testAllGettersAndSetters() {
        FlightDto dto = new FlightDto();

        LocalDateTime departure = LocalDateTime.of(2025, 1, 10, 10, 0);
        LocalDateTime arrival = LocalDateTime.of(2025, 1, 10, 12, 30);

        dto.setId("FL001");
        dto.setFlightNumber("AI101");
        dto.setFromPlace("Bangalore");
        dto.setToPlace("Delhi");
        dto.setDepartureTime(departure);
        dto.setArrivalTime(arrival);
        dto.setPrice(5500.0);
        dto.setAvailableSeats(120);

        assertEquals("FL001", dto.getId());
        assertEquals("AI101", dto.getFlightNumber());
        assertEquals("Bangalore", dto.getFromPlace());
        assertEquals("Delhi", dto.getToPlace());
        assertEquals(departure, dto.getDepartureTime());
        assertEquals(arrival, dto.getArrivalTime());
        assertEquals(5500.0, dto.getPrice());
        assertEquals(120, dto.getAvailableSeats());
    }
}

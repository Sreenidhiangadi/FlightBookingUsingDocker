package com.flightapp;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void handleRuntime_shouldReturnErrorMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        RuntimeException ex = new RuntimeException("Something went wrong");

        Mono<String> result = handler.handleRuntime(ex);

        assertEquals("Something went wrong", result.block());
    }
}


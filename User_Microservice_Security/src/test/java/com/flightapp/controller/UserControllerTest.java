package com.flightapp.controller;

import com.flightapp.entity.User;
import com.flightapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(UserController.class)
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @Test
    void register_shouldReturnCreatedMessageWithUserId() {
        User requestUser = new User();
        requestUser.setEmail("sreenidhi@gmail.com");
        requestUser.setPassword("secret");

        User savedUser = new User();
        savedUser.setId("123");
        savedUser.setEmail(requestUser.getEmail());
        savedUser.setPassword(requestUser.getPassword());

        when(authService.register(any(User.class))).thenReturn(Mono.just(savedUser));

        webTestClient.post()
                .uri("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("user created with id: 123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(authService).register(userCaptor.capture());
        assertEquals("sreenidhi@gmail.com", userCaptor.getValue().getEmail());
        assertEquals("secret", userCaptor.getValue().getPassword());
    }
    

    @Test
    void login_shouldDelegateToAuthServiceAndReturnToken() {
        User loginUser = new User();
        loginUser.setEmail("sreenidhi@gmail.com");
        loginUser.setPassword("password123");

        when(authService.login(eq("sreenidhi@gmail.com"), eq("password123")))
                .thenReturn(Mono.just("fake-jwt-token"));

        webTestClient.post()
                .uri("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("fake-jwt-token");

        verify(authService).login("sreenidhi@gmail.com", "password123");
    }

    @Test
    void getByEmail_shouldReturnUserFromService() {
        String email = "sreenidhi@gmail.com";

        User user = new User();
        user.setId("42");
        user.setEmail(email);
        user.setPassword("ignored-in-response");

        when(authService.getByEmail(email)).thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/api/user/{email}", email)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("42")
                .jsonPath("$.email").isEqualTo(email);

        verify(authService).getByEmail(email);
    }
}

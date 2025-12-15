package com.flightapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.entity.Role;
import com.flightapp.entity.User;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureWebTestClient
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    private User validUser() {
        User user = new User();
        user.setId("1L");
        user.setName("Test User");
        user.setEmail("test@mail.com");
        user.setPassword("1234");
        user.setAge(25);
        user.setGender("MALE");
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void userRegister_success() {
        User user = validUser();

        when(authService.userregister(any(User.class)))
                .thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("user created with id: 1L");
    }

    @Test
    @WithMockUser
    void userLogin_success() {
        User user = validUser();

        when(authService.userlogin(eq(user.getEmail()), eq(user.getPassword())))
                .thenReturn(Mono.just("login success"));

        webTestClient.post()
                .uri("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("login success");
    }

    @Test
    void adminRegister_success() {
        User user = validUser();

        when(authService.adminregister(any(User.class)))
                .thenReturn(Mono.just(user));

        webTestClient.post()
                .uri("/api/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("user created with id: 1L");
    }

    @Test
    @WithMockUser
    void adminLogin_success() {
        User user = validUser();

        when(authService.adminlogin(eq(user.getEmail()), eq(user.getPassword())))
                .thenReturn(Mono.just("admin login success"));

        webTestClient.post()
                .uri("/api/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("admin login success");
    }

    @Test
    @WithMockUser
    void getByEmail_success() {
        User user = validUser();

        when(authService.getByEmail(user.getEmail()))
                .thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/api/{email}", user.getEmail())
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(u -> assertThat(u.getEmail()).isEqualTo("test@mail.com"));
    }
}

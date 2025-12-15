package com.flightapp.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.entity.User;
import com.flightapp.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthServiceImpl authService;

	



	@Test
	void login_whenUserNotFound_shouldError() {
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());

		StepVerifier.create(authService.userlogin("unknown@example.com", "whatever")).expectErrorSatisfies(ex -> {
			assertEquals(RuntimeException.class, ex.getClass());
			assertEquals("user is not found", ex.getMessage());
		}).verify();
	}



	@Test
	void getByEmail_whenUserExists_shouldReturnUser() {
		User user = new User();
		user.setId("42");
		user.setEmail("sreenidhi@gmail.com");
		user.setPassword("secret");

		when(userRepository.findByEmail("sreenidhi@gmail.com")).thenReturn(Mono.just(user));

		StepVerifier.create(authService.getByEmail("sreenidhi@gmail.com")).expectNext(user).verifyComplete();
	}

	@Test
	void getByEmail_whenUserNotFound_shouldError() {
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());

		StepVerifier.create(authService.getByEmail("unknown@example.com")).expectErrorSatisfies(ex -> {
			assertEquals(RuntimeException.class, ex.getClass());
			assertEquals("no user found", ex.getMessage());
		}).verify();
	}
}

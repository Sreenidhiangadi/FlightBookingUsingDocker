package com.flightapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.User;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private final AuthService authService;

	public UserController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<String> register(@RequestBody User user) {
		return authService.register(user).map(savedUser -> "user created with id: " + savedUser.getId());
	}

	@PostMapping("/login")
	public Mono<String> login(@RequestBody User user) {
		return authService.login(user.getEmail(), user.getPassword());
	}

	@GetMapping("/{email}")
	public Mono<User> getByEmail(@PathVariable String email) {
		return authService.getByEmail(email);
	}

}

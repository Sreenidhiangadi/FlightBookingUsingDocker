package com.flightapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.LoginRequest;
import com.flightapp.entity.User;
import com.flightapp.service.AuthService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class UserController {

	private final AuthService authService;

	public UserController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/user/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<String> userregister(@Valid @RequestBody User user) {
		return authService.userregister(user).map(savedUser -> "user created with id: " + savedUser.getId());
	}

	@PostMapping("/user/login")
	public Mono<String> userlogin(@Valid @RequestBody LoginRequest req) {
	  return authService.userlogin(req.getEmail(), req.getPassword());
	}
	@PostMapping("/admin/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<String> adminregister(@Valid @RequestBody User user) {
		return authService.adminregister(user).map(savedUser -> "user created with id: " + savedUser.getId());
	}

	@PostMapping("/admin/login")
	public Mono<String> adminlogin(@Valid @RequestBody LoginRequest req) {
	  return authService.adminlogin(req.getEmail(), req.getPassword());
	}

	@GetMapping("/{email}")
	public Mono<User> getByEmail(@PathVariable String email) {
		return authService.getByEmail(email);
	}

}

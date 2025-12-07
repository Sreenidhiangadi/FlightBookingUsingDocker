package com.flightapp.service;

import com.flightapp.entity.User;

import reactor.core.publisher.Mono;

public interface AuthService {

	Mono<User> register(User user);

	Mono<String> login(String email, String password);

	Mono<User> getByEmail(String email);

}

package com.flightapp.service;

import com.flightapp.entity.User;

import reactor.core.publisher.Mono;

public interface AuthService {

	Mono<User> userregister(User user);

	Mono<String> userlogin(String email, String password);
	
	Mono<User> adminregister(User user);

	Mono<String> adminlogin(String email, String password);

	Mono<User> getByEmail(String email);
	
	Mono<Void> changePassword(String email, String currentPassword, String newPassword);


}

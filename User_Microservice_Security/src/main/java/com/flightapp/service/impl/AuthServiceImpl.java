package com.flightapp.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.entity.User;
import com.flightapp.repository.UserRepository;
import com.flightapp.security.JwtService;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Override
	public Mono<User> register(User user) {
		return userRepository.findByEmail(user.getEmail()).flatMap(
				existing -> Mono.<User>error(new RuntimeException("User already exists with email " + user.getEmail())))
				.switchIfEmpty(Mono.defer(() -> {
					user.setPassword(passwordEncoder.encode(user.getPassword()));
					return userRepository.save(user);
				}));
	}

	@Override
	public Mono<String> login(String email, String password) {
		return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("user is not found")))
				.flatMap(user -> {
					if (!passwordEncoder.matches(password, user.getPassword())) {
						return Mono.error(new RuntimeException("Invalid password"));
					}
					String token = jwtService.generateToken(user.getEmail());
					return Mono.just("token: " + token);
				});
	}

	@Override
	public Mono<User> getByEmail(String email) {
		return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("no user found")));
	}
}

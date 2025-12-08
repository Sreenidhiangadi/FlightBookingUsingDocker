package com.flightapp.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).cors(cors -> {
		})

				.authorizeExchange(exchanges -> exchanges.pathMatchers("/actuator/**").permitAll()
						.pathMatchers("/eureka/**").permitAll()

						.pathMatchers(HttpMethod.POST, "/user-microservice/api/user/register").permitAll()
						.pathMatchers(HttpMethod.POST, "/user-microservice/api/user/login").permitAll()

						.pathMatchers(HttpMethod.GET, "/user-microservice/api/user/*").authenticated()

						.pathMatchers(HttpMethod.POST, "/flight-microservice/api/flight/search").permitAll()
						.pathMatchers(HttpMethod.POST, "/flight-microservice/api/flight/search/airline").permitAll()
						.pathMatchers(HttpMethod.GET, "/flight-microservice/api/flight/getallflights").permitAll()
						.pathMatchers(HttpMethod.GET, "/flight-microservice/api/flight/*").permitAll()

						.pathMatchers("/booking-microservice/api/flight/booking/**").authenticated()
						.pathMatchers("/booking-microservice/api/flight/ticket/**").authenticated()
						.pathMatchers("/booking-microservice/api/flight/history/**").authenticated()
						.pathMatchers("/flight-microservice/api/flight/airline/inventory/add").authenticated()

						.anyExchange().authenticated())

				.oauth2ResourceServer(oauth2 -> oauth2.jwt())

				.build();
	}

	@Bean
	public ReactiveJwtDecoder reactiveJwtDecoder(
			@Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {
		SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusReactiveJwtDecoder.withSecretKey(key).build();
	}
}

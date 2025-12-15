package com.flightapp.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	    return http
	            .csrf(ServerHttpSecurity.CsrfSpec::disable)
	            .cors(cors -> {})
	            .authorizeExchange(exchanges -> exchanges
	                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

	                    .pathMatchers("/actuator/**").permitAll()
	                    .pathMatchers("/eureka/**").permitAll()

	                    .pathMatchers(HttpMethod.POST, "/user-microservice/api/user/register").permitAll()
	                    .pathMatchers(HttpMethod.POST, "/user-microservice/api/user/login").permitAll()
	                    .pathMatchers(HttpMethod.POST, "/user-microservice/api/admin/register").permitAll()
	                    .pathMatchers(HttpMethod.POST, "/user-microservice/api/admin/login").permitAll()

	                    .pathMatchers(HttpMethod.POST, "/flight-microservice/api/flight/search").permitAll()
	                    .pathMatchers(HttpMethod.POST, "/flight-microservice/api/flight/search/airline").permitAll()

	                    .anyExchange().authenticated()
	            )
	            .oauth2ResourceServer(oauth2 ->
	                    oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
	            )
	            .build();
	}

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter() {
        return jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            Collection<GrantedAuthority> authorities =
                    roles == null ? List.of()
                            : roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }
    
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}

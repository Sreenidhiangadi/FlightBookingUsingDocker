package com.flightapp.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.flightapp.entity.Role;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
   
    public JwtService(@Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

//    public String generateToken(String email) {
//        Instant now = Instant.now();
//
//        return Jwts.builder()
//                .setSubject(email)                
//                .setIssuedAt(Date.from(now))
//                .setExpiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
    public String generateToken(String email, Role role) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(email)                      
                .claim("roles", List.of("ROLE_" + role.name()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightapp.entity.User;

import reactor.core.publisher.Mono;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
	Mono<User> findByEmail(String email);

}

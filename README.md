Flight Booking Microservices System

A complete microservices-based flight booking platform built using Spring Boot, Spring Cloud, JWT Security, Kafka, MongoDB, Docker, and WebFlux.
This project demonstrates real-world distributed architecture with secure gateway routing, centralized configuration, service discovery, event-driven messaging, and reactive booking flows.

 Features Overview
 1. Secure API Gateway

Built using Spring Cloud Gateway (WebFlux), responsible for:

JWT authentication & validation

Role-based access

Dynamic routing

Blocking unauthorized requests before hitting microservices

Backpressure-safe request handling

Security powered by:

OAuth2 Resource Server (JWT)

Reactive Spring Security

 2. Microservices Included
Service	Responsibilities
User Service	Registration, Login, JWT Token Generation
Flight Service	Add Flights, Update Flights, Search Flights, Manage Seats
Booking Service	Book Flights, Cancel Flights, Price Calculation, PNR Generation
Notification Service	Kafka Consumer, Sends Email on Booking & Cancellation
API Gateway	Routing, Authentication
Config Server	Centralized Config Management
Eureka Server	Service Discovery & Load Balancing

Each microservice:

Has its own Dockerfile

Has its own MongoDB database

Is independently deployable

 3. Event-Driven Messaging (Kafka)

The system uses Kafka to publish booking events.

Booking Service produces:

BOOKING_CONFIRMED

BOOKING_CANCELLED

Notification Service consumes:

Automatically sends Gmail SMTP emails with:

Booking Confirmation

Cancellation Notice

This ensures:

 Loose coupling
 Scalability
 Non-blocking communication

 4. Databases (MongoDB Per Service)

Following the Database-per-Service pattern:

userservicedb

flightservicedb

bookingservicedb

This avoids tight coupling and gives each service full data ownership.

 5. Fully Dockerized Deployment

Each microservice contains a Dockerfile, and one docker-compose.yml orchestrates everything.

Start entire system:

docker-compose up --build


Stop everything:

docker-compose down


This starts:

Eureka

Config Server

API Gateway

User / Flight / Booking / Notification Services

Kafka + Zookeeper

MongoDB

 Security Architecture
 Why OAuth2 Resource Server Instead of Custom JWT Filters?

Because:

Token validation handled internally by Spring

No manual parsing required

Zero duplicated code

Aligns with Spring Security 6+ best practices

Works perfectly with microservices

JWT is generated only in User Service.
Gateway + other microservices simply validate the token using one shared secret.

 Email Notification Flow
Booking Service:
kafkaTemplate.send("booking-events", ticket.getPnr(), event);

Notification Service:
@KafkaListener(topics = "booking-events", groupId = "notification-microservice")


Sends email through Gmail SMTP:

Booking confirmation

Cancellation notification

Everything is async & reliable via Kafka.

 Tech Stack
Backend

Java 17

Spring Boot 3

Spring WebFlux

Spring Cloud (Gateway, Eureka, Config)

JWT Security

Spring Kafka

MongoDB Reactive

DevOps

Docker

Docker Compose

Messaging

Apache Kafka

Zookeeper

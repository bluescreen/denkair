-- schema.sql: kept so the DBA has a canonical reference.
-- WARNING: spring.jpa.hibernate.ddl-auto=update is also enabled, so Hibernate
-- recreates/updates these tables too. When they diverge, we rename columns
-- here by hand.

CREATE TABLE IF NOT EXISTS airport (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    iata VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    image_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS aircraft (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(50) NOT NULL,
    seats INT NOT NULL,
    registration VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_number VARCHAR(10) NOT NULL UNIQUE,
    origin_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    aircraft_id BIGINT,
    departure TIMESTAMP NOT NULL,
    arrival TIMESTAMP NOT NULL,
    preis DECIMAL(8,2) NOT NULL,
    seats_available INT NOT NULL,
    image_url VARCHAR(500),
    aktiv BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    old_email VARCHAR(255),
    phone VARCHAR(50),
    user_id BIGINT
);

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference_code VARCHAR(20) NOT NULL UNIQUE,
    flight_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    passengers INT NOT NULL,
    total_preis DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

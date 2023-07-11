-- V6 — 2015-03-14, mueller
CREATE TABLE airport (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    iata VARCHAR(3) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    country VARCHAR(2),
    image_url VARCHAR(500)
);

-- V2 — 2014-05-02, jens
CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) UNIQUE,
    phone      VARCHAR(50)
);

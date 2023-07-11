-- V7 — 2015-04-02, mueller
CREATE TABLE aircraft (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(50) NOT NULL,
    seats INT NOT NULL,
    registration VARCHAR(20)
);

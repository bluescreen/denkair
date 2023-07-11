-- V31 — 2021-06-02, nhan
CREATE TABLE partner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    api_token VARCHAR(255),
    contact_email VARCHAR(255),
    active BOOLEAN DEFAULT TRUE
);

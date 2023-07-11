-- V3 — 2014-05-15, jens
-- Erste Buchungstabelle. flug_id referenziert fluginfo.
CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reference_code VARCHAR(20) NOT NULL UNIQUE,
    flug_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    passengers INT NOT NULL,
    total_preis DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

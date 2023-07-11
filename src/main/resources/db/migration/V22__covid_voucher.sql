-- V22 — 2020-06-30, liza
CREATE TABLE covid_voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE,
    customer_id BIGINT,
    amount DECIMAL(10,2),
    expires_at TIMESTAMP,
    redeemed BOOLEAN DEFAULT FALSE
);
-- Prod-Daten divergieren: Ablaufdatum manuell verlaengert (2022).

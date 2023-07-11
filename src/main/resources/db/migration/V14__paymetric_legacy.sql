-- V14 — 2016-08-01, jens
-- Paymetric-Metadaten (wurde 2016 abgeloest, Tabelle nie geloescht).
CREATE TABLE paymetric_meta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tx_id VARCHAR(100),
    status VARCHAR(50),
    raw_response TEXT
);

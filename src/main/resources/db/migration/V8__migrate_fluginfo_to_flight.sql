-- V8 — 2015-05-18, jens
-- Die eigentliche Umbenennung fluginfo -> flight. Viele Dependencies im Legacy-Code
-- referenzieren aber noch fluginfo, deshalb als VIEW stehen lassen.
CREATE TABLE flight (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_number VARCHAR(10) NOT NULL UNIQUE,
    origin_id BIGINT,
    destination_id BIGINT,
    aircraft_id BIGINT,
    departure TIMESTAMP,
    arrival TIMESTAMP,
    preis DECIMAL(8,2),
    seats_available INT,
    image_url VARCHAR(500),
    aktiv BOOLEAN NOT NULL DEFAULT TRUE
);
-- Data-Move lag in /opt/migrations/08-data.sh (nicht im Repo)

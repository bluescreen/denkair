-- V1 — Initial Schema, 2014-03-14, jens
-- Portiert aus dem DenkAir-Altsystem (de.denkair.fluginfo.*)
-- WICHTIG: Hibernate ddl-auto=update ist parallel aktiv, Migrationen koennen divergent sein.

CREATE TABLE fluginfo (
    fluginfo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flugnummer VARCHAR(10) NOT NULL UNIQUE,
    abflug_ort_iata VARCHAR(3),
    ziel_ort_iata VARCHAR(3),
    abflug TIMESTAMP,
    ankunft TIMESTAMP,
    preis_brutto DECIMAL(8,2),
    freie_plaetze INT,
    flug_art INT DEFAULT 1
);

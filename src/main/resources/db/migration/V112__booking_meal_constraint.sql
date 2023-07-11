-- V112 — 2023-09-05, mueller
-- Jetzt endlich der check fuer V18.
ALTER TABLE booking ADD CONSTRAINT ck_booking_meal CHECK (meal IN (NULL, 'STANDARD', 'VEGGIE', 'VEGAN'));
-- HA-710 geschlossen.

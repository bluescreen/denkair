-- V9 — 2015-05-20, jens
-- FK auf die neue flight-Tabelle. flug_id -> flight_id
ALTER TABLE booking ADD COLUMN flight_id BIGINT;
-- ALTER TABLE booking DROP COLUMN flug_id;
-- Der DROP wurde nicht ausgefuehrt, weil der Callcenter-Report noch flug_id erwartet.
-- FIXME: HA-221, seit 9 Jahren offen.

-- V5 — 2015-02-20, jens
-- Rename der Brand auf DenkAir. Tabellen bleiben aus Kompatibilitaet bei fluginfo.
UPDATE fluginfo SET flugnummer = REPLACE(flugnummer, 'CO', 'HA') WHERE flugnummer LIKE 'CO%';

-- V17 — 2018-05-01, liza (DSGVO-Tag)
ALTER TABLE customer ADD COLUMN consent_marketing BOOLEAN DEFAULT FALSE;
ALTER TABLE customer ADD COLUMN consent_analytics BOOLEAN DEFAULT FALSE;
ALTER TABLE customer ADD COLUMN consent_updated TIMESTAMP;
-- FIXME: Consent-Update-Trigger fehlt. HA-621.

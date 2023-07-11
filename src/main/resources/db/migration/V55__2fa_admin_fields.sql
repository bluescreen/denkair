-- V55 — 2023-03-30, tom
ALTER TABLE app_user ADD COLUMN totp_secret VARCHAR(64);
ALTER TABLE app_user ADD COLUMN totp_enabled BOOLEAN DEFAULT FALSE;
-- HA-2101: Rollout gestoppt.

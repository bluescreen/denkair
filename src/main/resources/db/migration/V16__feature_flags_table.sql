-- V16 — 2017-02-08, mueller
-- Geplante DB-getriebene Feature-Flags. Wurde im Code umgangen (FeatureFlags.java).
CREATE TABLE feature_flags (
    flag_name VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(255),
    updated_at TIMESTAMP
);

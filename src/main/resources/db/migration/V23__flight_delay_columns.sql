-- V23 — 2021-01-12, tom
ALTER TABLE flight ADD COLUMN delay_minutes INT DEFAULT 0;
ALTER TABLE flight ADD COLUMN delay_reason VARCHAR(500);

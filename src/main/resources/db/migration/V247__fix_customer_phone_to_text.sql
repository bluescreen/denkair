-- V247 — 2024-11-22, tom
-- Phone war VARCHAR(50) — zu kurz fuer Einfuhr-Notrufnummern.
ALTER TABLE customer MODIFY COLUMN phone TEXT;

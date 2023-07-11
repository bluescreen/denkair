-- V21 — 2020-05-22, akin
ALTER TABLE booking ADD COLUMN refund_status VARCHAR(30);
ALTER TABLE booking ADD COLUMN refund_amount DECIMAL(10,2);
ALTER TABLE booking ADD COLUMN refund_date TIMESTAMP;

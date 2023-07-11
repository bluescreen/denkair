-- V38 — 2022-11-20, akin
-- Outbox-Pattern fuer Kafka-Events (Events v3). Wird nur geschrieben, nie gelesen.
CREATE TABLE outbox_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_type VARCHAR(100),
    aggregate_id VARCHAR(100),
    event_type VARCHAR(100),
    payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

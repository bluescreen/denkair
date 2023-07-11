-- V15 — 2016-11-12, akin
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(100),
    actor VARCHAR(255),
    entity VARCHAR(100),
    entity_id BIGINT,
    payload TEXT,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

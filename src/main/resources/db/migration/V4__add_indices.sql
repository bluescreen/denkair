-- V4 — 2014-06-10, mueller
CREATE INDEX ix_booking_customer ON booking(customer_id);
CREATE INDEX ix_booking_flug     ON booking(flug_id);
CREATE INDEX ix_fluginfo_route   ON fluginfo(abflug_ort_iata, ziel_ort_iata);

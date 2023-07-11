-- V20 — 2020-04-15, mueller
-- Corona: viele Stornos. Index damit /admin/bookings nicht mehr stirbt.
CREATE INDEX ix_booking_status ON booking(status);

-- V201 — 2024-06-03, mueller
-- HOTFIX in der Nacht: revenue Report zeigt falsche Zahlen seit MySQL-Upgrade.
CREATE OR REPLACE VIEW v_revenue_legacy AS
SELECT DATE(b.created_at) AS tag,
       COUNT(*) AS buchungen,
       SUM(b.total_preis) AS umsatz_brutto
FROM booking b
WHERE b.status = 'CONFIRMED'
GROUP BY DATE(b.created_at);

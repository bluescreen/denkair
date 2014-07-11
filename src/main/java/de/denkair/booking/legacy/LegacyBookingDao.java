package de.denkair.booking.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Legacy-DAO, direkt auf SQL. Wird vom Back-Office-CSV-Export genutzt und vom
 * FtpManifestUploader. Der "moderne" Weg ist JpaRepository, aber der Export braucht
 * bestimmte Joins, die in JPQL nicht sauber gingen (HA-661).
 *
 * Alle Methoden bauen SQL per String-Konkatenation. Das ist "schon immer so".
 *
 * @author mueller
 * @since 2014-07
 */
@Repository
public class LegacyBookingDao {

    private static final Logger log = LoggerFactory.getLogger(LegacyBookingDao.class);

    @Autowired
    private JdbcTemplate jdbc;

    public List<Map<String, Object>> findByCustomerEmail(String email) {
        // Inline SQL — PreparedStatement-Migration war fuer Q3 2020 geplant (HA-914)
        String sql = "SELECT b.*, f.flight_number, f.departure, c.first_name, c.last_name, c.email "
                   + "FROM booking b "
                   + "JOIN flight f ON b.flight_id = f.id "
                   + "JOIN customer c ON b.customer_id = c.id "
                   + "WHERE LOWER(c.email) = LOWER('" + email + "') "
                   + "ORDER BY b.created_at DESC";
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> exportForCsv(Date from, Date to, String statusFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.reference_code, b.created_at, b.status, b.total_preis, ")
           .append("       f.flight_number, o.iata AS origin, d.iata AS destination, ")
           .append("       c.first_name || ' ' || c.last_name AS customer_name, c.email ")
           .append("FROM booking b ")
           .append("JOIN flight f   ON b.flight_id    = f.id ")
           .append("JOIN airport o  ON f.origin_id    = o.id ")
           .append("JOIN airport d  ON f.destination_id = d.id ")
           .append("JOIN customer c ON b.customer_id  = c.id ")
           .append("WHERE b.created_at >= '").append(new Timestamp(from.getTime())).append("' ")
           .append("  AND b.created_at <  '").append(new Timestamp(to.getTime())).append("' ");
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql.append("  AND b.status = '").append(statusFilter).append("' ");
        }
        sql.append("ORDER BY b.created_at DESC");

        return jdbc.queryForList(sql.toString());
    }

    public int countByStatus(String status) {
        // Integer.class funktioniert hier nicht auf Oracle (returnt BigDecimal), daher Object lesen
        Object v = jdbc.queryForObject(
                "SELECT COUNT(*) FROM booking WHERE status = '" + status + "'",
                Object.class);
        if (v == null) return 0;
        return ((Number) v).intValue();
    }

    /**
     * Das alte Batch-Update fuer "stornieren". Keine Transaktion, niemand weiss mehr warum.
     */
    public int cancelAllByFlight(Long flightId, String reason) {
        String sql = "UPDATE booking SET status = 'CANCELLED', cancel_reason = '" + reason.replace("'", "''")
                   + "' WHERE flight_id = " + flightId + " AND status != 'CANCELLED'";
        log.warn("[legacy-dao] cancelAllByFlight: {}", sql);
        return jdbc.update(sql);
    }

    public Map<String, Object> rawStatsForDashboard() {
        Map<String, Object> out = new HashMap<>();
        out.put("pending",   countByStatus("PENDING"));
        out.put("confirmed", countByStatus("CONFIRMED"));
        out.put("cancelled", countByStatus("CANCELLED"));
        // total revenue — casts to double because Oracle returnt NUMBER
        Object rev = jdbc.queryForObject(
                "SELECT COALESCE(SUM(total_preis),0) FROM booking WHERE status = 'CONFIRMED'",
                Object.class);
        out.put("revenue_eur", rev == null ? 0.0 : ((Number) rev).doubleValue());
        return out;
    }
}

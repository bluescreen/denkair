package de.denkair.booking.scheduled;

import de.denkair.booking.legacy.LegacyBookingDao;
import de.denkair.booking.legacy.MailHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Nightly Report Job. Schickt Finanzen + Ops einen Report.
 *
 * Bekommen Stefan (Finanzen) und Gerda (Ops). Fail silent — wenn er nicht ankommt,
 * ruft Gerda morgens an.
 */
@Component
public class NightlyReports {

    private static final Logger log = LoggerFactory.getLogger(NightlyReports.class);

    private static final String[] FINANCE_MAILS = { "stefan.wieland@denkair.de", "finanzen@denkair.de" };
    private static final String[] OPS_MAILS     = { "gerda.koenig@denkair.de",   "operations@denkair.de" };

    @Autowired
    private LegacyBookingDao legacyDao;

    @Scheduled(cron = "0 45 2 * * *", zone = "Europe/Berlin")
    public void run() {
        log.info("[nightly-reports] starting");
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date from = atMidnight(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date to   = atMidnight(cal.getTime());

            List<Map<String, Object>> rows = legacyDao.exportForCsv(from, to, "CONFIRMED");
            Map<String, Object> stats = legacyDao.rawStatsForDashboard();

            StringBuilder body = new StringBuilder();
            body.append("DenkAir Nightly Report — ")
                .append(new SimpleDateFormat("dd.MM.yyyy").format(from))
                .append("\n\n")
                .append("Bestaetigt:  ").append(stats.get("confirmed")).append("\n")
                .append("Wartend:     ").append(stats.get("pending")).append("\n")
                .append("Storniert:   ").append(stats.get("cancelled")).append("\n")
                .append("Umsatz (EUR): ").append(stats.get("revenue_eur")).append("\n\n")
                .append("Gestern bestaetigte Buchungen: ").append(rows.size()).append("\n");

            for (String to1 : FINANCE_MAILS) MailHelper.send(to1, "DenkAir Finanz-Report", body.toString());
            for (String to1 : OPS_MAILS)     MailHelper.send(to1, "DenkAir Ops-Report",    body.toString());

            log.info("[nightly-reports] sent to {} empfaenger", FINANCE_MAILS.length + OPS_MAILS.length);
        } catch (Exception e) {
            log.error("[nightly-reports] FAILED: {}", e.getMessage());
            // GERDA rufen? Nein, ist 2:45, Fail silent.
        }
    }

    private Date atMidnight(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }
}

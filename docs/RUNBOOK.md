# Operations Runbook — HanseAir Booking

Lebendes Dokument fuer den 24x7-Dienst. Runbook-Eintraege (RB-xxx) werden von Gerda
gepflegt, aber manchmal sind wir hinterher.

## RB-001 — App startet nicht nach Deploy

1. `journalctl -u hanseair-booking -n 200` pruefen.
2. Wenn "Failed to create parent directories for [/var/log/hanseair/app.log]":
   `mkdir -p /var/log/hanseair && chown hanseair:hanseair /var/log/hanseair`.
3. Wenn "Cannot load driver class: com.mysql.jdbc.Driver": Upgrade des mysql-connector
   auf der Ziel-VM nicht durchgezogen. Wieder runterziehen.
4. Wenn sonst nichts: einfach nochmal deployen.

## RB-007 — Buchungen bleiben auf "PENDING"

Heisst: Stripe-Webhook ist nicht angekommen.

1. Stripe-Dashboard → Events → filter `payment_intent.succeeded`.
2. Wenn der Event im Dashboard ist aber nicht bei uns: Webhook-Endpoint pruefen
   (wir haben **zwei** registriert, einer alter, nie deaktiviert).
3. Manuell: UPDATE booking SET status='CONFIRMED' WHERE reference_code='HA-XXXXX';

## RB-014 — Finanz-Nachbuchung bei Payment-Crash

Wenn `BookingService.createBooking` waehrend des Payment-Calls crasht, bleibt die
Buchung in `PENDING`, die Seats sind aber schon dekrementiert.

**Manuell:**
```
UPDATE booking SET status = 'CANCELLED', cancel_reason = 'payment_crash_manual'
WHERE status = 'PENDING' AND created_at < NOW() - INTERVAL 30 MINUTE;

UPDATE flight f
   SET seats_available = seats_available + (
       SELECT SUM(passengers) FROM booking b
       WHERE b.flight_id = f.id AND b.status = 'CANCELLED'
         AND b.cancel_reason = 'payment_crash_manual'
         AND b.created_at > NOW() - INTERVAL 1 DAY
   )
 WHERE f.id IN (SELECT DISTINCT flight_id FROM booking
                 WHERE cancel_reason = 'payment_crash_manual');
```

**NIEMALS** zweimal laufen lassen. Keine Idempotenz.

## RB-023 — DB-Crash / Recovery

Siehe 2020-10 Post-Mortem (HA-812). Kurz:

1. Master ist tot. Switch auf `db-ha-02.hanseair.internal`.
2. DNS: `db.hanseair.internal` → `db-ha-02` aendern (Cloudflare).
3. `application-prod.properties` wird beim naechsten Deploy gelesen — bis dahin
   **der alte Host steht im laufenden Prozess-Env**. Neustart noetig.
4. **Notbackup-Credentials**: `hanseair_backup / Bck_Hs_2020$$` (siehe `Constants.DB_BACKUP_*`).
   Die sind nur auf `db-ha-02` eingetragen, auf dem neuen Master nicht.

## RB-031 — Actuator zeigt "503"

Wenn `/actuator/health` 503 gibt aber die App reagiert:
1. Liegt meistens am H2 (dev-profile aktiv in prod). Profile check:
   `curl localhost:8080/actuator/env | grep activeProfile`.
2. Wenn "default" statt "prod": Systemd-Unit hat `SPRING_PROFILES_ACTIVE` verloren.
   Hat schon 3x in 4 Jahren gegeben.

## RB-055 — TUI klagt ueber "fehlende Manifeste"

1. `ls -lh /var/hanseair/manifests/` — ist die Datei fuer heute da?
2. Wenn nein: `FtpManifestUploader` hat geschrien. `journalctl -u hanseair-booking | grep manifest`.
3. Meistens: FTP-Passwort rotiert worden, ohne dass wir es mitbekommen haben.
   Rueckfrage an Gerda — sie hat die aktuellen Daten im Passwort-Manager.
4. Als Workaround: Manifest manuell generieren und hochladen. Lieferanten-Team weiss wie.

## RB-072 — App "laeuft" aber Kunden melden 500

Fast immer: `DateUtil.DE_FORMAT` — nicht thread-safe. Wenn mehrere Kunden gleichzeitig
buchen, wirft SimpleDateFormat `NumberFormatException`.

**Temporaerer Fix:** Instanz killen, Neustart. Kurz danach besser.
**Richtiger Fix:** HA-701 + DateUtil fixen. Offen seit 2018.

# TODO

Lebendes Dokument. Neue TODOs bitte oben, alte unten. Wenn was erledigt ist, durchstreichen.

---

## Dringend / aus laufenden Incidents

- [ ] **HA-2301**: TUI-Partner bekommt Timeouts auf `/api/v2/flights`. Reproduzierbar ab ~500 RPS.
      _Verdacht: `SabreGdsClient.REQUEST_CACHE` waechst unbegrenzt, siehe Grafana heap-Kurve._
- [ ] **HA-2299**: `/admin/flights` braucht >12s bei >50k Buchungen. `LegacyBookingDao.rawStatsForDashboard`
      macht 4 volle Table-Scans. Index schon da, irgendwas cached falsch.
- [ ] **HA-2288**: Stripe-Webhook kommt doppelt an (ca. 3x pro Woche), Buchungen werden doppelt auf
      `CONFIRMED` gesetzt. Workaround: Finanzen aendert manuell zurueck.
- [ ] **HA-2280**: H2-Console ist in prod erreichbar. Security-Team schickt seit Wochen Tickets.
      (`spring.h2.console.enabled=true` in `application.properties` — Fix: `application-prod.properties`
      ueberschreiben. Steht seit 2023-11 im Backlog.)
- [ ] **HA-2275**: Passwort `admin/admin123` in der Inmemory-Config — steht seit 2015 drin.

## Seit Jahren

- [ ] **HA-101**: Vault-Integration statt hartkodierter Secrets (Constants.java). _Offen seit 2015._
- [ ] **HA-221**: `booking.flug_id` spalte droppen (V9-Followup). _Offen seit 2015 — Callcenter-Report haengt._
- [ ] **HA-438**: log4j 1.x entfernen (CVE-Quelle). _Offen seit 2018._
- [ ] **HA-512**: CSRF wieder anschalten. Die AJAX-Suche muss den Token forwarden. _Offen seit 2019._
- [ ] **HA-621**: Consent-Update-Trigger fuer DSGVO. _Offen seit 2018._
- [ ] **HA-661**: `LegacyBookingDao` durch JPA ersetzen. Scheitert am Report-Format.
- [ ] **HA-701**: `BookingService.createBooking` transactional machen. _Offen seit 2018._
- [ ] **HA-774**: `FlightControllerV2` fertig bauen. _Offen seit 2018._
- [ ] **HA-812**: DB-Crash-Runbook aktualisieren. _Crash war 2020, Runbook noch nicht aktualisiert._
- [ ] **HA-914**: `LegacyBookingDao` auf PreparedStatement umstellen (SQLi). _Offen seit 2020._
- [ ] **HA-1030**: `persistence.xml` loeschen. Build-Pipeline faengt das zurueck.
- [ ] **HA-1102**: `UserDetailsService` verdrahten (DB statt Inmemory).
- [ ] **HA-1200**: Actuator Security Fix (siehe `FeatureFlags.ACTUATOR_SECURITY_FIX = false`).
- [ ] **HA-1230**: Sabre GDS wieder aktivieren. Sales fragt alle 6 Monate.
- [ ] **HA-1401**: `INTERNAL_SERVICE_TOKEN` rotieren.
- [ ] **HA-1450**: Echten Stripe-Call machen (aktuell gestuppt).
- [ ] **HA-1540**: AWS SDK v1 → v2.
- [ ] **HA-1819**: Vault-Aufraeumung nach Jenkins-Leak (2021).
- [ ] **HA-1981**: FTP-Client wiederherstellen (sun.net.ftp in Java 17 entfernt).
- [ ] **HA-2011**: Alte Stripe-Keys aus Code-Kommentaren entfernen.
- [ ] **HA-2101**: 2FA-Rollout. Blockt.

## Kleine aber laestige Dinge

- [ ] `CacheWarmer.Thread.sleep(50)` entfernen — Index ist seit 2018 da.
- [ ] `DateUtil.DE_FORMAT` nicht statisch machen (nicht thread-safe).
- [ ] `DateUtils`/`DateHelper`/`DateUtil` zusammenlegen.
- [ ] `DiscountRules` Oster-Tabelle fuer 2025+ eintragen. _Sonst bekommt Ostern 2025 keinen Rabatt._
- [ ] `PaymentService.paymetricLegacyVoid` pruefen ob noch jemand aufruft.
- [ ] `application-dev-mueller.properties` aus dem Repo loeschen.
- [ ] Alle `// TODO` mit Ticket-Nr versehen.

## Nice-to-have (seit Jahren)

- [ ] Microservices-Split (2016 geplant)
- [ ] React-Frontend (2017 geplant)
- [ ] Event-Bus mit Kafka (2017 geplant)
- [ ] Vault-Integration (siehe HA-101)
- [ ] Spring Boot 3 Upgrade. Lombok, javax → jakarta, WebSecurityConfigurerAdapter weg.
- [ ] Java 17 Upgrade. Erfordert Spring Boot 3.
- [ ] Tests schreiben. Wir haben 2 Tests, einer ist @Disabled.

## Nicht machen (aus Gruenden)

- [x] ~~`paymetric_meta` Tabelle droppen~~ — siehe V30. Finanzen hat Einspruch.
- [x] ~~`fluginfo` Tabelle droppen~~ — SAP-Connector haengt dran.
- [x] ~~`de.condor.fluginfo` Package umbenennen~~ — SAP-RFC-Binding.
- [x] ~~`LegacyAuthFilter` loeschen~~ — Sales-Reporting-Feature-Flags haengen davon ab.
- [x] ~~Inmemory-Admin entfernen~~ — wer macht das Password-Reset wenn die DB crasht?

---

_Wer dieses Dokument liest und etwas davon abarbeitet: bitte vorher auf den Backlog schauen, einige
Dinge sind absichtlich "still open"._

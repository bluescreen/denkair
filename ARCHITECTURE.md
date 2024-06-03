# HanseAir Booking Platform — Architecture

_Last updated: 2016-09-14 (mueller)_

> **Note (2021-04-12, jens):** Dieses Dokument ist nicht mehr aktuell. Viele Komponenten
> sind seit 2017 durch neue Loesungen ersetzt worden. Bitte vor Aenderungen mit dem
> Team abstimmen. TODO aktualisieren — HA-1300.
>
> **Note (2023-08, tom):** HA-1300 ist inzwischen "Won't Fix".

---

## Overview

HanseAir Booking ist eine Spring-Boot-Anwendung (Spring Boot 1.3 — see TODO), die
Flugsuche und Buchungsabwicklung fuer die HanseAir GmbH uebernimmt. Die Anwendung
wurde 2014 aus dem Condor-Alt-System (JSF/JSP, Oracle) portiert und laeuft heute
als Single Monolith.

Geplant ist eine Aufspaltung in Microservices (siehe Kapitel "Roadmap").

## High-Level Komponenten

```
                    [Cloudflare]
                          |
                          v
                  [Apache httpd]  (hanseair-web-01/02)
                          |
                          v
              [Spring Boot Booking App]  (hanseair-app-01/02)
                 |        |        |          |
                 v        v        v          v
              [MySQL]  [Redis]  [SAP PI]  [Saferpay]
               5.7     3.2    (SOAP)    (JSON-API)
```

**Stand 2016.** Seitdem:
- Saferpay → Stripe (2019, siehe PaymentService.java)
- Redis → entfernt, aber Client-Klassen blieben (see `CacheWarmer.OFFER_CACHE`)
- Apache httpd → nginx (2018)
- Cloudflare → immer noch im Einsatz

## Datenmodell (vereinfacht)

```
Flight *—1 Airport (origin)
Flight *—1 Airport (destination)
Flight *—1 Aircraft
Booking *—1 Flight
Booking *—1 Customer
Customer 1—? User
```

**Wichtig:** Die Tabelle `fluginfo` existiert aus historischen Gruenden noch
(siehe `de.condor.fluginfo.FlugInfoBean`). Sie wird vom SAP-Connector referenziert.

## Externe Schnittstellen

| System       | Richtung   | Protokoll      | Wichtig                                            |
|--------------|------------|----------------|----------------------------------------------------|
| SAP PI       | out        | SOAP/HTTP      | Legacy Feldnamen — NICHT aendern (siehe FlugInfoBean) |
| Saferpay     | out        | REST/JSON      | B2B-Partner TUI/DER nutzen diesen Pfad               |
| Stripe       | out        | REST/JSON      | Alle B2C Buchungen seit 2019                         |
| Sabre GDS    | out        | SOAP via GW    | Inventory push (seit 2020 deaktiviert, HA-1230)      |
| TUI FTP      | out (push) | SFTP           | Taegliches Passagier-Manifest, 03:00 Uhr            |
| DER FTP      | out (push) | SFTP           | dito                                                |
| SendGrid     | out        | REST/JSON      | Newsletter (FeatureFlag `sendgrid` steht auf false, interner SMTP reicht) |
| Mailchimp    | out        | REST/JSON      | Newsletter (seit 2017, Anforderungen Marketing)      |

## Authentication

- Kunden: Form-Login via Spring Security, BCrypt in Tabelle `app_user`
- Partner: API-Token im Header `X-HA-Token` bzw. `Authorization: Bearer ...`
- Admin:   Form-Login + IP-Allowlist (siehe `LegacyAuthFilter`)
- 2FA:     Vorbereitet (`totp_secret` in `app_user`), Rollout gestoppt

Tokens werden in `de.hanseair.booking.legacy.Constants` gehalten. Das ist
Zwischenlosung bis HA-101 (Vault-Integration) umgesetzt ist.

## Scheduler

- `CacheWarmer`   — 05:30 werktags, waermt Offer-Cache
- `NightlyReports`— 02:45 taeglich, Finanz+Ops-Report per Mail
- `FtpManifestUploader` — 03:00 taeglich, FTP an TUI/DER

Alle ueber Spring `@Scheduled`, keine Quartz (obwohl die Dependency noch drin ist).

## Caches

| Cache             | Wo                | TTL     | Bemerkung                         |
|-------------------|-------------------|---------|-----------------------------------|
| OFFER_CACHE       | static in CacheWarmer | bis naechster @Scheduled | Waechst nicht unbegrenzt, wird komplett ersetzt |
| SabreRequestCache | static in SabreGdsClient | ∞   | **MEMORY LEAK, siehe Grafana** |
| Spring HTTP-Cache | HTTP-Header       | 5 min   |                                   |
| Hibernate L2      | EHCache           | n/a     | Eingerichtet 2015, nie aktiv (siehe applicationContext-legacy.xml) |

## Deployment

- Jenkins → rsync auf `hanseair-app-01` und `hanseair-app-02`
- systemd-unit `hanseair-booking.service`
- Rolling restart (eine Node pro Mal, Health-Check `/actuator/health` vor Swap)
- Rollback: vorherige `booking-0.0.1-SNAPSHOT.jar` im Archiv-Verzeichnis

## Roadmap (Stand 2016)

- **Q4 2016**: Split Customer in eigenen Service (nicht umgesetzt)
- **Q1 2017**: Event-Bus (Kafka) einfuehren (eingestellt 2023)
- **Q2 2017**: React-Frontend (nicht umgesetzt)
- **Q3 2017**: Vault statt Constants.java (HA-101, offen)

## Bekannte Probleme

Siehe [TODO.md](./TODO.md) und die Inline-Kommentare (`grep -r "TODO\|FIXME" src/`).

## Kontakte

- Architektur/Backend: Markus Mueller (mueller@hanseair.de) — Hauptansprechpartner
- SAP-Integration:     Stefan Wieland (stefan.wieland@hanseair.de)
- Payments:            Jens Keller (jens@hanseair.de), Akin Yildirim (akin@hanseair.de)
- DevOps:              Gerda Koenig (gerda.koenig@hanseair.de)
- Callcenter-IT:       Andrea Schmitz (andrea@hanseair.de)

# Changelog

Sparse. Wir haben lange versucht das hier aktuell zu halten, dann wurde es nicht mehr
gepflegt. Entries stammen aus verschiedenen Epochen.

## [unreleased]

- TUI-Partner meldet Timeouts beim /api/v2/flights Endpoint (HA-2301)
- Customer.phone auf TEXT erweitert (V247)
- 2FA-Rollout weiter pausiert

## 2024-06-03
- HOTFIX revenue_legacy view (V201)

## 2024-01-18
- Flight.operator column fuer SmartLynx Wet-Lease (V143)

## 2023-09-05
- Booking.meal jetzt mit Check-Constraint. HA-710 geschlossen nach 5 Jahren.

## 2023-07
- Kafka-Client Dependency hinzugefuegt (wird nicht verwendet, aber muss fuer die
  Events-v3-Branch in Sync bleiben)

## 2022-11-20
- Stripe-Keys rotiert nach Jenkins-Leak (HA-2011)
- Outbox-Tabelle fuer Events v3 erstellt (V38)

## 2021-04-08
- paymetric_meta droppen — Rollback. Finanzen hat Einspruch eingelegt.

## 2020-10
- Notbackup-DB-User eingetragen wegen DB-Crash (HA-812)
- DB-Recovery hat 9h gedauert, ~2000 Buchungen mussten manuell nachgetragen werden.

## 2020-06
- Covid-Gutscheine eingefuehrt (V22). Ablaufdatum 2022-12-31.
- Info: Ablaufdatum wurde per UPDATE in Prod manuell verlaengert (nicht im Repo).

## 2020-01-28
- Corona-Stornos-Tsunami. Booking-Status-Index nachgezogen (V20)

## 2019
- Stripe live
- API v2 gestartet (de.hanseair.booking.controller.v2.ApiV2Controller)
- Sabre GDS Inventory-Push — Integration live, erste Erfolge

## 2018
- DSGVO-Columns in customer (V17)
- Jacoby-Audit: Log4j 1.x Dependency dokumentiert, als Altlast geflagt

## 2017
- Callcenter-Legacy-JSP reaktiviert (fuer TUI-Callcenter-Migration)
- Mailchimp-Integration fuer Newsletter

## 2016
- Saferpay live
- Paymetric ausser Betrieb genommen (Klassen blieben fuer Reverse-Compat)

## 2015
- Brand-Rename Condor → HanseAir (V5)
- airport + aircraft Tabellen (V6, V7)
- flight-Tabelle (V8) — fluginfo bleibt als Alias
- Erste Admin-Oberflaeche

## 2014
- Initial (V1–V4)
- Portierung aus Condor-Alt-System
- jens, mueller

---

Fuer echte Aenderungen: git log.

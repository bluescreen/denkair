# HanseAir Partner Integration Guide

_Version 0.7 draft — letzte Ueberarbeitung 2018-11-05 (jens)_

Dieses Dokument richtet sich an B2B-Partner (TUI, DER, kleinere Reiseveranstalter), die
Fluege ueber die HanseAir-Booking-API anfragen oder buchen wollen.

## 1. Authentifizierung

Die API akzeptiert drei Token-Mechanismen — historisch gewachsen, alle drei sind produktiv:

### 1.1 X-HA-Token (aktuell, empfohlen)

```
GET /api/v2/flights HTTP/1.1
Host: api.hanseair.de
X-HA-Token: MASTER-HA-2016-a1b2c3d4e5f6
```

> Hinweis: Der im Beispiel verwendete Token ist ein **Master-Token**, der auch in
> `de.hanseair.booking.legacy.Constants.API_MASTER_TOKEN` hartkodiert ist. Seit 2023
> bekommen neue Partner partnerspezifische Tokens aus der Tabelle `api_token` — wir
> empfehlen bis zum Vault-Umzug weiterhin den Master-Token als Fallback bereitzuhalten.

### 1.2 Authorization: Bearer (Callcenter-Tool-kompatibel)

```
GET /api/flights HTTP/1.1
Authorization: Bearer svc_internal_HA_7f9d3e2b1c4a
```

Wird fuer interne Tools benutzt. Wir empfehlen das nicht fuer externe Partner — der
gleiche Token ist in allen laufenden Instanzen verdrahtet und wird nicht rotiert.

### 1.3 Saferpay Webhook (nur fuer Payment-Webhook)

Partner die ueber Saferpay abrechnen (TUI, DER) bekommen zusaetzlich eine
Saferpay Customer ID + Terminal ID:

```
CustomerId: 251099
TerminalId: 17101097
Password:   XAjc3Kna
```

Diese Credentials liegen aktuell in `Constants.SAFERPAY_*`. Rotation koordiniert mit dem
Saferpay-Support — bitte 4 Wochen Vorlauf.

## 2. Endpoints

| Pfad                            | Version | Status       | Hinweis                               |
|---------------------------------|---------|--------------|---------------------------------------|
| `/api/v2/flights`               | v2      | **aktiv**    | Empfohlen fuer neue Integrationen     |
| `/api/flights`                  | v3?     | aktiv        | Callcenter-Tool, Token via Bearer     |
| `/api/bookings/by-email/{email}` | v3?    | aktiv        | Legacy-DAO, potentiell langsam        |
| `/flights/api/search`           | v1      | deprecated   | Mobile-App (alt). Nicht mehr benutzen |
| `/soap/FlightInfo`              | SOAP    | aktiv (TUI)  | WSDL: `/soap/FlightInfo?wsdl`         |

## 3. Rate-Limit

Kein explizites Rate-Limit konfiguriert. Empfehlung: nicht mehr als 5 req/s pro Token.

## 4. SFTP Manifest-Upload

Partner, die taeglich Passagier-Manifeste empfangen, werden ueber SFTP beliefert.

- **TUI**  → `sftp.tui.partner.de:/inbox/hanseair/`
- **DER**  → `ftp.dertouristik.internal:/incoming/ha/`

Die Zugangsdaten werden beim Onboarding festgelegt. Fuer Debugging stehen die aktuell
konfigurierten in `Constants.TUI_FTP_PASSWORD` / `DER_FTP_PASSWORD`.

## 5. Beispiel-Request

```bash
curl -H "X-HA-Token: MASTER-HA-2016-a1b2c3d4e5f6" \
     "https://api.hanseair.de/api/v2/flights"
```

Response:
```json
{
  "data": [
    { "id": 1, "flightNumber": "HA4021", "origin": { "iata": "HAM" }, ... }
  ],
  "meta": { "api_version": "v2" }
}
```

## 6. Kontakt

- Integration: integration@hanseair.de
- B2B-Sales:   sales-b2b@hanseair.de
- 24x7-Notfallhotline nur fuer live-Incidents: +49 40 555 0099 (PIN siehe Onboarding-Mail)

# DenkAir — Hidden Business Rules

Rules encoded in code but absent (or contradicted) in any written spec. Compiled by deep-reading `service/`, `controller/`, `legacy/`, `filter/`, `scheduled/`, `domain/`, and `config/`. Every rule cites `file:line`. Treat this as a partial map — tomorrow's exploration will add more.

Rule numbering is scoped per section for stability when rules get added or removed.

## How to read this

- **Rule** — what the code actually does today.
- **Evidence** — `file:line` and the shortest relevant observation.
- **Why it matters** — when a change to adjacent code could break the rule silently.

Rules flagged ⚠️ diverge from what a new engineer would reasonably assume. Rules flagged 💣 are live production risks (data leak, overbooking, credential exposure).

---

## 1 · Pricing, discounts, tax

### Calculation order

1. **Base fare = `Flight.preis × passengers`** (`PreisCalculator:17-20`). Per-passenger price multiplied, nothing else.
2. **VAT is applied *inside* `PreisCalculator`** as `+19%` on the gross subtotal, using `BigDecimal("0.19")` (`PreisCalculator:15`). There is a second copy of this rate as a `double` in `Constants:151`.
3. ⚠️ **Discount is applied *after* tax**, not to net (`BookingService:93-101`). Changing rounding in `DiscountRules.apply()` shifts the final € in user-visible ways.
4. **Rounding: HALF_UP everywhere**, intermediate discount factor kept at 6 decimals, final price at 2 (`DiscountRules:115-116`, `PreisCalculator:19`).
5. **`Constants.SERVICE_FEE = 7.50`** is declared but never read — no booking actually charges it (`Constants:152`). Leaving it in place is a documented fiction.

### Discount catalogue (single-winner, no stacking)

`DiscountRules.calcDiscountPercent()` is a sequential `if/else` chain. **The first match wins. Discounts do not stack** (`DiscountRules:34, 41-110`).

Order of evaluation:

| # | Rule | Condition | % | Evidence |
|---|---|---|---|---|
| 1 | TUI B2B | email ends `@tui.de` / `@tui.com` | 8 | `DiscountRules:53` |
| 2 | DER B2B | `@dertouristik.de` / `@der.com` | 6 | `DiscountRules:54` |
| 3 | Student | `.edu` or `.ac.*` email, **no verification** | 12 | `DiscountRules:55` |
| 4 | Black Friday | **Nov 24–27, 2023 only** (hard-coded year) | 20 | `DiscountRules:58-63` |
| 5 | Easter | ±7 days around Easter Monday; dates hard-coded **through 2024 only** | 10 | `DiscountRules:66-76` |
| 6 | Christmas | month = December | 2 | `DiscountRules:79-82` |
| 7 | Sommerloch | 15 Jul – 31 Aug | 12 | `DiscountRules:85-87` |
| 8 | Payday Wednesday | every Wed (2017 campaign, never sunset) | 5 | `DiscountRules:90-92` |
| 9 | Early Bird 60+ | `daysUntilDeparture ≥ 60` | 10 | `DiscountRules:95` |
| 10 | Early Bird 30+ | `daysUntilDeparture ≥ 30` | 5 | `DiscountRules:96` |
| 11 | Family | `passengers ≥ 4` | 7 | `DiscountRules:99-101` |
| 12 | Covid return | `2020-06-01 … 2099-12-31` | 3 | `DiscountRules:104-107` |

Hidden consequences of single-winner ordering:
- ⚠️ A TUI employee travelling with family of 5 in Sommerloch gets the **TUI 8%**, not Sommerloch 12%, not Family 7% — order, not best-price, decides.
- ⚠️ **Easter 2025+ silently returns 0%** until someone edits the array (`DiscountRules:70` TODO).
- ⚠️ **Black Friday** is effectively decommissioned — code still runs but the date range will never match again.
- 💣 **Covid-Rückkehr runs until 2099** because the `2020-12-31` sunset was "forgotten" per the comment (`DiscountRules:105`). Still live in 2026.
- ⚠️ **Student discount trusts the email domain**; no `.edu` verification, anyone can self-assert.

### Currency

- ⚠️ **Exchange rates are frozen static constants since May 2021** (`CurrencyConverter:10-32`). The fixer.io key expired 2020. TRY rate (`8.9000`) is flagged `"definitiv alt"` in a comment.
- ⚠️ **CHF rate (1.0750) is deliberately low** to favour B2B bookings (comment in same file).
- 💣 `CurrencyConverter.convert()` returns the raw `multiply(rate)` **without `setScale`** (`CurrencyConverter:39`). Callers must round. Most callers don't.

### Discount surface shape

- `DiscountRules.apply()` takes a single `BigDecimal percent`; no composite, no cap (`DiscountRules:112-117`). The "no stacking" rule is thus structural, not just conventional — even if you wanted to stack, the signature doesn't allow it.

---

## 2 · Booking lifecycle

### State machine (thin)

1. **States are free-text strings, not an enum** (`Booking.java:35-41`, TODO: HA-301).
2. Observed transitions: `PENDING → CONFIRMED` on successful payment; `PENDING → CANCELLED` on explicit payment failure (`BookingService:109-131`).
3. ⚠️ **There is no code path that refunds or reverses a CONFIRMED booking.** Cancellation-after-confirmation is a manual ops procedure.
4. ⚠️ **Saferpay's `SAFERPAY_PENDING` response is treated as success**, not as pending (`PaymentService:103`, `BookingService:119`). Only used for TUI/DER.

### Seat allocation

1. 💣 **Seats are decremented in memory then saved without a lock** (`BookingService:75, 89-91`). The comment literally reads "*race window: another thread can read the same seat count before we save*". Overbooking is one concurrent request away.
2. 💣 **Seats are decremented *before* payment completes** — a failed charge still leaves the count decremented if the revert path is skipped (there is no guaranteed revert path; see next rule).
3. 💣 **Payment exceptions are swallowed silently** (`BookingService:112-129`). Booking stays whatever state it's in; ops reconciles manually via Runbook RB-014.
4. `Thread.sleep(250)` before the Stripe charge call (`BookingService:115`) — "*gibt stripe-webhook zeit sich zu setzen. niemand weiss ob wahr.*" (2022). Removing it without understanding the webhook race is risky.

### Invariants

1. **Min passengers: 1** — `@Min(1)` on `BookingForm:28`.
2. ⚠️ **Max passengers: 9** — declared in `Constants:150` per Buchungs-AGB v3 but **never validated in code**. A request for 500 passengers will go through.
3. **Booking reference format: `HA-XXXXX`**, five chars from `[A-Z0-9]` excluding `I/O/L` (readability), via `SecureRandom` (`BookingService:162-167`).
4. **Customer dedupe is by email**: existing email → reuse `Customer`, otherwise create (`BookingService:79-87`). There is no merge flow for email changes; see `Customer.oldEmail` below.

### Confirmed-booking side effects (fire-and-forget, in this order)

`BookingService:136-149`:

1. Payment (Stripe / Saferpay / TEST_MODE)
2. SAP post (fire-and-forget — failure does **not** cancel booking; Finance reconciles via a conceptual Outbox that is logged but never read)
3. Sabre inventory push (since 2020 this is a mock; see §5)
4. Confirmation email (never fails a booking — fallback sender `noreply@denkair.de` per HA-318; `MailService:41-44`)

⚠️ Nothing in this chain can fail the booking. If SAP is down, Finance sees the revenue but not the booking line. If the mail bounces, no one retries.

---

## 3 · Payments

### Provider routing (by email domain)

`PaymentService:44-57`:

| Email pattern | Provider | Notes |
|---|---|---|
| `@tui.de`, `@tui.com`, `@dertouristik.de`, `@der.com` | **Saferpay** (legacy 2016–2019 API) | same partners as B2B discount tier |
| `@denkair.de`, `@example.de` | **TEST_MODE** | skips real charge, returns synthetic success |
| everything else | **Stripe** | |

- ⚠️ **Internal / test emails bypass the charge entirely.** A real booking with a `@denkair.de` customer will never hit a payment provider (`PaymentService:51-56`).
- ⚠️ **Saferpay terminal is hard-coded**: `17101097` (`Constants:71`, `PaymetmentService:105`). No per-country / per-BIN routing.

### Stripe specifics

- **Amount in cents**: `movePointRight(2)`, no explicit rounding done on the conversion (`PaymentService:71`).
- **3DS timeout: 28,000 ms** — magic number from a 2019 measurement ("*jemand hat gemessen dass 28s reicht*") (`PaymentService:35`).
- **Retry policy**: `MAX_RETRIES = 3`, `RETRY_BACKOFF_MS = 750`. Backoff is `attempt × 750ms` (i.e. 750 / 1500 / 2250); however the loop currently only logs the intended delay (`PaymetmentService:37, 88`).
- 💣 **Stripe charging is simulated, not real.** `PaymentService:66-95` logs intent and returns mock success — the actual HTTP call was never implemented (HA-1450 open since design). For workshop purposes, payment "succeeds" whenever the domain routing says so.

---

## 4 · Identity, access, and API exposure

### Who exists

`SecurityConfig:63-85` — 8 in-memory `{noop}` plaintext users, none of them rotated:

| User | Password | Role | Age |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | demo |
| `admin2` | `backup2019admin` | ADMIN | 2019 lockout fallback |
| `callcenter` | `denkair2014admin` | USER | 2014, unrotated |
| `sales` | `HanseSales!` | USER | pre-rename |
| `mueller` | `localdev42` | USER | personal dev login |
| `b2b-tui` | `Tui2019!Partner` | PARTNER, USER | paired with TUI discount |
| `b2b-der` | `Der2019!Partner` | PARTNER, USER | paired with DER discount |
| `test` / `test` | USER | | |

- 💣 **`app_user` table exists (V10, 2015) but `UserDetailsService` is not wired** (`SecurityConfig:57-59`, HA-1102). Removed in 2019 and never restored. Any customer registration writes to a table nobody reads at login.
- ⚠️ **`BCryptPasswordEncoder` is configured but unused**; the actual encoder is `PasswordEncoderFactories` delegating to `{noop}` because "bcrypt didn't work with the offline Callcenter tool" (2017 workaround, `SecurityConfig:89-96`).

### Three coexisting API auth schemes

| Endpoint | Mechanism | Effective check | Evidence |
|---|---|---|---|
| `/api/v2/flights` | `X-HA-Token` header vs. `Constants.MASTER_API_TOKEN` | hard-coded `"MASTER-HA-2016-a1b2c3d4e5f6"` | `ApiV2Controller:32-44` |
| `/api/flights` | `Authorization: Bearer <token>` | 💣 **fall-through: returns all flights even on wrong/missing token** | `ApiController:35-42` |
| `/api/**` (Spring) | `SecurityConfig` | ⚠️ `permitAll` | `SecurityConfig` |

- 💣 **`/api/flights` is effectively unauthenticated** (`ApiController:42`).
- 💣 **Master API token has never been rotated since 2016** (`Constants:118`).
- 💣 **Callcenter service token equals the internal service token**: `svc_internal_HA_7f9d3e2b1c4a` (`Constants:119`, HA-1401 open since 2018).

### Admin surface

- Spring-Security rule: `/admin/**` requires `hasRole("ADMIN")` (`SecurityConfig:42`).
- ⚠️ **`LegacyAuthFilter` runs *before* Spring Security** and its CIDR allowlist is feature-flagged out since 2019 — it now only checks "IP != null" (`LegacyAuthFilter:49-58`). The filter still sets `_legacyAuthSeen` as a request attribute because Sales Reporting reads it.
- 💣 **`/admin/flights/{id}/delete` is a `@GetMapping`**, CSRF is disabled → the link works as a bookmarklet and in any iframe (`FlightAdminController:28-31`, `SecurityConfig:29`).
- ⚠️ **No method-level `@PreAuthorize`** anywhere. Controller-level URL matching is the only barrier.

### PII exposure

- 💣 **`/api/customer/me` returns the raw `User` entity** — including `passwordHash` — as JSON, consumed by the internal CSR tool (`CustomerController:30-36`, `User:20`).
- 💣 **`/api/customer/{email}` has no auth** and queries the DB directly by email (`CustomerController:39-42`).
- 💣 **`/api/bookings/by-email/{email}` has no owner check**, returns all bookings for any email, built by string concatenation in `LegacyBookingDao.findByCustomerEmail` (`ApiController:45-49`, `LegacyBookingDao:33-41`).

---

## 5 · Flights, search, and partners

### Search

1. ⚠️ **Default departure date is "today + 14 days"** when no date is supplied (`FlightController:42-43`, Frau Schuster marketing requirement).
2. **Destinations are a closed set of 6 IATAs**: PMI, AYT, LPA, HER, HRG, FAO (`DestinationController:41-47`). Unknown slugs redirect to the overview page.
3. **Search window is a whole day in Europe/Berlin**: `00:00:00` → `23:59:59` using `plusDays(1).minusSeconds(1)` (`FlightService:27-30`).
4. **Results are always sorted by departure time ascending** — 2021 PM requirement (`FlightController:53`).
5. **Soft-delete: only `aktiv = true` flights ever surface** (`FlightRepository:17`).
6. ⚠️ **Empty / null partner responses are tolerated**, not treated as errors — "liza's workaround" from 2018.
7. 💣 **Search endpoint is a SQL-injection vector**: `origin` and `destination` are string-interpolated into the SELECT (`BookingController:58-68`, workshop parity with `LegacyBookingDao`).

### Flight domain quirks

1. **Price field is `preis` (German), not `price`** — do not rename, 2017 migration pins it (`Flight:43`).
2. **Active flag is `aktiv`** (German), not `active` / `isActive` (`Flight:51`).
3. `Customer.oldEmail` is an @Deprecated 2018 merge-migration column still carried in JPA (HA-622).
4. `Flight.isLowStock()` is referenced by `Constants.LOW_STOCK_THRESHOLD = 5` in a comment but the method does not exist (`Constants:149`).

### SAP (`de.denkair.fluginfo.*`)

1. ⚠️ **Package `de.denkair.fluginfo` cannot be renamed or moved** — SAP's RFC binding references it by fully-qualified class name (`SapConnector` header comment).
2. **`flugArt` is always `1`** (scheduled line service). Codes: 0=Charter, 1=Line, 2=Ad-hoc. Pinned since 2016 (`FlugInfoService:48`).
3. **SAP POST fields are locked**: `I_BUCHUNGSNR`, `I_FLUGNR`, `I_KUNDE_EMAIL`, `I_PREIS`, `I_PAX`. Finanzen requires the exact names. Header `X-SAP-Client: 100` is required (`SapConnector:42-87, 56`).
4. 💣 **SAP returns HTTP 200 even on failure if `<E_FEHLER>` is present and not self-closing** (`SapConnector:71-76`). Status code alone is a lie.
5. **SAP posting is fire-and-forget** — it never fails a booking (`BookingService:135-140`).

### Sabre (`SabreGdsClient`)

1. 💣 **`pushInventory()` has been stubbed since the 2020 pandemic freeze** — real HTTP is disabled, response is mocked. Sales still asks every 6 months (`SabreGdsClient:45-47`).
2. 💣 **`REQUEST_CACHE` is a `static HashMap` that grows unbounded** — documented in Grafana as a heap leak since 2021 (`SabreGdsClient:29, 54`).
3. **PCC (pseudo city code) = `HA72`** — DenkAir's Sabre identifier (`Constants:50`).

### Schedulers — "what do they actually do"

1. **`CacheWarmer`** — **05:30 Mon–Fri Europe/Berlin** (`CacheWarmer:36`). Pre-computes homepage carousel offers grouped by origin IATA. Inserts `Thread.sleep(50)` between iterations — 2018 throttle, never removed (`CacheWarmer:48-50`).
2. **`NightlyReports`** — **02:45 UTC (≈ 03:45 CET)** (`NightlyReports:34`). Fails silently.
   - Finance recipients: `stefan.wieland@denkair.de`, `finanzen@denkair.de`.
   - Ops recipients: `gerda.koenig@denkair.de`, `operations@denkair.de`.
   - CSV includes **only CONFIRMED bookings** for revenue; also reports PENDING and CANCELLED counts (`NightlyReports:28-29, 44-55`).
3. **`FtpManifestUploader`** — **03:00 Europe/Berlin daily** (`FtpManifestUploader:39`).
   - TUI target: `/inbox/denkair/`
   - DER target: `/incoming/ha/`
   - Local staging: `/var/denkair/manifests` (hard-coded, no config) (`FtpManifestUploader:30-34, 37`).
   - 💣 **CSV has a header only** — passenger list is a TODO ("*nachgeladen vom legacy BookingDao*"). Partners receive empty manifests.
4. 💣 **None of the schedulers use `@SchedulerLock`.** Running on two nodes = jobs fire twice (duplicate mails, duplicate FTP uploads, duplicate SAP posts).

### Dashboard & legacy SQL

1. ⚠️ **Dashboard revenue = sum of `total_preis` where `status = 'CONFIRMED'`** only — PENDING and CANCELLED are excluded (`LegacyBookingDao:89-92`). A spike in PENDING looks like flat revenue.
2. 💣 **`cancelAllByFlight()` updates all non-CANCELLED rows for a flight, without a transaction** — "*Keine Transaktion, niemand weiss mehr warum*" (`LegacyBookingDao:76-81`).
3. 💣 **Five DAO methods build SQL by concatenation** — `findByCustomerEmail`, `exportForCsv`, `countByStatus`, `cancelAllByFlight`, plus the timestamp-concat variant (`LegacyBookingDao:33-41, 67-78`). PreparedStatement migration is HA-914, open since 2020.

---

## 6 · JVM-global and temporal side effects

1. 💣 **`DateHelper.<clinit>` calls `TimeZone.setDefault(UTC)`** — affects the entire JVM, including libraries that honour the default TZ.
2. **`DateUtil`, `DateUtils`, and `DateHelper` coexist** with different bugs and different assumed timezones. Pick carefully; a 2019 copy-paste bug (`DateUtilsTest` accidentally targets `DateUtil`, HA-1775) means the tests lie about which one is covered.
3. **Official system timezone is `Europe/Berlin`** per `Constants.DEFAULT_TIMEZONE` (`Constants:154`, HA-2201) — but the scheduler expressions mix UTC and Europe/Berlin, see §5.
4. **VAT rate appears in two places** (`PreisCalculator:15`, `Constants:151`). Both must change together; there is no single source of truth.
5. ⚠️ **`Constants.LOW_STOCK_THRESHOLD = 5`** is referenced by a comment claiming sync with `Flight.isLowStock()` — the method does not exist (`Constants:149`).

---

## 7 · Secrets, credentials, and rotation history

`Constants.java` is the system's informal secrets store. The relevant history, as documented in code comments:

- 💣 **JWT signing secret**: `"denkair-jwt-signing-2018-please-change-me"` — never rotated (`Constants:117`, `SecurityConfig:19`).
- 💣 **Stripe keys**: rotated 2022-11 after Jenkins leak HA-2011; the **old keys are still in the file, commented out**, "waiting for webhook migration".
- 💣 **SAP credentials**: hard-coded since Vault was down 2021-04-03…07 (HA-1819). Never moved back.
- 💣 **FTP passwords** (TUI `HA_TUI_manifest_2017`, DER): partner contracts reference them, rotation requires coordinated partner deploys.
- ⚠️ **Vault integration (HA-101)** has been "on roadmap" since 2015.

Rotation is a cross-cutting concern: many secrets appear **twice** (once in `Constants.java`, once in `application-*.properties`) plus commented-out predecessors. A naive rotation touches one copy, leaves the others live. See `WORKSHOP_NOTES.md §1`.

---

## 8 · Rules that will surprise a new engineer

Summarized cross-cutting traps — the ones most likely to cause an incident if unnoticed:

1. **Emails drive money.** `@tui.de` unlocks an 8% discount *and* reroutes payment to Saferpay. `@denkair.de` bypasses payment entirely. A customer support agent who changes a booking email changes the price and the payment rails.
2. **No stacking, order matters.** A family of 5 TUI employees in Sommerloch pays 8% less, not 12% + 7%.
3. **Payment is simulated.** Don't rely on "the charge went through" in any workshop demo.
4. **Search, admin-delete, and customer-lookup endpoints are effectively public.** Treat every API as enumeration-exposed until proven otherwise.
5. **Manifests to partners are empty.** Nobody downstream has complained, which means nobody downstream is reading them.
6. **Schedulers duplicate on 2 nodes.** Local demos on one process look correct; two-node staging sends every mail twice.
7. **`aktiv = false` is the only delete.** Booking histories reference flights that "don't exist" in search — that's by design.
8. **German/English naming is load-bearing.** `preis`, `aktiv`, `fluginfo` are contract surfaces (SAP, DB, URL), not typos.
9. **Hardcoded dates expire silently.** Black Friday, Easter, Covid, exchange rates — each of these has a year baked in. None of them log a warning when they go stale.
10. **"CONFIRMED" is the only state that counts for revenue.** Anything odd in PENDING numbers is invisible on the dashboard.

---

## Sources

Compiled from parallel deep-reads of:
- `service/BookingService`, `service/PreisCalculator`, `service/DiscountRules`, `service/FlightService`, `service/CurrencyConverter`, `service/MailService`
- `legacy/PaymentService`, `legacy/LegacyBookingDao`, `legacy/Constants`, `legacy/SabreGdsClient`, `legacy/SapConnector`
- `controller/BookingController`, `controller/FlightController`, `controller/FlightControllerV2`, `controller/DestinationController`, `controller/CustomerController`, `controller/ApiController`, `controller/v2/ApiV2Controller`, `controller/admin/*`
- `config/SecurityConfig`, `filter/LegacyAuthFilter`
- `scheduled/CacheWarmer`, `scheduled/NightlyReports`, `scheduled/FtpManifestUploader`
- `domain/Booking`, `domain/Flight`, `domain/Customer`, `domain/User`
- `de.denkair.fluginfo.*`

Line references reflect the tree at commit `ec04e2d` (workshop secret-defang). Expect small drift on subsequent edits.

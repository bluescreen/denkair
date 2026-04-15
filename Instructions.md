# Instructor Playbook — HanseAir Brownfield Workshop

> **Not for attendees.** Move or withhold this file before handing the repo to participants. Every planted smell is documented below with `file:line` references.

Companion artefact to the PHP/Symfony demo (`/Users/mmuschol/dev/brownfield`). Same pedagogy, different stack — so attendees practice TAC-v2 primitives across ecosystems.

## Workshop Purpose

Teach tactical agentic coding on brownfield codebases via three lessons:

1. **First steps** — exploration, context-building, resisting the urge to type code
2. **Harness engineering** — CLAUDE.md, plans, tasks, memory, skills, hooks
3. **Avoiding slop** — how Claude confabulates on legacy stacks, and how to prevent it

The codebase is a fictional 2020-era Spring Boot 2.2 marketing + booking site for "HanseAir" (Condor-alike). Every smell is deliberate.

---

## Running the stack

Requires JDK 8 (or 11 with `-Djava.version=1.8` overrides disabled).

```
./mvnw spring-boot:run
```

- App: http://localhost:8080
- H2 console (exposed!): http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:hanseair;MODE=MySQL;DB_CLOSE_DELAY=-1`, user `sa`, no password
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator
- Admin login: `admin` / `admin123`
- Test customer: `kunde@example.de` / `kunde123`

**Note:** `logback-spring.xml` tries to write to `/var/log/hanseair/app.log`. On dev boxes this path doesn't exist → log appender errors on startup but the app still runs. This is intentional (§12 Verify-Before-Work teaching surface). Attendees who fix it do so by passing `-Dlogging.file.path=./logs`.

---

## Planted Smell Map

### Security

| # | Where | What |
|---|---|---|
| S1 | `src/main/resources/application.properties` + `application-prod.properties` | DB + mail credentials committed to repo |
| S2 | `src/main/java/de/hanseair/booking/config/SecurityConfig.java:23-25` | `http.csrf().disable()` globally with "AJAX search" comment |
| S3 | `application.properties:10-12` | `spring.h2.console.enabled=true` + `web-allow-others=true` |
| S4 | `application.properties:18` / `application-prod.properties:16` | `management.endpoints.web.exposure.include=*` in **both** profiles |
| S5 | `config/SwaggerConfig.java` + `pom.xml` springfox 2.9.2 | Swagger UI reachable on `/swagger-ui.html` in prod |
| S6 | `controller/BookingController.java:53-63` | `JdbcTemplate.queryForList` with string-concat SQL → classic SQLi on `/flights/api/search` |
| S7 | `controller/admin/FlightAdminController.java:22-27` | `@GetMapping("/{id}/delete")` — no CSRF, GET deletes |
| S8 | `controller/CustomerController.java:34-40` | `/api/customer/me` returns `User` entity directly → leaks `passwordHash` |
| S9 | `config/SecurityConfig.java:42-47` | In-memory admin + user with `{noop}` prefix (plain text passwords) |
| S10 | `config/WebConfig.java:15-18` | CORS `.allowedOrigins("*")` on `/**` |

### Code smells

| # | Where | What |
|---|---|---|
| C1 | all services & controllers | `@Autowired` on fields instead of constructor injection |
| C2 | `domain/Flight.java`, `Booking.java`, `Customer.java`, `User.java` | `@Data` on JPA entities → mutable `equals/hashCode` including `id` |
| C3 | `service/BookingService.java:38-77` | `createBooking` not `@Transactional`; reads+decrements `seatsAvailable` without locking → race condition |
| C4 | `util/DateUtil.java:14-16` | `public static final SimpleDateFormat DE_FORMAT` reused concurrently (not thread-safe) |
| C5 | `service/PreisCalculator.java` | DE+EN method names (`berechnePreis`, `getSteuer`, `applyDiscount`) on the same class — agent slop magnet |
| C6 | `controller/FlightController.java:28,47` | `@RequestMapping(value, method = RequestMethod.GET)` old-style, Spring 2.2 valid — agents "modernize" to `@GetMapping` and sometimes miss edge cases |
| C7 | `dto/FlightDto.java:16` | Missing `aircraftType` field with comment "HA-555 waiting on design" — copy-paste gap |
| C8 | `controller/HomeController.java:27-52` | Business logic + repo calls + formatting in the controller |
| C9 | TODO comments scattered | No ticket-refs in many (`// TODO externalize`); some reference fake `HA-###` tickets |
| C10 | `domain/Customer.java:24-31` | `@Deprecated private String oldEmail` still persisted as `old_email` column |
| C11 | `repository/BookingRepository.java:14-23` | Three methods doing the same thing (`findByCustomerOrderByCreatedAtDesc`, `findAllByCustomer`, `findByCustomer`) |

### Build / ops smells

| # | Where | What |
|---|---|---|
| B1 | `pom.xml:55-60` | `log4j:log4j:1.2.17` transitive leftover |
| B2 | `pom.xml:6-11` | `spring-boot-starter-parent:2.2.6.RELEASE` — EOL |
| B3 | `pom.xml:17` | `<jackson.version>2.10.2</jackson.version>` — has known CVEs |
| B4 | `pom.xml:16` | `<java.version>1.8</java.version>` — EOL for public updates |
| B5 | `application.properties:16` + `spring.jpa.hibernate.ddl-auto=update` + `schema.sql` | Both schema management approaches active simultaneously |
| B6 | `logback-spring.xml:14-23` | Logs to `/var/log/hanseair/app.log` — breaks on every non-Linux dev machine |
| B7 | `pom.xml:92-98` | JUnit 4.12 explicit dep alongside Boot's JUnit 5 — tests split between styles |
| B8 | `README.md` + `HELP.md` | Spring Initializr boilerplate, never edited (README.md IS the smell) |
| B9 | `.mvn/wrapper/MavenWrapperDownloader.java` | `.java` file in `.mvn/wrapper/` — agent often assumes it belongs under `src/` |
| B10 | `bootstrap.properties` | References `config.hanseair.internal:8888` that doesn't exist |
| B11 | `application-prod.properties` committed | Production creds live in git |

### UX / compliance smells

| # | Where | What |
|---|---|---|
| U1 | `controller/FlightController.java:33` | `LocalDate.parse(date)` — no validation, allows past dates |
| U2 | `dto/BookingForm.java:18-20` | Email field is `@NotBlank` only, no `@Email`; comment blames "iOS autofill" |
| U3 | `templates/flights/results.html`, `index.html` | `<img>` without `alt` attributes |
| U4 | repo-wide templates | No cookie banner — 2020 GDPR reality check |
| U5 | `templates/fragments/header.html` | Inline GA (`UA-99887766-1`), no consent |
| U6 | `templates/fragments/footer.html` + `header.html` | CDN scripts/styles **without** `integrity=` (no SRI) |

---

## Smell → TAC-v2 primitive mapping

See `~/dev/nimm2/docs/TAC-v2.pdf` for the canonical reference.

| TAC-v2 primitive | Exercised via |
|---|---|
| §2 Closed-Loop Prompts | Exercise 4 (booking 500) → C4 race + thread-safety |
| §3 PITER / §4 Spec Prompts | Exercise 3 (meal field) — 8 files |
| §6 Hooks | Exercise 7 — PreToolUse hook blocking `application-prod.properties` (S1, B11) |
| §7 Three-Agent Harness | Exercise 8 security sweep — Generator vs Evaluator on S1–S10 |
| §8 Sub-Agents / §13 Progressive Disclosure | Exercise 1 — ~50 Java files; naive full-file reads blow context |
| §9 Doom-Loop Detection | "Make flight search case-insensitive" → agent loops between JdbcTemplate (S6) and JPA repo paths |
| §12 Verify-Before-Work | B6 (log path), B5 (schema conflict), C4 (race) — all need real-run verification |
| §15 Feedforward/Feedback | No CLAUDE.md shipped, minimal tests (BookingControllerIT disabled), no ArchUnit |
| §17 Regulation Categories | Maintainability (C1–C11), Architecture fitness (S4 actuator, S10 CORS, B6 log path), Behaviour (C3 race, U1 past-date) |
| §19 Context Entropy | CDN-loaded JS/CSS, no node_modules, no binary assets — deliberate context hygiene |

---

## Exercises (with expected failure modes)

### 1. "Land cold, figure out what this is."
**Prompt:** "Open this repo in Claude Code. In 15 minutes, explain what it does, how it's built, and what shape the code is in."

**Common failures:** Attendee lets Claude read every file top-down; Claude hallucinates Spring Boot 3 + Jakarta EE patterns from the directory shape alone.

**Steering moves:** Use an Explore sub-agent scoped to the `controller/` package first, then `service/`, then `domain/`. Observe how returning `filepath:line` refs keeps main context lean (§8).

### 2. "Write the CLAUDE.md you wish existed."
**Prompt:** "Now write a `CLAUDE.md` at repo root that future-you would want."

**What good looks like:** Calls out Spring Boot 2.2 (NOT 3), Java 8 target (javax.persistence, not jakarta), DE/EN method naming on `PreisCalculator`, `schema.sql` + `ddl-auto=update` both active, `/var/log/hanseair/` logback path, `application-prod.properties` is committed on purpose, `@RequestMapping(method=...)` is fine and doesn't need modernization.

**Common failures:** CLAUDE.md says "uses Spring Boot 3" because the agent reached for that default; recommends migrating `WebSecurityConfigurerAdapter` without acknowledging that works fine in 2.2.

### 3. "Add a `meal` field to Booking (vegetarian / standard / vegan)."
**Trap:** Touches **8 files**:
1. `domain/Booking.java` — new column
2. `schema.sql` — add column (both paths active, C5)
3. `dto/BookingForm.java` — new field
4. `service/BookingService.java` — persist
5. `templates/booking/form.html` — radio buttons
6. `templates/booking/confirmation.html` — display
7. `templates/admin/dashboard.html` — show in recent-bookings table (optional)
8. `data.sql` — backfill existing seed (optional)

**Side-by-side demo:** no-plan agent vs Spec Prompt agent. First usually misses `schema.sql` (because `ddl-auto=update` masks it) and the admin table.

### 4. "The booking confirmation page 500s for some customers."
**Prompt:** Vague bug report, no stack trace.

**Real root cause:** `DateUtil.DE_FORMAT` (C4) — static `SimpleDateFormat` used concurrently by the admin dashboard + confirmation page + booking form. Manifests as `NumberFormatException` at random. Agent that doesn't define "how do I verify this is fixed?" (§2 closed-loop prompts) will shotgun-fix wrong places.

### 5. "Upgrade Spring Boot to the latest version."
**Prompt:** "Bump to the latest Spring Boot."

**Expected agent behavior:** Blindly bumps parent to 3.x, removes `WebSecurityConfigurerAdapter` (deprecated in 5.7, removed in 6), changes `javax.persistence` → `jakarta.persistence`, says "done." Does not notice that Spring Boot 3 requires Java 17 and this pom pins 1.8. Does not run `./mvnw compile`.

**Steering moves:** Teach §12 — add a PostToolUse hook that runs `./mvnw -q -DskipTests compile` after any edit under `src/`. Suddenly the agent sees its own breakage.

### 6. "Build a skill for 'add new destination airport'."
**What good looks like:** Skill edits `data.sql` with picsum seed, adds option to the homepage + results-page destination selects (already templated), updates admin nav if needed. Matches existing DE/EN conventions (airport city in DE, country code uppercase).

### 7. "Add a PreToolUse hook that blocks edits to `application-prod.properties`."
**Teaching point:** §6 deterministic control. Once configured, even if attendees explicitly ask "rotate the prod DB password," Claude physically cannot touch the file until the hook is lifted.

### 8. "Find all the planted security smells."
**Generator alone:** usually catches S6 (SQLi), S2 (CSRF off), S1 (committed creds). Misses S3 (h2-console), S4 (actuator *), S5 (swagger), S8 (password leak), S10 (CORS *), B1 (log4j 1.x).

**Three-agent harness (§7):** Evaluator (e.g. `security-review` skill) catches the whole set. Compare findings lists side-by-side — makes the Builder/Validator split tangible.

---

## Instructor tips

- **When an agent "modernizes" `WebSecurityConfigurerAdapter`:** let it finish, then run the app. Seeing the security config fail to wire up is the lesson. Don't pre-empt it.
- **When an agent flips `allowedOrigins("*")` to `allowedOrigins("https://hanseair.de")`:** ask "how did you verify that's the right origin?" They can't. Good opening for exercise 2 (CLAUDE.md with the prod origin documented).
- **H2 console behavior:** sometimes hangs on new browsers. Use `/h2-console` incognito.
- **Log path:** if attendees get distracted by the log error, the lesson is §12 Verify-Before-Work — fix the environment before iterating. Don't rescue them; let them diagnose.

## Reset between cohorts

```
./mvnw clean
rm -rf logs/
git clean -xfd
git checkout .
```

## Tech-Debt Ramp-Up (v2 — 10 Jahre Sediment)

Additional smell layers added after the initial build. These simulate "5+ devs over 10 years";
nothing here is real, but every name + pattern is deliberately picked to be
grep-able by workshop attendees.

### Inline-Secrets (deliberately viral — 40+ credentials)

| File | What |
|---|---|
| `src/main/java/de/hanseair/booking/legacy/Constants.java` | **Central secrets dump.** DB creds, SAP prod password, Sabre+Amadeus API keys, Stripe live+public+webhook keys (plus commented-out rotated keys), Saferpay/Paymetric creds, TUI/DER FTP passwords, AWS access key, Redis/Elasticsearch/Kafka creds, JWT signing secret, API master token, TOTP seed, SMTP+Mailchimp+SendGrid keys, Salesforce creds, SSH passphrase, Covid-era admin passwords, CIDR admin allowlist. ~40 hardcoded secrets. |
| `src/main/resources/application-staging.properties` | staging creds committed |
| `src/main/resources/application-dev-mueller.properties` | personal dev override with mailtrap token |
| `src/main/resources/META-INF/persistence.xml` | JDBC url + password embedded |
| `src/main/resources/applicationContext-legacy.xml` | refs Saferpay/payment beans |
| `src/main/webapp/WEB-INF/jsp/legacy/old-booking.jsp` | hardcoded `condor2014admin` password check in JSP |
| `src/main/java/de/hanseair/booking/config/SecurityConfig.java` | expanded to **8** in-memory users including `sales/HanseSales!`, `mueller/localdev42`, `admin2/backup2019admin`; exposes `JWT_SECRET` as public static |
| `docs/PARTNER_INTEGRATION.md` | sample curl with real master token |
| `docs/RUNBOOK.md` | mentions backup DB password inline |

### Legacy Integrations (pretend-external systems)

| File | What |
|---|---|
| `legacy/SapConnector.java` | hand-written SOAP over `HttpURLConnection`; Basic auth from Constants; swallows exceptions |
| `legacy/SabreGdsClient.java` | unbounded static `REQUEST_CACHE` (documented memory leak); GDS push stubbed since 2020 |
| `legacy/FtpManifestUploader.java` | `@Scheduled(cron="0 0 3 * * *")` nightly FTP to TUI/DER; creds inline; `sun.net.ftp` stubbed |
| `legacy/PaymentService.java` | Stripe/Saferpay/Paymetric 3-way router; secrets hardcoded; retry loop with `Thread.sleep`; stubbed HTTP |
| `legacy/LegacyBookingDao.java` | raw `JdbcTemplate` with string-concat SQL — multiple SQLi vectors; non-transactional batch update |
| `legacy/MailHelper.java` | pre-Spring JavaMail singleton; plaintext SMTP; no TLS |

### Legacy / Condor-era package (pre-rename)

| File | What |
|---|---|
| `de.condor.fluginfo.FlugInfoBean` | Java 7 style, `Date` fields, `@Deprecated`; referenced by SAP connector |
| `de.condor.fluginfo.FlugInfoService` | hand-rolled singleton (`getInstance`); called via reflection per JSP |

### Abandoned rewrites

| File | What |
|---|---|
| `controller/FlightControllerV2.java` | 3 dated TODO comments (Markus 2018, jens 2019, mueller 2020); entire logic commented out; one ping endpoint kept "just so something responds" |
| `controller/v2/ApiV2Controller.java` | partial API v2; token check against master-token constant |
| `controller/ApiController.java` | third attempt; token-check falls through to public regardless |
| `util/DateUtils.java`, `util/DateHelper.java` | duplicate utils; DateHelper sets `TimeZone.setDefault(UTC)` globally in a static block |
| `util/StringUtil.java` | inconsistent helpers, `istLeer` (DE) alongside `isBlank` (EN) |

### Scheduled jobs + business crustiness

| File | What |
|---|---|
| `scheduled/CacheWarmer.java` | `@Scheduled` 05:30 MON-FRI; `Thread.sleep(50)` between records "because of a 2014 index that's been there since 2018"; static unbounded `OFFER_CACHE` |
| `scheduled/NightlyReports.java` | 02:45 nightly mail; hardcoded recipient lists; uses `LegacyBookingDao.exportForCsv` (SQLi) + `MailHelper` |
| `service/CurrencyConverter.java` | 9 hardcoded FX rates last updated 2021; "CHF deliberately low, it's business" |
| `service/DiscountRules.java` | 100+ line if-else with stacking rules; **hardcoded Easter dates 2017–2024** (no 2025+); Covid rebate expired 2020-12-31 but never disabled |
| `service/FeatureFlags.java` | public static final flags; some @Deprecated but still read; `ACTUATOR_SECURITY_FIX = false` |

### Build / XML fossils

| File | What |
|---|---|
| `pom.xml` | **24 additional dependencies**: log4j 1.2.17, commons-lang 2.6 + lang3, commons-{io,codec,collections,beanutils,fileupload,net,dbcp}, Guava 18, Joda-Time, commons-httpclient 3.1 + httpclient 4.5.6, EHCache 2.10.6, Jackrabbit 2.18, Velocity 1.7, FreeMarker 2.3.28, Quartz 2.3.1, JavaMail, jsch, org.json, AWS SDK v1, Kafka-clients 2.3.0, HSQLDB 2.3.1 (test), POI 3.17. Many transitive CVE surfaces. |
| `applicationContext-legacy.xml` | Spring XML config beside Java config; registers `saferpayClient` + `flugInfoService` beans |
| `META-INF/persistence.xml` | legacy JPA XML with plaintext password; Spring Boot ignores it but build pipeline keeps it |
| `webapp/WEB-INF/web.xml` | pre-Boot servlet descriptor, pretends to be needed |
| `webapp/WEB-INF/jsp/legacy/old-booking.jsp` | Condor-era JSP still "used by Callcenter" |
| `resources/soap/FlightInfo.wsdl` | SOAP WSDL shipped for a TUI terminal |
| `resources/templates/email/booking-confirmation.vm` | Velocity template next to Thymeleaf |
| `resources/templates/legacy/admin-booking.ftl` | FreeMarker template — third engine |
| `resources/db/migration/V1..V247__*.sql` | **33 Flyway-style migrations** with dated commits (jens/mueller/akin/liza/nhan/tom), intentional divergences from prod, an ATTEMPT that got rolled back, a 9-year-old `flug_id` column still lingering |

### Modifications to originally-clean files

| File | Layered rot |
|---|---|
| `service/BookingService.java` | 8 dependencies injected (payment, SAP, Sabre, discount, ...); `System.out.println` for legacy grep; `Thread.sleep(250)` "for Stripe webhook"; multiple swallowed exceptions with `e.printStackTrace()`; transactionality still missing; multiplied side-effects (SAP + Sabre + Mail) each in their own try/catch |
| `config/SecurityConfig.java` | 8 in-memory users; `public static final` JWT secret exposed at class-level |
| `filter/LegacyAuthFilter.java` | pre-Spring-Security filter; comment says "real CIDR check disabled 2019, only checks IP non-null"; sets attribute that "sales reporting depends on" |

### Accumulated docs (stale)

| File | What |
|---|---|
| `README.md` | Spring Initializr boilerplate, never edited (smell B8 from v1) |
| `HELP.md` | Initializr autogen, untouched |
| `ARCHITECTURE.md` | "Last updated 2016-09-14", with 2021 + 2023 "not-current-anymore" notes |
| `CHANGELOG.md` | sparse, gaps of years, references inline-commented incidents |
| `TODO.md` | 30+ dated tickets, "open for 9 years" entries, "Nicht machen (aus Gruenden)" section with 5 deliberately-won't-do items |
| `docs/PARTNER_INTEGRATION.md` | sample curl with hardcoded master token |
| `docs/RUNBOOK.md` | operational recipes that explicitly reference `DateUtil.DE_FORMAT` race and thread-unsafety as the expected 500 cause |

### Additional TAC-v2 teaching surfaces

- **§8 Sub-Agents + §19 Context Entropy:** 90+ files, 33 SQL migrations, 4 markdown docs. Full-file reads will blow context by the 5th file.
- **§12 Verify-Before-Work:** pretty much every component has "stubbed" behaviour, `Thread.sleep`, or a missing runtime dependency (FTP uploader on Java 17, EHCache inert, Kafka not initialized). "It compiles" is not the same as "it works".
- **§17 Regulation Categories (Behaviour):** `BookingService.createBooking` now silently: runs non-transactional multi-entity writes, swallows payment exceptions, writes to stdout, sleeps for 250ms per call, calls 3 external systems each with its own try/catch fallback.
- **§9 Doom-Loop Detection:** "rotate the JWT secret" now fans out across `Constants.JWT_SIGNING_SECRET`, `SecurityConfig.JWT_SECRET`, `docs/PARTNER_INTEGRATION.md`, `ARCHITECTURE.md` — the naive agent will rotate in one place and declare victory.

### Exercises unlocked by the v2 layer

9. **"Find all hardcoded secrets."** Target: ≥40. Most agents stop at 10–15 unless a Security-Review skill + Evaluator split is used. Teaches §7 three-agent harness on a higher signal surface.
10. **"Rotate the Stripe keys."** Traps: old key in code-comments (for "webhook rollback"), key in 3 markdown docs, in SecurityConfig public field, in application-prod.properties. Teaches §3 Spec-Prompts (touch list first).
11. **"Why do some bookings stay PENDING?"** Bug report from RB-007. Real root cause: `BookingService.createBooking` silently cancels on payment exception, but SAP still gets posted — data divergence. Teaches §2 closed-loop prompts with multi-step validation.
12. **"Delete dead code."** Trick question: `de.condor.fluginfo`, `LegacyAuthFilter`, `paymetric_meta`, `FlugInfoBean` all look dead but are load-bearing. TODO.md's "Nicht machen" section is the key. Teaches §15 feedforward — that CLAUDE.md must capture "don't touch" conventions.

## Credentials reference (all intentional, all fake)

- DB dev (H2): `sa` / (empty)
- DB prod (MySQL): `hanseair_app` / `Hanse2019!`
- Mail SMTP: `no-reply@hanseair.de` / `Mail2019!` (dev) / `Hanse2019Mail!` (prod)
- Admin user: `admin` / `admin123` (in-memory, `{noop}`)
- Test customer: `kunde@example.de` / `kunde123` (in-memory, `{noop}`)
- Fake GA: `UA-99887766-1`

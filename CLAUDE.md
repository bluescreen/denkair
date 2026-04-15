# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

**DenkAir Booking** — a deliberately brownfield Spring Boot app used as a workshop fixture. The pain (secrets in git, broken tests, dead rewrites, raw SQL, multi-source auth, EOL deps) is **intentional teaching material**. See `WORKSHOP_NOTES.md` for the curated list of smells and `FIRST_STEPS.md` for the attendee flow. Treat both as authoritative context.

## Stack facts (don't guess these)

- **Spring Boot 2.2.6** (NOT 3.x). Java **1.8** target. `javax.persistence`, not `jakarta.*`.
- Maven (`pom.xml`, `mvnw` wrapper). H2 in-memory locally.
- Package root: `de.denkair.booking`. Legacy package `de.denkair.fluginfo` looks dead but **do not delete** — the SAP RFC connector references classes by name via reflection.
- Rename history: Condor → HanseAir → DenkAir. Stray `condor*` / `hanse*` identifiers are historical, not typos.
- DE/EN naming split is deliberate: `getPreis()` next to `getName()`, `berechnePreis` + `applyDiscount` on the same class. Match the local convention of the file you're editing.

## Commands

All day-to-day commands are in `Makefile`. Prefer them over raw `mvn`.

- `make start` — run in background, writes PID to `.app.pid`, waits for `http://localhost:8080/`
- `make stop` / `make restart` / `make status` / `make logs`
- `make run` — foreground run (Ctrl+C to stop)
- `make probe` — curl the canonical public routes (smoke test; use this as the real signal of "it works")
- `make tests` — full suite + brownfield report (disabled tests, TODO/FIXME, skipped)
- `make test` — plain `mvn test`
- Single test: `./mvnw test -Dtest=ClassName` or `-Dtest=ClassName#method`
- `make package` / `make build` / `make clean`
- `make scan-secrets` / `make scan-secrets-verify` / `make scan-history` — trufflehog (install via brew or docker, see Makefile header)

Overrides: `PROFILE=prod make run`, `PORT=9090 make run`.

## Verify-before-work: `make tests` is a lie

BUILD SUCCESS does **not** mean the change is safe. The suite has class-level `@Disabled`, `assertTrue(true)` placeholders, three JUnit generations (3/4/5) in one tree, and no coverage gate. Before changing behavior:

1. Find (or write) a test that fails **without** your fix.
2. Only then change the code.
3. Smoke with `make start && make probe`, not just `make tests`.

## Logging quirk

`logback-spring.xml` hard-codes `/var/log/denkair/`. The Makefile passes `-Ddenkair.log.dir=./logs` as a workaround — any invocation that bypasses the Makefile (bare `mvn spring-boot:run`) will crash on non-Linux machines. Keep the override.

## Auth landscape (three sources, all active)

- `config/SecurityConfig` — 8 in-memory `{noop}` plaintext users (demo: `admin/admin123`).
- `filter/LegacyAuthFilter` — pre-Spring-Security filter still in the chain; Sales reporting reads attributes it sets. Do not remove casually.
- `controller/v2/ApiV2Controller` validates a master token constant; `controller/ApiController` silently falls back to `permitAll`.

Production target is a DB-backed `UserDetailsService` (HA-1102) but not wired yet.

## Don't touch without discussion

- `legacy/Constants.java` — secret inventory pending vault migration (HA-101). Do not silently rotate or reshape; coordinate first.
- `application-*.properties` (especially `-prod`, `-staging`) — committed by design for the workshop; treat any edit as a review-worthy event.
- `filter/LegacyAuthFilter` — Sales reporting depends on a side-effect attribute.
- `paymetric_meta` table and related refs — Finance has vetoed removal.
- `src/main/resources/db/migration/V*.sql` — treat as immutable history even though Flyway is **not** on the classpath and these files don't execute. New changes go in a new `V{N+1}__*.sql`.
- `controller/FlightControllerV2` and `controller/v2/*` — abandoned rewrites. Don't "modernize" them; use `FlightController`.

## Schema reality

Three parallel sources of truth, all active simultaneously:

- `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
- `schema.sql` + `data.sql` via `spring.sql.init.mode=always` (runs every start)
- `db/migration/V*.sql` — Flyway-style filenames but Flyway is not a dependency
- `META-INF/persistence.xml` declares a different DB URL again

When asked to "add a field", ask which source(s) to touch. Default dev behavior is "H2 + ddl-auto rewrites the world for you" — that hides prod divergence.

## Known hotspots (fix carefully, test first)

- **SQL injection**: `controller/BookingController` `searchNative` (line ~53), `legacy/LegacyBookingDao` (five string-concat methods incl. `findByCustomerEmail`, `exportForCsv`, `countByStatus`, `cancelAllByFlight`).
- **Race / overbooking**: `service/BookingService.createBooking` — synchronous SAP + Sabre + Mail, swallowed try/catch, no lock on seat count.
- **JVM-global side effects**: `util/DateHelper.<clinit>` calls `TimeZone.setDefault(UTC)`. Three date utils (`DateUtil`, `DateUtils`, `DateHelper`) with different bugs and timezones.
- **Scheduler duplication**: `@Scheduled` in `CacheWarmer`, `NightlyReports`, `FtpManifestUploader` — no ShedLock, fires per node. Run only one instance locally.
- **Unbounded `static` caches**: `CacheWarmer.OFFER_CACHE`, `SabreGdsClient.REQUEST_CACHE` (HA-2301 heap leak).
- **PII leak**: `CustomerController.me()` returns the `User` entity directly — `passwordHash` lands in JSON.

## Workshop docs to read in order

`TODO.md` → `ARCHITECTURE.md` (stale since 2016, note the drift) → `CHANGELOG.md` → `docs/RUNBOOK.md` → `WORKSHOP_NOTES.md`. `README.md` and `HELP.md` are Spring Initializr boilerplate — skip.

# DenkAir Booking

DenkAir online booking portal — Spring Boot brownfield codebase used as a workshop playground for refactoring, debugging, and AI-assisted engineering.

A decade of drift lives here on purpose: mixed view engines (Thymeleaf + FreeMarker + Velocity + JSP), three date utilities, duplicate Apache Commons versions, dead legacy integrations (Sabre, SAP, FTP), and Spring Boot 2.2.6 on Java 8 source running on a JDK 17 image. Don't use it as a pattern — use it as a patient.

## Stack

- Spring Boot 2.2.6 · Java 8 source · JDK 17 runtime
- Spring MVC + Thymeleaf · Spring Data JPA / Hibernate
- H2 (dev, in-memory, MySQL mode) · MySQL connector (prod)
- Spring Security · Actuator · Springfox Swagger 2.9
- Docker + Railway deployment (see `railway.toml`, `Dockerfile`)

Entry point: `de.denkair.booking.BookingApplication`.

## Quick start

```bash
make run          # foreground (Ctrl+C)
make start        # background (PID in .app.pid)
make stop / restart / status / logs
make probe        # curl every public route
make test         # unit + coverage (JaCoCo)
make tests        # brownfield report with @Disabled / TODO breadcrumbs
make e2e          # Gherkin/Cucumber end-to-end scenarios (real HTTP)
```

Open <http://localhost:8080> · H2 console at `/h2-console` · Swagger at `/swagger-ui.html` · Actuator at `/actuator`.

Docker:

```bash
docker compose up --build
```

## Docs

All living documentation is under [`docs/`](docs/):

| Document | What it tells you |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | System overview, package layout, HTTP surface, Mermaid diagrams, brownfield smells |
| [`docs/booking-flow.excalidraw`](docs/booking-flow.excalidraw) + [`.png`](docs/booking-flow.png) | Booking request flow (browser → controller → service → legacy branch) |
| [`docs/DESIGN_SYSTEM.md`](docs/DESIGN_SYSTEM.md) | Tokens, radii, signature patterns |
| [`docs/BUSINESS_RULES.md`](docs/BUSINESS_RULES.md) | Discount rules, pricing, partner logic extracted from code |
| [`docs/RUNBOOK.md`](docs/RUNBOOK.md) | Operational runbook (payments, SAP, mail, incident trails) |
| [`docs/PARTNER_INTEGRATION.md`](docs/PARTNER_INTEGRATION.md) | Partner (TUI/DER) integration notes |
| [`docs/TEST_COVERAGE.md`](docs/TEST_COVERAGE.md) | Coverage baseline → 94.6% line / 80.2% branch, exclusions, surfaced bugs |
| [`docs/E2E_TESTS.md`](docs/E2E_TESTS.md) | Cucumber/Gherkin E2E suite — features, steps vocabulary, how to add scenarios |
| [`FIRST_STEPS.md`](FIRST_STEPS.md) | 30-second orientation for workshop attendees |
| [`WORKSHOP_NOTES.md`](WORKSHOP_NOTES.md) | Instructor playbook |

Project-level guidance for AI assistants lives in [`CLAUDE.md`](CLAUDE.md).

## Testing

JaCoCo-backed test suite: **163 tests**, 2 `@Disabled` (both documented), 0 failing.

- **Line: 94.6% · Branch: 80.2% · Method: 97.4% · Class: 100%**
- Report: `target/site/jacoco/index.html` after `mvn test`
- Exclusions: `legacy/` integrations (Sabre/SAP/FTP/MailHelper/LegacyBookingDao), `scheduled/`, `filter/`, `config/`, `BookingApplication`, `fluginfo/` — see `docs/TEST_COVERAGE.md` for rationale
- Test slices: Mockito units for services, `@WebMvcTest` for every controller, `@DataJpaTest` for repositories, JSR-303 validation for DTOs
- **E2E:** 10 Gherkin/Cucumber scenarios via `make e2e` — full Spring context on random port, real HTTP. See `docs/E2E_TESTS.md`.

## Security

- `make scan-secrets` — trufflehog filesystem (offline)
- `make scan-secrets-verify` — live-verification
- `make scan-history` — full git history
- Demo secrets in `legacy/Constants.java` are intentionally defanged (`sk_DEMO_*`, `AKIA-DEMO-*`) for workshop use. See commit `ec04e2d`.

## Deployment

1. `mvn package` → `target/booking-0.0.1-SNAPSHOT.jar`
2. Multi-stage Dockerfile (Maven 3.9 → Temurin 17 JRE, non-root uid 1001), `JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"`, port `${PORT:-8080}`
3. Railway uses the same Dockerfile; healthcheck `/actuator/health`, max 3 restarts on failure

## References (Spring Boot 2.2.6)

- [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)
- [Actuator](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#production-ready)
- [Spring Web](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)
- [Thymeleaf](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-template-engines)
- [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-jpa-and-spring-data)
- [Spring Security](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-security)

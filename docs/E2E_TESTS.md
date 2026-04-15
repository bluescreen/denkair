# End-to-End Tests (Gherkin / Cucumber)

Gherkin-style black-box tests that boot the full Spring application on a random port and drive it over **real HTTP** (TestRestTemplate), a **real headless Chrome browser** (Selenium), and a **real SMTP server** (GreenMail) — so every layer of the booking flow is exercised against real protocols. Complements the unit + slice tests in `docs/TEST_COVERAGE.md`.

## Stack

- **Cucumber 6.11** (`cucumber-java`, `cucumber-spring`, `cucumber-junit`)
- **Spring Boot Test** with `webEnvironment = RANDOM_PORT` — real Tomcat, real filters, real Security
- **Seeded H2** — `data.sql` runs on boot, so scenarios pick real flights by id
- **maven-failsafe-plugin** runs classes named `*IT.java`; surefire excludes them so `make test` stays unit-only
- **Selenium 4.15 + headless Chrome** for `@browser`-tagged scenarios. Chromedriver is auto-resolved from `~/.cache/selenium/chromedriver/...` (Selenium Manager downloads it on first run); Chrome.app is found at the standard macOS path.
- **GreenMail 1.6** in-process SMTP on `127.0.0.1:3025`. `spring.mail.host/port` are overridden per test run so `MailService.sendBookingConfirmation` delivers to GreenMail instead of `smtp.denkair.de`. Scenarios can then assert on recipient, sender, subject, and body of the captured `MimeMessage`.

## Layout

```
src/test/
├── java/de/denkair/booking/e2e/
│   ├── E2EIT.java                 # Cucumber runner (@RunWith(Cucumber.class))
│   ├── CucumberSpringConfig.java  # @CucumberContextConfiguration + @SpringBootTest + GreenMail @Bean
│   ├── HttpSteps.java             # TestRestTemplate steps
│   ├── BrowserSteps.java          # Selenium/ChromeDriver steps (@browser scenarios)
│   └── MailSteps.java             # GreenMail inbox assertions
└── resources/features/
    ├── home_and_search.feature    # public browsing, actuator, swagger (HTTP)
    ├── booking.feature            # booking + API v2 auth (HTTP)
    ├── browser.feature            # real-browser user journey (@browser tag)
    └── mail.feature               # booking-confirmation email contents
```

## Run it

```bash
make e2e                                              # all 15 scenarios
mvn test-compile failsafe:integration-test -Dit.test=E2EIT

# HTTP + mail only (skip Selenium browser scenarios)
E2E_SKIP_BROWSER=1 make e2e
```

Report: `target/cucumber-report.html` · Chromedriver log: `target/chromedriver.log`

## Scenarios (15 total)

**Public browsing — HTTP** (`home_and_search.feature`)
- Home page renders HTML with "DenkAir"
- Swagger UI is exposed
- Actuator health reachable (accepts 200 or 503 — mail healthcheck fails if SMTP is unreachable)
- Native flight search `/flights/api/search?origin=HAM&destination=PMI` returns JSON
- `/ziele/palma-mallorca` renders
- Unknown destination slug redirects to `/ziele`

**Booking + API — HTTP** (`booking.feature`)
- Internal `@example.de` customer books a seeded flight → 302 to `/booking/HA-…`
- Booking form shows the flight (`Passagiere`)
- `/api/v2/flights` without token → 401
- `/api/v2/flights` with `X-HA-Token: MASTER-HA-2016-…` → 200 + `data`

**Real browser — Selenium** (`browser.feature`, tagged `@browser`)
- Home page renders in Chrome, title contains "DenkAir"
- Clicking a destination tile from `/ziele` navigates to `/ziele/palma-mallorca`
- Completing the Thymeleaf booking form redirects to `/booking/HA-…`

**Booking-confirmation email — GreenMail** (`mail.feature`)
- After POSTing a booking, GreenMail captures exactly 1 message addressed to the customer, `From: no-reply@denkair.de`, subject containing "Buchungsbestätigung", body containing the `HA-` reference code and `Passagiere: 2`
- Body contains the structural template (`Flug: HA4…`, `Von:`, `Nach:`, `Gesamtpreis:`, `Vielen Dank`) independent of the specific seeded flight

## Writing new scenarios

1. Add or extend a `.feature` file under `src/test/resources/features/`. Prefix with `@browser` if it needs a real browser; otherwise it runs over HTTP and can still read mail via GreenMail.
2. Reuse steps from `HttpSteps.java`, `BrowserSteps.java`, or `MailSteps.java`, or add new ones in the same package.

**HTTP step vocabulary** (`HttpSteps.java`):
- `Given I pick a seeded active flight`
- `When I GET "<path>"` / `When I GET "<path>" with header "<name>" = "<value>"`
- `When I GET without following redirects "<path>"`
- `When I GET the booking form for that flight`
- `When I POST a booking form with` + a `| key | value |` data table
- `Then the response status is <code>` / `is one of <a>, <b>`
- `Then the response content-type contains "<token>"`
- `Then the body contains "<substring>"`
- `Then the location header contains|ends with "<substring>"`

**Browser step vocabulary** (`BrowserSteps.java`):
- `Given I open "<path>" in a real browser`
- `When I click the first link whose href contains "<substring>"`
- `When I fill in "<name>" with "<value>"`
- `When I submit the booking form`
- `Then the page title contains "<substring>"`
- `Then the page body contains "<substring>"`
- `Then the page URL contains "<substring>"`

**Mail step vocabulary** (`MailSteps.java`):
- `Then the inbox for "<address>" has <N> message(s)`
- `Then the latest mail to "<address>" has subject containing "<substring>"`
- `Then the latest mail to "<address>" body contains "<substring>"`
- `Then the latest mail to "<address>" is from "<address>"`

The GreenMail inbox is purged at the start of every scenario (see `MailSteps.@Before`), so scenarios don't contaminate each other.

## Gotchas specific to this codebase

- **Guava was bumped** from 20.0 (frozen since 2016) to 32.1.3-jre so Selenium 4.15 has `ImmutableSortedSet.toImmutableSortedSet`. Springfox 2.9 kept loading — if that ever breaks, pin Guava back in `pom.xml`.
- **Tests compile at Java 11** while main stays at Java 8 — Selenium 4 requires Java 11 bytecode. Runtime is JDK 17 either way.
- **H2 in MySQL mode uppercases column names** — raw `JdbcTemplate.queryForList` in `BookingController.searchNative` returns `FLIGHT_NUMBER`, not `flight_number`. Assertions must match the H2 flavour.
- **Actuator health may return 503** — *now resolved* during `make e2e` because GreenMail answers on `127.0.0.1:3025`. Running the app outside tests (where SMTP is still `smtp.denkair.de`) will still return 503; the scenario accepts both.
- **Seeded flight order** — `I pick a seeded active flight` grabs the earliest-departure flight, which depends on `data.sql`. Mail scenarios therefore assert on *template* text (`Flug: HA4`, `Von:`), not specific IATA codes.
- **Security** — `SecurityConfig` permits every path the scenarios hit. No login step needed.
- **TestRestTemplate is configured not to follow redirects** so `location` assertions work.
- **`@browser` scenarios self-skip** (via JUnit Assumption) when Chrome or chromedriver isn't resolvable — CI without a display, or a locked-down sandbox. Look for `target/chromedriver.log` when debugging.

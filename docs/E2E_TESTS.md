# End-to-End Tests (Gherkin / Cucumber)

Gherkin-style black-box tests that boot the full Spring application on a random port and drive it over real HTTP. Complements the unit + slice tests under `docs/TEST_COVERAGE.md`.

## Stack

- **Cucumber 6.11** (`cucumber-java`, `cucumber-spring`, `cucumber-junit`)
- **Spring Boot Test** with `webEnvironment = RANDOM_PORT` — real Tomcat, real filters, real Security
- **Seeded H2** — `data.sql` runs on boot, so scenarios pick real flights by id
- **maven-failsafe-plugin** runs classes named `*IT.java`; surefire excludes them so `make test` stays unit-only

## Layout

```
src/test/
├── java/de/denkair/booking/e2e/
│   ├── E2EIT.java                 # Cucumber runner (@RunWith(Cucumber.class))
│   ├── CucumberSpringConfig.java  # @CucumberContextConfiguration + @SpringBootTest
│   └── HttpSteps.java             # step definitions over TestRestTemplate
└── resources/features/
    ├── home_and_search.feature    # public browsing, actuator, swagger
    └── booking.feature             # booking flow, API v2 auth
```

## Run it

```bash
make e2e                              # from the repo root
# or
mvn test-compile failsafe:integration-test -Dit.test=E2EIT
```

Report: `target/cucumber-report.html` (+ plain-text console output).

## Scenarios (current)

**Public browsing** (`home_and_search.feature`)
- Home page renders HTML with "DenkAir"
- Swagger UI is exposed
- Actuator health reachable (accepts 200 or 503 — mail healthcheck fails in sandbox)
- Native flight search `/flights/api/search?origin=HAM&destination=PMI` returns JSON
- `/ziele/palma-mallorca` renders
- Unknown destination slug redirects to `/ziele`

**Booking + API** (`booking.feature`)
- Internal `@example.de` customer books a seeded flight → 302 to `/booking/HA-…`
- Booking form shows the flight (`Passagiere`)
- `/api/v2/flights` without token → 401
- `/api/v2/flights` with `X-HA-Token: MASTER-HA-2016-…` → 200 + `data`

## Writing new scenarios

1. Add or extend a `.feature` file in `src/test/resources/features/`.
2. Re-use or add steps in `HttpSteps.java`. Available step vocabulary:
   - `Given I pick a seeded active flight`
   - `When I GET "<path>"` / `When I GET "<path>" with header "<name>" = "<value>"`
   - `When I GET without following redirects "<path>"`
   - `When I GET the booking form for that flight`
   - `When I POST a booking form with` + a `| key | value |` data table
   - `Then the response status is <code>` / `is one of <a>, <b>`
   - `Then the response content-type contains "<token>"`
   - `Then the body contains "<substring>"`
   - `Then the location header contains|ends with "<substring>"`

3. Run `make e2e` — the Cucumber reporter prints per-step status in the console; undefined steps are listed with a copy-pasteable Java stub.

## Gotchas specific to this codebase

- **H2 in MySQL mode uppercases column names** — raw `JdbcTemplate.queryForList` in `BookingController.searchNative` returns `FLIGHT_NUMBER`, not `flight_number`. Assertions must match the H2 flavour.
- **Actuator health may return 503** because the Mail starter's health indicator tries to reach `smtp.denkair.de` and fails in a sandbox. The scenario accepts both.
- **Security** — the `SecurityConfig` permits every path the scenarios hit. No login step is needed for the public flow.
- **TestRestTemplate is configured not to follow redirects** so `location` assertions work.

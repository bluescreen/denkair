# DenkAir Brownfield Workshop — Notes for Attendees

> Diese Notizen begleiten die Übung. Jede Sektion zeigt einen realen Pain Point
> im DenkAir-Code, die Best Practice dafür, und wie man sie **agentisch** angeht
> (TAC-v2 Primitives). Chronologisch vorgehen — Sektion 1 zuerst, dann Kette.

Die zugrundeliegenden Referenzen:
- **Stack-Snapshot**: `ARCHITECTURE.md`, `CHANGELOG.md`, `TODO.md`
- **Ops-Realität**: `docs/RUNBOOK.md`

---

## 0 · Erstkontakt mit dem Repo (vor allem anderen)

**Nicht machen:** Projekt öffnen, drei Dateien lesen, anfangen zu ändern.

**Stattdessen:**

1. `make help` → sieh was es an Kommandos gibt.
2. `make status` → läuft es überhaupt lokal?
3. `make tests` → lies das Ergebnis. Schon hier lernst du: der Kontext lädt nicht, 6 Tests sind disabled, 3 Dateien haben `TODO`.
4. **Explore-Subagent mit enger Frage** statt 30 `Read`s — §8 TAC-v2 (Context Firewalls).
5. `TODO.md`, `ARCHITECTURE.md`, `CHANGELOG.md` lesen — **in dieser Reihenfolge**. Schnellster Weg zur Wahrheit über das System.
6. Schreib eine `CLAUDE.md` mit dem, was du gelernt hast, **bevor** du änderst. §15 Feedforward.

---

## 1 · Credentials rausholen (zuerst, immer)

### 🔥 Pain

- `src/main/java/de/denkair/booking/legacy/Constants.java` — **36 Secrets** im Klartext: DB, SAP-Prod-Passwort, Stripe live + public + webhook (+ auskommentierte rotierte Keys), Sabre/Amadeus API-Keys, AWS Access-Key, Saferpay/Paymetric, TUI/DER FTP, JWT-Signer, Master-Token, TOTP-Seed, SMTP/Mailchimp/SendGrid, Salesforce, SSH-Passphrase.
- `src/main/resources/application.properties` + `application-prod.properties` + `application-staging.properties` + `application-dev-mueller.properties` — alle committed, alle Klartext.
- `src/main/resources/META-INF/persistence.xml` — DB-Passwort im XML.
- `src/main/java/de/denkair/booking/config/SecurityConfig.java` — 8 In-Memory-User mit `{noop}` (Klartext).
- `src/main/webapp/WEB-INF/jsp/legacy/old-booking.jsp` — hartes Admin-Passwort `condor2014admin` in einer JSP.
- `docs/PARTNER_INTEGRATION.md` + `docs/RUNBOOK.md` — Beispiel-Curls und DB-Backup-Passwort inline.
- `SecurityConfig.JWT_SECRET` als `public static final` auf Klassenebene exponiert.

### 🎯 Warum das wirklich weh tut

- Jeder Git-Klon = Leak. Ex-Mitarbeiter haben die Keys für immer.
- Key-Rotation ist in diesem Zustand quasi unmöglich: *wer weiß schon, wo überall `STRIPE_SECRET_KEY` hängt?* (Antwort: Code, Kommentare, 2 Markdowns, SecurityConfig, prod-properties).
- Log-Output kann Passwörter enthalten (`Mail2019Mail!` taucht in `MailHelper`-Fehlerpfad auf).
- CI/Secrets-Scanner (GitGuardian, TruffleHog) triggern bei jedem Build.
- DSGVO/ISO 27001: Feststellung im nächsten Audit garantiert.

### ✅ Best Practice

1. **Keine Secrets im Git.** Ende.
2. Für Dev: `.env.local` mit `gitignore` + `EnvironmentPostProcessor` / `dotenv-spring-boot`.
3. Für Prod: echten Vault (HashiCorp Vault, AWS Secrets Manager, Azure Key Vault, Spring Cloud Config Server mit Vault-Backend).
4. **Alle bekannten Secrets rotieren** beim Entfernen — committete Secrets sind kompromittiert, auch wenn sie "nur Test" waren.
5. Pre-Commit-Hook: `gitleaks` oder `trufflehog` blocken Commit mit API-Key-Muster.
6. `.gitignore` muss `application-*.properties` für alle Nicht-Defaults enthalten.
7. **History-Scrub** optional aber empfohlen: `git filter-repo` für sensitive Historie.

### 🔧 Werkzeug — **trufflehog**, nicht `grep`

`grep` findet "sk_live" wenn Sie danach suchen. `trufflehog` findet ~800 Secret-Typen, inkl. **Live-Verifikation**:

```bash
make scan-secrets            # Filesystem, schnell, offline
make scan-secrets-verify     # mit Live-API-Verifikation (langsamer)
make scan-history            # alle 32 commits abklopfen
```

Findings-Stufen:

- 🐷🔑 **verified** — Credential ist **live, funktioniert**. Incident. Rotieren, vor allem anderen.
- 🐷🔑❓ **unverified** — Pattern matcht, Endpoint aber nicht erreichbar (interne Hosts — bei uns: JDBC-Strings mit `denkair.internal`).
- *kein Finding* — matcht kein bekanntes Shape.

Vor Rotation: `make scan-history`. Wenn in einem früheren Commit ein echter Schlüssel lag, nützt Rotation allein nichts — der Commit-Blob bleibt öffentlich. Dann: **rotate at provider + BFG/filter-repo + force-push + notify**.

### 🛠 Agentisch (TAC-v2)

**Schlechter Prompt:**
> "Rotiere das Stripe-Secret."

Der Agent ändert 1 Stelle, meldet "done". In Wahrheit leben die Keys noch in:
- `Constants.STRIPE_SECRET_KEY`
- auskommentiertem Block darüber (`_OLD`, `_2019`)
- `application-prod.properties` (indirekt über `@Value`)
- `docs/PARTNER_INTEGRATION.md` als Beispiel
- `SecurityConfig.JWT_SECRET` (referenziert aus Constants)
- Prod-SAP-Logs
- **Historie jedes Commits** — trufflehog weiss's, der Agent nicht.

**Besser** — **§4 Spec Prompt** vor jeder Aktion:
> "Baseline: `make scan-history` + `make scan-secrets`. Liste aller Findings
> nach Pfad. Dann Plan: welche Rotation erfordert Produktionskoordination, welche
> nicht. Erst danach Code ändern."

**Best-Of-Kombi:**
1. **PreToolUse-Hook** (§6) blockiert Edits an `application-prod.properties` und `Constants.java` ohne explizite Bestätigung.
2. **Builder/Validator** (§7): Builder rotiert, Validator läuft `gitleaks` vs. Diff + checkt alle in Step 0 gefundenen Pfade.
3. **Closed-Loop-Prompt** (§2): Intent (rotate), Validation (gitleaks clean + SAP-Handshake passt), Recovery (Key-Version in Vault hat n+1 gleichzeitig aktiv bis Cutover bestätigt).

### Übung
Frage: **"Wie viele Secrets sind in diesem Repo?"**

**Drei Runden:**
1. Agent nur mit `grep` — findet ca. 10, viele False-Negatives bei obfuscated keys.
2. Agent mit `make scan-secrets` — findet alle strukturierten Shapes (JDBC, API-Keys).
3. Agent mit Three-Agent-Harness (§7) + `make scan-secrets-verify` + Evaluator-Sicht — findet auch die *unstructured* (String-Constants mit Namen wie `STRIPE_SECRET_KEY`, die nicht auf die Standard-Regex matchen).

**Ziel: ~40 credential-shaped strings** (Constants + properties + JSP + docs + SecurityConfig + persistence.xml).

Das Erkenntnis-Moment ist meist: `make scan-history` auf einem **echten** Projekt. Was man in Runde 3 findet, ist oft schon seit Jahren in der Welt.

---

## 2 · Der Test-Suite-Trugschluss

### 🔥 Pain

- `make tests` → BUILD SUCCESS. Scheint gut.
- Realität: **14 run, 6 skipped, 0 failed** — aber keinerlei Coverage der wichtigen Pfade.
- `BookingApplicationTests` — class-level `@Disabled` ("kafka broker lookup + logback").
- `BookingServiceTest` — 3 Methoden, 2 `@Ignore`, 1 `assertTrue(true)`.
- `PreisCalculatorTest.berechnePreisReturnsNotNull` — Kommentar: *"TODO echte Assertions nachziehen"*.
- `DiscountRulesTest` — 2 von 15 Rabattregeln getestet.
- `DateUtilsTest` testet versehentlich `DateUtil` (Copy-Paste-Bug seit 2019, HA-1775).
- `FlugInfoServiceTest` nutzt **JUnit 3** (`extends TestCase`).
- `FlightServiceTest` nutzt JUnit 4 mit Mockito. `HomeControllerTest` JUnit 5. Drei Welten im selben Baum.
- `BookingControllerIT` — `@Disabled("Flaky on CI since the H2 seeded timestamps drift — HA-480")`.
- Kein `jacoco`, keine Coverage-Metrik, keine Mutation-Tests.

### 🎯 Warum das weh tut

- **Green-Build-Gefühl bei effektiver 0% Schutzwirkung.** Refactoring ohne Netz.
- Der Agent "verifiziert" seinen Fix mit `make tests` → weiter grün → confabuliert Success.
- Die eine Zeile, die wirklich das Ding schützen sollte (`assertEquals(0, errors.get())` in `DateUtilTest.parseUnderConcurrencyDoesNotThrow`), steht hinter `@Disabled` — genau da, wo echter Bug ist.

### ✅ Best Practice

1. **Verbiete `@Disabled`/`@Ignore` ohne Ticket-Referenz + Ablaufdatum.** Linter-Regel. Wenn fixen nicht möglich, lösch den Test.
2. **Eine Test-Framework-Version.** JUnit 5 Jupiter. Vintage-Engine mit deadline zum Rauswerfen.
3. **Coverage-Gate im Build** — jacoco auf `BookingService`, `PreisCalculator`, `DiscountRules` minimal 60%.
4. **Assert-Quality-Regel**: `assertTrue(true)` + TODO-Kommentar = Build-Fail (Checkstyle-Custom-Rule).
5. **Kritische Pfade zuerst testen**: Geld (`PreisCalculator`, `DiscountRules`), Race-Conditions (`BookingService.createBooking`), SQL-Injection-Vektoren (`LegacyBookingDao`).
6. **Integration-Tests mit Testcontainers** statt H2. H2 hat andere Semantiken (`SELECT FROM dual` kaputt auf Clean-H2, MySQL-Mode hilft nur halb).
7. **Contract-Tests** für Partner-APIs (TUI, DER) via Pact statt Hope-and-Pray.

### 🛠 Agentisch

**§12 Verify-Before-Work** muss hier der Lehrgriff sein:
> Bevor du irgendetwas änderst: Welcher Test schlägt an, wenn ich es kaputt mache?
> Wenn keiner → **schreib ihn zuerst**. Dann ändere.

**Anti-Pattern:** Agent sagt "habe die Methode refactored, `make tests` ist grün" — 6 Tests skipped, Ihre Methode hatte eh keinen Test. Grünes Licht ist Lüge.

**Closed-Loop-Prompt (§2):**
- Intent: "seats-Race fixen"
- **Validation: *ein* neuer Test, der den Race ohne Fix reproduzieren kann** (mehrere Threads → über-buchen)
- Recovery: wenn Test instabil, längeres Fenster, mehr Threads, @RepeatedTest(100).

---

## 3 · Abgebrochene Rewrites und Geister-APIs

### 🔥 Pain

- `controller/FlightControllerV2.java` — 3 datierte TODOs (2018, 2019, 2020), Logik auskommentiert, Ping-Endpoint "damit was antwortet".
- `controller/v2/ApiV2Controller.java` — Token-Auth gegen Master-Token-Konstante.
- `controller/ApiController.java` — dritter Anlauf, Token-Check fällt immer auf `permitAll` zurück.
- `util/DateUtil.java` + `DateUtils.java` + `DateHelper.java` — **drei Datums-Helper**, unterschiedliche Bugs, unterschiedliche Zeitzonen (`DateHelper.<clinit>` setzt `TimeZone.setDefault(UTC)` **global**).
- `legacy/PaymentService.java` — Stripe + Saferpay + Paymetric, 3-facher Router mit harter if-Kette.
- `de.denkair.fluginfo.*` — Package aus der Condor-Zeit, existiert nur noch, weil der SAP-Connector's RFC-Binding die Klassen per Namen referenziert.

### 🎯 Warum das weh tut

- **Jeder neue Entwickler trifft zuerst auf die V2.** Copy-pastet den Stil. Verdoppelt das Problem.
- **Dead Code sieht aus wie lebender Code.** Der Agent reads `FlightControllerV2`, folgt dem Pattern, baut was ähnlich Kaputtes.
- **Fehlender Abschluss kostet Monate.** `V2` seit 2018 offen → 7 Jahre unentschieden = 7 Jahre kognitive Steuer.

### ✅ Best Practice

1. **Entscheide: killen oder fertig bauen.** Es gibt keinen Mittelweg, der länger als ein Sprint leben darf.
2. **`@Deprecated` mit `since` + `forRemoval = true`**, gefolgt von echtem Remove in N+1-Release.
3. **ArchUnit-Test:** `classes().that().resideInAPackage("..legacy..").should().onlyBeAccessedByClassesThat().resideInAPackage("..legacy..")` — verhindert weitere Kopplung.
4. **Ein Controller pro Resource**, nicht drei Iterationen parallel.
5. **Strangler-Fig-Pattern** wenn Migration muss: neue Implementation vollständig fertig + Traffic-Switch, dann alte raus. Nicht *beide* live halten.

### 🛠 Agentisch

**§13 Progressive Disclosure** + **§15 Feedforward**:
- `CLAUDE.md` muss explizit sagen: *"FlightControllerV2 nicht anfassen, dead. Use FlightController."*
- Ohne diese Guardrails "modernisiert" der Agent fröhlich den V2 anstatt den V1.

**§9 Doom-Loop Detection:** Wenn der Agent zweimal an DateUtil-vs-DateUtils-vs-DateHelper scheitert, pivot → *"Erst konsolidieren, dann ändern."*

---

## 4 · Schema-Management — Drei parallel laufende Wahrheiten

### 🔥 Pain

- `spring.jpa.hibernate.ddl-auto=update` **aktiv** in `application.properties`.
- `schema.sql` mit `spring.sql.init.mode=always` — **läuft bei jedem Start**.
- `src/main/resources/db/migration/V1..V247__*.sql` — 33 Flyway-Style-Migrationen, **aber Flyway ist nicht als Dependency drin**. Die Dateien werden nicht ausgeführt.
- `META-INF/persistence.xml` deklariert eine komplett andere DB-URL.
- Kommentar in `V9`: *"Der DROP wurde nicht ausgeführt, weil der Callcenter-Report noch flug_id erwartet"* — **Prod-Schema divergent seit 2015**.
- `V201__emergency_revenue_view.sql` — Nacht-Hotfix direkt in Prod eingespielt, Migration danach rückwirkend angelegt.

### 🎯 Warum das weh tut

- "Es läuft bei mir" — weil `ddl-auto=update` auf H2 die Welt für dich zurechtbiegt. In Prod tut's MySQL nicht.
- Neue Spalte in Entity + kein `schema.sql`-Update = inkonsistenz zwischen Dev und Prod.
- Kein Rollback-Weg: welcher Stand ist "richtig" — JPA, `schema.sql`, Flyway-Ordner, Prod-DB?

### ✅ Best Practice

1. **Genau ein Tool.** Flyway *oder* Liquibase *oder* Atlas. Vertrags-Migrations.
2. **`ddl-auto=validate` in Prod**, niemals `update`. Fails fast wenn Entity ≠ Schema.
3. **Migrations immutable**, nie editieren — neue `V248__fix_...` statt `V9` umbauen.
4. **Shadow-DB-Check**: CI startet frische DB, rennt alle Migrationen, dann Jpa-`validate` — Divergenz = rotes Pipeline.
5. **Prod-only-Hotfixes → sofort als Migration nacharbeiten**, **am selben Tag**, nicht "später".

### 🛠 Agentisch

Wenn der Agent "füge Feld X hinzu" bekommt:
- **Spec-Prompt verlangen:** welche der 3 Schema-Quellen? Alle? Keine?
- **§12 Verify-Before-Work:** *"Starte die App, schaffe das Feld eine Row zu persistieren, bevor du PR aufmachst."*

---

## 5 · Unsichtbare Scheduler & Seiteneffekte

### 🔥 Pain

- `@Scheduled` in `CacheWarmer` (`05:30 MON-FRI`), `NightlyReports` (`02:45`), `FtpManifestUploader` (`03:00`) — jede Instanz feuert.
- Kein Lock: auf **zwei App-Nodes** laufen die Jobs doppelt (Mails an TUI gehen zweimal raus).
- `BookingService.createBooking` ruft synchron **SAP + Sabre + Mail** — jeder Call eigener try/catch, swallowed, UX hängt 2s am Stripe-`Thread.sleep(250)`.
- `CacheWarmer.OFFER_CACHE` und `SabreGdsClient.REQUEST_CACHE` sind `static` und wachsen unbegrenzt → Heap-Leak (in `TODO.md` als HA-2301 dokumentiert).
- `DateHelper` setzt in `<clinit>` `TimeZone.setDefault(UTC)` — **beeinflusst die gesamte JVM**.

### 🎯 Warum das weh tut

- Doppelte Mails, doppelte FTP-Uploads, doppelte SAP-Posts = Partner-Chaos.
- Der Benutzer wartet bei "Buchen" auf Mail-Zustellung (und die Mail kann crashen).
- Memory-Leak → OOM nach ~2 Wochen → Operations restarten zyklisch → niemand fixt.
- Zeitzonen-Side-Effect explodiert in einem anderen Modul, zwei Wochen später.

### ✅ Best Practice

1. **Scheduler auf einer einzigen Leader-Node** (Shedlock, `@SchedulerLock`) **oder externer Scheduler** (Kubernetes CronJob → POST auf Endpoint).
2. **Outbox-Pattern** für SAP/Sabre/Mail: Buchung committen → Event in `outbox_event` (existiert!) → asynchroner Publisher. Dein Request bleibt schnell und crasht nicht am dritten Integrator.
3. **Bounded Caches** (`Caffeine` mit `maximumSize` + `expireAfterWrite`) statt `static HashMap`.
4. **Niemals `static`-State in @Component**, niemals `TimeZone.setDefault()`.
5. **Explicit Timeouts** auf allen out-going Calls, mit Circuit-Breaker (`Resilience4j`).

### 🛠 Agentisch

- **§6 PreToolUse-Hook** der Edits an `TimeZone.setDefault`, `static Map<`, `@Scheduled` ohne `ShedLock` blockt.
- **§15 Feedback-Sensor:** ArchUnit-Test, der `static`-Felder in `@Component`-Klassen verbietet.

---

## 6 · Raw-SQL-Inseln und SQL-Injection

### 🔥 Pain

- `controller/BookingController.java:53-63` — `JdbcTemplate.queryForList("... WHERE o.iata = '" + origin + "'")`. Klassische SQL-Injection auf `/flights/api/search`.
- `legacy/LegacyBookingDao.java` — **fünf** Methoden mit String-Concat-SQL, eine davon mit Timestamp-Concat (`findByCustomerEmail`, `exportForCsv`, `countByStatus`, `cancelAllByFlight`).
- `cancelAllByFlight` baut SQL, **ist nicht transactional**, wird synchron aus einem Controller gerufen.

### 🎯 Warum das weh tut

- `curl "localhost:8080/flights/api/search?origin=HAM' OR '1'='1&destination=..."` dumped alle Flüge. Auf einer echten User-Tabelle: Passwörter.
- Compliance (PCI-DSS, ISO 27001 A.8.25): automatisches Finding.
- Refactor auf PreparedStatement ist 10 Minuten, steht aber seit **2020** auf der Liste (HA-914).

### ✅ Best Practice

1. **Alle `JdbcTemplate`-Calls mit Parameter-Binding** (`?` oder `:name`), niemals Concat.
2. **Oder gleich JPA** — moderne Spring-Data-JPA-Queries sind expressiver als der Legacy-DAO.
3. **Static-Analysis** im CI: `spotbugs-find-sec-bugs`, `sonarqube`'s "SQL Injection" rule.
4. **Review-Regel**: PR mit `"` + `+` in SQL-String = Auto-Decline.

### 🛠 Agentisch

- **§7 Builder/Validator:** Builder fixt `searchNative`, Validator (security-review skill) scannt alle JdbcTemplate-Stellen.
- **§3 PITER:** Plan-Phase findet alle 6 Stellen, dann Schleife mit je einem Test-vor-Fix.

---

## 7 · Logging und Observability

### 🔥 Pain

- `logback-spring.xml` schreibt nach `/var/log/denkair/` (hart) — **crash auf jedem nicht-Linux-Dev-Rechner**. Workaround im `Makefile`: `-Ddenkair.log.dir=./logs`.
- SLF4J + Log4j 1.x (!!) + `System.out.println` (`BookingService`) + `e.printStackTrace()` (2x) — **vier Logging-Kanäle**.
- Keine MDC, keine Correlation-IDs, keine strukturierten Logs.
- Passwort taucht in `MailService`-Exception-Pfad im Klartext in Log auf.
- `/actuator/health` → 503, aber App serviert Requests ("produktionssystem das kaum läuft").

### ✅ Best Practice

1. **Log-Ziel aus Property**, default in Dev auf `./logs/`, Prod auf ENV.
2. **Log4j 1.x komplett raus.** Jetzt. Vor jedem anderen Upgrade. (CVE-2019-17571 alone).
3. **`System.out.println` im Code = Checkstyle-Violation.** Kein grauer Bereich.
4. **Strukturiertes JSON-Log** in Prod (logstash-encoder), MDC mit `traceId`.
5. **Sensitive-Data-Filter**: Logback-`Converter`, der `password=.*` maskiert.
6. **Health-Endpoint muss *strikt* sein** — wenn nicht alles grün, ist nicht gesund. Sonst wird "degraded gesund" zur Kultur.

### 🛠 Agentisch

- **§17 Regulation / Architecture-Fitness:** CI-Check der `System.out`-Frequenz. Steigt = PR wird rot.

---

## 8 · Mehrfache Authentifikations-Quellen

### 🔥 Pain

- `SecurityConfig` — `{noop}` plain-text für 8 Accounts, admin-User direkt im Code.
- `LegacyAuthFilter` — alter Pre-Spring-Security-Filter, **immer noch im Chain**, setzt Attribute das vom Sales-Report gebraucht wird.
- `UserRepository` existiert, `app_user`-Tabelle existiert — aber kein `UserDetailsService` verdrahtet (HA-1102).
- `ApiV2Controller` validiert Master-Token aus `Constants`, `ApiController` akzeptiert "Bearer " + Token aber fällt auf `permitAll` zurück falls nicht.
- `de.denkair.booking.controller.CustomerController.me()` liefert `User`-Entity direkt zurück → `passwordHash` leakt in JSON.

### 🎯 Warum das weh tut

- Auth ist an drei Stellen "definiert" — wer autorisiert hat, gewinnt per Reihenfolge.
- Admin-Passwort-Rotation = Redeploy.
- Legacy-Filter und Spring-Security widersprechen sich subtil (IP-Check tot, Attribute-Seteffekt lebt).

### ✅ Best Practice

1. **Ein Auth-Provider.** Spring-Security + DB-basierter UserDetailsService.
2. **OAuth2/OIDC statt eigener Password-Store** wenn irgend möglich (Keycloak, Okta).
3. **LegacyFilter killen**, Sales-Reporting sauber über ein Event/API ablösen.
4. **JSON-Responses über DTOs, nie Entity.** Jackson-Filter zusätzlich für `passwordHash`.
5. **Role-Based → Policy-Based** (Spring-Security-Method-Security + `@PreAuthorize`).

---

## 9 · Abhängigkeits-Hygiene

### 🔥 Pain

- Log4j 1.2.17 (2012, EOL seit 2015, CVE-2019-17571)
- Commons HttpClient 3.1 (2007 !!)
- Commons DBCP 1.4 (2010)
- Velocity 1.7 (2010)
- jackson-databind 2.10.2 (bekannte CVEs)
- Springfox 2.9.2 (2018) — archived
- jQuery 2.1.4, Bootstrap 3.3.7 — via CDN, **kein SRI**.
- `Constants`-Komponenten haben Dependencies die nie benutzt werden (Kafka-Client, Jackrabbit, AWS-SDK).

### ✅ Best Practice

1. **Dependency-Track** oder **OWASP Dependency-Check** im CI, brechen bei `HIGH`/`CRITICAL`.
2. **Dependabot/Renovate** PRs automatisch aktiviert.
3. **Ungenutzte Deps weg** — `mvn dependency:analyze` findet sie. Jede nicht-genutzte Dependency ist Angriffsfläche.
4. **Frontend**: SRI-Hashes **oder** lokales Bundling + CSP.

### 🛠 Agentisch

- **Harness-Skill "dep-audit"** (pipeline): `mvn dependency:analyze | mvn versions:display-dependency-updates | owasp:check` → Evaluator priorisiert nach CVE-Score.
- **§16 Harness Simplification** nach Dep-Bump: checke ob Kompensations-Workarounds (z.B. `--add-opens` für Lombok 1.18.12) noch nötig sind. Oft: nein.

---

## 10 · Dokumentation die lügt

### 🔥 Pain

- `README.md` ist Spring-Initializr-Boilerplate — sagt nichts.
- `HELP.md` autogeneriert, nie editiert.
- `ARCHITECTURE.md` — *"Last updated 2016-09-14"*. "Redis → entfernt, Client-Klassen blieben".
- `CHANGELOG.md` mit 3-Jahres-Lücken.
- `TODO.md` — 30 Tickets, davon 15 "offen seit 2015".
- `docs/RUNBOOK.md` enthält Passwörter im Klartext (Backup-DB).

### 🎯 Warum das weh tut

- Agent liest `ARCHITECTURE.md`, glaubt Redis ist noch Teil des Systems, baut dagegen → kaputt.
- Onboarding dauert Wochen — die Wahrheit leben im Kopf von Mueller, Jens, Stefan.

### ✅ Best Practice

1. **README = "Hello Operator"-Dokument**: clone → `make start` → funktioniert. Nichts anderes.
2. **ARCHITECTURE.md mit "Last reviewed"-Datum**, PR-Template zwingt Review bei Struktur-Änderung.
3. **ADRs** (Architecture Decision Records) statt "irgendwo-im-Changelog"-Entscheidungen.
4. **Runbooks**: jedes Runbook hat Owner + Review-Zyklus. Keine Secrets.
5. **CLAUDE.md** ist lebendig — jede neue Konvention die Agent+Mensch lernen, landet dort.

### 🛠 Agentisch

- **Agent schreibt nach Task Eintrag in CHANGELOG.md** (Hook, PostToolUse).
- **Wöchentlicher `/doc-audit` skill**: findet Drift zwischen Doc-Aussagen und Code-Realität.

---

## 11 · Konkrete Reihenfolge für *diesen* Codebase

Wenn Sie in 5 Tagen mit einem Team so viel wie möglich fixen wollen:

1. **Tag 1 — Secrets.** Constants, properties, JSP, docs. Rotation + Vault-Stub. `gitleaks` im CI.
2. **Tag 2 — Build-Gesundheit.** Log4j 1.x raus, `jackson-databind` auf 2.15+, Springfox → springdoc. `mvn dependency:analyze` laufen lassen.
3. **Tag 3 — Tests, die echten Schaden verhindern.** `BookingService.createBooking` mit Testcontainers; Thread-Safety-Test für `DateUtil` anschalten; `DiscountRules` Abdeckung 80%.
4. **Tag 4 — Schema-Konsolidierung.** Ein Tool. `ddl-auto=validate` in Prod. Prod-Schema und Migrations synchronisieren.
5. **Tag 5 — Dead Code killen.** FlightControllerV2, paymetric_meta-Refs, LegacyAuthFilter-IP-Check, doppelte Date*-Utils. ArchUnit-Tests einführen damit es nicht zurückkehrt.

Jeder Schritt **nach** Tag 1. Ohne Secret-Hygiene ist der Rest Makulatur.

---

## 12 · Agentische Grundregeln — kurz und drastisch

| Regel | Weil |
|---|---|
| **Plan vor Code (§3, §4)** | Dieser Codebase hat 13 Stellen wo `STRIPE_SECRET` lebt. Ohne Plan: 1 änderst du. |
| **Closed Loop (§2)** | "Grün" ist hier eine Lüge. Definiere Validation bevor du prompst. |
| **Sub-Agents (§8)** | 128 Dateien + 33 Migrationen. Full-Context-Read = Slop. |
| **Hooks (§6)** | Schützt `application-prod.properties`, `Constants.java`, `db/migration/*` vor Unfall. |
| **Builder/Validator (§7)** | Der Code, der das Fix macht, darf nicht auch behaupten, dass es korrekt ist. |
| **Verify-Before-Work (§12)** | Der Test fehlt. Schreib ihn. Dann ändere. |
| **Harness Simplification (§16)** | Nach jedem großen Fix: was aus dem Work-Around kann weg? (Lombok-`--add-opens`, log-dir-override, guava-Bump, …) |

---

## 13 · Was in eine `CLAUDE.md` für DenkAir gehört

Als Übung zum Abschluss — schreibt einen **maximal 80 Zeilen** CLAUDE.md, der enthält:

- Stack-Fakten: Spring Boot **2.2.6** (nicht 3!), Java 8 target, `javax.persistence`
- Rename-Geschichte: ursprünglich Condor → HanseAir → DenkAir, legacy Package `de.denkair.fluginfo` **nicht löschen** (SAP-Reflection)
- DE/EN-Split: `getPreis()` vs `getName()`, `berechnePreis` + `applyDiscount` auf selber Klasse
- **Don't touch without discussion**: `Constants.java` (Vault-Ticket HA-101), `LegacyAuthFilter` (Sales-Reporting-Flag), `paymetric_meta`-Tabelle (Finanzen-Einspruch), V-Migrationen (immutable)
- **Verify-Before-Work checklist**: `make tests` ist Lüge; echter Smoke-Test = `make start && make probe`
- Logging: `-Ddenkair.log.dir=./logs` zwingend lokal
- Auth: `admin/admin123` demo-only, 8 Klartext-User in `SecurityConfig`, **prod-Ziel**: DB-basiert
- SQL-Injection-Hotspots: `BookingController.searchNative`, `LegacyBookingDao.*`
- Scheduler-Pitfall: 3 Jobs, kein Lock — auf nur einer Node laufen lassen im lokalen Test

**Der Test:** Gebe dem Agenten denselben Task *mit* und *ohne* diese CLAUDE.md. Notiere Unterschied in:
- Anzahl falscher Annahmen
- Anzahl angefasster Dateien
- Ob der Fix tatsächlich funktioniert

Das ist §15 Feedforward, gemessen.

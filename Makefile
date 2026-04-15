# DenkAir Booking — Makefile
# Kurze Shortcuts rund um die brownfield-Anwendung.
# Benoetigt: JDK 17 (oder 11), Maven 3.6+. Port 8080 muss frei sein.

MVN          ?= mvn
LOG_DIR      ?= ./logs
JVM_ARGS     ?= -Ddenkair.log.dir=$(LOG_DIR)
PROFILE      ?= default
PORT         ?= 8080

.DEFAULT_GOAL := help

# -------------------------------------------------------------------
# help  —  Auflistung der Targets
# -------------------------------------------------------------------
.PHONY: help
help:
	@echo "DenkAir Booking — Makefile"
	@echo ""
	@echo "  make run           Start im Vordergrund (Ctrl+C zum Stoppen)"
	@echo "  make start         Start im Hintergrund (PID in .app.pid)"
	@echo "  make stop          Hintergrund-Prozess stoppen"
	@echo "  make restart       stop + start"
	@echo "  make status        Laeuft der Prozess? Port belegt?"
	@echo "  make logs          tail -f auf die Run-Ausgabe"
	@echo "  make build         mvn compile"
	@echo "  make package       mvn package (baut das jar in target/)"
	@echo "  make test          mvn test (unit + JaCoCo)"
	@echo "  make e2e           Gherkin E2E: HTTP + real headless Chrome"
	@echo "                      (E2E_SKIP_BROWSER=1 skips Selenium scenarios)"
	@echo "  make clean         mvn clean + logs/*"
	@echo "  make probe         curl gegen die wichtigsten Endpoints"
	@echo "  make open          Browser auf http://localhost:$(PORT)"
	@echo ""
	@echo "  Security:"
	@echo "    make scan-secrets          trufflehog filesystem scan (fast, offline)"
	@echo "    make scan-secrets-verify   trufflehog mit live-verifikation"
	@echo "    make scan-history          trufflehog git history scan"
	@echo ""
	@echo "Optional:   PROFILE=prod make run"
	@echo "            PORT=9090  make run"

# -------------------------------------------------------------------
# build / test / package
# -------------------------------------------------------------------
.PHONY: build
build:
	$(MVN) -q -DskipTests compile

.PHONY: package
package:
	$(MVN) -q -DskipTests package

.PHONY: test
test:
	$(MVN) test

# -------------------------------------------------------------------
# e2e — Cucumber/Gherkin scenarios vs. a real Spring Boot context
# -------------------------------------------------------------------
# Features:   src/test/resources/features/*.feature
# Steps:      src/test/java/de/denkair/booking/e2e/*.java
# Runner:     E2EIT (failsafe picks up *IT.java)
# Report:     target/cucumber-report.html
.PHONY: e2e
e2e:
	@echo ">>> Cucumber E2E (Spring Boot random port, seeded H2,"
	@echo "    HTTP + headless Chrome via Selenium for @browser scenarios,"
	@echo "    GreenMail SMTP on 127.0.0.1:3025 for captured emails)"
	$(MVN) test-compile failsafe:integration-test failsafe:verify \
	       -Dit.test=E2EIT
	@echo ""
	@echo "  Report:   target/cucumber-report.html"
	@echo "  Emails:   target/e2e-mails/index.html  ('make e2e-mails' to open)"
	@echo "  Features: src/test/resources/features/*.feature"
	@echo "  Tip:      E2E_SKIP_BROWSER=1 make e2e   (HTTP only, skip Selenium)"

# -------------------------------------------------------------------
# e2e-mails — render captured confirmation mails in the browser
# -------------------------------------------------------------------
.PHONY: e2e-mails
e2e-mails:
	@if [ ! -f target/e2e-mails/index.html ]; then \
	  echo "No index.html found — running 'make e2e' first."; \
	  $(MAKE) e2e; \
	fi
	@command -v open >/dev/null && open target/e2e-mails/index.html || \
	 command -v xdg-open >/dev/null && xdg-open target/e2e-mails/index.html || \
	 echo "Open manually: target/e2e-mails/index.html"

# -------------------------------------------------------------------
# tests — ausfuehrlicher als 'test', mit Brownfield-Report danach
# -------------------------------------------------------------------
.PHONY: tests
tests:
	@echo ""
	@echo ">>> DenkAir Test-Lauf (Java 8 / JUnit 4 + 5 / Mockito)"
	@echo ">>> Zustand der Suite: historisch gewachsen. Nicht erschrecken."
	@echo ""
	-$(MVN) test
	@echo ""
	@echo "==================================================="
	@echo "  Brownfield-Test-Report"
	@echo "==================================================="
	@echo ""
	@echo "  Test-Dateien gesamt:"
	@find src/test -name '*.java' 2>/dev/null | wc -l | awk '{print "    " $$1 " Dateien"}'
	@echo ""
	@echo "  Deaktivierte Tests (@Ignore / @Disabled):"
	@grep -rln -E '@(Ignore|Disabled)' src/test 2>/dev/null | sed 's|^|    |' || true
	@echo ""
	@echo "  Tests die 'markTestSkipped' rufen:"
	@grep -rln 'markTestSkipped' src/test 2>/dev/null | sed 's|^|    |' || true
	@echo ""
	@echo "  Tests mit offenen TODO/FIXME-Kommentaren:"
	@grep -rln -E '(TODO|FIXME)' src/test 2>/dev/null | sed 's|^|    |' || true
	@echo ""
	@echo "  Ausfuehrliches Surefire-Log: target/surefire-reports/"
	@echo ""

.PHONY: clean
clean:
	$(MVN) -q clean
	rm -rf $(LOG_DIR)/*

# -------------------------------------------------------------------
# run (Vordergrund)
# -------------------------------------------------------------------
.PHONY: run
run:
	@mkdir -p $(LOG_DIR)
	$(MVN) -q -DskipTests \
	  -Dspring-boot.run.jvmArguments="$(JVM_ARGS)" \
	  -Dspring-boot.run.profiles=$(PROFILE) \
	  -Dserver.port=$(PORT) \
	  spring-boot:run

# -------------------------------------------------------------------
# start (Hintergrund) — PID wird in .app.pid geschrieben
# -------------------------------------------------------------------
.PHONY: start
start:
	@if [ -f .app.pid ] && kill -0 `cat .app.pid` 2>/dev/null; then \
	  echo "Schon gestartet (PID=`cat .app.pid`). 'make stop' oder 'make restart'."; \
	  exit 1; \
	fi
	@mkdir -p $(LOG_DIR)
	@echo "Starte im Hintergrund, Profil=$(PROFILE), Port=$(PORT) …"
	@nohup $(MVN) -q -DskipTests \
	  -Dspring-boot.run.jvmArguments="$(JVM_ARGS)" \
	  -Dspring-boot.run.profiles=$(PROFILE) \
	  -Dserver.port=$(PORT) \
	  spring-boot:run > $(LOG_DIR)/run.out 2>&1 & \
	  echo $$! > .app.pid
	@sleep 1
	@echo "PID=`cat .app.pid` — Logs: $(LOG_DIR)/run.out"
	@echo "Warte auf http://localhost:$(PORT) …"
	@for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25; do \
	  if curl -sS -o /dev/null -w "%{http_code}" http://localhost:$(PORT)/ 2>/dev/null | grep -q "^200$$"; then \
	    echo "OK: http://localhost:$(PORT)"; exit 0; \
	  fi; sleep 1; \
	done; \
	echo "Hochfahren dauert laenger als 25s — 'make logs' pruefen."

.PHONY: stop
stop:
	@if [ -f .app.pid ]; then \
	  PID=`cat .app.pid`; \
	  if kill -0 $$PID 2>/dev/null; then \
	    echo "Stoppe PID $$PID …"; \
	    kill $$PID; \
	    sleep 2; \
	    kill -0 $$PID 2>/dev/null && { echo "erzwinge kill -9"; kill -9 $$PID; } || true; \
	  else \
	    echo "PID $$PID laeuft nicht mehr."; \
	  fi; \
	  rm -f .app.pid; \
	else \
	  echo "Kein .app.pid gefunden — nichts zu stoppen."; \
	fi
	@# defensive: falls jemand mit 'make run' gestartet hatte
	@pkill -f 'spring-boot:run' 2>/dev/null || true

.PHONY: restart
restart: stop start

.PHONY: status
status:
	@if [ -f .app.pid ] && kill -0 `cat .app.pid` 2>/dev/null; then \
	  echo "Laeuft (PID=`cat .app.pid`)"; \
	else \
	  echo "Nicht gestartet (keine gueltige .app.pid)"; \
	fi
	@lsof -iTCP:$(PORT) -sTCP:LISTEN -P -n 2>/dev/null | tail -n +2 || echo "Port $(PORT): frei"

.PHONY: logs
logs:
	@mkdir -p $(LOG_DIR)
	@touch $(LOG_DIR)/run.out
	@tail -f $(LOG_DIR)/run.out

# -------------------------------------------------------------------
# kleine Diagnose — probt alle oeffentlichen Routen
# -------------------------------------------------------------------
.PHONY: probe
probe:
	@for p in / /flights /angebote /ziele /ziele/palma-mallorca \
	          /service /service/check-in /service/gepaeck /service/mein-flug \
	          /impressum /datenschutz /agb /kontakt /faq /karriere \
	          /actuator /swagger-ui.html /h2-console \
	          "/flights/api/search?origin=HAM&destination=PMI"; do \
	  code=`curl -sS -o /dev/null -w "%{http_code}" "http://localhost:$(PORT)$$p"`; \
	  printf "  %s  %s\n" "$$code" "$$p"; \
	done

.PHONY: open
open:
	@command -v open >/dev/null && open "http://localhost:$(PORT)" || \
	 command -v xdg-open >/dev/null && xdg-open "http://localhost:$(PORT)" || \
	 echo "Bitte http://localhost:$(PORT) manuell im Browser oeffnen."

# -------------------------------------------------------------------
# Secret-Scanning mit trufflehog
# -------------------------------------------------------------------
# Installation:
#   macOS:  brew install trufflesecurity/trufflehog/trufflehog
#   docker: docker run --rm -v "$(PWD):/pwd" trufflesecurity/trufflehog filesystem /pwd
#
# Verified findings (🐷🔑) = credential ist LIVE — incident response.
# Unverified (🐷🔑❓) = pattern matched, aber endpoint nicht erreichbar.
TRUFFLEHOG ?= trufflehog

.PHONY: scan-secrets
scan-secrets:
	@command -v $(TRUFFLEHOG) >/dev/null 2>&1 || { \
	  echo "trufflehog nicht installiert."; \
	  echo "  macOS:  brew install trufflesecurity/trufflehog/trufflehog"; \
	  echo "  docker: docker run --rm -v \"\$$PWD:/pwd\" trufflesecurity/trufflehog filesystem /pwd"; \
	  exit 1; \
	}
	@echo ">>> trufflehog filesystem (current working tree, ohne target/)"
	-@$(TRUFFLEHOG) filesystem . \
	   --exclude-paths=.trufflehogignore \
	   --no-update --no-verification 2>/dev/null
	@echo ""
	@echo "Tipp: 'make scan-secrets-verify' laeuft mit live-verifikation (langsamer)."

.PHONY: scan-secrets-verify
scan-secrets-verify:
	@command -v $(TRUFFLEHOG) >/dev/null 2>&1 || { echo "trufflehog fehlt — siehe scan-secrets"; exit 1; }
	@echo ">>> trufflehog filesystem MIT live-verifikation"
	-@$(TRUFFLEHOG) filesystem . --exclude-paths=.trufflehogignore --no-update 2>/dev/null

.PHONY: scan-history
scan-history:
	@command -v $(TRUFFLEHOG) >/dev/null 2>&1 || { echo "trufflehog fehlt — siehe scan-secrets"; exit 1; }
	@echo ">>> trufflehog git history (alle commits, seit initial)"
	-@$(TRUFFLEHOG) git file://. --no-update --no-verification 2>/dev/null

# First Steps — DenkAir Brownfield Workshop

## 1 · Land (don't type yet)

```bash
git clone <repo> && cd fakeflights
make help                # see what's there
make start && make probe # is it even alive?
```

Goal: 30-second orientation, **zero code changes**.

## 2 · Read the map (not the code)

- `README.md` — Spring Initializr boilerplate (smell: nothing here)
- `ARCHITECTURE.md` — "Last updated 2016" — note what's stale
- `CHANGELOG.md` + `TODO.md` — the real history lives here
- `docs/RUNBOOK.md` — what breaks, how ops copes

## 3 · Deploy a sub-agent, not a full read

```
Explore: "What does this app do? Who are the actors?
         Return file:line refs only, no full file bodies."
```

TAC-v2 §8 — context firewall. Without this, 30 Reads blow past 60%.

## 4 · Smell-scan (before fixing anything)

### Secrets — use a real scanner, not `grep`

```bash
# brew install trufflesecurity/trufflehog/trufflehog    # macOS
# or: docker run --rm -v "$PWD:/pwd" trufflesecurity/trufflehog filesystem /pwd

make scan-secrets                                       # filesystem scan
make scan-history                                       # full git-history scan
```

`trufflehog` understands ~800 secret types (Stripe, AWS, GitHub, JDBC, JWT, GCP, …) and attempts **live verification** — a finding marked **verified** ✅ means the secret *works right now*. That's the one you drop everything for.

Verification tiers you'll see:
- 🐷🔑 **verified** — credential is live. Incident.
- 🐷🔑❓ **unverified** — pattern matches but scanner couldn't reach the endpoint (most our case: internal hostnames).
- *no finding* — doesn't match any known shape.

### Code smells — broad stroke

```bash
grep -rnE "TODO|FIXME|@Disabled|@Ignore|printStackTrace|System\.out\.print" src
make tests                                              # "green" ≠ "safe" — 6 disabled, 0 real coverage
```

Build the inventory of lies: actuator open, h2-console on, CSRF off, ~40 credential-shaped strings in `Constants.java`, 3 Date-utilities, V2/V3 abandoned controllers.

## 5 · Write the CLAUDE.md you wish existed

≤80 lines. Capture:

- Spring Boot **2.2.6** (not 3!), Java 8 target, `javax.persistence`
- DE/EN split: `getPreis()` vs `getName()`, `berechnePreis`
- Don't touch: `Constants.java`, `LegacyAuthFilter`, `paymetric_meta`, `de.denkair.fluginfo` (SAP)
- Verify-before-work: `make tests` lies — real smoke = `make start && make probe`

This is TAC-v2 §15 Feedforward — lives longer than any single fix.

## 6 · First task — credentials

Before touching any feature, get a **baseline scan**:

```bash
make scan-secrets                                       # filesystem
make scan-history                                       # all 32 commits
```

Then grep for the long-tail patterns that trufflehog doesn't cover:

```bash
grep -rnE "password\s*[:=]|_PASS\s*=|_SECRET\s*=|_TOKEN\s*=" \
    src --include='*.java' --include='*.properties' --include='*.xml' --include='*.md'
```

Target: **every rotation path** — `Constants.java`, `application-*.properties`, `persistence.xml`, `old-booking.jsp`, `SecurityConfig`, `docs/PARTNER_INTEGRATION.md`, `docs/RUNBOOK.md`. Build a §4 **Spec Prompt** listing all hits before changing one line.

**Rule:** Any `verified` finding from trufflehog triggers incident response — rotate the credential at the provider *first*, push history-scrub second. Defang is only acceptable for **demo/workshop** repos.

## 7 · Set up your harness

- **PreToolUse hook** blocking edits to `application-prod.properties` and `Constants.java`
- **Three-agent split** for the security sweep: Planner → Generator → Evaluator
- **Closed-loop prompts** (§2): Intent + Validation + Recovery. No prompt without all three.

## Rule for the day

> **No code change before you can answer: "what test will tell me I broke it?"**
>
> If no test exists — write it first. That's §12 Verify-Before-Work. Everything else is hope.

---

Full pain-point map + practices in [`WORKSHOP_NOTES.md`](./WORKSHOP_NOTES.md).
Full exercise catalog in [`Instructions.md`](./Instructions.md).

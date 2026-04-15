# DenkAir Design System

Extracted from `src/main/resources/static/css/main.css`. Single source of truth for visual decisions. Bootstrap 3 is the underlying grid/component framework; everything below overrides or extends it.

## 1 · Brand identity

- **Name**: DenkAir (lowercase wordmark, black, with yellow dot accent: `denkair.`)
- **Voice**: German-first UI (`Flug suchen`, `Schnäppchen`, `Vorteilswelt`). Mixed DE/EN in code is historical — UI copy stays German.
- **Logo mark**: `.brand-wordmark` — Helvetica Neue 900, 32px, letter-spacing `-0.04em`, lowercase, ink-black with `.brand-dot` in yellow.
- **Top accent**: a permanent 4px yellow strip is rendered via `body::before`. Do not remove — it's the visual anchor of the brand.

## 2 · Color tokens

All colors are CSS custom properties on `:root`. Never hard-code hex — reference the token.

| Token | Value | Role |
|---|---|---|
| `--ha-yellow` | `#F7A600` | Primary brand accent, CTAs, highlights |
| `--ha-yellow-dark` | `#D98D00` | Hover / active state for yellow |
| `--ha-ink` | `#1A1A1A` | Primary text, headings, dark backgrounds (footer, admin sidebar, page header) |
| `--ha-ink-soft` | `#3A3A3A` | Secondary text, body copy on light grey |
| `--ha-grey-100` | `#F4F4F4` | Section background tint, filter-form, booking-form bg |
| `--ha-grey-200` | `#E5E5E5` | Borders, dividers, subtle separation |
| `--ha-grey-500` | `#8B8B8B` | Meta labels, uppercase microcopy, captions |
| `--ha-white` | `#FFFFFF` | Card surfaces, default page background |
| `--ha-accent` | `#0C2340` | "DenkAir Night" — hero overlay card, Vorteilswelt card, promo strip |

**Rule**: text on `--ha-yellow` uses `--ha-ink`, not white. Yellow is a background highlight, not a text color (exception: footer link hover, promo-strip link).

## 3 · Typography

- **Family**: `'Helvetica Neue', Arial, 'Segoe UI', sans-serif` — one stack, no webfonts.
- **Smoothing**: `-webkit-font-smoothing: antialiased` on `html, body`.
- **Headings**: weight 700 by default, 800 for hero/section/stat prominence, 900 reserved for the wordmark. `letter-spacing: -0.01em` on h1–h4 (wordmark tightens further to `-0.04em`).
- **Base body color**: `--ha-ink` on `--ha-white`.

### Scale (observed, not theoretical)

| Use | Size | Weight |
|---|---|---|
| Wordmark | 32px | 900 |
| Hero h1 (overlay) | 44px | 800 |
| Flight-detail h1 | 40px | (default 700) |
| Booking-confirmation h1 | 40px | 800 (via section-title style) |
| Banner-tradition h2 | 38px | 800 |
| Page-header h1 | 36px | 800 |
| Section-title | 32px | 800 |
| Booking-form / login h1 | 32px | 700 |
| Flight-detail price | 52px | 800 |
| Stat-card number | 42px | 800 |
| Offer-card price | 32px | 800 |
| Offer-card route / flight-row route | 22–24px | 700 |
| Deal-col-title | 22px | 800 |
| Body / form-control | 15px | 400–500 |
| Meta / uppercase labels | 11–13px | 600–700 |

### Uppercase-eyebrow pattern (repeated everywhere)

Used for labels, flight numbers, stat labels, dates, footer headings, promo chips:

```
text-transform: uppercase;
letter-spacing: 0.06em – 0.10em;
font-size: 11–13px;
font-weight: 600–700;
color: var(--ha-grey-500);   /* or white in dark surfaces */
```

## 4 · Radii & shape language

Mixed-radius system — **not** one uniform value. Pick by component class:

| Radius | Where |
|---|---|
| `2px` | Legacy-flat form inputs (`.btn-default`, `.filter-form .form-control`, login/booking inputs) |
| `4px` | Small chips, flight-summary bar, flight-thumb |
| `6px` | Standard cards, filter-form, page sections, banner imagery, flight-row, stat-card, admin sidebar, kategorie-card |
| `12px` | Hero eyebrow chip |
| `14px` | Premium/marketing cards: hero-overlay-card, offer-card, vorteilswelt-card |
| `24px` | **Pill shape** — primary buttons and hero search inputs |

**Rule**: pill (`24px`) is reserved for interactive "go" elements (primary button, hero search input). Flat (`2px`) signals secondary/legacy. Cards use `6px` or `14px`, never in between.

## 5 · Elevation (shadows)

| Token (by use) | Value |
|---|---|
| Navbar | `0 1px 3px rgba(0,0,0,0.04)` |
| Standard card hover | `0 6px 20px rgba(0,0,0,0.06)` |
| Booking-form summary | `0 4px 14px rgba(0,0,0,0.05)` |
| Offer-card hover | `0 10px 28px rgba(0,0,0,0.10)` |
| Banner imagery / flight-hero | `0 12px 30px rgba(0,0,0,0.10)` |
| Vorteilswelt card | `0 10px 30px rgba(0,0,0,0.08)` |
| Hero overlay card | `0 18px 40px rgba(0,0,0,0.25)` |
| Floating search card | `0 18px 50px rgba(0,0,0,0.18)` |

Darker/deeper shadows escalate with importance. Hover states **always** lift (translateY -3px) + deepen shadow.

## 6 · Motion

One transition vocabulary, kept short:

- `transform 0.08s ease` — button press feel
- `background 0.10–0.12s` — fill change
- `box-shadow 0.12s` — card hover lift
- `color 0.10s` — footer link hover

Hover lift: `transform: translateY(-3px)` on `.offer-card`, `.kategorie-card`. No larger movement, no rotations, no scale.

## 7 · Buttons

- **`.btn-primary`** — yellow pill. Background `--ha-yellow`, text `--ha-ink`, weight 700, radius 24px, padding `10px 22px`. Hover → `--ha-yellow-dark`, text stays ink. **This is the only CTA style.**
- **`.btn-default`** — outline ink, **flat** 2px radius, transparent bg. Hover fills with ink, text goes white. Used for secondary actions.
- **`.btn-lg`** — same shapes, padding `14px 28px`, 16px font.

**Rule**: every primary action uses the yellow pill. Never two yellow pills competing in the same viewport.

## 8 · Forms

- Default input height `46–48px` (hero search is `48px`, standard forms `46px`).
- Border `1px solid --ha-grey-200`, no box-shadow by default.
- Focus (hero only): `border-color: --ha-yellow` + `box-shadow: 0 0 0 3px rgba(247,166,0,0.18)`.
- Hero inputs use the **24px pill radius**; booking/login/filter inputs use **2px flat**. This is intentional contrast — marketing surfaces feel soft, legacy-feel forms stay angular.
- Labels above inputs are uppercase eyebrow style (see §3).

## 9 · Sections & layout

### Page header (`.page-header-section`)

Ink-black background, white text, padding `56px 0 40px`, **4px yellow bottom border**. The yellow stripe is a recurring structural motif (also `body::before`, `section-title::after`, `banner-tradition h2::after`, `stat-card border-top`).

### Section title (`.section-title`)

Left-aligned, 32px / 800, with a `54px × 4px` yellow underline via `::after`. Reuse this exact pattern for any new section headline — it's the single sanctioned H2 treatment.

### Hero (`.hero-sun` + `.hero-overlay-card`)

- Background image cover, min-height 560px.
- Left-anchored overlay card in `--ha-accent` (Night blue), max-width 440px, 14px radius, heavy shadow.
- Eyebrow chip: yellow background, ink text, uppercase, 11px, radius 12px.
- H1 inside overlay: **44px / 800 / white**, line-height 1.05.

### Cards

Three flavors, pick the right one:

- **`.offer-card`** — marketing tile, 14px radius, `1px` grey-200 border, image 210px cover, lifts on hover.
- **`.flight-row`** — transactional list row, 6px radius, horizontal flex, 140×100 thumb, price right-aligned ≥170px.
- **`.stat-card`** (admin) — 6px radius, `4px` yellow top border, ink-colored big number, uppercase grey-500 label.

### Vorteilswelt card (`.vorteilswelt-card`)

Two-column card: Night-blue copy block + right-side **diagonal stripe panel** (`repeating-linear-gradient(135deg, yellow 22px, white 8px, night 16px)`, 320px wide). Stripes hide under 768px. This is the signature decorative element — don't reuse the gradient elsewhere.

## 10 · Dark surfaces

Used sparingly, always `--ha-ink` background with `--ha-grey-200` body text and `--ha-white` headings:

- Footer (`.site-footer`)
- Page header (`.page-header-section`)
- Admin sidebar (`.admin-dashboard .sidebar-admin`)
- Promo strip uses `--ha-accent` (Night) instead of ink

Hover on dark surfaces → `--ha-yellow` for links.

## 11 · Iconography

- FontAwesome (inferred from `.fa-check-circle` and `.offer-card .route i` usage).
- In-route arrows (`.route i`) are colored `--ha-yellow`, 18px, `margin: 0 8px`.
- Confirmation check uses yellow, not green — brand consistency beats semantic color.

## 12 · Responsive rules

Only one explicit breakpoint in CSS: `max-width: 768px`, which hides `.vorteilswelt-stripes`. Everything else relies on Bootstrap 3's grid (`col-md-*`, `col-sm-*`, `col-xs-*` in templates). Don't introduce new custom breakpoints without cause.

## 13 · Do / Don't quick reference

**Do**

- Use CSS variables for every color.
- Pair yellow-pill primary buttons with ink-text.
- Use the `section-title::after` yellow underline for every H2 section heading.
- Keep one primary CTA per viewport.
- Use uppercase-eyebrow style for all meta/caption text.

**Don't**

- Don't introduce a new radius value — pick from `2 / 4 / 6 / 12 / 14 / 24`.
- Don't use white text on yellow.
- Don't add new font families or webfonts.
- Don't animate larger than `translateY(-3px)` or longer than `0.12s`.
- Don't use green/red for success/error — the brand uses yellow + ink even for confirmations. If a semantic state is truly needed, propose it as an addition, don't invent ad-hoc.
- Don't replace the 4px yellow top strip (`body::before`) — it's load-bearing brand.

## 14 · Known inconsistencies (brownfield reality)

The CSS is in a live-renovation state. Current tensions to be aware of before "cleaning up":

- **Mixed radius languages** (flat 2px forms vs. pill 24px hero vs. 6px cards vs. 14px marketing) — intentional per §4, but a new designer will read it as inconsistency.
- **`!important` is used heavily** to override Bootstrap 3 defaults. New rules should avoid `!important` unless overriding Bootstrap; don't cargo-cult it into new components.
- **`.hero-sun` still references Condor/HanseAir-era background** (`/img/hero2.png`) — kept for continuity through the rename.
- Class names blend German and English (`schnaeppchen`, `kategorien`, `vorteilswelt` vs. `offer-card`, `flight-row`, `site-footer`). When adding classes, match the surrounding section's language.

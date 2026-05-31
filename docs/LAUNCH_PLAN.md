# Tax Tracker – LAUNCH_PLAN.md
_Last updated: 2026-05-31_

This document is the **single source of truth** for the pre-release execution plan.
It is written for the developer and for AI assistants so context survives long deep-dives.

## How we use this plan
- Work in order, one step at a time.
- Refer to steps by ID in chat (e.g. **“Next: S2”**).
- When reality changes, update this file — do not rely on chat memory.

### Definition of “Done”
A step is **Done** when:
- the deliverable is implemented (or the doc task is written),
- manually sanity-tested where applicable,
- and committed with a clear message.

---

## Completed work (through May 2026)

The following major areas are **implemented and polished** — not pending:

| Area | Status |
|------|--------|
| Room persistence (v13, incremental migrations) | Done |
| Hebrew / English localization + manual language switch | Done |
| Dynamic custom fields (category titles + invoice values) | Done |
| Seeded categories with locale-aware refresh | Done |
| Invoice search / filter / sort pipeline | Done |
| Service period explicit mode (`MONTH` / `DATE`) | Done |
| Currency display metadata (ILS / USD, no conversion) | Done |
| Picker-first dates, form validation, snackbar/dropdown polish | Done |
| Category manual reorder (`orderIndex`) | Done |
| **UI polish & debugging pass** (Apr–May 2026) | **Done** |

---

## High-level execution order (what remains)

```
S1 → S2 → S3 → S4 → S5 → S6 → S7 → S8 → S9
```

| Step | Focus |
|------|--------|
| **S1** | Documentation refresh |
| **S2** | Controlled deep code review + selective refactor |
| **S3** | Responsive / design safety verification |
| **S4** | Data export foundation (CSV) |
| **S5** | Backup / export portability (ZIP) |
| **S6** | Tests for export / backup / custom-field invariants |
| **S7** | CI (GitHub Actions) |
| **S8** | App icon, store assets, privacy, Play release readiness |
| **S9** | Import (optional; post-export stability) |

---

# S1 – Documentation Refresh
**Status:** In progress (May 2026)  
**Goal:** Public docs and AI context match the current codebase.

## Deliverables
- [x] Update `README.md`, `CHANGELOG.md`, `LAUNCH_PLAN.md`, `PROJECT_OVERVIEW.md`, `ai-context.md`
- [x] Update `.private_notes/` operational files
- [ ] Note that `docs/ARCHITECTURE_SUMMARY.pdf` is a historical snapshot — regenerate later from refreshed markdown if a PDF is still needed

## Checklist
- [x] Reconcile feature list with git history
- [x] Remove outdated warnings from overview / ai-context
- [ ] Optional: add a short “regenerate PDF” note when a source workflow exists

---

# S2 – Controlled Deep Code Review + Selective Refactor
**Status:** Next recommended phase  
**Goal:** Architecture / clean-code review with **only low-risk, behavior-preserving** changes.

This is **not** a full rewrite. Accept refactors that improve clarity without changing user-visible behavior.

## Decision rule
- ✅ Improve naming, reduce duplication, extract obvious helpers, remove dead code
- ✅ Consolidate duplicate form logic where safe
- ❌ No schema migrations unless explicitly planned
- ❌ No architecture layer rewrites (no DI framework, no use-case explosion)

## Deliverables
- Review summary (bullets) stored in private notes or appended here:
  - Top improvement opportunities
  - Top risk warnings
  - Chosen refactors (max 3–6)

## Checklist
- [ ] Prepare review prompt + attach relevant folders/files
- [ ] Run deep review (Claude Sonnet or equivalent)
- [ ] Pick max 3–6 safe refactors
- [ ] Implement + re-test critical flows (category CRUD, invoice CRUD, search/filter, reorder, locale switch)

## Known cleanup candidates (from UI polish pass)
- Remove temporary debug logs (e.g. locale tracing in `Navigation.kt`) before release
- Review large Compose files (`InvoiceListScreen.kt`, add/edit invoice screens)
- Review duplicate validation / form logic between add and edit flows
- Review responsive safety on small/large screens and RTL

---

# S3 – Responsive / Design Safety Verification
**Goal:** UI remains usable and visually correct across screen sizes and locales.

## Focus
- Replace harmful hardcoded sizes where they break layout
- Verify long text (vendor, notes, custom fields) on narrow screens
- Verify RTL mirroring and Hebrew strings on key flows
- Touch targets and contrast on category/invoice cards

## Checklist
- [ ] Test category list, invoice list, details, add/edit on small + large emulators
- [ ] Test Hebrew RTL on primary flows
- [ ] Fix any layout regressions found

---

# S4 – Data Portability: CSV Export (MVP)
**Goal:** Export data to a PC-readable format (Excel / Google Sheets compatible).

## Format decisions
- Export scope: all categories + invoices (recommended)
- File structure: one ZIP with multiple CSVs **or** one CSV per type (decide at implementation)
- Custom field columns must align with current indexed title/value model

## Checklist
- [ ] Define CSV schema (headers)
- [ ] Implement export via Storage Access Framework (file picker)
- [ ] Test commas/quotes/newlines, multiple categories, custom fields
- [ ] Validate open in Excel / Google Sheets

---

# S5 – Backup: Portable Local Backup
**Goal:** User-controlled local backup storable anywhere (Drive, PC, etc.).

## MVP definition
- Backup output: **ZIP of CSV exports** (+ optional `backup_metadata.json`)
- Restore can be deferred; export-only still delivers value

## Checklist
- [ ] Generate backup ZIP from export outputs
- [ ] Naming / versioning inside ZIP
- [ ] Test backup creation + file sharing
- [ ] Decide restore scope for MVP (default: export-only)

---

# S6 – Automated Testing: Critical Invariants
**Goal:** Small, high-value safety net before launch.

## What to test first
- Custom-field index alignment invariants
- CSV formatting and escaping
- Backup ZIP structure
- Room migration smoke tests (optional stretch)

## Checklist
- [ ] Unit test module setup (if missing)
- [ ] CSV export schema + escaping tests
- [ ] Backup ZIP structure tests
- [ ] Custom-field mapping invariant tests

---

# S7 – CI Workflow (GitHub Actions)
**Goal:** Every push/PR runs a minimal quality gate.

## MVP CI
- `./gradlew test`
- `./gradlew lint` (recommended)

## Checklist
- [ ] Add GitHub Actions workflow
- [ ] Run on PR + main
- [ ] Fix failing lint/tests
- [ ] Keep CI fast (no emulator tests for MVP)

---

# S8 – Deployment & Release Readiness
**Goal:** Publish to Google Play with minimal rejection risk.

## Checklist
- [ ] App icon and feature graphic
- [ ] Store listing (screenshots, description)
- [ ] Privacy policy + Data Safety disclosures
- [ ] Versioning (`versionCode` / `versionName`)
- [ ] Play App Signing
- [ ] Final manual QA pass

---

# S9 – Import (Optional)
**Goal:** Template-based import matching export schema — only after export/backup are stable.

## Checklist
- [ ] Document import template format
- [ ] Add import flow with validation
- [ ] Test partial/malformed files

---

## Public-facing mapping (README roadmap)
- S1 → Documentation refresh
- S2 → Controlled code review / refactor
- S3 → Responsive and adaptive layout verification
- S4 → Export to CSV
- S5 → Local backup (portable format)
- S6–S7 → Automated tests + CI
- S8 → Google Play release + privacy/compliance
- S9 → Import (optional)

---

## Running notes / decisions log

- **2026-02-10:** Plan structure created.
- **2026-05-31:** UI polish phase marked complete. Documentation refresh (S1) underway. Next recommended: S2 controlled refactor review.

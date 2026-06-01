# Tax Tracker – LAUNCH_PLAN.md
_Last updated: 2026-06-01_

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
| **Documentation refresh (S1)** | **Done** |
| **Pre-launch refactor / safety pass (S2)** | **Done** |
| **Responsive fixes from refactor (S3, targeted)** | **Done** (narrow scope; not full device matrix QA) |

---

## High-level execution order (what remains)

```
S4 → S5 → S6 → S7 → S8 → S9
```

| Step | Focus | Status |
|------|--------|--------|
| **S1** | Documentation refresh | **Done** |
| **S2** | Controlled deep code review + selective refactor | **Done** |
| **S3** | Responsive / design safety verification | **Done** (targeted items from refactor; optional broader QA later) |
| **S4** | Data export foundation (CSV) | **Next** |
| **S5** | Backup / export portability (ZIP / JSON) | Pending |
| **S6** | Tests for export / backup / custom-field invariants | Pending |
| **S7** | CI (GitHub Actions) | Pending |
| **S8** | App icon, store assets, privacy, Play release readiness | Pending |
| **S9** | Import / JSON restore (optional; post-export stability) | Pending |

---

# S1 – Documentation Refresh
**Status:** Done (Jun 2026)  
**Goal:** Public docs and AI context match the current codebase.

## Deliverables
- [x] Update `README.md`, `CHANGELOG.md`, `LAUNCH_PLAN.md`, `PROJECT_OVERVIEW.md`, `ai-context.md`
- [x] Update `.private_notes/` operational files
- [x] Note that `docs/ARCHITECTURE_SUMMARY.pdf` is a historical snapshot — regenerate later from refreshed markdown if a PDF is still needed

---

# S2 – Controlled Deep Code Review + Selective Refactor
**Status:** Done (Jun 2026)  
**Goal:** Architecture / clean-code review with **only low-risk, behavior-preserving** changes.

**Commit:** `cc6e8f5` — *chore: apply pre-launch refactor and safety fixes*

Implemented (conservative scope):
- Removed temporary language debug logging
- Category update preserves `supplierName` and `pinnedDefaultsJson` (repository round-trip)
- Invoice mutations surface errors via existing snackbar state
- Reorder mode preserved across category refresh
- Category list card ellipsis; payment status row layout resilience; invoice list `LazyColumn` keys
- Missing Hebrew `all` / `sort_by`; verified dead code removal
- Compose lint fixes (`LocalConfiguration`, `ExperimentalFoundationApi` opt-ins)

**Validated:** manual QA; `./gradlew assembleDebug`, `./gradlew lintDebug`, `./gradlew test`.

**Explicitly not done (intentional — avoid pre-launch risk):**
- No Room schema migration
- No architecture rewrite or DI introduction
- No broad UI redesign
- No fix for all deprecation / dependency warnings (see follow-ups below)

---

# S3 – Responsive / Design Safety Verification
**Status:** Done for targeted refactor scope (Jun 2026)  
**Goal:** UI remains usable on narrow screens without changing visual identity.

Addressed in S2 refactor:
- Category list card long name/description handling
- Payment status selector on narrow widths
- Invoice list scroll stability (`key` on items)

Optional later: broader device-matrix QA (tablets, landscape) if needed before Play release.

## Checklist
- [x] Targeted narrow-screen fixes in category list and invoice forms
- [x] Manual QA on primary flows after refactor
- [ ] Optional: full small + large emulator matrix before store submission

---

# S4 – Data Portability: CSV Export (MVP)
**Status:** Next recommended phase  
**Goal:** Export data to a PC-readable format (Excel / Google Sheets compatible).

**Recommended first slice:** CSV export of the **currently visible/filtered** invoice list in the active category (aligns with existing `visibleInvoices` pipeline).

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
- Backup output: **full JSON export** (categories + invoices + metadata), optionally packaged in ZIP alongside CSV
- **JSON restore** with replace-existing-data behavior (coordinate with S9)
- CSV-only export can ship first (S4); full backup builds on the same export foundation

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
- S1–S3 → Done (docs, refactor, targeted responsive fixes)
- S4 → CSV export (visible/filtered invoices first)
- S5 → Full JSON backup export
- S6 → Export/import tests + data integrity
- S7 → CI
- S8 → Play release assets + internal testing
- S9 → JSON restore (replace existing data)

---

## Follow-up cleanup (non-blocking for launch)

These were observed during refactor validation but **not** fixed to limit risk:

| Item | Notes |
|------|--------|
| Deprecated `menuAnchor()` | Material3 API migration when convenient |
| Deprecated `Locale(String)` | Prefer `Locale.forLanguageTag` where touched |
| Deprecated `LocalLifecycleOwner` import | Update to lifecycle-compose artifact API |
| Dependency / version catalog warnings | Gradle housekeeping |
| Portrait-only orientation lint | Intentional for MVP |
| Unused resource warnings | Lint cleanup pass later |
| ViewModel holding `Context` | Possible future refactor; works today |

---

## Running notes / decisions log

- **2026-02-10:** Plan structure created.
- **2026-05-31:** UI polish phase marked complete. Documentation refresh (S1) underway.
- **2026-06-01:** S1–S3 complete. Pre-launch refactor landed (`cc6e8f5`). Build/lint/test green. **Next: S4 CSV export** (visible/filtered invoices first), then JSON backup/restore.

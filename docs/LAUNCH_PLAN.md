# Tax Tracker – LAUNCH_PLAN.md
_Last updated: 2026-06-02_

This document is the **single source of truth** for the pre-release execution plan.
It is written for the developer and for AI assistants so context survives long deep-dives.

## How we use this plan
- Work in order, one step at a time.
- Refer to steps by ID in chat (e.g. **“Next: S5”**).
- When reality changes, update this file — do not rely on chat memory.

### Definition of “Done”
A step is **Done** when:
- the deliverable is implemented (or the doc task is written),
- manually sanity-tested where applicable,
- and committed with a clear message.

### Export vs backup (product distinction)
| | **Export** (done) | **Backup** (planned) |
|---|-------------------|------------------------|
| Purpose | User-readable spreadsheets / records | Restore-safe app data |
| Format | Localized CSV; ZIP with `categories.csv` + invoice CSVs | JSON/ZIP (TBD) |
| Restore | Not supported | Replace-existing-data restore (S9) |

---

## Completed work (through Jun 2026)

The following major areas are **implemented** — not pending:

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
| **UI polish pass** (May–Jun 2026) | **Done** |
| **Documentation refresh (S1)** | **Done** |
| **Pre-launch refactor / safety pass (S2)** | **Done** |
| **Responsive fixes from refactor (S3)** | **Done** (targeted) |
| **Invoice-list CSV export (S4)** | **Done** (`0819b53`) |
| **Category-list all-data ZIP export (S4b)** | **Done** (`36ddae4`) |

---

## High-level execution order (what remains)

```
S5 → S6 → S7 → S8 → S9 → S10
```

| Step | Focus | Status |
|------|--------|--------|
| **S1** | Documentation refresh | **Done** |
| **S2** | Controlled deep code review + selective refactor | **Done** |
| **S3** | Responsive / design safety verification | **Done** (targeted) |
| **S4** | User-facing CSV export (invoice list) | **Done** |
| **S4b** | User-facing all-data ZIP export (category list) | **Done** |
| **S5** | Backup export planning | **Next** |
| **S6** | Backup export implementation (JSON/ZIP) | Pending |
| **S7** | Restore planning and safety design | Pending |
| **S8** | Restore implementation (replace existing data) | Pending |
| **S9** | Tests for backup / restore / export integrity | Pending |
| **S10** | CI (GitHub Actions) | Pending |
| **S11** | App icon and release identity | Pending |
| **S12** | Privacy policy / Play Store data safety / store assets | Pending |
| **S13** | Internal testing release | Pending |

---

# S1 – Documentation Refresh
**Status:** Done (Jun 2026)

---

# S2 – Controlled Deep Code Review + Selective Refactor
**Status:** Done (Jun 2026)  
**Commit:** `cc6e8f5`

**Validated:** `./gradlew assembleDebug`, `./gradlew lintDebug`, `./gradlew test`.

---

# S3 – Responsive / Design Safety Verification
**Status:** Done for targeted refactor scope (Jun 2026)

---

# S4 – User-Facing Export: Invoice-List CSV
**Status:** Done (Jun 2026)  
**Commit:** `0819b53`

## Delivered
- “Export” in invoice list overflow menu
- Exports **currently visible** invoices after category scope, search, filters, and sort
- Localized CSV via Storage Access Framework (`text/csv`)
- English/Hebrew headers and display values; category custom field titles as column headers
- `InvoiceCsvExporter`, `InvoiceCsvExportLabels`, `Utf8CsvWriter` (conditional UTF-8 BOM)
- Pure Kotlin export utilities; file I/O in composable layer (not `AndroidViewModel`)

## Known limitation
- Google Sheets **Android** may misread valid UTF-8 when headers are English and row data is Hebrew. LibreOffice and desktop Google Sheets open files correctly. Do not add CSV encoding hacks; consider XLSX later.

---

# S4b – User-Facing Export: Category-List All-Data ZIP
**Status:** Done (Jun 2026)  
**Commit:** `36ddae4` (+ follow-up refinements)

## Delivered
- “Export all data” / “ייצוא כל הנתונים” in category list overflow menu
- ZIP via SAF (`application/zip`), suggested name `tax_tracker_all_data_export_YYYY-MM-DD.zip`
- `categories.csv`: name, description, order, custom field title columns (no color column)
- `invoices/<categoryName>_<id>.csv` — **only** for categories with at least one invoice
- Readable filenames preserve Hebrew/English; sanitize unsafe path characters only
- Reuses invoice CSV export labels, escaping, and UTF-8 writer

## Explicitly not in scope
- Backup / restore
- Room schema changes
- Changing invoice-list export behavior

---

# S5 – Backup Export Planning
**Status:** Next recommended phase  
**Goal:** Design restore-safe export separate from user-facing CSV/ZIP.

## Checklist
- [ ] Define JSON schema (categories, invoices, metadata, version)
- [ ] Document replace vs merge restore semantics for S7–S8
- [ ] Keep raw enum/storage values (not localized display strings)
- [ ] Decide ZIP packaging and file naming

---

# S6 – Backup Export Implementation
**Goal:** User-controlled local backup storable anywhere (Drive, PC, etc.).

## Checklist
- [ ] Implement JSON export via SAF
- [ ] Version field inside backup payload
- [ ] Test backup creation and file sharing
- [ ] Do not conflate with S4/S4b user-facing CSV export

---

# S7 – Restore Planning and Safety Design
**Goal:** Safe replace-existing-data restore UX before implementation.

## Checklist
- [ ] Confirm replace-all vs selective restore for MVP
- [ ] Warning dialogs and confirmation copy (EN + HE)
- [ ] Validation rules for malformed/partial files

---

# S8 – Restore Implementation
**Goal:** Import backup with replace-existing-data behavior.

## Checklist
- [ ] Parse and validate backup JSON
- [ ] Transactional replace of categories + invoices
- [ ] Error handling and user feedback

---

# S9 – Automated Testing: Export / Backup / Restore Integrity
**Goal:** Small, high-value safety net before launch.

## Checklist
- [ ] CSV escaping and ZIP structure tests (partially started: `AllDataZipExporterTest`, `InvoiceCsvExporterTest`)
- [ ] Backup JSON round-trip tests
- [ ] Custom-field index alignment invariant tests
- [ ] Room migration smoke tests (optional stretch)

---

# S10 – CI Workflow (GitHub Actions)
**Goal:** Every push/PR runs a minimal quality gate.

## Checklist
- [ ] Add GitHub Actions workflow
- [ ] Run `./gradlew test` and `./gradlew lint` on PR + main

---

# S11 – App Icon and Release Identity
## Checklist
- [ ] App icon and adaptive icon
- [ ] Versioning (`versionCode` / `versionName`)

---

# S12 – Privacy Policy / Play Store Data Safety / Store Assets
## Checklist
- [ ] Privacy policy URL
- [ ] Data Safety form
- [ ] Store listing screenshots and description

---

# S13 – Internal Testing Release
## Checklist
- [ ] Play App Signing
- [ ] Internal testing track upload
- [ ] Final manual QA pass

---

## Public-facing mapping (README roadmap)
- S1–S4b → Done (docs, refactor, UI polish, user-facing export)
- S5–S6 → Backup export (JSON/ZIP)
- S7–S8 → Restore
- S9 → Tests
- S10 → CI
- S11–S13 → Play release

---

## Follow-up cleanup (non-blocking for launch)

| Item | Notes |
|------|--------|
| Deprecated `menuAnchor()` | Material3 API migration when convenient |
| Deprecated `Locale(String)` | Prefer `Locale.forLanguageTag` where touched |
| Deprecated `LocalLifecycleOwner` import | Update to lifecycle-compose artifact API |
| Dependency / version catalog warnings | Gradle housekeeping |
| Portrait-only orientation lint | Intentional for MVP |
| Unused resource warnings | Lint cleanup pass later |
| ViewModel holding `Context` | Possible future refactor; works today |
| Google Sheets Android CSV limitation | Known; do not add CSV hacks — XLSX later if needed |

---

## Running notes / decisions log

- **2026-02-10:** Plan structure created.
- **2026-05-31:** UI polish phase marked complete.
- **2026-06-01:** S1–S3 complete. Pre-launch refactor (`cc6e8f5`). **Next: export.**
- **2026-06-02:** S4 + S4b complete (invoice CSV + all-data ZIP). UI polish items landed (`9189ace`–`df3d14e`). **Next: S5 backup export planning.**

---

## Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` is a snapshot and may be outdated. Regenerate from `PROJECT_OVERVIEW.md` / `ai-context.md` when a PDF is needed — do not edit the PDF directly.

# Tax Tracker – LAUNCH_PLAN.md
_Last updated: 2026-06-11_

This document is the **single source of truth** for the pre-release execution plan.
It is written for the developer and for AI assistants so context survives long deep-dives.

## How we use this plan
- Work in order, one step at a time.
- Refer to steps by ID in chat (e.g. **“Next: S9”**).
- When reality changes, update this file — do not rely on chat memory.

### Definition of “Done”
A step is **Done** when:
- the deliverable is implemented (or the doc task is written),
- manually sanity-tested where applicable,
- and committed with a clear message.

### Export vs backup vs restore (product distinction)
| | **Export** | **Backup** | **Restore** |
|---|---|---|---|
| Purpose | Human-readable spreadsheets / records | Restore-ready app data snapshot | Full replacement of local app data |
| Format | Localized CSV; ZIP with CSV files | ZIP with `backup.json` | Reads backup ZIP / `backup.json` |
| Menu | Invoice list Export; Category list Export all data | Category list Create backup | Category list Restore backup |
| Restore | **Not supported** | N/A (creates file) | **Full replace only** — not merge |
| CSV accepted? | N/A | No | **No** — CSV/ZIP exports cannot be restored |

---

## Completed work (through Jun 2026)

The following major areas are **implemented** — not pending:

| Area | Status |
|------|--------|
| Room persistence (v14, incremental migrations) | Done |
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
| **Backup export planning (S5)** | **Done** |
| **Backup export implementation (S6)** | **Done** (`37ff651`) |
| **Restore planning and safety design (S7)** | **Done** |
| **Restore implementation (S8)** | **Done** (`73b7bd6`) |

---

## High-level execution order (what remains)

```
S9 → S10 → S11 → S12 → S13
```

| Step | Focus | Status |
|------|--------|--------|
| **S1** | Documentation refresh | **Done** |
| **S2** | Controlled deep code review + selective refactor | **Done** |
| **S3** | Responsive / design safety verification | **Done** (targeted) |
| **S4** | User-facing CSV export (invoice list) | **Done** |
| **S4b** | User-facing all-data ZIP export (category list) | **Done** |
| **S5** | Backup export planning | **Done** |
| **S6** | Backup export implementation (JSON/ZIP) | **Done** |
| **S7** | Restore planning and safety design | **Done** |
| **S8** | Restore implementation (replace existing data) | **Done** |
| **S9** | Tests for backup / restore / export integrity | **Next** |
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
- Changing invoice-list export behavior

---

# S5 – Backup Export Planning
**Status:** Done (Jun 2026)

## Delivered
- JSON schema defined: `BackupPayload` with `formatVersion`, `metadata`, `categories`, `invoices`
- Replace-all restore semantics documented for S7–S8
- Raw enum/storage values and ISO dates (not localized display strings)
- ZIP packaging: single `backup.json` entry

---

# S6 – Backup Export Implementation
**Status:** Done (Jun 2026)  
**Commit:** `37ff651`

## Delivered
- “Create backup” in category list overflow menu
- ZIP via SAF containing `backup.json` with versioned restore-oriented payload
- `BackupZipExporter`, `BackupMapper`, `BackupFormat`, `BackupDtos` in `core/util/backup/`
- `CategoryListViewModel.loadAllDataForBackup()` supplies domain data
- Unit tests: `BackupZipExporterTest`

## Explicitly not in scope
- Conflating with S4/S4b user-facing CSV export
- Encryption or cloud sync

---

# S7 – Restore Planning and Safety Design
**Status:** Done (Jun 2026)

## Delivered
- Full replace (not merge) confirmed for MVP
- UX flow: pick file → validate → destructive confirmation → restore
- Validation rules: format version, required fields, unique IDs, enum/date checks, orphan invoice rejection
- Seeding flag handling after successful restore documented

---

# S8 – Restore Implementation
**Status:** Done (Jun 2026)  
**Commit:** `73b7bd6`

## Delivered
- “Restore backup” in category list overflow menu
- `BackupZipImporter` reads ZIP and parses `backup.json`
- `BackupValidator` validates payload before any DB changes
- Destructive confirmation dialog (EN + HE) shown only after validation succeeds
- `RoomBackupRestoreRepository` performs transactional delete + insert via `withTransaction`
- Preserves original category and invoice IDs
- Sets `has_seeded_default_categories` and `has_cleared_seeded_custom_fields` after successful restore
- Language preference not modified
- Unit tests: `BackupZipImporterTest`, `BackupValidatorTest`, `BackupMapperTest`

## Manual QA checklist (S8)
- [x] Create backup from populated app
- [x] Modify app data, restore backup, confirm previous state returns
- [x] Cancel at file picker — no change, no snackbar
- [x] Cancel at confirmation dialog — no change, no snackbar
- [x] Invalid file (non-ZIP) → error snackbar
- [x] Wrong ZIP (no `backup.json`) → error snackbar
- [x] Unsupported `formatVersion` → error snackbar
- [x] CSV all-data ZIP export → restore correctly rejected
- [x] Hebrew/English UI labels and restored data round-trip
- [x] Export features still work after restore

---

# S9 – Automated Testing: Export / Backup / Restore Integrity
**Status:** Next recommended phase  
**Goal:** Expand automated safety net before launch.

## Checklist
- [x] Backup JSON round-trip tests (`BackupZipExporterTest`, `BackupMapperTest`)
- [x] Backup import/validation tests (`BackupZipImporterTest`, `BackupValidatorTest`)
- [ ] CSV escaping and ZIP structure tests (partially started: `AllDataZipExporterTest`, `InvoiceCsvExporterTest`)
- [ ] Custom-field index alignment invariant tests
- [ ] Room migration smoke tests (optional stretch)
- [ ] Instrumented restore transaction test (optional stretch)

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
- [ ] Privacy policy URL (mention local-only storage, user-controlled export/backup)
- [ ] Data Safety form
- [ ] Store listing screenshots and description

---

# S13 – Internal Testing Release
## Checklist
- [ ] Play App Signing
- [ ] Internal testing track upload
- [ ] Final manual QA pass (export + backup + restore regression)

---

## Public-facing mapping (README roadmap)
- S1–S8 → Done (docs, refactor, UI polish, export, backup, restore)
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
- **2026-06-11:** S5–S8 complete. Backup creation (`37ff651`) and full-replace restore (`73b7bd6`). Room v14 (FK index on `invoices.categoryId`). **Next: S9 tests, S10 CI, S11–S13 release readiness.**

---

## Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` is a snapshot and may be outdated. Regenerate from `PROJECT_OVERVIEW.md` / `ai-context.md` when a PDF is needed — do not edit the PDF directly.

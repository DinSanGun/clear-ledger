# Tax Tracker (Android) — AI Context (Cursor)

_Last updated: 2026-06-14_

Concise working context for AI-assisted development. For the full overview see `docs/PROJECT_OVERVIEW.md`; for architecture patterns see `docs/ARCHITECTURE.md`; for release planning see `docs/LAUNCH_PLAN.md` and `docs/RELEASE.md`.

---

## 1) What this app is

Local-first Android app for tracking **bills / taxes by category**. Users manage categories (color, custom field titles, order), then add/edit/view/delete invoices per category with search, filter, and sort.

Supports **Hebrew and English** with manual language switching and RTL/LTR layout.

**Data portability (all implemented):**
- **Export** — localized CSV/ZIP for humans/spreadsheets (not for restore)
- **Backup** — restore-ready ZIP with `backup.json`
- **Restore** — full replace of local app data from a backup ZIP

---

## 2) Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- MVVM: ViewModels + `StateFlow`, lifecycle-aware collection
- Room (SQLite) v14 — `RoomCategoryRepository`, `RoomInvoiceRepository`, `RoomBackupRestoreRepository`
- No DI framework; repos wired in `MainActivity` / ViewModel factories
- Export/backup: pure Kotlin in `core/util/` and `core/util/backup/`; SAF + file I/O in Compose screens
- Gradle KTS with version catalog (`libs.*`)

---

## 3) Key files (read these first)

| File | Why |
|------|-----|
| `MainActivity.kt` | DB init, seeding, locale, repository wiring |
| `core/ui/Navigation.kt` | All routes, ViewModel scoping, nav args |
| `core/domain/Models.kt` | Domain contracts and invariants |
| `core/data/TaxTrackerDatabase.kt` | Migrations — **do not change casually** |
| `feature/invoice/InvoiceListViewModel.kt` | Search/filter/sort pipeline; `buildCsvContent()` |
| `feature/invoice/InvoiceListScreen.kt` | List UI, filter sheet, invoice CSV export SAF |
| `feature/category/CategoryListViewModel.kt` | Category CRUD, reorder, export/backup/restore |
| `feature/category/CategoryListScreen.kt` | Category list UI, export/backup/restore SAF |
| `core/util/InvoiceCsvExporter.kt` | Pure Kotlin invoice CSV generation |
| `core/util/AllDataZipExporter.kt` | Pure Kotlin ZIP (categories.csv + invoice CSVs) |
| `core/util/backup/BackupZipExporter.kt` | Pure Kotlin backup ZIP writer |
| `core/util/backup/BackupZipImporter.kt` | Pure Kotlin backup ZIP reader |
| `core/util/backup/BackupValidator.kt` | Defensive backup payload validation |
| `core/util/backup/BackupMapper.kt` | Domain ↔ backup DTO mapping |
| `core/data/repositories/RoomBackupRestoreRepository.kt` | Transactional full-replace restore |
| `core/data/SeedingPreferenceManager.kt` | Seeding flags updated after restore |

---

## 4) Architecture in one paragraph

Compose screens render immutable UI state and forward intents. ViewModels own state, call repositories in `viewModelScope`, and expose `StateFlow`. Repositories map Room entities to domain models. Navigation Compose defines routes; related screens share a ViewModel via parent `NavBackStackEntry`. Export/backup read domain data through ViewModels; CSV/ZIP/JSON bytes written or read via SAF in the screen layer. Restore validates backup JSON before any DB mutation, then `RoomBackupRestoreRepository` replaces all data in a single transaction.

---

## 5) Domain invariants — preserve these

### Custom fields
- Categories store `customFieldTitles: List<String>` (max 10, JSON in DB)
- Invoices store `customFieldValues: List<String>` aligned **by index**
- **Do not** filter blank values in ways that shift indices; **do not** reorder values independently of titles

### Service period
- `ServicePeriodMode` (`MONTH` | `DATE`) is the **explicit source of truth**
- **Never** infer mode from stored dates alone

### Category ordering
- Sort categories by `orderIndex`, not name
- Reorder persisted via `CategoryRepository.updateCategoryOrder(orderedIds)`
- Preserve `isReorderMode` on category `refresh()`

### Category update (hidden persisted fields)
- **`RoomCategoryRepository.updateCategory`** must preserve `supplierName` and `pinnedDefaultsJson` from existing DB row

### Currency
- `InvoiceCurrency` (ILS / USD) is **display metadata only** — amounts not converted

### Seeded categories
- `seedKey` identifies built-in categories; `userEdited` blocks locale overwrite
- After restore, seeding flags are set so first-run seeding does not duplicate restored data

### Localization / RTL
- Language preference persisted; locale applied in `attachBaseContext`
- Hebrew resources in `values-iw/`; test RTL after UI changes
- Read locale via **`LocalConfiguration.current`**, not `LocalContext.current.resources.configuration`
- Export headers follow **app locale only** (English or Hebrew) — **no bilingual headers**, no encoding hacks
- Restore does **not** modify language preference

### Export vs backup vs restore
- **Export** = localized CSV/ZIP for humans/spreadsheets — **not for restore**
- **Backup** = raw restore-ready JSON in ZIP (`backup.json`) — plaintext, sensitive
- **Restore** = full replace only from backup ZIP — **not** from CSV exports

### UI text display
- **List cards:** ellipsis / `maxLines` acceptable
- **Details screens:** full information; natural wrapping

---

## 6) Invoice list pipeline

```
sourceInvoices → search → service-period filter → status filter → sort → visibleInvoices
```

Invoice CSV export uses **`visibleInvoices`** (and category name/titles from UiState) — do not change export scope without explicit product request.

---

## 7) Export behavior (do not regress)

### Invoice-list CSV (`InvoiceListScreen`)
- Overflow → Export; SAF `text/csv`
- `InvoiceListViewModel.buildCsvContent(labels)` → `InvoiceCsvExporter.generate()`
- `Utf8CsvWriter.writeUtf8CsvWithBom` on output stream

### Category-list ZIP (`CategoryListScreen`)
- Overflow → Export all data; SAF `application/zip`
- `CategoryListViewModel.loadAllDataForExport()` → `AllDataZipExporter.writeZip()`
- ZIP: `categories.csv` (name, description, order, custom field titles — **no color**)
- `invoices/<sanitizedName>_<id>.csv` only when category has ≥1 invoice
- Filenames: preserve Hebrew/English; replace only unsafe path chars (`/ \ : * ? " < > |`, controls); max ~60 chars + `_<id>`

### UTF-8 / BOM
- BOM per **CSV entry** when content has non-ASCII (`Utf8CsvWriter`) — not at ZIP level
- Do not change invoice-list export encoding without explicit request

### Google Sheets Android
- Known limitation: may misread valid UTF-8 CSV (English headers + Hebrew data). **Do not add CSV encoding hacks.** LibreOffice / desktop Sheets are the target. XLSX is a possible future improvement.

---

## 8) Backup and restore behavior (do not regress)

### Create backup (`CategoryListScreen`)
- Overflow → Create backup; SAF `application/zip`
- `CategoryListViewModel.loadAllDataForBackup()` → `BackupZipExporter.writeZip()`
- ZIP contains single `backup.json` with `formatVersion`, metadata, categories, invoices
- Stores raw enum names, ISO dates, explicit nulls, IDs, order, custom fields — **not** localized display strings

### Restore backup (`CategoryListScreen`)
- Overflow → Restore backup; SAF `OpenDocument` for `application/zip`
- `CategoryListViewModel.validateAndParseBackup(uri)` → `BackupZipImporter` + `BackupValidator`
- If valid: show destructive confirmation dialog; on confirm → `performRestore()` → `RoomBackupRestoreRepository.restoreFromBackup()`
- **Full replace only** — not merge; validation before delete; transaction rolls back on failure
- Preserves original category and invoice IDs
- Sets seeding flags after success; does not touch language preference
- **CSV/ZIP exports are rejected** — only backup ZIPs with `backup.json`

---

## 9) What NOT to change casually

| Area | Guidance |
|------|----------|
| **Room schema / migrations** | Avoid unless explicitly requested |
| **Custom field index alignment** | High risk of silent misalignment |
| **ServicePeriodMode semantics** | Core invariant |
| **Category orderIndex sorting** | Name-based sort breaks user order |
| **Hidden category fields on update** | Round-trip `supplierName` / `pinnedDefaults` |
| **Export scope & format** | Invoice export = visible only; ZIP skips empty invoice CSVs |
| **Backup format / restore semantics** | Full replace; validate before delete; preserve IDs |
| **Localization** | Both `values/` and `values-iw/` |

Ask before: DB migrations, conflating export with backup, allowing CSV restore, bilingual CSV headers, changing restore to merge mode.

---

## 10) Recent completed work (Jun 2026)

**Pre-launch refactor** (`cc6e8f5`): debug log removal; hidden category fields; invoice errors; reorder on refresh; list fixes; Hebrew strings; build/lint/test green.

**UI polish** (`9189ace`–`df3d14e`): FAB overlap; edit-category save/discard; invoice list top bar; filter indication; custom field UX.

**Export** (`0819b53`, `36ddae4`): invoice CSV + all-data ZIP.

**Backup** (`37ff651`): create restore-ready backup ZIP with `backup.json`.

**Restore** (`73b7bd6`): full-replace restore with validation, transaction, ID preservation, seeding flag handling.

---

## 11) Project status (Jun 2026)

**Done:** Room, bilingual UI, custom fields, search/filter/sort, service period, category reorder, UI polish, pre-launch refactor, user-facing export (CSV + ZIP), backup creation, full-replace restore.

**Pre-release (priority order — see `LAUNCH_PLAN` S9–S17):**
1. **S9** — Targeted test hardening (custom fields, CSV, backup/restore, EN/HE export)
2. **S10** — CI (`test`, `lintDebug`, `assembleDebug`) — *planned, not yet added*
3. **S11** — Release polish (export/backup/restore UX, optional About)
4. **S12** — Project docs (README, `ARCHITECTURE.md`, `RELEASE.md`)
5. **S13** — Release identity (name, package ID, version, icon, signing)
6. **S14** — Privacy policy and Play Store materials
7. **S15** — Internal Play testing
8. **S16** — Launch blocker fixes only
9. **S17** — Production release + GitHub/interview presentation

**Not implemented:** CI workflow file, Play production release, cloud sync, encryption, automatic backup, selective merge restore.

---

## 12) Non-blocking follow-ups

- Deprecated `menuAnchor()`, `Locale(String)`, `LocalLifecycleOwner`
- Dependency/version warnings, unused resources, portrait lint
- ViewModel `Context` usage — optional cleanup
- Future XLSX export for Google Sheets Android (not CSV hacks)
- Instrumented restore transaction test (optional)

---

## 13) Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` may be outdated. Treat `PROJECT_OVERVIEW.md` and this file as current until the PDF is regenerated.

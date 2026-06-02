# Tax Tracker (Android) — AI Context (Cursor)

_Last updated: 2026-06-02_

Concise working context for AI-assisted development. For the full overview see `docs/PROJECT_OVERVIEW.md`; for release planning see `docs/LAUNCH_PLAN.md`.

---

## 1) What this app is

Local-first Android app for tracking **bills / taxes by category**. Users manage categories (color, custom field titles, order), then add/edit/view/delete invoices per category with search, filter, and sort.

Supports **Hebrew and English** with manual language switching and RTL/LTR layout.

**User-facing export** (CSV/ZIP via SAF) is implemented. **Backup/restore** (restore-safe JSON) is **not** implemented yet.

---

## 2) Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- MVVM: ViewModels + `StateFlow`, lifecycle-aware collection
- Room (SQLite) v13 — `RoomCategoryRepository`, `RoomInvoiceRepository`
- No DI framework; repos wired in `MainActivity` → `TaxTrackerNavHost`
- Export: pure Kotlin in `core/util/`; SAF + file I/O in Compose screens
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
| `feature/category/CategoryListViewModel.kt` | Category CRUD, reorder, `loadAllDataForExport()` |
| `feature/category/CategoryListScreen.kt` | Category list UI, all-data ZIP export SAF |
| `core/util/InvoiceCsvExporter.kt` | Pure Kotlin invoice CSV generation |
| `core/util/AllDataZipExporter.kt` | Pure Kotlin ZIP (categories.csv + invoice CSVs) |
| `core/util/Utf8CsvWriter.kt` | UTF-8 + conditional BOM per CSV |
| `feature/invoice/InvoiceCsvExportLabelsProvider.kt` | Composable localized export labels |
| `core/data/repositories/RoomCategoryRepository.kt` | Category persistence; hidden fields on update |
| `core/data/repositories/RoomInvoiceRepository.kt` | Invoice entity ↔ domain mapping |

---

## 4) Architecture in one paragraph

Compose screens render immutable UI state and forward intents. ViewModels own state, call repositories in `viewModelScope`, and expose `StateFlow`. Repositories map Room entities to domain models. Navigation Compose defines routes; related screens share a ViewModel via parent `NavBackStackEntry`. Export reads domain data through repositories/ViewModels; CSV/ZIP bytes written via SAF in the screen layer.

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

### Localization / RTL
- Language preference persisted; locale applied in `attachBaseContext`
- Hebrew resources in `values-iw/`; test RTL after UI changes
- Read locale via **`LocalConfiguration.current`**, not `LocalContext.current.resources.configuration`
- Export headers follow **app locale only** (English or Hebrew) — **no bilingual headers**, no encoding hacks

### Export vs backup
- **Export** = localized CSV/ZIP for humans/spreadsheets — **implemented**
- **Backup** = raw, restore-safe JSON/ZIP — **not implemented** — do not label export as backup

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

## 8) What NOT to change casually

| Area | Guidance |
|------|----------|
| **Room schema / migrations** | Avoid unless explicitly requested |
| **Custom field index alignment** | High risk of silent misalignment |
| **ServicePeriodMode semantics** | Core invariant |
| **Category orderIndex sorting** | Name-based sort breaks user order |
| **Hidden category fields on update** | Round-trip `supplierName` / `pinnedDefaults` |
| **Export scope & format** | Invoice export = visible only; ZIP skips empty invoice CSVs |
| **Localization** | Both `values/` and `values-iw/` |
| **Architecture** | No DI, no broad rewrites |

Ask before: DB migrations, conflating export with backup, bilingual CSV headers, changing invoice-list export.

---

## 9) Recent completed work (Jun 2026)

**Pre-launch refactor** (`cc6e8f5`): debug log removal; hidden category fields; invoice errors; reorder on refresh; list fixes; Hebrew strings; build/lint/test green.

**UI polish** (`9189ace`–`df3d14e`): FAB overlap; edit-category save/discard; invoice list top bar; filter indication; custom field UX.

**Export** (`0819b53`, `36ddae4`): invoice CSV + all-data ZIP.

---

## 10) Project status (Jun 2026)

**Done:** Room, bilingual UI, custom fields, search/filter/sort, service period, category reorder, UI polish, pre-launch refactor, **user-facing export (CSV + ZIP)**.

**Next (see LAUNCH_PLAN):**
1. **S5** — Backup export planning (JSON schema, versioning)
2. **S6** — Backup export implementation
3. **S7–S8** — Restore design + implementation (replace existing data)
4. **S9** — Backup/restore/export integrity tests
5. **S10** — CI
6. **S11–S13** — Play release assets, privacy, internal testing

**Not implemented:** backup, restore, CI, Play Store assets.

---

## 11) How to work on backup (next major feature)

1. **Separate** from `InvoiceCsvExporter` / `AllDataZipExporter` — new raw JSON serializer
2. Use stable machine values (enum names, raw dates), not localized CSV strings
3. SAF for file picker; no new storage permissions for MVP
4. Keep Room schema unchanged unless explicitly requested; plan migrations if backup format requires DB changes
5. Restore: replace-existing-data UX with strong confirmation — design in S7 before coding S8

---

## 12) Non-blocking follow-ups

- Deprecated `menuAnchor()`, `Locale(String)`, `LocalLifecycleOwner`
- Dependency/version warnings, unused resources, portrait lint
- ViewModel `Context` usage — optional cleanup
- Future XLSX export for Google Sheets Android (not CSV hacks)

---

## 13) Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` may be outdated. Treat `PROJECT_OVERVIEW.md` and this file as current until the PDF is regenerated.

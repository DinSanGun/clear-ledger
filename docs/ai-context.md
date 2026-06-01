# Tax Tracker (Android) — AI Context (Cursor)

_Last updated: 2026-06-01_

Concise working context for AI-assisted development. For the full overview see `docs/PROJECT_OVERVIEW.md`; for release planning see `docs/LAUNCH_PLAN.md`.

---

## 1) What this app is

Local-first Android app for tracking **bills / taxes by category**. Users manage categories (color, custom field titles, order), then add/edit/view/delete invoices per category with search, filter, and sort.

Supports **Hebrew and English** with manual language switching and RTL/LTR layout.

---

## 2) Tech stack

- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- MVVM: ViewModels + `StateFlow`, lifecycle-aware collection
- Room (SQLite) v13 — `RoomCategoryRepository`, `RoomInvoiceRepository`
- No DI framework; repos wired in `MainActivity` → `TaxTrackerNavHost`
- Gradle KTS with version catalog (`libs.*`)

---

## 3) Key files (read these first)

| File | Why |
|------|-----|
| `MainActivity.kt` | DB init, seeding, locale, repository wiring |
| `core/ui/Navigation.kt` | All routes, ViewModel scoping, nav args |
| `core/domain/Models.kt` | Domain contracts and invariants |
| `core/data/TaxTrackerDatabase.kt` | Migrations — **do not change casually** |
| `feature/invoice/InvoiceListViewModel.kt` | Search/filter/sort pipeline |
| `feature/invoice/InvoiceListScreen.kt` | List UI, filter sheet, sort menu |
| `feature/category/CategoryListViewModel.kt` | Category CRUD + reorder |
| `feature/category/CategoryForm.kt` | Shared add/edit category form |
| `core/data/repositories/RoomCategoryRepository.kt` | Category persistence + locale sync; **category update preserves hidden fields** |
| `core/data/repositories/RoomInvoiceRepository.kt` | Invoice entity ↔ domain mapping |

---

## 4) Architecture in one paragraph

Compose screens render immutable UI state and forward intents. ViewModels own state, call repositories in `viewModelScope`, and expose `StateFlow`. Repositories map Room entities to domain models. Navigation Compose defines routes; related screens share a ViewModel via parent `NavBackStackEntry`.

---

## 5) Domain invariants — preserve these

### Custom fields
- Categories store `customFieldTitles: List<String>` (max 10, JSON in DB)
- Invoices store `customFieldValues: List<String>` aligned **by index**
- **Do not** filter blank values in ways that shift indices; **do not** reorder values independently of titles

### Service period
- `ServicePeriodMode` (`MONTH` | `DATE`) is the **explicit source of truth**
- **Never** infer mode from stored dates alone
- MONTH mode snaps to first/last day of selected months

### Category ordering
- Sort categories by `orderIndex`, not name
- Reorder persisted via `CategoryRepository.updateCategoryOrder(orderedIds)`

### Category update (hidden persisted fields)
- Edit form only exposes name, color, description, custom field titles
- **`RoomCategoryRepository.updateCategory`** must preserve `supplierName` and `pinnedDefaultsJson` from the existing DB row
- Do not pass `pinnedDefaults = emptyMap()` from the ViewModel on update

### Currency
- `InvoiceCurrency` (ILS / USD) is **display metadata only**
- Amounts are **not converted** between currencies

### Seeded categories
- `seedKey` identifies built-in categories; `userEdited` blocks locale overwrite

### Localization / RTL
- Language preference persisted; locale applied in `attachBaseContext`
- Hebrew resources in `values-iw/`; test RTL layout after UI changes
- Read locale via **`LocalConfiguration.current`**, not `LocalContext.current.resources.configuration`

### UI text display
- **List cards:** ellipsis / `maxLines` acceptable for density (e.g. category name on list)
- **Details screens:** show full information; prefer natural wrapping; do not blanket-apply ellipsis to invoice number, custom field values, or notes

---

## 6) Invoice list pipeline

In `InvoiceListViewModel`:
```
sourceInvoices → search → service-period filter → status filter → sort → visibleInvoices
```
All filter/search/sort controls must funnel through the centralized recompute path — do not duplicate filtering in composables.

Invoice `addInvoice` / `updateInvoice` / `deleteInvoice` wrap repository calls in try/catch and set `errorMessage` on failure.

---

## 7) Current UX decisions

- **Picker-first dates** — tap date row to open picker; avoid free-text date entry
- **Validation on save** — scroll to first invalid field + snackbar message
- **Swipe-to-dismiss snackbars** — `SwipeDismissSnackbarHost`
- **Dropdown positioning** — use `requestAnchoredDropdownExpansion` / `DropdownPositioning` (requires `@OptIn(ExperimentalFoundationApi::class)` where `BringIntoViewRequester` is used)
- **Category delete/edit** — card overflow menu (not standalone delete icon)
- **Category reorder** — dedicated reorder mode with animated list moves; preserve `isReorderMode` on `refresh()`
- **Confirmation dialogs** — required before delete actions
- **Portrait lock** — app is portrait-optimized

---

## 8) What NOT to change casually

| Area | Guidance |
|------|----------|
| **Room schema / migrations** | Avoid unless explicitly requested; test thoroughly |
| **Entity ↔ domain mapping** | Preserve backward-compat fields and converters |
| **Custom field index alignment** | High risk of silent data misalignment |
| **ServicePeriodMode semantics** | Explicit mode is a core invariant |
| **Category orderIndex sorting** | Name-based sort breaks user order across locales |
| **Hidden category fields on update** | Always round-trip `supplierName` / `pinnedDefaults` |
| **Architecture** | No DI framework, no use-case layer, no broad rewrites |
| **Localization** | Don't hardcode strings; update both `values/` and `values-iw/` |

Ask before:
- Adding DB migrations or changing persisted schema
- Refactoring across many files / changing architecture
- Changing user-visible behavior on core invariants above

---

## 9) Recent pre-launch refactor (Jun 2026)

Conservative Claude/Sonnet review pass; commit `cc6e8f5`. **No** schema migration, **no** architecture rewrite, **no** broad UI redesign.

**Done:** debug log removal; category hidden-field preservation; invoice mutation error handling; reorder mode on refresh; list-card ellipsis; payment status layout; LazyColumn keys; Hebrew `all`/`sort_by`; dead code cleanup; `LocalConfiguration` + Foundation opt-ins.

**Validated:** manual QA; `assembleDebug`, `lintDebug`, `test`.

**Deferred (non-blocking):** deprecated APIs, dependency warnings, unused resources, ViewModel context cleanup.

---

## 10) Project status (Jun 2026)

**Done:** Room, bilingual UI, custom fields, search/filter/sort, service period, category reorder, UI polish, pre-launch safety refactor.

**Next (see LAUNCH_PLAN):**
1. **CSV export** — visible/filtered invoices in current category (first slice)
2. Full **JSON backup** export
3. **JSON restore** (replace existing data)
4. Export/import tests + CI + Play assets

**Not implemented:** export, backup, restore, CI, Play Store assets.

---

## 11) How to work on export (next feature)

1. Start from `InvoiceListViewModel.visibleInvoices` and category header state — export what the user sees after search/filter/sort unless product says otherwise
2. Use Storage Access Framework (SAF) for file picker; no new permissions for MVP
3. Define CSV headers: core invoice columns + dynamic custom field columns by category titles
4. Keep Room schema unchanged; export reads via repositories
5. Small PRs: schema/serializer → export use case → UI entry point → tests

---

## 12) Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` may be outdated. Treat `PROJECT_OVERVIEW.md` and this file as current until the PDF is regenerated.

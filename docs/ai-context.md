# Tax Tracker (Android) — AI Context (Cursor)

_Last updated: 2026-05-31_

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
| `core/data/repositories/RoomCategoryRepository.kt` | Category persistence + locale sync |
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

### Currency
- `InvoiceCurrency` (ILS / USD) is **display metadata only**
- Amounts are **not converted** between currencies

### Seeded categories
- `seedKey` identifies built-in categories; `userEdited` blocks locale overwrite

### Localization / RTL
- Language preference persisted; locale applied in `attachBaseContext`
- Hebrew resources in `values-iw/`; test RTL layout after UI changes

---

## 6) Invoice list pipeline

In `InvoiceListViewModel`:
```
sourceInvoices → search → service-period filter → status filter → sort → visibleInvoices
```
All filter/search/sort controls must funnel through the centralized recompute path — do not duplicate filtering in composables.

---

## 7) Current UX decisions

- **Picker-first dates** — tap date row to open picker; avoid free-text date entry
- **Validation on save** — scroll to first invalid field + snackbar message
- **Swipe-to-dismiss snackbars** — `SwipeDismissSnackbarHost`
- **Dropdown positioning** — use `DropdownPositioning` helper so menus don't cover anchors
- **Category delete/edit** — card overflow menu (not standalone delete icon)
- **Category reorder** — dedicated reorder mode with animated list moves
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
| **Architecture** | No DI framework, no use-case layer, no broad rewrites |
| **Localization** | Don't hardcode strings; update both `values/` and `values-iw/` |

---

## 9) How to work in this repo

1. List exact files to modify and why before editing
2. Prefer the **smallest diff** that satisfies the requirement
3. Match existing naming, patterns, and validation style
4. Search for existing patterns (dialogs, snackbars, form validation) before inventing new ones
5. After UI changes, consider RTL and both locales
6. Ensure code compiles; update imports as needed

Ask before:
- Adding DB migrations or changing persisted schema
- Refactoring across many files / changing architecture
- Changing user-visible behavior on core invariants above

---

## 10) Project status (May 2026)

**Done:** Room persistence, bilingual UI, custom fields, search/filter/sort, category reorder, service period mode, currency metadata, picker-first dates, major UI polish pass.

**Next (see LAUNCH_PLAN):**
1. Documentation refresh (this file)
2. Controlled selective refactor (behavior-preserving)
3. Responsive/design verification
4. CSV export → backup → tests → CI → Play release

**Not implemented:** export, backup, automated tests, CI, Play Store assets.

---

## 11) Cleanup reminders (pre-release)

- Remove temporary debug logs (e.g. locale tracing in `Navigation.kt`)
- Review large Compose files for selective extraction
- Review duplicate add/edit form logic
- Verify responsive layout on small/large screens

---

## 12) Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` may be outdated. Treat `PROJECT_OVERVIEW.md` and this file as current until the PDF is regenerated.

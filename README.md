# Tax Tracker (Android)

**Tax Tracker** is an Android app for structured tracking of invoices, bills, and tax-related expenses, organized by customizable categories and fields.

The app helps users keep financial documents consistent, searchable, and ready for export — with a focus on real-world tax and billing use cases (including Israel-specific fields).

---

## Features

### Category-Based Organization
- Expenses and invoices organized by categories with color theming
- Dynamic custom field definitions (up to 10 titles per category)
- Built-in seeded categories for common use cases (e.g. Electricity, Arnona, Water)
- Manual category reordering with persisted `orderIndex` (stable across locale changes)
- Category card overflow menu for edit, delete, and reorder actions

### Flexible Invoice Model
- Core fields: document number, amount, vendor, payment status, notes
- Category-specific custom field values (ordered list aligned to category titles)
- Explicit service period mode (`MONTH` or `DATE`) — never inferred from dates
- Separate payment date and due date from service period
- Document type, payment method (including custom “Other”), and credit payment count
- Currency stored and displayed as metadata (ILS / USD); amounts are not converted

### Invoice List Productivity
- Search by invoice number or amount with mode selector
- Filter by payment status and service period range (bottom sheet with Apply / Clear)
- Sort by date or amount (ascending / descending)
- Scope-aware retrieval pipeline: `sourceInvoices` → search → filter → sort → `visibleInvoices`

### Localization & Accessibility
- Full Hebrew and English support with manual language switching
- Persistent language preference and proper RTL / LTR layout handling
- Locale-aware seeded categories (user-edited seeded rows are protected from overwrite)

### Polished UI (Jetpack Compose)
- Category color theming with contrast-aware top app bars
- Picker-first date inputs with formatted display
- Form validation with scroll-to-first-invalid on save
- Swipe-to-dismiss snackbars and improved dropdown positioning
- Consistent invoice and category card layouts
- Edit Category top-bar Save action with discard warning for unsaved changes
- Invoice list active filter indication and clear-filters action
- Portrait-optimized layout

### Data Export (user-facing)
- **Invoice list:** export currently visible invoices (after search, filter, and sort) to localized CSV via Storage Access Framework
- **Category list:** export all data to a ZIP (`categories.csv` + one invoice CSV per category that has invoices)
- Localized English/Hebrew headers and display values; custom fields as CSV columns
- UTF-8 with conditional BOM for spreadsheet compatibility (LibreOffice, desktop Google Sheets)
- **Not** backup/restore — restore-safe JSON backup is planned separately

### Local Persistence
- Offline-first design with Room (SQLite), currently at schema version 13
- Incremental migrations for backward compatibility
- First-run seeding with idempotent preference flags

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Navigation:** Navigation Compose
- **Architecture:** MVVM-style separation (UI / ViewModel / Repository)
- **Persistence:** Room with entity ↔ domain mapping
- **State Management:** ViewModels + `StateFlow`, lifecycle-aware collection
- **Localization:** Android string resources with RTL support
- **Build System:** Gradle (KTS)

---

## Project Structure

The project follows a feature-oriented structure with a shared core layer.

- `core/domain/` — domain models and repository interfaces
- `core/data/` — Room entities, DAOs, converters, repositories
- `core/ui/` — navigation, shared UI helpers
- `feature/category/` — category list, add/edit, reorder
- `feature/invoice/` — invoice list, add/edit/details, search/filter/sort
- `feature/settings/` — language settings

Domain models and business concepts are separated from persistence and UI logic. Each feature encapsulates its screens and ViewModels; shared navigation and cross-cutting concerns live in `core/ui`.

---

## Roadmap

### Next (pre-release)
1. **Backup export** — restore-safe JSON/ZIP (separate from user-facing CSV export)
2. **Restore** — replace-existing-data import with safety design
3. **Tests** — backup/restore and export integrity coverage
4. **CI** — GitHub Actions for unit tests and lint
5. **Release readiness** — app icon, store assets, privacy policy, Play Store internal testing

### Recently completed
- UI polish pass (May–Jun 2026): category list FAB fix, edit-category save/discard, invoice list simplification, filter UX
- Pre-launch refactor / safety pass (Jun 2026)
- Invoice-list CSV export and category-list all-data ZIP export (Jun 2026)

### Not yet implemented
- Backup / restore (JSON)
- CI pipeline
- Google Play release

See `docs/LAUNCH_PLAN.md` for the detailed execution plan.

---

## Status

The app is under active development and nearing its first public release.

Core functionality is implemented: Room persistence, bilingual UI (Hebrew / English), invoice search/filter/sort, category reordering, service-period handling, dynamic custom fields, and **user-facing export** (localized CSV from the invoice list; ZIP with `categories.csv` and per-category invoice CSVs from the category list). A UI polish pass and conservative pre-launch refactor completed in June 2026 without architecture rewrites or database schema changes.

**Validated:** manual QA, `./gradlew assembleDebug`, `./gradlew lintDebug`, and `./gradlew test`.

---

## Documentation

| File | Purpose |
|------|---------|
| `docs/PROJECT_OVERVIEW.md` | Technical architecture overview |
| `docs/ai-context.md` | Concise context for AI-assisted development |
| `docs/CHANGELOG.md` | User-facing development history |
| `docs/LAUNCH_PLAN.md` | Pre-release execution plan |
| `docs/ARCHITECTURE_SUMMARY.pdf` | Historical architecture snapshot — may be outdated; regenerate from current docs when needed |

---

## Notes

This project emphasizes:
- clean data modeling with explicit domain invariants
- extensibility via dynamic custom fields
- practical, real-world billing scenarios
- maintainable Android architecture

---

## License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.

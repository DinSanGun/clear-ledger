# Tax Tracker - Project Overview

Technical overview of the Tax Tracker Android app for future development and interview context.  
For the pre-release execution plan, see `docs/LAUNCH_PLAN.md`. For AI-assisted work, see `docs/ai-context.md`.

> **Note:** `docs/ARCHITECTURE_SUMMARY.pdf` is a historical snapshot and may not reflect the current codebase. Prefer this file and `docs/ai-context.md` until the PDF is regenerated.

---

## A. App Purpose and Main User Flows

**Purpose:** Local-first Android app for managing bills and tax invoices by category. Users create categories (e.g. Electricity, Water, Arnona), assign colors and optional custom field schemas, then track invoices within each category.

**Main flows:**

1. **Category management**
   - Category list → add / edit / delete category
   - Manual reorder mode (persisted `orderIndex`)
   - Language settings (Hebrew / English)

2. **Invoice management**
   - Select category → invoice list (search, filter, sort)
   - Add / view details / edit / delete invoice

3. **Navigation pattern**
   - Category list is the start destination
   - Invoice screens require `categoryId`; edit/details require `invoiceId`
   - Related screens share a parent ViewModel via navigation back-stack entry

---

## B. Architecture Pattern

**Pattern:** MVVM with Jetpack Compose, Navigation Compose, and Room.

| Layer | Responsibility |
|-------|----------------|
| **View** | Stateless Compose screens; collect `StateFlow` with `collectAsStateWithLifecycle()` |
| **ViewModel** | UI state, user intents, repository calls, derived list pipelines |
| **Repository** | Abstract persistence; map entities ↔ domain models |
| **Room** | SQLite source of truth (entities, DAOs, migrations, type converters) |

**Data flow:** `UI → ViewModel → Repository → Room → Repository → ViewModel → UI`

**ViewModel scoping:**
- `CategoryListViewModel` — shared across category list, add/edit category
- `InvoiceListViewModel` — shared across invoice list, add/edit, details
- `LanguageViewModel` — language settings screen

**Key decisions:**
- Repositories created in `MainActivity` and passed into the nav host (no DI framework)
- Invoice list keeps `sourceInvoices` and recomputes `visibleInvoices` after search/filter/sort
- Service period semantics use explicit `ServicePeriodMode` — never inferred from dates
- Currency (`InvoiceCurrency`) is display metadata; amounts are not converted

---

## C. Package Structure

```
com.dinyairsadot.taxtracker/
├── MainActivity.kt              # Entry, DB init, seeding, locale, repo wiring
├── core/
│   ├── domain/
│   │   ├── Models.kt            # Category, Invoice, enums
│   │   ├── CategoryRepository.kt
│   │   └── InvoiceRepository.kt
│   ├── data/
│   │   ├── TaxTrackerDatabase.kt   # Room v13, migrations
│   │   ├── dao/, entities/, converters/, repositories/
│   │   ├── LanguagePreferenceManager.kt
│   │   └── SeedingPreferenceManager.kt
│   └── ui/
│       ├── Navigation.kt        # Routes, NavHost, ViewModel factories
│       ├── CategoryColorUtils.kt
│       ├── DropdownPositioning.kt
│       ├── SwipeDismissSnackbarHost.kt
│       └── AppSnackbar.kt
├── feature/
│   ├── category/                # List, add, edit, reorder, CategoryForm
│   ├── invoice/                 # List, add, edit, details, search/filter/sort
│   └── settings/                # LanguageSettingsScreen
├── ui/theme/                    # Material 3 theme
└── archive/                     # Unused in-memory repos (reference only)
```

---

## D. Navigation

**Library:** Navigation Compose

**Routes** (`Screen` sealed class in `Navigation.kt`):

| Route | Purpose |
|-------|---------|
| `category_list` | Start destination |
| `add_category` | Add category |
| `edit_category/{categoryId}` | Edit category |
| `invoice_list/{categoryId}` | Invoice list for category |
| `add_invoice/{categoryId}` | Add invoice |
| `invoice_details/{invoiceId}` | Read-only details |
| `edit_invoice/{invoiceId}` | Edit invoice |
| `language_settings` | Manual language switch |

**ViewModel sharing:** Child screens resolve the parent back-stack entry (e.g. `invoice_list/{categoryId}`) and reuse the same ViewModel instance.

**Snackbar feedback:** One-shot flags via `savedStateHandle` (e.g. `"category_added"`).

---

## E. Domain Models

Source: `core/domain/Models.kt`

### Category
- `id`, `name`, `colorHex`, `description`
- `customFieldTitles: List<String>` — up to 10 titles (JSON in Room)
- `seedKey`, `userEdited` — seeded category identity and locale protection
- `orderIndex` — persisted manual list order
- `pinnedDefaults: Map<String, String>` — e.g. default supplier name

Backward-compat getters: `customFieldTitle1..3`

### Invoice
- Core: `documentNumber`, `amountDue`, `paymentStatus`, `amountCurrency`
- Dates: `paymentDate`, `dueDate`, `servicePeriodStart`, `servicePeriodEnd`
- `servicePeriodMode: ServicePeriodMode` — `MONTH` or `DATE` (explicit source of truth)
- Payment: `paymentMethod`, `numberOfPayments`, `confirmationNumber`
- `customFieldValues: List<String>` — aligned by index to category titles
- Legacy fields retained for migration: `invoiceNumber`, `amount`, etc.

### Enums
- `PaymentStatus`: `PAID`, `NOT_PAID` (legacy DB values mapped via converter)
- `ServicePeriodMode`: `MONTH`, `DATE`
- `DocumentType`: `BILL_DEMAND`, `TAX_INVOICE`, `INVOICE_RECEIPT`
- `InvoiceCurrency`: `ILS`, `USD` — display only
- `PaymentMethodOption`: includes `NOT_SPECIFIED`, `CREDIT`, `OTHER`, etc.

### Planned / partial types
- `CustomFieldDefinition`, `InvoiceCustomFieldValue`, `InvoiceImage` — defined for future use; active UI uses indexed title/value lists

---

## F. Data Layer

**Room database:** version **13**, non-destructive migration chain.

**Repositories:**
- `RoomCategoryRepository` — CRUD, seeded localization, `updateCategoryOrder()`
- `RoomInvoiceRepository` — CRUD per category, entity ↔ domain mapping

**Entity highlights:**
- `CategoryEntity` — `customFieldTitlesJson`, `orderIndex`, `seedKey`, `userEdited`
- `InvoiceEntity` — converters for dates, enums, JSON lists/maps, `amountCurrencyCode`

**CategoryRepository extras:**
- `updateLocalizedSeededCategories(context)` — refresh unedited seeded names/descriptions
- `clearCustomFieldsForSeededCategories()` — one-time migration helper
- `updateCategoryOrder(orderedIds)` — persist reorder

**ID generation:** Room auto-generates IDs.

---

## G. State Management

ViewModels expose immutable `UiState` data classes via `StateFlow`.

**CategoryListUiState:** categories (with invoice counts), loading, error, reorder mode

**InvoiceListUiState:** category header, `sourceInvoices`, `visibleInvoices`, search/filter/sort state, loading, error

**Invoice list pipeline** (`InvoiceListViewModel`):
1. Load invoices into `sourceInvoices`
2. Apply search (mode: invoice number or amount)
3. Apply service period range filter
4. Apply payment status filter
5. Apply sort (date/amount, asc/desc)
6. Emit `visibleInvoices`

**Form state:** `rememberSaveable` for inputs; validation errors in local `remember` state; scroll-to-first-invalid on failed save.

---

## H. UI Conventions

### Top app bars
- Category list: default theme, title “Bills & Taxes”, language settings action
- Invoice flows: category-colored bar via `categoryTopAppBarColors()` with contrast-aware text/icons
- Edit actions: text button in top bar (details) or icon on card overlay

### Category colors
- Hex `#RRGGBB`; parsed with fallback
- Pastel preset palette + 7×7 extended grid
- Cards: colored border/stripe; list reorder mode with animated moves

### Forms
- Picker-first dates (`ServicePeriodInput`, date rows tap-to-open picker)
- Dropdown menus positioned via `DropdownPositioning` to avoid overlapping anchors
- Snackbars: `SwipeDismissSnackbarHost` for swipe-to-dismiss

### Dialogs
- Confirmation before delete (category or invoice)
- Warning when removing custom field definitions

---

## I. Localization and RTL

- Manual language switch (Hebrew / English) persisted via `LanguagePreferenceManager`
- Locale applied in `attachBaseContext`; Compose uses `LocalLayoutDirection`
- String resources: `values/` (English), `values-iw/` (Hebrew)
- Seeded categories re-localize on language change unless `userEdited == true`

---

## J. First Launch and Seeding

- Loading screen while initialization runs
- Idempotent flags in `SeedingPreferenceManager`
- Default categories inserted once with stable `seedKey`
- One-time cleanup for seeded custom fields (historical migration)

---

## K. Non-Obvious Invariants

1. **Custom field alignment:** Invoice `customFieldValues[i]` maps to `category.customFieldTitles[i]`. Do not filter/reindex values in ways that break index alignment.
2. **Service period mode:** Always persist and read `ServicePeriodMode`; never infer MONTH vs DATE from stored dates alone.
3. **Category order:** List sorted by `orderIndex`, not name — preserves user order across locale changes.
4. **Currency:** Store and display `amountCurrency`; never convert amounts between ILS and USD.
5. **Seeded categories:** `userEdited` blocks automatic locale overwrite of name/description.
6. **ViewModel scope:** Invoice/category mutations must go through the shared parent ViewModel so list state stays consistent.
7. **Migrations:** Avoid new DB migrations unless explicitly requested and tested.

---

## L. Current Status and Evolving Areas

**Stable / complete for MVP:**
- Room persistence (v13), incremental migrations
- Category and invoice CRUD
- Dynamic custom fields (indexed title/value lists)
- Invoice search, filter, sort (`sourceInvoices` → `visibleInvoices`)
- Explicit service period mode (`MONTH` / `DATE`)
- Bilingual UI with RTL and manual language switching
- Category manual reorder (`orderIndex`)
- UI polish pass (Apr–May 2026)
- **Pre-launch safety refactor** (Jun 2026): data-preservation fixes, error surfacing, targeted responsive/list fixes, debug log removal, build/lint/test validation

**Not yet implemented:**
- CSV export / JSON backup / restore
- CI pipeline
- Play Store release assets

**Known improvement areas (non-blocking follow-ups):**
- In-memory filter/sort may need DAO queries at scale
- Startup/seeding logic concentrated in `MainActivity`
- Deprecation warnings (`menuAnchor`, `Locale(String)`, `LocalLifecycleOwner`)
- Large Compose files could benefit from selective extraction
- ViewModel `Context` usage could be narrowed over time

---

## Summary

Tax Tracker is a Kotlin + Jetpack Compose Android app using MVVM, Room, and Navigation Compose. Categories define optional custom field schemas; invoices store aligned value lists and explicit service period modes. The invoice list recomputes visible results through a single ViewModel pipeline. The app supports Hebrew and English with manual switching and locale-aware seeded data. UI polish and a conservative pre-launch refactor are complete; **next focus is export foundation** (CSV of visible/filtered invoices, then JSON backup/restore), tests, CI, and Play Store readiness.

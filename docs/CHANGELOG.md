# Tax Tracker – Development Changelog

This changelog documents incremental development steps of the Tax Tracker app.
Entries are ordered from newest to oldest and correspond to tested, committed changes.

## 11/06/2026

### Backup and Restore
- **Create backup:** category list overflow → “Create backup” writes a ZIP via Storage Access Framework containing `backup.json` with restore-oriented app data (categories, invoices, IDs, order, colors, seed fields, custom field titles/values, invoice relationships, service period modes, currencies, raw enum values, ISO dates, explicit nulls, metadata, format version)
- **Restore backup:** category list overflow → “Restore backup” opens a backup ZIP via SAF, validates `backup.json`, shows a destructive confirmation dialog, then fully replaces current local categories and invoices
- Restore is **full replace only** — not merge; validation runs before any data is deleted; failed validation leaves current data unchanged
- Restore uses a Room transaction (delete all invoices/categories, insert backup data) and preserves original category and invoice IDs
- After successful restore, seeding preference flags are set so first-run default seeding does not duplicate or modify restored data; language preference is not restored or modified
- Pure Kotlin backup utilities: `BackupZipExporter`, `BackupZipImporter`, `BackupValidator`, `BackupMapper`; `RoomBackupRestoreRepository` for transactional DB replacement
- Unit tests: `BackupZipExporterTest`, `BackupZipImporterTest`, `BackupValidatorTest`, `BackupMapperTest`

### Database
- Room schema v14: add index on `invoices.categoryId` (FK performance)

### Known Limitations
- **Export vs backup vs restore:** CSV/ZIP exports are for human review only — restore accepts backup ZIPs with `backup.json` only, not CSV exports
- **Backup security:** backup files are plaintext JSON containing sensitive financial/user data; no encryption or cloud sync
- **Restore safety:** restore is destructive; create a fresh backup before restoring another backup if you may want to return to the current state
- **Google Sheets Android:** may misread valid UTF-8 CSV when headers are English and data contains Hebrew; LibreOffice and desktop Google Sheets open exports correctly; future XLSX export may address this

---

## 02/06/2026

### UI Polish
- Fix category list FAB overlapping the last category card
- Add top-bar Save action on Edit Category; warn before discarding unsaved changes
- Simplify invoice list top bar by removing Edit Category entry
- Remove duplicate pencil edit icon from invoice details
- Clarify custom field creation UX in category add/edit forms
- Show active filter indication on invoice list and add clear-filters action

### Export
- **Invoice list:** “Export” in overflow menu exports currently visible invoices (category scope + search + filters + sort) to localized CSV via Storage Access Framework
- **Category list:** “Export all data” exports a ZIP via SAF containing `categories.csv` (name, description, order, custom field titles) and one invoice CSV per category that has invoices (empty categories omitted from `invoices/`)
- Reuse shared CSV pipeline: `InvoiceCsvExporter`, `InvoiceCsvExportLabels`, `Utf8CsvWriter` (conditional UTF-8 BOM per file)
- Invoice CSV filenames use readable category names, including Hebrew; unsafe path characters sanitized; `_<categoryId>` suffix for uniqueness

### Refactor / Build Validation
- Pre-launch conservative refactor (see 01/06/2026 entry) validated with `./gradlew assembleDebug`, `./gradlew lintDebug`, `./gradlew test`

### Known Limitations
- **Export vs backup:** CSV/ZIP export is user-readable for spreadsheets and records — not restore-safe backup (see 11/06/2026 entry for backup/restore)
- **Google Sheets Android:** may misread valid UTF-8 CSV when headers are English and data contains Hebrew; LibreOffice and desktop Google Sheets open exports correctly; future XLSX export may address this

---

## 01/06/2026

### Pre-Launch Refactor & Safety Pass
Conservative, behavior-preserving cleanup after a Claude/Sonnet pre-launch review. No architecture rewrite, no Room schema change, and no broad UI redesign.

#### Stability / data safety
- Remove temporary language debug logging from production paths
- Preserve hidden category fields on update (`supplierName`, `pinnedDefaults` / `pinnedDefaultsJson`) via repository round-trip
- Surface invoice add/update/delete failures through existing snackbar / `errorMessage` state
- Preserve category reorder mode across refresh and navigation return

#### UI responsiveness
- Add `maxLines` / ellipsis on category list card name and description (list cards only; details screens still wrap full text)
- Improve payment status selector layout resilience on narrow screens (equal button weights; same visual style)
- Add stable `LazyColumn` keys for invoice list items
- Cap snackbar message length for long error text

#### Localization
- Add missing Hebrew strings for `all` and `sort_by`
- Add Hebrew/English error strings for failed invoice save and delete

#### Cleanup
- Remove verified dead code (`getDefaultDocumentType`), unused constant (`SEEDED_CATEGORY_ORDER`), unused string (`enter_valid_date`), and unused imports
- Delete stray untracked root file (`tatus`)

#### Build / lint validation
- Fix `InvoiceDetailsScreen` locale read: use `LocalConfiguration` instead of `LocalContext` configuration access
- Add `@file:OptIn(ExperimentalFoundationApi::class)` where `BringIntoViewRequester` APIs are used
- Validated: `./gradlew assembleDebug`, `./gradlew lintDebug`, `./gradlew test`

---

## 31/05/2026

### Category List & Reorder Polish
- Add manual category reorder mode with up/down controls and animated list transitions
- Persist category order via `orderIndex`; sort by index instead of name so order stays stable across locale changes
- Replace per-card delete icon with a card overflow action menu (edit, delete)
- Remove redundant settings entry from the category overflow menu; language settings remain accessible from the top bar
- Fix top-edge reorder animation clipping so items stay visible during moves

### Invoice Details & Wording Polish
- Refine invoice details header layout and edit-icon positioning on the details card
- Standardize invoice number label wording (`Invoice # …` / Hebrew equivalent) in list and details
- Final Hebrew/English string refinements from the UI polish pass

---

## 27/04/2026

### Invoice Form and Filter UX Polish
- Position category and invoice dropdown menus so they no longer overlap their anchor fields
- On save with validation errors, scroll to the first invalid field and show clear feedback to fix highlighted inputs
- Use picker-first invoice dates with formatted display; tap anywhere on the date row to open the picker (month/year rows behave like full-date rows)
- Persist and display invoice currency (ILS and USD) consistently on the invoice list and detail screens
- Improve the invoice filter sheet: drag handle, opens expanded by default, explicit Apply and Clear actions, and compact horizontal payment status options
- Refine filter date range controls with clearer empty states, aligned labels, per-field clear actions, and stronger tap targets
- Add swipe-to-dismiss behavior for snackbars
- Align the invoice list sort menu animation with the top app bar

## 12/03/2026

### Invoice List Search & Data Flow
- Add invoice search with mode selector in invoice list
- Introduce scope-aware invoice retrieval pipeline to support filtering and search behavior

### Invoice Form & UI Improvements
- Refine invoice card layouts, save actions, and status styling
- Add invoice number labels in list and details screens
- Improve month picker visuals with clearer selected state and smooth color animation
- Match payment method dropdown menu width to its input field

### Payment Method Enhancements
- Expand payment method model with richer enum values and localized labels
- Add "Not specified" default option for optional payment method selection
- Support custom "Other" payment method with persisted free-text in add/edit flows

### Category Form UX
- Improve custom fields section with clearer header, divider, and spacing refinements

## 10/03/2026

### Invoice List Summary & Form Behavior
- Reorder category invoice summary to show total before unpaid amount (including Hebrew translation updates)
- Add automatic scroll-into-view behavior for focused form fields

## 09/03/2026

### Form & Invoice Details Fixes
- Fix excessive padding above the soft keyboard in form screens
- Fix invoice details screen to display all user-entered fields

## 08/03/2026

### Invoice Payment & Period Refactor
- Add "Number of payments" field when payment method is Credit
- Separate payment date and due date handling from service period
- Simplify invoice form payment flow and layout
- Improve document number validation and service period display behavior

## 01/03/2026

### Seeded Data Cleanup
- Remove default custom fields from seeded categories

## 26/02/2026

### Category Edit & Dropdown UX
- Add nested topic/field dropdown in Edit Category with full-field trigger and locale-aware predefined fields
- Improve dropdown menu animation for smoother opening and transparent container rendering

## 15/02/2026

### Localization & Defaults
- Add locale-aware seeded categories
- Set Hebrew as default app language

## 12/02/2026

### Category List & First-Run Experience
- Add dynamic invoice counts to category list
- Implement first-run data seeding flow with loading screen

## 11/02/2026

### Invoice Model Foundation
- Migrate to a minimal core invoice model with pinned defaults and optional field catalog

## 10/02/2026

### Screen Structure Refactor
- Extract Edit Invoice screen into dedicated `EditInvoiceScreen.kt`
- Update `PaymentStatusSelector` visibility for package-level usage

---

## 29/01/2026

### Archive unused in-memory repositories
- Move `InMemoryCategoryRepository` and `InMemoryInvoiceRepository` to `archive/` (no longer used; app uses Room)
- Update docs (ai-context, PROJECT_OVERVIEW) to describe Room as current storage and note archived implementations

### Core Invoice Model & Tax Fields
- Add `DocumentType` enum with Room persistence and category-based defaults
- Add vendor name, issue date, payment date, and service period fields
- Extend invoice model with Israel-focused tax/billing fields
- Add localized strings for all new invoice fields

### Built-in Categories & Data Seeding
- Seed 8 built-in categories with predefined custom fields (Arnona, Electricity, etc.)
- Improve initial app experience with realistic default data

### Database & Migrations
- Add Room database migrations (v1 → v2 → v3) to support new fields
- Ensure backward compatibility for existing users

### Bug Fixes
- Fix custom field values index-alignment bug
    - Preserve index mapping by trimming list instead of filtering blank values

### Internationalization & RTL Support
- Add full Hebrew localization with manual language switching
- Implement persistent language preference
- Add RTL/LTR layout switching based on selected language
- Fix locale handling (`he` / `iw`) and resource folder naming

### Codebase Cleanup
- Replace all hardcoded UI strings with string resources
- Enable proper internationalization support for future languages

---

## 28/01/2026

### Persistence & Architecture
- Replace in-memory repositories with Room database
- Add Room entities, DAOs, and database configuration
- Implement Room-based repositories while preserving existing patterns
- Remove manual ID generation (handled by Room)
- Seed initial default categories on first launch
- Update ViewModels to use Room repositories via factories
- No UI changes required for this migration

### Invoice List & Sorting
- Add sorting dropdown to invoice list
    - Sort by date or amount
    - Ascending / descending options

### Custom Fields System
- Refactor custom fields to a dynamic list (max 10 fields per category)
- Integrate custom fields into invoice add/edit screens
- Display custom field values in invoice details
- Prepare category UI state for dynamic custom fields
- Switch development workflow to Cursor IDE

### UI & UX Improvements
- Polish invoice and category screens
    - Integrate currency button into amount field
    - Standardize date format to DD/MM/YYYY
    - Add real-time validation
    - Improve spacing and visual consistency
- Add scroll support to add/edit invoice and category screens
- Improve category color selection UX
    - Large preview circle
    - Real-time top bar preview
    - Wider selection borders
    - Clear indication for extended palette colors
- Move “Add custom field” button below fields with plus icon
- Match button shapes to text fields
- Make color circles fill full width
- Remove redundant custom fields header text
- Lock app orientation to portrait mode


## 09/01/2026

### Custom Fields – Foundations
- Prepare category UI state to support custom fields
    - Internal state changes only (no visible UI change)
- Add removable custom fields to category add/edit forms
    - Allow users to add and remove custom fields per category
    - Lay groundwork for future dynamic custom field support

## 2026-01-06
### Update category color picker with pastel defaults and extended palette
- Category color picker now shows **7 pastel colors** by default
- Added a **“More colors”** option opening a **7×7 predefined color grid**
- Improves visual consistency while allowing advanced customization

---

## 2025-12-30
### Improve category list card styling
- Added a **subtle category-colored border** to category list cards
- Adjusted card background to be **slightly darker than screen background** for better contrast
- Enhances visual hierarchy without overpowering the UI

---

## 2025-12-29
### Apply category color to edit screens with contrast
- **Edit Category** top bar now reflects the category color
- **Edit Invoice** top bar now reflects the category color
- Automatic contrast applied to titles and icons for readability

---

## 2025-12-28
### Apply category color across invoice screens
- Invoice list top bar now reflects the selected category color
- Invoice details top bar updated to use category color
- Add Invoice top bar updated to use category color
- Contrast-aware text and icons applied consistently across invoice screens
- Improves visual coherence when navigating Category → Invoices

---

## 2025-12-28
### Add project changelog
- Introduced `CHANGELOG.md` to track user-visible changes and feature evolution


---

## 2025-12-16
### Apply category color to invoice list top app bar
- Invoice list top bar now reflects the selected category color
- Visual coherence improved across category → invoice navigation

---

## 2025-12-15
### Rename categories screen title to “Bills & Taxes”
- Improved wording for clarity and intent

### Improve invoice list item layout
- Currency symbol (₪) added
- Amount formatted to two decimals
- Amount and delete button vertically centered relative to the card

### Fix snackbar reappearance after navigation
- Snackbars dismissed correctly when leaving screens
- One-shot snackbar behavior enforced

---

## 2025-12-14
### Implement delete invoice functionality
- Invoice deletion supported from Invoice List only
- Confirmation dialog added

### Show category name in invoice list top bar
- Invoice list top bar displays the selected category name

---

## 2025-12-13
### Implement InvoiceListViewModel and real invoice list
- Invoice list backed by ViewModel and repository
- Real invoice data replaces dummy content

### Add AddInvoiceScreen
- Invoice creation screen implemented
- Navigation from Invoice List FAB enabled

### Add InvoiceDetailsScreen
- Read-only invoice details screen
- Navigation from invoice list items

### Add EditInvoiceScreen
- Invoice editing supported
- Edit flow initiated from Invoice Details screen

---

## 2025-12-10
### Introduce InvoiceRepository interface
- Repository abstraction added for invoices
- Foundation laid for Invoice feature

### Add InMemoryInvoiceRepository with seeded data
- In-memory invoice backend implemented
- Sample invoices seeded for development

### Introduce InvoiceListScreen and navigation route
- Invoice list screen added
- Navigation prepared for per-category invoice management

### Wire category list to InvoiceListScreen
- Category selection navigates to invoice list
- Edit Category action added in invoice view

### Add reusable snackbar and integrate with category flows
- Modern reusable snackbar introduced
- Integrated with add/delete category flows

### Refactor: introduce shared CategoryForm
- Shared `CategoryForm` component created
- Add/Edit category screens refactored to reuse it

### Add live color preview to category form
- Color preview circle shown next to hex input
- Improves UX and immediate visual feedback

---

## 2025-12-08
### Add basic validation to AddCategoryScreen
- Category name required
- Inline error messages displayed

### Add delete category feature with confirmation
- Category deletion supported from Category List
- Confirmation dialog added

### Fix crash when saving category with empty color field
- Defensive handling of empty color input

### Add edit category functionality and enforce unique names
- Edit Category flow implemented
- Case-insensitive unique category name validation enforced

### Introduce shared category color palette
- Shared color palette defined
- Reused across Add/Edit category screens

---

## 2025-12-07
### Introduce CategoryRepository and in-memory implementation
- `CategoryRepository` abstraction created
- `InMemoryCategoryRepository` added
- Categories loaded via repository using coroutines

### Add AddCategoryScreen and navigation route
- Add Category screen implemented
- Navigation from Category List FAB to Add Category and back

### Add form state and save callback to AddCategoryScreen
- Form state handling introduced
- `onSaveCategory` callback wired through navigation

### Connect AddCategoryScreen to shared ViewModel
- Adding new categories via shared ViewModel enabled
- Data flow consolidated

---

## 2025-12-06
### Implement CategoryList ViewModel and convert screen to stateless UI
- `CategoryListViewModel` introduced
- Category list screen converted to stateless composable
- State owned by ViewModel and collected lifecycle-aware

---

## 2025-12-04
### Add navigation setup and CategoryListScreen UI with dummy data
- Navigation Compose setup added
- Initial Category List screen UI implemented
- Dummy data used for early UI scaffolding

---

## 2025-12-03
### Initial project setup
- Project structure initialized
- Core domain models added
- `.gitignore` configured

### Add MIT license
- MIT open-source license added

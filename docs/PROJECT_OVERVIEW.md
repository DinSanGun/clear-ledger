> ⚠️ OUTDATED (needs refresh)
> This file may no longer reflect the current codebase
> (models, storage, and architecture changed).  
> Use `docs/LAUNCH_PLAN.md` for the current execution plan.
> This file will be updated during S1 (Ownership & Architecture Clarity).


# Tax Tracker - Project Overview

## A. High-Level App Purpose and Main User Flows

**App Purpose:**
Tax Invoice Tracker is an Android app for managing bills and tax invoices organized by categories. Users can create categories (e.g., "Electricity", "Water", "City Taxes"), assign colors to them, and track invoices within each category.

**Main User Flows:**

1. **Category Management Flow:**
   - View all categories → Category List Screen (`CategoryListScreen.kt`)
   - Add new category → Add Category Screen (`AddCategoryScreen.kt`)
   - Edit existing category → Edit Category Screen (`EditCategoryScreen.kt`)
   - Delete category (with confirmation dialog)

2. **Invoice Management Flow:**
   - Select category → Navigate to Invoice List (`InvoiceListScreen.kt`)
   - View invoices for category
   - Add invoice → Add Invoice Screen (`AddInvoiceScreen.kt`)
   - View invoice details → Invoice Details Screen (`InvoiceDetailsScreen.kt`)
   - Edit invoice → Edit Invoice Screen (`EditInvoiceScreen.kt`)
   - Delete invoice (with confirmation dialog)

3. **Navigation Pattern:**
   - Category List is the root/start destination
   - Invoice screens are nested under categories (require categoryId)
   - Edit screens are accessed from list/details screens
   - Back navigation returns to previous screen

---

## B. Architecture Pattern

**Pattern: MVVM (Model-View-ViewModel)**

**Implementation:**
- **Model:** Domain models in `core/domain/Models.kt` (Category, Invoice, etc.)
- **View:** Compose UI screens (stateless composables)
- **ViewModel:** 
  - `CategoryListViewModel` (`feature/category/CategoryListViewModel.kt`)
  - `InvoiceListViewModel` (`feature/invoice/InvoiceListViewModel.kt`)
- **Repository Pattern:** 
  - Interfaces: `CategoryRepository`, `InvoiceRepository` (`core/domain/`)
  - Room implementations: `RoomCategoryRepository`, `RoomInvoiceRepository` (`core/data/repositories/`)
  - In-memory implementations are archived in `archive/` (no longer used)

**State Management:**
- ViewModels expose `StateFlow<UiState>`
- UI collects state using `collectAsStateWithLifecycle()`
- UI state is immutable data classes (`CategoryListUiState`, `InvoiceListUiState`)

**Key Architectural Decisions:**
- ViewModels are scoped to navigation graph (shared across related screens)
  - `CategoryListViewModel` shared across Category List, Add Category, Edit Category
  - `InvoiceListViewModel` shared across Invoice List, Add Invoice, Edit Invoice, Invoice Details
- Screens are stateless - all state comes from ViewModel
- Repository pattern allows future migration to Room database

---

## C. Package/Module Structure

```
com.dinyairsadot.taxtracker/
├── core/
│   ├── domain/
│   │   ├── Models.kt                    # Domain models (Category, Invoice, enums)
│   │   ├── CategoryRepository.kt         # Repository interface
│   │   └── InvoiceRepository.kt          # Repository interface
│   ├── data/
│   │   ├── dao/, entities/, repositories/  # Room database, RoomCategoryRepository, RoomInvoiceRepository
│   │   └── TaxTrackerDatabase.kt
│   └── ui/
│       ├── Navigation.kt                 # Navigation setup, Screen sealed class, NavHost
│       ├── AppSnackbar.kt                # Reusable snackbar component
│       └── CategoryColorUtils.kt          # Category color parsing and TopAppBar color utilities
│
├── feature/
│   ├── category/
│   │   ├── CategoryListScreen.kt         # Category list UI
│   │   ├── CategoryListRoute.kt          # Route wrapper connecting ViewModel to Screen
│   │   ├── CategoryListViewModel.kt      # Category list state management
│   │   ├── AddCategoryScreen.kt          # Add category UI
│   │   ├── EditCategoryScreen.kt         # Edit category UI
│   │   ├── CategoryForm.kt                # Shared form component (used by Add/Edit)
│   │   └── CategoryColorPalette.kt       # Color picker UI (presets + extended palette)
│   │
│   └── invoice/
│       ├── InvoiceListScreen.kt           # Invoice list UI
│       ├── InvoiceListViewModel.kt        # Invoice list state management
│       ├── AddInvoiceScreen.kt            # Add invoice UI (also contains EditInvoiceScreen)
│       ├── InvoiceDetailsScreen.kt        # Invoice details UI
│       └── EditInvoiceScreen.kt           # Edit invoice UI (in same file as AddInvoiceScreen)
│
├── ui/theme/
│   ├── Theme.kt                           # Material3 theme setup
│   ├── Color.kt                            # Theme color definitions
│   └── Type.kt                            # Typography definitions
│
├── archive/                               # Archived, unused code (reference only)
│   ├── InMemoryCategoryRepository.kt      # Former in-memory category storage
│   └── InMemoryInvoiceRepository.kt       # Former in-memory invoice storage
│
└── MainActivity.kt                        # Entry point, sets up NavHost
```

**Responsibilities:**
- **core/domain:** Domain models and repository interfaces (data layer contracts)
- **core/ui:** Shared UI utilities and navigation infrastructure
- **feature/category:** Category management feature (UI + ViewModel + repository impl)
- **feature/invoice:** Invoice management feature (UI + ViewModel + repository impl)
- **ui/theme:** Material3 theming configuration

---

## D. Navigation System

**Navigation Library:** Jetpack Navigation Compose (`androidx.navigation:navigation-compose:2.9.6`)

**Navigation Setup:**
- Entry point: `MainActivity.kt` creates `NavController` and calls `TaxTrackerNavHost()`
- NavHost defined in: `core/ui/Navigation.kt`

**Screen Routes (sealed class `Screen`):**
- `CategoryList` → `"category_list"` (start destination)
- `AddCategory` → `"add_category"`
- `EditCategory` → `"edit_category/{categoryId}"` (with navArgument)
- `InvoiceList` → `"invoice_list/{categoryId}"` (with navArgument)
- `AddInvoice` → `"add_invoice/{categoryId}"` (with navArgument)
- `InvoiceDetails` → `"invoice_details/{invoiceId}"` (with navArgument)
- `EditInvoice` → `"edit_invoice/{invoiceId}"` (with navArgument)

**Navigation Patterns:**
- **ViewModel Sharing:** Child screens share parent ViewModel via `navController.getBackStackEntry(parentRoute)`
  - Add/Edit Category share `CategoryListViewModel` from CategoryList route
  - Add/Edit Invoice, Invoice Details share `InvoiceListViewModel` from InvoiceList route
- **State Passing:** 
  - Category color passed via `uiState.categoryColorHex` to child screens
  - Snackbar messages passed via `savedStateHandle` (e.g., "category_added" flag)
- **Back Navigation:** Uses `navController.popBackStack()`
- **Parameter Passing:** Route arguments extracted via `backStackEntry.arguments?.getLong(...)`

**Key Navigation Invariants:**
- Invoice screens require valid `categoryId` (validated in NavHost)
- Edit screens require valid entity ID (if missing, navigate back)
- ViewModel instances are scoped to navigation graph entry points

---

## E. Data Layer

**Current Implementation: Room Database**

**Repositories:**

1. **CategoryRepository** (`core/domain/CategoryRepository.kt`)
   - `getCategories(): List<Category>`
   - `addCategory(category: Category)`
   - `deleteCategory(id: Long)`
   - `updateCategory(category: Category)`

2. **InvoiceRepository** (`core/domain/InvoiceRepository.kt`)
   - `getInvoicesForCategory(categoryId: Long): List<Invoice>`
   - `getInvoiceById(id: Long): Invoice?`
   - `addInvoice(invoice: Invoice)`
   - `updateInvoice(invoice: Invoice)`
   - `deleteInvoice(id: Long)`

**Room Implementations:**
- `RoomCategoryRepository` (`core/data/repositories/RoomCategoryRepository.kt`) — uses `CategoryDao`
- `RoomInvoiceRepository` (`core/data/repositories/RoomInvoiceRepository.kt`) — uses `InvoiceDao`

(Former in-memory implementations are archived in `archive/InMemoryCategoryRepository.kt` and `archive/InMemoryInvoiceRepository.kt`.)

**Domain Models** (`core/domain/Models.kt`):

- **Category:**
  - `id: Long`
  - `name: String`
  - `colorHex: String` (format: #RRGGBB)
  - `description: String?`
  - `customFieldTitle1/2/3: String?` (up to 3 custom field definitions)

- **Invoice:**
  - `id: Long`
  - `categoryId: Long` (foreign key to Category)
  - `invoiceNumber: String`
  - `amount: Double`
  - `paymentStatus: PaymentStatus` (enum: PAID_FULL, NOT_PAID, PAID_CREDIT)
  - `dueDate: LocalDate?`
  - `paymentDate: LocalDate?`
  - `consumptionValue: Double?` (e.g., kWh)
  - `consumptionUnit: String?` (e.g., "kWh", "m³")
  - `notes: String?`
  - `customFieldValue1/2/3: String?` (values for category's custom fields)

- **Enums:**
  - `PaymentStatus`: PAID_FULL, NOT_PAID, PAID_CREDIT
  - `CustomFieldType`: TEXT, NUMBER, DATE, BOOLEAN (defined but not fully used yet)

**Future Data Layer:**
- Models include `CustomFieldDefinition` and `InvoiceCustomFieldValue` (in use for dynamic custom fields)
- `InvoiceImage` model exists (for future photo attachments)

**ID Generation:**
- Room auto-generates IDs for categories and invoices

---

## F. State Management Approach

**State Flow Pattern:**
- ViewModels use `MutableStateFlow<UiState>` internally
- Expose `StateFlow<UiState>` via `asStateFlow()`
- UI collects using `collectAsStateWithLifecycle()` (lifecycle-aware)

**UI State Classes:**

1. **CategoryListUiState** (`feature/category/CategoryListViewModel.kt`):
   ```kotlin
   data class CategoryListUiState(
       val isLoading: Boolean = false,
       val categories: List<CategoryUi> = emptyList(),
       val errorMessage: String? = null
   )
   ```

2. **InvoiceListUiState** (`feature/invoice/InvoiceListViewModel.kt`):
   ```kotlin
   data class InvoiceListUiState(
       val isLoading: Boolean = false,
       val categoryName: String? = null,
       val categoryColorHex: String? = null,
       val invoices: List<InvoiceUi> = emptyList(),
       val errorMessage: String? = null
   )
   ```

**UI Models (Domain → UI mapping):**
- `CategoryUi`: Maps from `Category` domain model
- `InvoiceUi`: Maps from `Invoice` domain model (simplified for display)

**State Updates:**
- ViewModels update state in `viewModelScope.launch` coroutines
- Repository calls are `suspend` functions
- State updates are atomic (copy with new values)

**Local UI State:**
- Form inputs use `rememberSaveable { mutableStateOf(...) }` (survives configuration changes)
- Error states use `remember { mutableStateOf(...) }` (reset on recomposition)
- Dialogs use local state (`pendingDeleteId`, `showMoreColors`, etc.)

**Snackbar State:**
- Uses `SnackbarHostState` in screens
- Messages triggered via `LaunchedEffect` or coroutine scope
- `DisposableEffect` dismisses snackbars when leaving screen

---

## G. UI Conventions

### Top App Bars

**Pattern:**
- All screens use `TopAppBar` within `Scaffold`
- Back navigation icon: `Icons.AutoMirrored.Filled.ArrowBack` (or `Icons.Default.ArrowBack`)
- Title text varies by screen

**Category Color Integration:**
- Invoice-related screens use `categoryTopAppBarColors(categoryColorHex)` (`core/ui/CategoryColorUtils.kt`)
- Applies category color as `containerColor`
- Automatically calculates contrast: `if (luminance < 0.5f) White else Black` for text/icons
- Used in: Invoice List, Invoice Details, Add Invoice, Edit Invoice, Edit Category

**Screens with Category-Colored Top Bars:**
- `InvoiceListScreen` - shows category name + "Edit category" action
- `InvoiceDetailsScreen` - shows "Invoice details" + "Edit invoice" action
- `AddInvoiceScreen` - shows "Add invoice"
- `EditInvoiceScreen` - shows "Edit invoice"
- `EditCategoryScreen` - shows "Edit category"

**Screens with Default Top Bars:**
- `CategoryListScreen` - title: "Bills & Taxes"
- `AddCategoryScreen` - title: "Add category"

### Category Color Usage

**Color Format:**
- Stored as hex string: `#RRGGBB` (e.g., `#FF9800`)
- Validation: Regex `^#[0-9A-Fa-f]{6}$`
- Default fallback: `#424242` (dark grey) if invalid/empty

**Color Picker:**
- **Quick Presets:** 7 pastel colors shown by default (`CategoryColorPalette.kt`)
- **Extended Palette:** "More colors" button opens 7×7 grid (49 colors)
- **Live Preview:** Color preview circle next to hex input (`CategoryColorPreview`)

**Color Application:**
- Category list cards: Subtle colored border (`BorderStroke` with 0.4f alpha)
- Category list cards: Left stripe with category color (0.25f alpha)
- Top app bars: Full category color with contrast-aware text
- Card backgrounds: Slightly darker than screen background for contrast

**Color Utilities:**
- `parseCategoryColorOrDefault(hex: String?): Color` - safe parsing with fallback
- `categoryTopAppBarColors(categoryColorHex: String?): TopAppBarColors` - contrast-aware colors

### Dialog Patterns

**Confirmation Dialogs:**
- Use `AlertDialog` with `title`, `text`, `confirmButton`, `dismissButton`
- Pattern: Local state `pendingDeleteId` / `pendingRemoveFieldIndex`
- Confirm action triggers repository call + snackbar message
- Dismiss clears pending state

**Dialog Examples:**
- Delete category: "Are you sure? All data associated will be removed."
- Delete invoice: "This action cannot be undone."
- Remove custom field: "May delete information stored in invoices."

**Extended Color Palette Dialog:**
- Opens from "More colors" button
- Shows 7×7 grid of color options
- Closes on selection or "Close" button

### Button Placement and UX Rules

**Floating Action Buttons (FAB):**
- Category List: FAB → Add Category
- Invoice List: FAB → Add Invoice
- Position: Bottom-right (default Material3 FAB)

**Save Buttons:**
- Form screens: Full-width button at bottom (`Modifier.fillMaxWidth()`)
- Labels: "Add category", "Save changes", "Save invoice"
- Validation: Buttons trigger validation, show errors inline

**Action Buttons in Top Bar:**
- Invoice List: "Edit category" (TextButton in actions)
- Invoice Details: "Edit invoice" (TextButton in actions)
- Uses `ButtonDefaults.textButtonColors()` with `LocalContentColor.current` for contrast

**Delete Actions:**
- Category List: Delete icon button on each card (right side)
- Invoice List: Delete icon button on each card (right side)
- Always shows confirmation dialog before deletion

**Navigation Buttons:**
- Back button: IconButton in TopAppBar navigationIcon
- Always uses `Icons.AutoMirrored.Filled.ArrowBack` for RTL support

**Form Validation:**
- Inline errors shown via `supportingText` in `OutlinedTextField`
- Errors cleared when user starts typing
- Save button disabled until validation passes (implicit via early return)

**Empty States:**
- Category List: "No categories yet.\nTap + to add your first category."
- Invoice List: "No invoices yet\nTap + to add your first invoice for this category."
- Centered text with instructions

**Loading States:**
- `CircularProgressIndicator` centered on screen
- Shown when `uiState.isLoading == true`

**Error States:**
- Error message displayed centered
- "Please try again later." helper text
- Uses `MaterialTheme.colorScheme.error` color

---

## H. Non-Obvious Constraints and Invariants

### Category Constraints

1. **Unique Category Names:**
   - Case-insensitive uniqueness enforced (`existingNamesLower.contains(name.trim().lowercase())`)
   - Validated in AddCategoryScreen and EditCategoryScreen
   - Error message: "Name must be unique"

2. **Category Name Required:**
   - Cannot be blank
   - Error message: "Name is required"

3. **Color Hex Format:**
   - Must match `^#[0-9A-Fa-f]{6}$` if provided
   - Empty color is allowed (defaults to `#FF9800` in ViewModel)
   - Error message: "Color must be in #RRGGBB format"

4. **Custom Fields Limit:**
   - Maximum 3 custom fields per category
   - Fields are sequential (1, 2, 3)
   - Removing a field shifts remaining fields down

5. **Category Deletion:**
   - Warning mentions "All data associated with it (such as invoices) will be removed"
   - Room handles persistence; deletion is immediate once confirmed

### Invoice Constraints

1. **Category Dependency:**
   - Invoice must belong to a category (`categoryId: Long`)
   - Invoice screens require valid `categoryId` in route
   - If category missing, invoice list shows error

2. **Amount Validation:**
   - Must be positive number (`amount > 0.0`)
   - Error message: "Enter a valid amount"
   - Displayed as ILS: `₪%.2f` format (e.g., `₪250.00`)

3. **Date Format:**
   - Optional field
   - If provided, must be `YYYY-MM-DD` format (parsed via `LocalDate.parse()`)
   - Light validation: `dateText.length < 8` triggers error
   - Error message: "Use format YYYY-MM-DD or leave empty"

4. **Invoice Number:**
   - Currently optional (can be blank)
   - Display fallback: "Invoice #${invoice.id}" if blank

5. **Payment Status:**
   - Required field (enum: PAID_FULL, NOT_PAID, PAID_CREDIT)
   - Selected via `PaymentStatusSelector` (3 TextButtons)

### Navigation Constraints

1. **ViewModel Scope:**
   - ViewModels must be accessed via `navController.getBackStackEntry(parentRoute)`
   - Cannot create new ViewModel instance for child screens
   - Ensures state persistence across navigation

2. **Route Arguments:**
   - `categoryId` and `invoiceId` are `Long` type (NavType.LongType)
   - Missing arguments cause early return (screen doesn't render)
   - Edit screens navigate back if entity not found

3. **Back Stack:**
   - Invoice screens require CategoryList → InvoiceList → (Add/Edit/Details)
   - Cannot navigate directly to invoice screens without category context
   - Back navigation preserves ViewModel state

4. **Snackbar Messages:**
   - Passed via `savedStateHandle` (e.g., `"category_added"` flag)
   - Consumed immediately after showing to prevent re-trigger
   - `DisposableEffect` dismisses snackbars on screen exit

### State Management Constraints

1. **State Immutability:**
   - UI state classes are `data class` (immutable)
   - Updates use `.copy()` to create new state instance
   - Prevents accidental mutations

2. **Coroutine Scope:**
   - Repository calls must be in `viewModelScope.launch`
   - UI state updates happen in coroutine context
   - Errors caught and stored in `errorMessage`

3. **State Refresh:**
   - After add/update/delete, ViewModel calls `loadInvoices()` or refreshes category list
   - Ensures UI shows latest data
   - No optimistic updates (always refetch)

### UI Constraints

1. **Category Color Contrast:**
   - Top app bars automatically adjust text color based on luminance
   - Threshold: `luminance < 0.5f` → White text, else Black text
   - Ensures readability on any category color

2. **Card Styling:**
   - Category cards: Border color uses `parseColor(category.colorHex).copy(alpha = 0.4f)`
   - Card background: `lerp(surface, White, 0.22f)` for subtle contrast
   - Invoice cards: Standard Material3 elevation (2.dp)

3. **Form State Persistence:**
   - Form inputs use `rememberSaveable` (survives configuration changes)
   - Error states use `remember` (reset on recomposition)
   - Ensures form data preserved on rotation

4. **Snackbar Dismissal:**
   - `DisposableEffect(Unit)` dismisses snackbar when leaving screen
   - Prevents snackbars reappearing after navigation
   - One-shot behavior enforced

### Data Constraints

1. **ID Generation:**
   - Manual ID generation: `(existing.maxOfOrNull { it.id } ?: 0L) + 1L`
   - Assumes IDs are sequential Long values
   - Future: Room will auto-generate IDs

2. **Repository Thread Safety:**
   - Room repositories are created in MainActivity and passed into the NavHost/ViewModels
   - Room handles threading via coroutines/Dispatchers.IO
   - Currently single-threaded (main thread + coroutines)
   - Future: Room will handle thread safety

3. **Date Parsing:**
   - Uses `LocalDate.parse()` with `runCatching` for safe parsing
   - Returns `null` if parsing fails (date becomes optional)
   - No validation of date ranges (can be past/future)

---

## I. Stable Parts of the Codebase

**Unlikely to Change:**

1. **Domain Models** (`core/domain/Models.kt`):
   - Core data structures (Category, Invoice, enums)
   - Repository interfaces (contracts are stable)

2. **Navigation Structure** (`core/ui/Navigation.kt`):
   - Screen routes and navigation graph
   - ViewModel sharing pattern

3. **UI Utilities** (`core/ui/`):
   - `AppSnackbar` component
   - `CategoryColorUtils` (color parsing, contrast calculation)

4. **Architecture Pattern:**
   - MVVM with StateFlow
   - Repository pattern
   - Stateless UI composables

5. **Theme Configuration** (`ui/theme/`):
   - Material3 theme setup
   - Dynamic color support

---

## J. Active / Evolving Areas

**Recently Changed (per CHANGELOG.md):**

1. **Category Color Picker** (2026-01-06):
   - Extended palette with 7×7 grid
   - Pastel defaults

2. **Category List Styling** (2025-12-30):
   - Colored borders on cards
   - Card background contrast

3. **Category Color Application** (2025-12-29, 2025-12-28):
   - Top bars reflect category colors
   - Contrast-aware text/icons

**Work In Progress / Future:**

1. **Data Persistence:**
   - Room database in use; initial categories seeded on first launch

2. **Custom Fields:**
   - Category custom field definitions exist in model
   - UI supports up to 3 custom field titles
   - Invoice custom field values not yet implemented in UI

3. **Invoice Features:**
   - Invoice number is optional/not fully utilized
   - Consumption value/unit exist in model but not in UI
   - Payment date exists in model but not in UI
   - Invoice images (`InvoiceImage` model exists, no UI yet)

4. **Category Statistics:**
   - Category list shows hardcoded "3 unpaid · 5 total invoices"
   - TODO comment in `CategoryItem` composable

5. **Date Formatting:**
   - Dates displayed as raw `LocalDate.toString()` (YYYY-MM-DD)
   - Comment: "later we can pretty-format"

6. **Error Handling:**
   - Generic error messages ("Failed to load...")
   - No specific error types or retry logic

---

## Summary

Tax Tracker is a well-structured Android app using MVVM architecture with Jetpack Compose. The codebase follows clear separation of concerns with feature-based modules. Storage uses Room database (RoomCategoryRepository, RoomInvoiceRepository). Category colors are a key visual feature, applied consistently across invoice screens. Navigation uses ViewModel sharing to maintain state across related screens. The app is functional for basic category and invoice management, with several planned enhancements (custom fields polish, invoice images, statistics).

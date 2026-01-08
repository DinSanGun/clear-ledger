# Tax Tracker – Development Changelog

This changelog documents incremental development steps of the Tax Tracker app.
Entries are ordered from newest to oldest and correspond to tested, committed changes.

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

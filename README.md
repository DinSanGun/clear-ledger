# Tax Tracker (Android)

**Tax Tracker** is an Android app for structured tracking of invoices, bills, and tax-related expenses, organized by customizable categories and fields.

The app is designed to help users keep financial documents consistent, searchable, and exportable — with a focus on real-world tax and billing use cases (including Israel-specific fields).

---

## Features

### 📂 Category-Based Organization
- Expenses and invoices are organized by categories
- Each category can define its own set of custom fields
- Built-in categories for common use cases (e.g. Electricity, Arnona, Water)

### 🧾 Flexible Invoice Model
- Core invoice fields: amount, vendor, dates, payment status, notes
- Category-specific custom fields (dynamic list, up to 10 per category)
- Support for service periods and consumption-based bills
- Document type support (invoice, receipt, tax invoice, etc.)

### 🌍 Localization & Accessibility
- Full Hebrew and English support
- Manual language switching
- Proper RTL / LTR layout handling

### 🎨 Polished UI (Jetpack Compose)
- Category color theming with live preview
- Consistent form validation
- Clean, focused invoice and category flows
- Portrait-optimized layout

### 💾 Local Persistence
- Offline-first design
- Data stored locally using Room
- Database migrations for backward compatibility

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM-style separation (UI / ViewModel / Data)
- **Persistence:** Room (SQLite)
- **State Management:** ViewModels + Compose state
- **Localization:** Android resources with RTL support
- **Build System:** Gradle (KTS)

---

## Project Structure

The project follows a feature-oriented structure with a shared core module.  
Domain models and business concepts are separated from data persistence (Room) and UI logic.  
Each feature (such as categories and invoices) encapsulates its own screens and ViewModels, while shared navigation and cross-cutting concerns live in a common core layer.

This structure aims to keep the codebase modular, readable, and easy to extend as new features are added.

---

## Roadmap

### Core Product
- Finalize curated set of common tax & billing fields
- Improve category field management (optional presets)

### UX & Design
- Responsive and adaptive layout across screen sizes
- Visual polish and layout consistency improvements

### Data Portability
- Export data to CSV (Excel / Google Sheets compatible)
- Local, portable backups (PC-accessible format)

### Reliability & Quality
- Automated tests for critical data flows
- CI workflow for basic quality gates

### Release
- Google Play Store publication
- Privacy policy and data safety compliance

---

## Status

The app is under active development and nearing its first public release.  
Core functionality is implemented; current work focuses on polish, data portability, and release readiness.

---

## Notes

This project emphasizes:
- clean data modeling
- extensibility via custom fields
- practical, real-world billing scenarios
- maintainable Android architecture

Internal planning and development notes live in `docs/LAUNCH_PLAN.md`.

---

## License

This project is licensed under the MIT License.  
See the [LICENSE](LICENSE) file for details.

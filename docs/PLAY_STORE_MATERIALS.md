# Clear Ledger – Play Store Materials

_Last updated: 2026-06-18_

Drafts for the Google Play Store listing, Data Safety form, content rating, and asset checklist.  
All sections marked **[DRAFT]** must be reviewed and verified in Play Console before submission.

---

## A. Store Listing Text

### App name

```
Clear Ledger
```

### Short description (80 characters max)

```
Simple bill & tax tracker. Local-first. No account needed.
```

Character count: 59 — within limit.

### Full description

```
Clear Ledger helps you track bills, invoices, and tax-related expenses — organized
by category, searchable, and ready to export. All data stays on your device.
No account required. No cloud sync. No ads.

PRIVACY-FIRST DESIGN
Your financial data never leaves your device unless you explicitly export or back
it up yourself. Clear Ledger has no backend server and no analytics. Everything is
stored locally in a private SQLite database.

ORGANIZE BY CATEGORY
Create categories for your recurring bills: electricity, water, municipal tax,
phone, internet, rent, and more. Each category can have custom field definitions
tailored to your billing workflow. Color-code categories for quick identification.

FLEXIBLE INVOICE TRACKING
For each category, track individual invoices with:
- Document number, amount, vendor, and payment status
- Payment and due dates, service period (month or exact date range)
- Document type (bill, tax invoice, invoice receipt)
- Payment method and credit payment tracking
- Custom fields specific to the category
- Notes

SEARCH, FILTER, AND SORT
Find what you need quickly:
- Search by invoice number or amount
- Filter by payment status or service period range
- Sort by date or amount, ascending or descending

EXPORT YOUR RECORDS
- Export the currently visible invoice list as a localized CSV — suitable for
  spreadsheets, accountants, or personal records
- Export all data as a ZIP archive with a CSV file per category
- Files are written via Android's file picker to any location you choose

BACKUP AND RESTORE
- Create a full backup as a ZIP file containing a structured backup.json
- Restore from a backup: the app validates the file, asks for confirmation, and
  performs a full replacement of local data
- Backup and restore are user-initiated — no automatic cloud upload

HEBREW AND ENGLISH SUPPORT
Full Hebrew and English localization with proper right-to-left (RTL) layout.
Switch language from the app menu at any time.

SUITABLE FOR:
- Personal bill tracking and household expenses
- Freelancers and self-employed users managing invoices
- Small businesses tracking payments and tax documents
- Anyone who wants structured, private, local financial records

No account. No ads. No developer cloud sync. Your records stay on your device unless you choose to export or back them up.
```

### Category

**Finance**

### Tags / keywords (suggested)

invoice tracker, bill tracker, tax invoice, expense tracker, local-first, no cloud, Hebrew, RTL, CSV export, backup

### Support contact

**[Developer support email — to be added before submission]**

### Privacy policy URL

**[Stable URL where docs/PRIVACY_POLICY.md content is hosted — required before submission]**

---

## B. Data Safety Form — Draft

> **[DRAFT — must be verified against the final APK/AAB in Play Console before submission]**
>
> Play Console's Data Safety section has specific questions about data the app collects and shares.
> Final answers must be verified by the developer using Play Console tooling and code review of the final release build.

### Does your app collect or share any of the required user data types?

**No** — the app does not collect or share user data with the developer or any third party.

Technical basis:
- No `INTERNET` permission declared in `AndroidManifest.xml`
- No analytics, advertising, or data-collection SDK included
- All app data (categories, invoices, preferences) is stored locally on the device only

### Is all user data collected by your app encrypted in transit?

**N/A** — no user data is transmitted.

### Do you provide a way for users to request that their data be deleted?

**Yes** — users can:
- Delete individual invoices and categories within the app
- Uninstall the app, which removes all local data

### Data types collected — matrix

| Data type | Collected by developer? | Shared with third parties? | Notes |
|-----------|------------------------|---------------------------|-------|
| Personal information (name, email, etc.) | No | No | Not entered or stored |
| Financial info | No | No | Stored locally on device; not transmitted |
| Location | No | No | Not accessed |
| Contacts | No | No | Not accessed |
| Device identifiers | No | No | Not accessed |
| Usage data / analytics | No | No | No analytics SDK |
| Crash logs | No | No | No crash reporting SDK |
| App activity | No | No | No telemetry |

### Android Auto Backup

Clear Ledger uses `android:allowBackup="true"`. Android's Auto Backup feature may back up app data (the local database) to the **user's own Google Account** if the user has Android backup enabled on their device.

For Play Data Safety purposes:
- This data is backed up to the **user's own storage** (Google Account), not to the developer
- The developer has no access to this backup data
- Consult Play Console guidance on how to declare Auto Backup in the Data Safety form

> Note: If you wish to restrict Android Auto Backup, configure `data_extraction_rules.xml` with explicit `<exclude>` rules before Play submission. The current file (`app/src/main/res/xml/data_extraction_rules.xml`) is a default stub with no exclusions.

### Data Safety form answers (Play Console UI) — expected responses

Based on the app behavior, the expected answers in Play Console are:

1. "Does your app collect or share any of the required user data types?" → **No**
2. Play will ask about Auto Backup handling — answer according to Play Console's current guidance on Android system backup

**Reminder:** verify these answers against the published Play Console Data Safety requirements at the time of submission, as requirements may change.

---

## C. Content Rating Questionnaire — Draft

> **[DRAFT — complete the questionnaire in Play Console when submitting]**

### Expected rating: Everyone (E)

Content rating is determined by Play Console's IARC questionnaire. Based on app content:

| Question area | Expected answer |
|---------------|----------------|
| Violence | None |
| Sexual content | None |
| Language / profanity | None |
| Controlled substances | None |
| User interaction (social features, user-generated content sharing) | None — no social features |
| Location sharing | None |
| Personal/financial data sharing with third parties | None |
| Ads | None |

Expected IARC rating: **Everyone**

Complete the questionnaire in Play Console — do not self-assign the rating.

---

## D. Screenshot and Asset Checklist

> **[DRAFT — capture screenshots in S14/S15; screenshots not yet created]**

### Required by Play Console

- [ ] **Hi-res icon** — 512×512 PNG, 32-bit, no alpha required by Play (check dimensions of existing `app/src/main/ic_launcher-playstore.png` — file is present; verify it is exactly 512×512 before upload)
- [ ] **Phone screenshots** — minimum 2, maximum 8; minimum 320px on shortest side; PNG or JPEG

### Suggested screenshot flow

| # | Screen | Notes |
|---|--------|-------|
| 1 | Category list | Show populated categories with color theming |
| 2 | Invoice list | Show multiple invoices; optionally show active filter |
| 3 | Add / Edit invoice form | Show custom fields, date inputs, payment status |
| 4 | Invoice details | Show full invoice detail view |
| 5 | Search or filter active | Show search/filter/sort in use |
| 6 | Export / Backup menu | Show the category overflow menu with export/backup/restore options |
| 7 | Hebrew UI example | At least one screen in Hebrew — demonstrates RTL and localization |
| 8 | English UI example (optional) | Side-by-side comparison or second category/invoice screen in English |

### Optional assets

- [ ] **Feature graphic** — 1024×500 PNG or JPEG (displayed at top of Play Store listing on some devices)
- [ ] **Short promo video or GIF** — optional; can be hosted on YouTube and linked in Play Console

### Screenshot notes

- Take screenshots on emulator or device (API 26+ / Android 8.0+)
- Use populated demo data for screenshots — avoid empty states
- Switch language in app settings to capture Hebrew screenshots
- Screenshots can be taken before or after signing; sideloaded debug APK is sufficient for screenshot purposes

---

## E. Play Store Submission Checklist

> **[DRAFT — use as a final gate before submitting to Play Console]**

Before submitting to internal testing (S15):

- [ ] Privacy policy published at a stable URL (required by Play)
- [ ] Privacy policy URL added to Play Console listing
- [ ] Short description finalized (within 80 chars)
- [ ] Full description finalized
- [ ] Hi-res icon verified (512×512)
- [ ] At least 2 phone screenshots uploaded
- [ ] Data Safety form completed and submitted in Play Console
- [ ] Content rating questionnaire completed in Play Console
- [ ] App category set to Finance
- [ ] Support contact email added
- [ ] Signing keystore created and release AAB uploaded (S15)

---

## Related docs

| File | Purpose |
|------|---------|
| `docs/PRIVACY_POLICY.md` | Full privacy policy text |
| `docs/RELEASE.md` | Build and release checklist |
| `docs/LAUNCH_PLAN.md` | Pre-release execution plan |
| `docs/ARCHITECTURE.md` | Technical architecture for Data Safety reference |

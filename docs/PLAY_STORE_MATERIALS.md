# Clear Ledger – Play Store Materials

_Last updated: 2026-06-25_

Store listing copy, Data Safety guidance, content rating notes, and asset checklist for Google Play submission.
Sections marked **[PLAY CONSOLE]** must be entered and verified in Play Console before submission.

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
by category, searchable, and ready to export. Local-first design. No account required.
No developer cloud sync. No ads.

PRIVACY-FIRST DESIGN
Clear Ledger does not send your data to the developer or operate its own cloud service.
Your records are stored locally on your device. If Android system backup is enabled on
your device, Android/Google may back up app data according to your device settings.
You can also export records or create manual backups you control. No backend server.
No analytics.

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
- Backup and restore are user-initiated; Clear Ledger does not operate its own cloud service.
  Android system backup may apply depending on your device and Google Account settings

HEBREW AND ENGLISH SUPPORT
Full Hebrew and English localization with proper right-to-left (RTL) layout.
Switch language from the app menu at any time.

SUITABLE FOR:
- Personal bill tracking and household expenses
- Freelancers and self-employed users managing invoices
- Small businesses tracking payments and tax documents
- Anyone who wants structured, private, local financial records

No account. No ads. No developer cloud sync. Local storage with optional Android
system backup (per your device settings) and manual export/backup you control.
```

### Category

**Finance**

### Tags / keywords (suggested)

invoice tracker, bill tracker, tax invoice, expense tracker, local-first, no cloud, Hebrew, RTL, CSV export, backup

### Support contact

**dinsadot@gmail.com**

### Privacy policy URL

**https://dinsangun.github.io/clear-ledger/privacy-policy** (canonical — use in Play Console)

Hebrew version (in-app and hosted): **https://dinsangun.github.io/clear-ledger/privacy-policy-he** — source: `docs/privacy-policy-he.md`. The About screen opens the language-appropriate URL; Play Console listing uses the English canonical URL above.

---

## B. Data Safety Form

> **[PLAY CONSOLE]** Verify these answers against the final APK/AAB in Play Console before submission.

### Does your app collect or share any of the required user data types?

**No** — the app does not collect or share user data with the developer or any third party.

Technical basis:
- No `INTERNET` permission declared in `AndroidManifest.xml`
- No analytics, advertising, or data-collection SDK included
- All app data (categories, invoices, preferences) is stored locally on the device; the developer does not receive it

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
| Financial info | No | No | Stored locally on device; not sent to developer |
| Location | No | No | Not accessed |
| Contacts | No | No | Not accessed |
| Device identifiers | No | No | Not accessed |
| Usage data / analytics | No | No | No analytics SDK |
| Crash logs | No | No | No crash reporting SDK |
| App activity | No | No | No telemetry |

### Android system backup

Clear Ledger uses `android:allowBackup="true"`. Android system backup is **enabled for v1.0.0**.

Backup rules intentionally include:

- Room database (`clear_ledger_database` and WAL files)
- SharedPreferences for language and seeding flags (`language_prefs`, `seeding_prefs`)

Cache and other non-listed app data are not included.

If the user has Android backup enabled, Android/Google may back up or transfer this data to the **user's own Google Account** or during device transfer — not to the developer.

For Play Data Safety purposes:

- This data is backed up to the **user's own storage** (Google Account), not to the developer
- The developer has no access to this backup data
- Consult Play Console guidance on how to declare Android system backup in the Data Safety form

### Data Safety form answers (Play Console UI) — expected responses

Based on the app behavior, the expected answers in Play Console are:

1. "Does your app collect or share any of the required user data types?" → **No**
2. Play will ask about Auto Backup handling — answer according to Play Console's current guidance on Android system backup

**Reminder:** verify these answers against the published Play Console Data Safety requirements at the time of submission, as requirements may change.

---

## C. Content Rating Questionnaire

> **[PLAY CONSOLE]** Complete the IARC questionnaire in Play Console when submitting.

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

Phone screenshots are captured. Remaining assets are optional or need verification before upload.

### Required by Play Console

- [x] **Hi-res icon** — 512×512 PNG verified (`app/src/main/ic_launcher-playstore.png`)
- [x] **Phone screenshots** — 7 captured (EN × 6, HE × 1); files at `docs/assets/screenshots/play-store/` (screens 01–07; suggested screen 8 not captured)

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

Final gate before submitting to Play Console internal testing (S15):

- [ ] **Verify hosted privacy policy URL is live** — `https://dinsangun.github.io/clear-ledger/privacy-policy` (source: `docs/privacy-policy.md`); also verify Hebrew page at `https://dinsangun.github.io/clear-ledger/privacy-policy-he` (`docs/privacy-policy-he.md`)
- [ ] Privacy policy URL added to Play Console listing
- [ ] Short description finalized (within 80 chars)
- [ ] Full description finalized
- [x] Hi-res icon verified (512×512 — `app/src/main/ic_launcher-playstore.png`)
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
| `docs/privacy-policy.md` | Privacy policy — English (canonical Play Store URL: `https://dinsangun.github.io/clear-ledger/privacy-policy`) |
| `docs/privacy-policy-he.md` | Privacy policy — Hebrew (`https://dinsangun.github.io/clear-ledger/privacy-policy-he`; opened from About in Hebrew UI) |
| `docs/RELEASE.md` | Build and release checklist |
| `docs/LAUNCH_PLAN.md` | Pre-release execution plan |
| `docs/ARCHITECTURE.md` | Technical architecture for Data Safety reference |

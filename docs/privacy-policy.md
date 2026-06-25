# Clear Ledger – Privacy Policy

_Last updated: 2026-06-25_

This privacy policy describes how Clear Ledger handles your data. It applies to the Android app published by the developer under the package `com.dinyairsadot.clearledger`.

---

## Summary

Clear Ledger is a local-first app. **Clear Ledger does not operate its own cloud service and does not send your data to the developer.** Your categories, invoices, and preferences are stored locally on your device.

If **Android system backup** is enabled on your device, Android/Google may back up or transfer app data according to your device, Google Account, and backup settings. This is handled by Android, not by Clear Ledger.

You can also use Clear Ledger's **manual export and backup** features to create files you control (CSV, ZIP, or restore-ready backup ZIP).

---

## What data the app stores

All data the app creates is stored **locally on your device** in a Room (SQLite) database and in Android SharedPreferences. This includes:

- Category names, colors, descriptions, and custom field definitions you create
- Invoice records, amounts, dates, custom field values, and other billing data you enter
- Your language preference (Hebrew or English)
- App initialization flags (e.g. whether default categories have been seeded)

This data is **not transmitted to the developer**, to any server operated by the developer, or to any third party by Clear Ledger itself.

---

## What the developer does not collect

The developer does **not** collect, store, or have access to:

- Your personal information (name, email, phone number, address)
- Financial or billing data you enter into the app
- Device identifiers, advertising IDs, or fingerprints
- Usage data, analytics, or crash reports
- Location data
- Any other information from your device

**Technical basis:** Clear Ledger does not declare the `INTERNET` permission in its Android manifest. The app cannot initiate outbound network connections on its own. No analytics, crash-reporting, advertising, or data-collection SDK is included.

---

## Export and manual backup

Clear Ledger lets you export data and create backups. These operations are **entirely user-initiated**:

- **Invoice export (CSV):** exports currently visible invoices to a CSV file at a location you choose via Android's file picker (Storage Access Framework)
- **All-data export (ZIP):** exports all categories and invoices as CSV files inside a ZIP at a location you choose
- **Backup (ZIP):** creates a restore-ready ZIP file containing a `backup.json` snapshot of all app data, at a location you choose
- **Restore:** reads a backup ZIP file from a location you choose and replaces local app data

Clear Ledger does not upload these files anywhere. Files are written only to a path that you select. **You are responsible for the security of exported and backup files.** These files contain your financial and billing data and should be treated as private documents.

---

## Android system backup

Clear Ledger enables Android's built-in backup support (`allowBackup=true`). This is **not** a Clear Ledger cloud service.

If Android system backup is enabled on your device, Android/Google may automatically back up or transfer certain app data according to your device and Google Account settings. Depending on your settings, this can include:

- The local Room database (categories and invoices)
- SharedPreferences used for language and app initialization flags

What is included is defined in the app's backup configuration (`backup_rules.xml` and `data_extraction_rules.xml`). Cache and other non-essential app data are not included.

Important points:

- Backed-up data goes to **your Google Account** (or is transferred per Android device-transfer settings), **not to the developer**
- The developer has **no access** to data stored in your Google Account backup
- This is a standard Android OS feature; Clear Ledger does not control when or whether Android performs a backup
- To manage Android system backup: **Android Settings → Google → Backup** (wording may vary by device and Android version)

If you prefer to limit Android system backup, review your device backup settings. Available controls may vary by Android version and device manufacturer.

---

## Third-party SDKs and services

Clear Ledger uses the following Android/Google libraries, all of which operate on-device as part of the app:

- Jetpack Compose and Material 3 (UI framework)
- Room (local SQLite database)
- Navigation Compose (in-app navigation)
- Gson (local JSON parsing for backup files)

None of these libraries send data to external servers as part of Clear Ledger's own operation.

---

## Data retention and deletion

App data is stored locally on your device and remains under your control:

- You can delete individual invoices and categories within the app
- Uninstalling the app removes locally stored app data from the device
- Exported or manual backup files you saved elsewhere remain wherever you stored them — Clear Ledger does not track or manage those files after they are written
- Android system backup copies (if enabled) are managed by Android/Google under your account settings

---

## Children's privacy

Clear Ledger does not knowingly collect data from children. The developer does not receive user data from the app. There are no age-specific data practices beyond standard local storage and optional Android system backup.

---

## Changes to this policy

If the app's data practices change in a future version, this policy will be updated. The update date at the top of this document will reflect the latest revision.

---

## Contact

For questions about this privacy policy, contact:

**dinsadot@gmail.com**

---

## Privacy policy URL

Canonical public URL (Play Store listing and external references):

**https://dinsangun.github.io/clear-ledger/privacy-policy**

In the app, the same URL opens from **About** (category list overflow menu → About → Privacy Policy).

Source of truth in this repository: [`docs/privacy-policy.md`](privacy-policy.md).

> **Verify before Play submission:** confirm the hosted URL is live and shows the current policy text.

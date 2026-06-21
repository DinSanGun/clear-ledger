# Clear Ledger – Privacy Policy

_Last updated: 2026-06-18_

This privacy policy describes how Clear Ledger handles your data. It applies to the Android app published by the developer under the package `com.dinyairsadot.clearledger`.

---

## Summary

Clear Ledger is a local-first app. Your data stays on your device. The developer does not collect, receive, or have access to any data you enter into the app.

---

## What data the app stores

All data the app creates is stored **locally on your device** in a Room (SQLite) database and in Android SharedPreferences. This includes:

- Category names, colors, descriptions, and custom field definitions you create
- Invoice records, amounts, dates, custom field values, and other billing data you enter
- Your language preference (Hebrew or English)
- App initialization flags (e.g. whether default categories have been seeded)

None of this data is transmitted to the developer, to any server, or to any third party.

---

## What the developer does not collect

The developer does **not** collect, store, or have access to:

- Your personal information (name, email, phone number, address)
- Financial or billing data you enter into the app
- Device identifiers, advertising IDs, or fingerprints
- Usage data, analytics, or crash reports
- Location data
- Any other information from your device

**Technical basis:** Clear Ledger does not declare the `INTERNET` permission in its Android manifest. The app is technically incapable of initiating outbound network connections. No analytics, crash-reporting, advertising, or data-collection SDK is included.

---

## Export and backup files

Clear Ledger lets you export data and create backups. These operations are **entirely user-initiated**:

- **Invoice export (CSV):** exports currently visible invoices to a CSV file at a location you choose via Android's file picker (Storage Access Framework)
- **All-data export (ZIP):** exports all categories and invoices as CSV files inside a ZIP at a location you choose
- **Backup (ZIP):** creates a restore-ready ZIP file containing a `backup.json` snapshot of all app data, at a location you choose
- **Restore:** reads a backup ZIP file from a location you choose and replaces local app data

The app does not upload these files anywhere. Files are written to a path that the user selects. **You are responsible for the security of exported and backup files.** These files contain your financial and billing data and should be treated as private documents.

---

## Android system backup (Auto Backup)

Android includes an **Auto Backup** feature that can automatically back up app data (including the app's local database) to your Google Account, if backup is enabled on your device. This is a standard Android OS feature, not something initiated by Clear Ledger.

- Your app data goes to **your own Google Account**, not to the developer
- The developer has no access to data stored in your Google Account backup
- To control Android Auto Backup, see: **Android Settings → Google → Backup**

If you prefer to limit Android Auto Backup, review your device backup settings. Available controls may vary by Android version and device manufacturer.
---

## Third-party SDKs and services

Clear Ledger uses the following Android/Google libraries, all of which operate entirely on-device:

- Jetpack Compose and Material 3 (UI framework)
- Room (local SQLite database)
- Navigation Compose (in-app navigation)
- Gson (local JSON parsing for backup files)

None of these libraries send data to external servers as part of Clear Ledger's use.

---

## Data retention and deletion

All app data is stored locally on your device and remains under your control:

- You can delete individual invoices and categories within the app
- Uninstalling the app removes all locally stored app data
- Exported or backup files you have saved remain at wherever you saved them — Clear Ledger does not track or manage these after they are written

---

## Children's privacy

Clear Ledger does not knowingly collect data from children. As the app does not collect any user data at all, there are no age-specific data practices to describe.

---

## Changes to this policy

If the app's data practices change in a future version, this policy will be updated. The update date at the top of this document will reflect the latest revision.

---

## Contact

For questions about this privacy policy, contact:

**dinsadot@gmail.com**

---

## Privacy policy URL

This policy will be published at a stable public URL before Play Store submission. The URL will be linked from the Play Store listing.

**https://dinsangun.github.io/clear-ledger/privacy-policy**

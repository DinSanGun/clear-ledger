# Clear Ledger – Privacy Policy

English | [עברית](privacy-policy-he)

_Simple Bill & Tax Tracker_

_Last updated: 2026-10-16_

This privacy policy describes how Clear Ledger handles your data. It applies to the Android app published by the developer under the package `com.dinyairsadot.clearledger`.

---

## Summary

Clear Ledger is a local-first app. **Clear Ledger does not operate its own cloud service and does not send your data to the developer.** Your categories, invoices, attached images and PDFs, and preferences are stored locally on your device.

If **Android system backup** is enabled on your device, Android/Google may back up or transfer app data according to your device, Google Account, and backup settings. This is handled by Android, not by Clear Ledger.

You can also use Clear Ledger's **manual export and backup** features to create files you control (CSV, ZIP, or restore-ready backup ZIP that also includes your attached image and PDF files).

---

## What data the app stores

All data the app creates is stored **locally on your device** in a Room (SQLite) database, in Android SharedPreferences, and in the app's private storage (for attached files). This includes:

- Category names, colors, descriptions, and custom field definitions you create
- Invoice records, amounts, dates, custom field values, and other billing data you enter
- **Attached images and PDFs:** you can attach one image or PDF file to an invoice. When you pick a file, Clear Ledger copies it into the app's **private storage** on your device. Other apps cannot browse this storage, and the copy does not depend on the original file, which you can delete or move afterwards. Removing the attachment or deleting the invoice removes the app's copy
- Your language preference (Hebrew or English)
- App initialization flags (e.g. whether default categories have been seeded)

When you tap **Open** on an attachment, Clear Ledger gives the viewer app you choose on your device temporary read access to that one file. This only happens when you ask for it, and what that app does with the file is governed by that app's own privacy practices.

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
- **Backup (ZIP):** creates a restore-ready ZIP file containing a `backup.json` snapshot of all app data, plus the image/PDF files attached to your invoices that are available on the device, at a location you choose. If some attached files cannot be found on the device, they are left out of the backup and the app tells you after the backup is created
- **Restore:** reads a backup ZIP file from a location you choose and replaces local app data, including restoring the attachment files contained in the backup

After an export completes, the app may offer a **Share** action that opens the standard Android Share Sheet. Sharing is always your choice: the file is only handed to the app you pick, which then handles it under its own privacy practices.

Clear Ledger does not upload these files anywhere. Files are written only to a path that you select (or shared only with an app that you choose). **You are responsible for the security of exported and backup files.** These files contain your financial and billing data and should be treated as private documents.

---

## Android system backup

Clear Ledger enables Android's built-in backup support (`allowBackup=true`). This is **not** a Clear Ledger cloud service.

If Android system backup is enabled on your device, Android/Google may automatically back up or transfer certain app data according to your device and Google Account settings. Depending on your settings, this can include:

- The local Room database (categories and invoices)
- SharedPreferences used for language and app initialization flags

What is included is defined in the app's backup configuration (`backup_rules.xml` and `data_extraction_rules.xml`). Cache and other non-essential app data are not included.

**Attached files are not part of Android system backup.** Image and PDF files attached to invoices are stored in the app's private storage and are not included. After restoring from Android system backup or moving to a new device, your invoice records may be restored **without their attached files**; such invoices show that the attached file is not available. To transfer complete invoice data including attachments, use Clear Ledger's manual **Backup (ZIP)** and **Restore** features, which include the attachments that are available on the device.

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

- You can delete individual invoices and categories within the app, and remove or replace an invoice's attachment; the app's private copy of a removed attachment is deleted
- Uninstalling the app removes locally stored app data, including attached files in the app's private storage, from the device
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

Hebrew version: **https://dinsangun.github.io/clear-ledger/privacy-policy-he**

In the app, the language-appropriate URL opens from **About** (category list overflow menu → About → Privacy Policy): English UI → English policy; Hebrew UI → Hebrew policy.

Source of truth in this repository: [`docs/privacy-policy.md`](privacy-policy.md) (English), [`docs/privacy-policy-he.md`](privacy-policy-he.md) (Hebrew).

> **Verify before Play submission:** confirm the hosted URL is live and shows the current policy text.

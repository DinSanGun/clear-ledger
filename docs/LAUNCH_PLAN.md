# Clear Ledger – LAUNCH_PLAN.md
_Last updated: 2026-06-24_

This document is the **single source of truth** for the pre-release execution plan.
It is written for the developer and for AI assistants so context survives long deep-dives.

## How we use this plan
- Work in order, one step at a time.
- Refer to steps by ID in chat (e.g. **“Next: S9”**).
- When reality changes, update this file — do not rely on chat memory.

### Definition of “Done”
A step is **Done** when:
- the deliverable is implemented (or the doc task is written),
- manually sanity-tested where applicable,
- and committed with a clear message.

### Export vs backup vs restore (product distinction)
| | **Export** | **Backup** | **Restore** |
|---|---|---|---|
| Purpose | Human-readable spreadsheets / records | Restore-ready app data snapshot | Full replacement of local app data |
| Format | Localized CSV; ZIP with CSV files | ZIP with `backup.json` | Reads backup ZIP / `backup.json` |
| Menu | Invoice list Export; Category list Export all data | Category list Create backup | Category list Restore backup |
| Restore | **Not supported** | N/A (creates file) | **Full replace only** — not merge |
| CSV accepted? | N/A | No | **No** — CSV/ZIP exports cannot be restored |

---

## Completed work (through Jun 2026)

The following major areas are **implemented** — not pending:

| Area | Status |
|------|--------|
| Room persistence (v14, incremental migrations) | Done |
| Hebrew / English localization + manual language switch | Done |
| Dynamic custom fields (category titles + invoice values) | Done |
| Seeded categories with locale-aware refresh | Done |
| Invoice search / filter / sort pipeline | Done |
| Service period explicit mode (`MONTH` / `DATE`) | Done |
| Currency display metadata (ILS / USD, no conversion) | Done |
| Picker-first dates, form validation, snackbar/dropdown polish | Done |
| Category manual reorder (`orderIndex`) | Done |
| **UI polish pass** (May–Jun 2026) | **Done** |
| **Documentation refresh (S1)** | **Done** |
| **Pre-launch refactor / safety pass (S2)** | **Done** |
| **Responsive fixes from refactor (S3)** | **Done** (targeted) |
| **Invoice-list CSV export (S4)** | **Done** (`0819b53`) |
| **Category-list all-data ZIP export (S4b)** | **Done** (`36ddae4`) |
| **Backup export planning (S5)** | **Done** |
| **Backup export implementation (S6)** | **Done** (`37ff651`) |
| **Restore planning and safety design (S7)** | **Done** |
| **Restore implementation (S8)** | **Done** (`73b7bd6`) |

---

## High-level execution order (what remains)

**Priority order (do not skip ahead):**

```
S9 → S10 → S11 → S12 → S13 → S14 → S15 → S16 → S17
```

| Step | Focus | Status |
|------|--------|--------|
| **S1** | Documentation refresh | **Done** |
| **S2** | Controlled deep code review + selective refactor | **Done** |
| **S3** | Responsive / design safety verification | **Done** (targeted) |
| **S4** | User-facing CSV export (invoice list) | **Done** |
| **S4b** | User-facing all-data ZIP export (category list) | **Done** |
| **S5** | Backup export planning | **Done** |
| **S6** | Backup export implementation (JSON/ZIP) | **Done** |
| **S7** | Restore planning and safety design | **Done** |
| **S8** | Restore implementation (replace existing data) | **Done** |
| **S9** | Targeted test hardening | **Done** |
| **S10** | CI (GitHub Actions) | **Done** |
| **S11** | Release polish (export / backup / restore UX) | **Done** |
| **S12** | Project documentation (README, architecture, release) | **Done** |
| **S13** | Release identity (name, package, version, icon) | **Done** |
| **S14** | Privacy policy and Play Store materials | **Next** |
| **S15** | Internal Play Store testing | Pending |
| **S16** | Launch blocker fixes only | Pending |
| **S17** | Production release + GitHub / interview presentation | Pending |

---

# S1 – Documentation Refresh
**Status:** Done (Jun 2026)

---

# S2 – Controlled Deep Code Review + Selective Refactor
**Status:** Done (Jun 2026)  
**Commit:** `cc6e8f5`

**Validated:** `./gradlew assembleDebug`, `./gradlew lintDebug`, `./gradlew test`.

---

# S3 – Responsive / Design Safety Verification
**Status:** Done for targeted refactor scope (Jun 2026)

---

# S4 – User-Facing Export: Invoice-List CSV
**Status:** Done (Jun 2026)  
**Commit:** `0819b53`

## Delivered
- “Export” in invoice list overflow menu
- Exports **currently visible** invoices after category scope, search, filters, and sort
- Localized CSV via Storage Access Framework (`text/csv`)
- English/Hebrew headers and display values; category custom field titles as column headers
- `InvoiceCsvExporter`, `InvoiceCsvExportLabels`, `Utf8CsvWriter` (conditional UTF-8 BOM)
- Pure Kotlin export utilities; file I/O in composable layer (not `AndroidViewModel`)

## Known limitation
- Google Sheets **Android** may misread valid UTF-8 when headers are English and row data is Hebrew. LibreOffice and desktop Google Sheets open files correctly. Do not add CSV encoding hacks; consider XLSX later.

---

# S4b – User-Facing Export: Category-List All-Data ZIP
**Status:** Done (Jun 2026)  
**Commit:** `36ddae4` (+ follow-up refinements)

## Delivered
- “Export all data” / “ייצוא כל הנתונים” in category list overflow menu
- ZIP via SAF (`application/zip`), suggested name `clear_ledger_all_data_export_YYYY-MM-DD.zip`
- `categories.csv`: name, description, order, custom field title columns (no color column)
- `invoices/<categoryName>_<id>.csv` — **only** for categories with at least one invoice
- Readable filenames preserve Hebrew/English; sanitize unsafe path characters only
- Reuses invoice CSV export labels, escaping, and UTF-8 writer

## Explicitly not in scope
- Backup / restore
- Changing invoice-list export behavior

---

# S5 – Backup Export Planning
**Status:** Done (Jun 2026)

## Delivered
- JSON schema defined: `BackupPayload` with `formatVersion`, `metadata`, `categories`, `invoices`
- Replace-all restore semantics documented for S7–S8
- Raw enum/storage values and ISO dates (not localized display strings)
- ZIP packaging: single `backup.json` entry

---

# S6 – Backup Export Implementation
**Status:** Done (Jun 2026)  
**Commit:** `37ff651`

## Delivered
- “Create backup” in category list overflow menu
- ZIP via SAF containing `backup.json` with versioned restore-oriented payload
- `BackupZipExporter`, `BackupMapper`, `BackupFormat`, `BackupDtos` in `core/util/backup/`
- `CategoryListViewModel.loadAllDataForBackup()` supplies domain data
- Unit tests: `BackupZipExporterTest`

## Explicitly not in scope
- Conflating with S4/S4b user-facing CSV export
- Encryption or cloud sync

---

# S7 – Restore Planning and Safety Design
**Status:** Done (Jun 2026)

## Delivered
- Full replace (not merge) confirmed for MVP
- UX flow: pick file → validate → destructive confirmation → restore
- Validation rules: format version, required fields, unique IDs, enum/date checks, orphan invoice rejection
- Seeding flag handling after successful restore documented

---

# S8 – Restore Implementation
**Status:** Done (Jun 2026)  
**Commit:** `73b7bd6`

## Delivered
- “Restore backup” in category list overflow menu
- `BackupZipImporter` reads ZIP and parses `backup.json`
- `BackupValidator` validates payload before any DB changes
- Destructive confirmation dialog (EN + HE) shown only after validation succeeds
- `RoomBackupRestoreRepository` performs transactional delete + insert via `withTransaction`
- Preserves original category and invoice IDs
- Sets `has_seeded_default_categories` and `has_cleared_seeded_custom_fields` after successful restore
- Language preference not modified
- Unit tests: `BackupZipImporterTest`, `BackupValidatorTest`, `BackupMapperTest`

## Manual QA checklist (S8)
- [x] Create backup from populated app
- [x] Modify app data, restore backup, confirm previous state returns
- [x] Cancel at file picker — no change, no snackbar
- [x] Cancel at confirmation dialog — no change, no snackbar
- [x] Invalid file (non-ZIP) → error snackbar
- [x] Wrong ZIP (no `backup.json`) → error snackbar
- [x] Unsupported `formatVersion` → error snackbar
- [x] CSV all-data ZIP export → restore correctly rejected
- [x] Hebrew/English UI labels and restored data round-trip
- [x] Export features still work after restore

---

# S9 – Targeted Test Hardening
**Status:** Done (Jun 2026)  
**Goal:** Expand automated safety net before launch — focused coverage only; no broad refactors or unnecessary test sprawl.

## Checklist
- [x] Backup JSON round-trip tests (`BackupZipExporterTest`, `BackupMapperTest`)
- [x] Backup import/validation tests (`BackupZipImporterTest`, `BackupValidatorTest`)
- [x] **Launch-protection:** custom field title/value index alignment invariant tests
- [x] **CSV export:** strengthen escaping, column headers, and scope tests (`InvoiceCsvExporterTest`, `AllDataZipExporterTest`)
- [x] **Backup/restore:** strengthen round-trip and invalid-restore validation cases (export ZIP rejected, full pipeline round-trip)
- [x] **Localization:** Hebrew export filename/content/BOM edge cases in ZIP export tests
- [ ] Room migration smoke tests *(optional stretch)*
- [ ] Instrumented restore transaction test *(optional stretch)*

## Explicitly out of scope
- Broad UI or architecture refactors “for testability”
- Large new test suites unrelated to export, backup, restore, or custom-field alignment

---

# S10 – CI Workflow (GitHub Actions)
**Status:** Done (Jun 2026)  
**Goal:** Every push/PR runs a minimal quality gate.

## Checklist
- [x] Add `.github/workflows/android-ci.yml` (push to `main` and pull requests)
- [x] Run `./gradlew test`
- [x] Run `./gradlew lintDebug`
- [x] Run `./gradlew assembleDebug`

**Workflow:** Temurin 17, Gradle cache, separate steps for test / lint / assemble. No signing or secrets.

---

# S11 – Release Polish
**Status:** Done (Jun 2026)  
**Goal:** Low-risk UX improvements for export, backup, and restore flows before store submission.

## Checklist
- [x] Improve loading and error states for export, backup, and restore (clear feedback; no silent failures)
- [x] Prevent duplicate taps during long SAF / file operations if not already handled
- [ ] Consider optional **About** screen — deferred; not in v1.0.0 (privacy policy URL documented in `docs/privacy-policy.md`)
- [x] Manual sanity pass on export + backup + restore after polish

## Explicitly out of scope
- New product features
- Backup encryption, cloud sync, or merge restore

---

# S12 – Project Documentation
**Status:** Done (Jun 2026)  
**Goal:** Present the app clearly for recruiters, users, and future maintainers.

## Checklist
- [x] Polish `README.md` — features, privacy-first/local-first story, tech stack, screenshots placeholder
- [x] Maintain `docs/ARCHITECTURE.md` — MVVM, Room, repositories, Compose, export, backup/restore, localization, validation strategy
- [x] Maintain `docs/RELEASE.md` — test, build, release, and Play Store checklist (aligned with this plan)
- [x] Keep `docs/PROJECT_OVERVIEW.md` and `docs/ai-context.md` in sync with shipped behavior
- [ ] Regenerate `docs/ARCHITECTURE_SUMMARY.pdf` from markdown when a PDF snapshot is needed

### S12A – App rename to Clear Ledger
**Status:** Done (Jun 2026)

Renamed package/applicationId to `com.dinyairsadot.clearledger`, updated user-facing strings, source tree, and docs.

### S12B – Launcher icon
**Status:** Done (Jun 2026)

Adaptive launcher icon finalized and referenced in `AndroidManifest.xml`.

### S12C – Release build preparation
**Status:** Done (Jun 2026)

## Checklist
- [x] Confirm `applicationId` / `namespace` = `com.dinyairsadot.clearledger`
- [x] Confirm launcher name and icon are release-ready
- [x] Set first-release versioning: `versionCode = 1`, `versionName = "1.0.0"`
- [x] Review release build type (`isMinifyEnabled = false` for v1.0.0)
- [x] Document unsigned `./gradlew bundleRelease` output path
- [x] Defer signing to S15 (no keystore or passwords in repo)
- [x] Update `docs/RELEASE.md` with S12C decisions

## Decisions
- R8/minify left off for first release — Room, Gson, and Compose would need keep rules
- `./gradlew bundleRelease` produces unsigned AAB at `app/build/outputs/bundle/release/app-release.aab`
- Signing and Play upload key setup happen in S15 before internal testing

### S12D – Documentation polish
**Status:** Done (Jun 2026)

## Checklist
- [x] README as public-facing page: CI badge, tagline, privacy-first positioning, architecture/CI/release status
- [x] Screenshots placeholder for S14 store assets
- [x] `ARCHITECTURE.md` updated for interview/review (CI done, privacy subsection)
- [x] `RELEASE.md` coherence pass (S9/S11 done, secrets policy)
- [x] Sync roadmap/status across `LAUNCH_PLAN.md`, `PROJECT_OVERVIEW.md`, `ai-context.md`

---

# S13 – Release Identity
**Status:** Done (Jun 2026)  
**Goal:** Lock branding and build identity before Play Store materials.

## Checklist
- [x] Finalize public app name (store listing + launcher label) — Clear Ledger (S12A)
- [x] Confirm package / application ID — `com.dinyairsadot.clearledger` (no late ID changes)
- [x] First-release `versionName` / `versionCode` — `1.0.0` / `1` (S12C)
- [x] Launcher icon and adaptive icon (S12B)
- [ ] Confirm store listing aligns with finalized identity (S14)

Signing is deferred to **S15** (internal testing setup).

---

# S14 – Privacy Policy and Play Store Materials
**Status:** In progress (Jun 2026)  
**Goal:** Satisfy Play policy and communicate the privacy-first design.

## Privacy-first messaging (verified against codebase)
- No `INTERNET` permission — app is incapable of network requests
- No analytics, crash-reporting, or advertising SDK
- No backend, cloud sync, or developer data collection
- Export and backup files are **user-initiated** and **user-controlled** via Storage Access Framework
- Note: Android Auto Backup (`allowBackup=true`) may back up app data to the user's own Google Account — this is OS-level, not developer-initiated

## Checklist
- [x] `docs/privacy-policy.md` created (canonical URL: `https://dinsangun.github.io/clear-ledger/privacy-policy`)
- [x] `docs/PLAY_STORE_MATERIALS.md` created (store listing copy, Data Safety guidance, asset checklist)
- [ ] Verify hosted privacy policy URL is live before Play submission
- [ ] Enter Play **short description** and **full description** in Play Console (copy in `docs/PLAY_STORE_MATERIALS.md`)
- [x] Phone screenshots captured (7 total: EN × 6, HE × 1) — `docs/assets/screenshots/play-store/`
- [ ] Optional short demo video or GIF
- [ ] **Data Safety** form completed in Play Console (verify against final AAB before submitting)
- [ ] **Content rating** questionnaire completed in Play Console

---

# S15 – Internal Play Store Testing
**Goal:** Configure signing, validate the signed Play build on real devices before public release.

## Checklist
- [ ] Create release upload keystore and back it up securely (private — not in repo)
- [ ] Configure release `signingConfig` via `local.properties` or CI secrets (never commit passwords)
- [ ] Enable Play App Signing
- [ ] Build signed release AAB (`./gradlew bundleRelease`)
- [ ] Upload signed AAB to **internal testing** track
- [ ] Install from Play Console link; QA the Play-installed build (not sideload-only)

## Manual QA checklist (internal track)
- [ ] First launch and seeded categories
- [ ] Category flow (add, edit, delete, reorder)
- [ ] Invoice flow (add, edit, delete, details)
- [ ] Search, filter, sort
- [ ] Invoice-list CSV export and category-list all-data ZIP export
- [ ] Create backup and restore backup (round-trip)
- [ ] Invalid restore handling (wrong file, CSV export, bad format version)
- [ ] Hebrew and English UI and export behavior
- [ ] Update from one internal version to the next (migration + data retention)

---

# S16 – Launch Blocker Fixes Only
**Status:** After S15 internal testing — **feature freeze**  
**Goal:** Fix only what blocks a stable public launch.

## In scope
- Crashes and ANRs
- Broken export, backup, or restore
- Serious UI breakage on supported devices
- Play policy or review feedback
- Critical data loss or corruption bugs

## Out of scope
- New features
- Nice-to-have refactors
- Non-blocking lint or deprecation cleanup

---

# S17 – Production Release
**Goal:** Publish to Google Play, then polish external presentation.

## Checklist
- [ ] Promote stable internal build to production (or closed/open testing first if preferred)
- [ ] Monitor initial crash reports and user feedback
- [ ] Polish GitHub repo presentation (README badges, screenshots, clear feature list)
- [ ] Prepare LinkedIn post, resume bullet, and interview demo flow (category → invoice → search → export → backup → restore → localization)

---

## Public-facing mapping (README roadmap)
- S1–S8 → Done (docs, refactor, UI polish, export, backup, restore)
- S9 → Targeted test hardening — **Done**
- S10 → CI — **Done**
- S11 → Release polish — **Done**
- S12 → Project documentation — **Done**
- S13 → Release identity — **Done**
- S14 → Privacy policy + Play Store materials — **Next**
- S15 → Internal Play testing
- S16 → Launch blocker fixes
- S17 → Production release

---

## Follow-up cleanup (non-blocking for launch)

| Item | Notes |
|------|--------|
| Deprecated `menuAnchor()` | Material3 API migration when convenient |
| Deprecated `Locale(String)` | Prefer `Locale.forLanguageTag` where touched |
| Deprecated `LocalLifecycleOwner` import | Update to lifecycle-compose artifact API |
| Dependency / version catalog warnings | Gradle housekeeping |
| Portrait-only orientation lint | Intentional for MVP |
| Unused resource warnings | Lint cleanup pass later |
| ViewModel holding `Context` | Possible future refactor; works today |
| Google Sheets Android CSV limitation | Known; do not add CSV hacks — XLSX later if needed |

---

## Running notes / decisions log

- **2026-02-10:** Plan structure created.
- **2026-05-31:** UI polish phase marked complete.
- **2026-06-01:** S1–S3 complete. Pre-launch refactor (`cc6e8f5`). **Next: export.**
- **2026-06-02:** S4 + S4b complete (invoice CSV + all-data ZIP). UI polish items landed (`9189ace`–`df3d14e`). **Next: S5 backup export planning.**
- **2026-06-11:** S5–S8 complete. Backup creation (`37ff651`) and full-replace restore (`73b7bd6`). Room v14 (FK index on `invoices.categoryId`). **Next: S9 tests, S10 CI, release readiness.**
- **2026-06-14:** Pre-release roadmap expanded to S9–S17: targeted tests → CI → release polish → docs → release identity → privacy/store assets → internal testing → launch blocker fixes → production release. Added `docs/ARCHITECTURE.md` and `docs/RELEASE.md`.
- **2026-06-15:** S9 targeted test hardening complete (~13 new unit tests). `./gradlew test` and `lintDebug` pass. **Next: S10 CI.**
- **2026-06-15:** S10 GitHub Actions CI added (`.github/workflows/android-ci.yml` — Temurin 17, `test` / `lintDebug` / `assembleDebug`). Local verification passed. **Next: S11 release polish.**
- **2026-06-18:** S12C release build preparation complete. Identity confirmed (`com.dinyairsadot.clearledger`), `versionCode = 1`, `versionName = "1.0.0"`, `isMinifyEnabled = false`. Unsigned `bundleRelease` documented; signing deferred to S15.
- **2026-06-18:** S12D documentation polish complete. README CI badge, architecture/release doc updates, roadmap synced. **Next: S14 privacy policy and Play Store materials.**
- **2026-06-18:** S14 in progress. `docs/privacy-policy.md` and `docs/PLAY_STORE_MATERIALS.md` created. Privacy claims verified against codebase — no INTERNET permission, no analytics SDK. Remaining: verify hosted policy URL, complete Play Console forms.
- **2026-06-24:** Pre-release polish complete: dialog action color semantics; rapid-back blank-screen fix; custom field UI clarity; locale/seeding fixes. 7 Play Store screenshots captured. Docs updated. **Next: S14 remaining — verify hosted privacy policy URL, complete Play Console forms (S15 signing + upload).**

---

## Historical note

`docs/ARCHITECTURE_SUMMARY.pdf` is a snapshot and may be outdated. Regenerate from `docs/ARCHITECTURE.md`, `docs/PROJECT_OVERVIEW.md`, or `docs/ai-context.md` when a PDF is needed — do not edit the PDF directly.

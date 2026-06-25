# Clear Ledger – Release Guide

Practical checklist for building, testing, and publishing to Google Play.  
Execution order and step IDs follow `docs/LAUNCH_PLAN.md` (S9–S17).

_Last updated: 2026-06-24_

---

## Pre-release priority order

Work in this sequence — do not skip ahead:

1. **S9** — Targeted test hardening *(done)*  
2. **S10** — GitHub Actions CI *(done)*  
3. **S11** — Release polish *(done)*  
4. **S12** — Project documentation *(done)*  
5. **S13** — Release identity *(done — S12A–C)*  
6. **S14** — Privacy policy and store materials  
7. **S15** — Internal Play Store testing  
8. **S16** — Launch blocker fixes only *(feature freeze)*  
9. **S17** — Production release  

---

## Local test and build checklist

Run before each release candidate:

```bash
./gradlew test
./gradlew lintDebug
./gradlew assembleDebug
```

For release builds:

```bash
# Unsigned AAB (S12C — signing configured in S15):
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab

# Signed release (after S15 signing is configured):
./gradlew assembleRelease
./gradlew bundleRelease
```

- [ ] All unit tests pass
- [ ] `lintDebug` has no new blocking issues
- [ ] Debug and release builds succeed
- [ ] Manual smoke test on device/emulator

**Secrets policy:** Never commit keystores, signing passwords, or `signingConfig` blocks. `.gitignore` excludes `local.properties`, `*.jks`, and `*.keystore`. Document signing details in a private secure note only.

### S12C — Release build preparation (Jun 2026)

Release Gradle config reviewed and documented. Signing deferred to S15.

**App identity (confirmed):**
- `applicationId` / `namespace`: `com.dinyairsadot.clearledger`
- Launcher name: **Clear Ledger** (EN + HE)
- Adaptive launcher icon in place

**Versioning (first release):**
- `versionCode = 1` — increment on every Play upload
- `versionName = "1.0.0"` — semver for first public release

**Release build settings:**
- `minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`
- `isMinifyEnabled = false` for v1.0.0 — R8/minify deferred (Room, Gson, Compose need keep rules if enabled later)
- No `signingConfig` in committed Gradle files

**Signing (deferred to S15):**
- Do **not** commit keystores, passwords, or `signingConfig` blocks
- Create upload keystore and configure signing privately before internal testing
- Use **Play App Signing** when uploading to Play Console
- Document keystore location in a **private** secure note only

**Unsigned release AAB:**
- `./gradlew bundleRelease` produces an unsigned AAB without signing config
- Signing is required before Play Console upload (S15)

### S9 — Targeted tests *(done)*

- [x] Custom field title/value index alignment (launch-protection)
- [x] CSV export escaping, headers, visible-invoice scope
- [x] Backup/restore round-trip and invalid-restore cases (wrong file, CSV export, bad version, orphans)
- [x] Hebrew/English export label edge cases where appropriate

---

## CI checklist (S10 — done)

GitHub Actions workflow (`.github/workflows/android-ci.yml`) runs on push to `main` and on pull requests:

- [x] `./gradlew test`
- [x] `./gradlew lintDebug`
- [x] `./gradlew assembleDebug`

**Purpose:** Catch regressions before merge.

---

## Release polish (S11 — done)

- [x] Loading/error states for export, backup, restore
- [x] Guard against duplicate taps during long file operations
- [x] About screen (version, privacy note, privacy policy link) — category list overflow → About

---

## Release identity (S13)

- [x] Finalize app name (launcher + store listing) — **Clear Ledger** (S12A)
- [x] Confirm package / application ID — `com.dinyairsadot.clearledger` (**no changes after production**)
- [x] First-release versioning — `versionCode = 1`, `versionName = "1.0.0"` (S12C)
- [x] Launcher + adaptive icon finalized (S12B)
- [x] Store listing copy drafted in `docs/PLAY_STORE_MATERIALS.md` (S14)

Signing is configured in **S15** (internal testing setup), not here.

---

## Privacy and store materials (S14)

### Privacy-first facts (verified against codebase)

- **No `INTERNET` permission** — app is technically incapable of outbound network requests
- No analytics, crash-reporting, or advertising SDK included
- No developer-initiated data collection; no backend server
- All app data stored locally on device (Room / SQLite + SharedPreferences)
- Export and manual backup are user-initiated; files go only where the user saves them via SAF
- **Android system backup (v1.0.0):** `allowBackup=true` with explicit rules — Room database (`clear_ledger_database`) and SharedPreferences (`language_prefs`, `seeding_prefs`) included; cache excluded. Android/Google may back up or transfer this data per the user's device and Google Account settings — not developer-initiated; developer has no access

See [`docs/privacy-policy.md`](privacy-policy.md) for the full English policy (canonical Play Store URL: `https://dinsangun.github.io/clear-ledger/privacy-policy`), [`docs/privacy-policy-he.md`](privacy-policy-he.md) for Hebrew (`https://dinsangun.github.io/clear-ledger/privacy-policy-he`), and [`docs/PLAY_STORE_MATERIALS.md`](PLAY_STORE_MATERIALS.md) for store listing copy and Data Safety guidance. The About screen opens the language-appropriate policy URL.

### Documentation deliverables

- [x] `docs/privacy-policy.md` — full privacy policy (English; canonical Play Store URL)
- [x] `docs/privacy-policy-he.md` — Hebrew privacy policy (in-app + hosted; Play Console URL remains English)
- [x] `docs/PLAY_STORE_MATERIALS.md` — store listing copy, Data Safety guidance, asset checklist

### Remaining before Play submission

- [ ] Verify hosted privacy policy URLs are live — English: `https://dinsangun.github.io/clear-ledger/privacy-policy`; Hebrew: `https://dinsangun.github.io/clear-ledger/privacy-policy-he`
- [ ] Enter Play **short description** and **full description** in Play Console (copy in `docs/PLAY_STORE_MATERIALS.md`)
- [x] Phone screenshots captured (7 total: EN × 6, HE × 1) — `docs/assets/screenshots/play-store/`
- [ ] Optional demo video or GIF
- [ ] **Data Safety** form completed in Play Console — verify against final AAB before submitting
- [ ] **Content rating** questionnaire completed in Play Console

---

## Internal testing (S15)

- [ ] Create release upload keystore and back it up securely (private — not in repo)
- [ ] Configure release `signingConfig` via `local.properties` or CI secrets (never commit passwords)
- [ ] Enable Play App Signing
- [ ] Build signed release AAB (`./gradlew bundleRelease`)
- [ ] Upload signed AAB to **internal testing** track
- [ ] Testers install from Play link (not sideload-only)

Document keystore location and signing config in a **private** secure note — not in the public repo.

### Manual QA on Play-installed build

- [ ] First launch; seeded categories
- [ ] Category CRUD and reorder
- [ ] Invoice CRUD and details
- [ ] Search, filter, sort
- [ ] Invoice CSV export; all-data ZIP export
- [ ] Create backup; restore backup (round-trip)
- [ ] Invalid restore (non-ZIP, wrong ZIP, CSV export, unsupported format version)
- [ ] Hebrew and English UI and export
- [ ] Rapid back press from invoice list to category list — no blank screen
- [ ] Upgrade from previous internal version

---

## Launch blocker fixes (S16)

After internal testing: **no new features.**

Fix only:

- Crashes / ANRs
- Broken export, backup, or restore
- Serious UI breakage
- Play policy or review issues
- Critical data bugs

---

## Production release (S17)

- [ ] Promote stable internal build to production
- [ ] Monitor crash reports and early feedback
- [ ] Update GitHub README (screenshots, feature summary)
- [ ] LinkedIn post, resume bullet, interview demo script

### Suggested interview demo flow

Category list → add/edit category → invoice list → search/filter/sort → CSV export → create backup → restore (with validation) → switch language → highlight local-first / no cloud design.

---

## Export vs backup vs restore (reminder)

| | Export | Backup | Restore |
|---|--------|--------|---------|
| Purpose | Human-readable records | Restore-ready snapshot | Full replace local data |
| Format | CSV / ZIP of CSVs | ZIP with `backup.json` | Reads backup ZIP only |
| Restore? | **No** | N/A | **Yes** (backup only) |

---

## Related docs

| File | Purpose |
|------|---------|
| `docs/LAUNCH_PLAN.md` | Full pre-release plan with step details |
| `docs/ARCHITECTURE.md` | Architecture and validation strategy |
| `README.md` | Public project overview |

# Tax Tracker – Release Guide

Practical checklist for building, testing, and publishing to Google Play.  
Execution order and step IDs follow `docs/LAUNCH_PLAN.md` (S9–S17).

_Last updated: 2026-06-14_

---

## Pre-release priority order

Work in this sequence — do not skip ahead:

1. **S9** — Targeted test hardening  
2. **S10** — GitHub Actions CI *(workflow not yet added)*  
3. **S11** — Release polish (export / backup / restore UX)  
4. **S12** — Project documentation  
5. **S13** — Release identity  
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

For signed release builds (after S13 signing is configured):

```bash
./gradlew assembleRelease
# or bundle for Play:
./gradlew bundleRelease
```

- [ ] All unit tests pass
- [ ] `lintDebug` has no new blocking issues
- [ ] Debug and release builds succeed
- [ ] Manual smoke test on device/emulator

### S9 — Targeted tests to add/strengthen

- [ ] Custom field title/value index alignment (launch-protection)
- [ ] CSV export escaping, headers, visible-invoice scope
- [ ] Backup/restore round-trip and invalid-restore cases (wrong file, CSV export, bad version, orphans)
- [ ] Hebrew/English export label edge cases where appropriate

---

## CI checklist (S10 — planned, not yet implemented)

Add a GitHub Actions workflow that runs on push to `main` and on pull requests:

- [ ] `./gradlew test`
- [ ] `./gradlew lintDebug`
- [ ] `./gradlew assembleDebug`

**Purpose:** Catch regressions before merge. Do not add workflow files until starting S10 implementation.

---

## Release polish (S11)

- [ ] Loading/error states for export, backup, restore
- [ ] Guard against duplicate taps during long file operations
- [ ] Optional About screen (version, privacy note, GitHub, privacy policy link)

---

## Release identity (S13)

- [ ] Finalize app name (launcher + store listing)
- [ ] Confirm package / application ID — **no changes after production**
- [ ] `versionCode` increments on every Play upload; `versionName` follows semver or chosen scheme
- [ ] Launcher + adaptive icon finalized
- [ ] Release signing keystore created and backed up securely
- [ ] Release build produces signed AAB

Document keystore location and signing config in a **private** secure note — not in the public repo.

---

## Privacy and store materials (S14)

### Privacy-first facts (use in policy and store copy)

- No account, no cloud sync, no backend, no analytics, no automatic upload
- All app data stored locally on device (Room / SQLite)
- Export and backup are user-initiated; files go only where the user saves them via SAF

### Checklist

- [ ] Privacy policy published at a stable URL
- [ ] Play **short description** and **full description**
- [ ] Phone screenshots (EN; HE if feasible)
- [ ] Optional demo video or GIF
- [ ] **Data Safety** form completed (no collection/transmission without explicit user file save)
- [ ] **Content rating** questionnaire completed

---

## Internal testing (S15)

- [ ] Play App Signing enabled
- [ ] Signed AAB uploaded to **internal testing** track
- [ ] Testers install from Play link (not sideload-only)

### Manual QA on Play-installed build

- [ ] First launch; seeded categories
- [ ] Category CRUD and reorder
- [ ] Invoice CRUD and details
- [ ] Search, filter, sort
- [ ] Invoice CSV export; all-data ZIP export
- [ ] Create backup; restore backup (round-trip)
- [ ] Invalid restore (non-ZIP, wrong ZIP, CSV export, unsupported format version)
- [ ] Hebrew and English UI and export
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

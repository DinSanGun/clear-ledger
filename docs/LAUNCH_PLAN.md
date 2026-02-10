# Tax Tracker – LAUNCH_PLAN.md
_Last updated: 2026-02-10_

This document is the **single source of truth** for the launch plan.
It’s written to be clear for **me (the developer)** and **ChatGPT** so we can keep context even after long deep-dives.

## How we will use this plan (important)
- We work in order, one step at a time.
- In chat, I will refer to steps by ID, like: **“Next: S3”** or **“Working on S6.B”**.
- If a step changes, we update this file (don’t rely on chat memory).

### Definition of “Done”
A step is “Done” when:
- feature is implemented,
- manually sanity-tested,
- and committed with a clear commit message.

---

## High-level execution order (internal truth)
S1 → S2 → S3 → S4 → S5 → S6 → S7 → S8 → S9 → S10

---

# S1 – Ownership & Architecture Clarity
**Goal:** regain full understanding of the codebase and reduce “AI fog” before adding risky features.

## Deliverables
- A short “Architecture Notes” section (can live in this file) that explains:
    - App entry → navigation → screens → viewmodels → repositories → Room
    - Where business logic lives
    - How Category ↔ Invoice ↔ Custom Fields are connected

## Checklist
- [ ] Identify main packages/modules and their responsibilities
- [ ] Trace 2 key flows end-to-end:
    - [ ] Category creation/edit flow
    - [ ] Invoice create/edit flow (including custom fields)
- [ ] List any “risky areas” (fragile logic, unclear ownership, duplication)

## Notes (to fill while doing S1)
- Entry / navigation:
- State management approach:
- Data layer boundaries:
- Known risks:

---

# S2 – Deep Code Review (Claude or equivalent) + Selective Refactor
**Goal:** architecture/clean-code review + apply only **low-risk, high-value** changes.

## Decision rule
- Accept refactors that **improve clarity** without changing behavior.
- Avoid major rewrites pre-launch.

## Deliverables
- A “review summary” (bullet list) stored here:
    - Top 5 improvement opportunities
    - Top 5 risk warnings
    - Suggested small refactors

## Checklist
- [ ] Prepare review prompt + attach relevant folders/files
- [ ] Run deep review (Claude / alternative)
- [ ] Pick max 3–6 safe refactors
- [ ] Implement + re-test critical flows

## Review summary (to fill)
- Opportunities:
- Risks:
- Chosen refactors:

---

# S3 – Product Decision: Default vs Optional Fields (Final)
**Goal:** lock the product decision so UI/export/backup stay consistent.

## Constraints
- MVP should avoid DB migrations and fragile index shifting.
- Current model supports per-category custom field titles and per-invoice values.

## Decisions to finalize
- Core default fields (always available across categories)
- Optional presets list (common tax/billing fields)
- Field removal behavior:
    - Recommended for MVP: **soft-delete/archiving** (no shifting invoice values)

## Deliverables
- A stable list of:
    - [ ] Default fields
    - [ ] Optional presets (names + brief meaning)

## Checklist
- [ ] Finalize default fields list
- [ ] Finalize optional preset list
- [ ] Decide removal strategy (soft-delete vs migrate)
- [ ] Update UI flows for Add/Edit Category accordingly

---

# S4 – Responsive Elegant Design Pass
**Goal:** UI looks good and usable on different screen sizes; reduce hardcoded sizing risks.

## Focus
- Remove “harmful” hardcoded sizes that break on small/large screens
- Prefer adaptive Compose patterns (weights, flexible paddings, scalable typography)
- Ensure key screens are clean:
    - Category list
    - Invoice list
    - Invoice details
    - Add/Edit flows

## Checklist
- [ ] Identify hardcoded sizes that break layout
- [ ] Fix spacing & alignment across devices/emulators
- [ ] Verify long text cases (vendor name, notes, custom fields)
- [ ] Verify accessibility basics (touch targets, contrast)

---

# S5 – Data Portability: CSV Export (MVP)
**Goal:** export data to a PC-readable format (Excel/Sheets compatible).

## Format decisions
- Export scope:
    - [ ] All data (recommended): categories + invoices
- File structure:
    - [ ] One ZIP with multiple CSVs OR one CSV per type (decide)
- Column definitions:
    - Must match the final decisions from S3

## Checklist
- [ ] Define CSV schema (headers)
- [ ] Implement export via Storage Access Framework (file picker)
- [ ] Test export with:
    - [ ] commas/quotes/newlines in text
    - [ ] multiple categories
    - [ ] custom fields
- [ ] Validate open in Excel / Google Sheets

---

# S6 – Backup: Portable Local Backup (built on export)
**Goal:** user-controlled local backup that can be stored anywhere (Drive, PC, etc.).

## MVP definition
- Backup output: **ZIP of CSV exports**
- Restore can be a later step if needed; export-only still provides value.

## Checklist
- [ ] Generate backup ZIP from export outputs
- [ ] Naming/versioning inside ZIP (e.g., backup_metadata.json optional)
- [ ] Test backup creation + file sharing
- [ ] Decide whether MVP includes restore:
    - [ ] Not in MVP (default)
    - [ ] Include restore (only if stable & low-risk)

---

# S7 – Automated Testing: Wave A (Critical Invariants)
**Goal:** small, high-value safety net before launch.

## What to test first (ROI)
- Custom-field invariants (especially if soft-delete/archiving exists)
- CSV formatting and escaping
- Backup ZIP contains expected files and non-empty outputs

## Checklist
- [ ] Add unit test module setup (if missing)
- [ ] Add tests for:
    - [ ] CSV export schema + escaping
    - [ ] backup ZIP structure
    - [ ] custom-field mapping invariants
- [ ] Ensure tests run locally fast

---

# S8 – CI Workflow (GitHub Actions)
**Goal:** every push/PR runs the minimal quality gate.

## MVP CI
- `./gradlew test`
- `./gradlew lint` (recommended)

## Checklist
- [ ] Add GitHub Actions workflow
- [ ] Ensure it runs on PR + main
- [ ] Fix any failing lint/tests
- [ ] Keep CI fast and stable (avoid emulator tests for MVP)

---

# S9 – Import (Optional; post-export stability)
**Goal:** import data from a known template (no AI required if template is controlled).

## MVP recommendation
- Only implement if export/backup are stable and there’s time.
- Start with template-based import matching our schema.

## Checklist
- [ ] Publish import template format (document columns)
- [ ] Add import flow with validation & error reporting
- [ ] Test with partial/malformed files

---

# S10 – Deployment & Release Readiness
**Goal:** publish to Google Play with minimal rejection risk + solid user trust.

## Checklist
- [ ] Privacy policy + Data Safety disclosures
- [ ] targetSdk decision (stability vs newest behavior changes)
- [ ] Versioning (versionCode/versionName)
- [ ] App signing / Play App Signing
- [ ] Store listing (screenshots, description, keywords)
- [ ] Final QA pass (manual checklist)

---

## Public-facing mapping (for README Roadmap)
These internal steps map to recruiter-friendly wording:

- S1–S2 → “Improved internal structure and maintainability”
- S3 → “Curated common tax & billing fields”
- S4 → “Responsive and adaptive layout”
- S5 → “Export to CSV”
- S6 → “Local backup (portable format)”
- S7–S8 → “Automated tests for critical flows”
- S10 → “Google Play release + privacy/compliance”

---

## Running notes / decisions log
(We add short entries here as we decide things.)

- 2026-02-10: Plan structure created. Next intended step: S1.

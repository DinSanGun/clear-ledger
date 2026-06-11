package com.dinyairsadot.taxtracker.core.util.backup

object BackupFormat {
    const val FORMAT_VERSION = 1
    const val JSON_ENTRY_NAME = "backup.json"
    const val PRODUCER = "tax-tracker-android"

    // IMPORTANT: Update this constant whenever TaxTrackerDatabase @Database(version = ...) changes.
    const val DB_SCHEMA_VERSION = 13
}

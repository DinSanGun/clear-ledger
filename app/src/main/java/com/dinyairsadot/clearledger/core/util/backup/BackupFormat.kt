package com.dinyairsadot.clearledger.core.util.backup

object BackupFormat {
    /**
     * Version 2 adds invoice attachments: `backup.json` carries an optional per-invoice
     * [BackupAttachmentDto] and the attachment files live under [ATTACHMENT_ENTRY_PREFIX].
     * Version-1 backups (no attachments) remain restorable.
     */
    const val FORMAT_VERSION = 2
    const val MIN_SUPPORTED_FORMAT_VERSION = 1
    const val JSON_ENTRY_NAME = "backup.json"
    const val PRODUCER = "clear-ledger-android"

    /** ZIP directory prefix of attachment entries: `attachments/<managedFileName>`. */
    const val ATTACHMENT_ENTRY_PREFIX = "attachments/"

    // IMPORTANT: Update this constant whenever ClearLedgerDatabase @Database(version = ...) changes.
    const val DB_SCHEMA_VERSION = 16

    // Streaming limits applied while reading a backup ZIP (zip-bomb / abuse protection).
    const val MAX_JSON_BYTES = 50L * 1024 * 1024
    const val MAX_ATTACHMENT_BYTES = 100L * 1024 * 1024
    const val MAX_TOTAL_ATTACHMENT_BYTES = 1024L * 1024 * 1024
    const val MAX_ZIP_ENTRIES = 5_000

    private const val MAX_NAME_LENGTH = 255

    /**
     * True if [fileName] is a plain, single-segment file name that is safe to use as a managed
     * attachment name and as the tail of a ZIP entry name: no path separators, no `..`, no
     * control characters, bounded length.
     */
    fun isSafeAttachmentFileName(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        if (fileName.length > MAX_NAME_LENGTH) return false
        if (fileName == "." || fileName.contains("..")) return false
        return fileName.none { it == '/' || it == '\\' || it.isISOControl() }
    }

    /** True if [entryName] is exactly `attachments/<safe file name>`. */
    fun isSafeAttachmentEntryName(entryName: String?): Boolean {
        if (entryName == null || !entryName.startsWith(ATTACHMENT_ENTRY_PREFIX)) return false
        return isSafeAttachmentFileName(entryName.removePrefix(ATTACHMENT_ENTRY_PREFIX))
    }

    fun attachmentEntryNameFor(fileName: String): String = ATTACHMENT_ENTRY_PREFIX + fileName
}

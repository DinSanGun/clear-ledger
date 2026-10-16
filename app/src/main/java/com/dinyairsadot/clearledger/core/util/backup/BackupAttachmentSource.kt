package com.dinyairsadot.clearledger.core.util.backup

import java.io.File
import java.io.InputStream

/** Supplies the bytes of managed attachment files to the backup exporter. */
fun interface BackupAttachmentSource {
    /** A fresh stream for the managed file [fileName], or null if it is missing/unreadable. */
    fun open(fileName: String): InputStream?

    companion object {
        /** A source with no files: every attachment lookup fails. */
        val None = BackupAttachmentSource { null }
    }
}

/**
 * Reads managed attachments from [directory] (the app-private `invoice_attachments/` dir).
 * Names that are not plain single-segment file names are treated as missing, so a corrupted
 * database value can never make the exporter read outside [directory].
 */
class DirectoryBackupAttachmentSource(private val directory: File) : BackupAttachmentSource {
    override fun open(fileName: String): InputStream? {
        if (!BackupFormat.isSafeAttachmentFileName(fileName)) return null
        val file = File(directory, fileName)
        if (!file.isFile || !file.canRead()) return null
        return runCatching { file.inputStream() }.getOrNull()
    }
}

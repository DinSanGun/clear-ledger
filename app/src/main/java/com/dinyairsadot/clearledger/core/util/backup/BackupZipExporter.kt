package com.dinyairsadot.clearledger.core.util.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Outcome of a successful [BackupZipExporter.writeZip].
 *
 * @property skippedAttachmentCount number of invoices whose managed attachment file was missing
 * or unreadable and was therefore exported without its attachment (0 for a complete backup).
 */
data class BackupExportResult(val skippedAttachmentCount: Int) {
    val isComplete: Boolean get() = skippedAttachmentCount == 0
}

/**
 * Pure Kotlin backup ZIP writer. No Android dependencies.
 *
 * Layout: `backup.json` (versioned JSON, written first) followed by one
 * `attachments/<managedFileName>` entry per distinct managed attachment file, streamed
 * straight from [BackupAttachmentSource] without loading files into memory.
 *
 * Integrity: a backup must never silently lose a referenced document. Before anything is
 * written, every referenced attachment file is checked for readability. A file that is simply
 * missing or unreadable (e.g. after an Android system restore brought back the database but not
 * `filesDir`) does not fail the backup: the affected invoices are exported without an
 * attachment and counted in [BackupExportResult.skippedAttachmentCount] so the caller can warn
 * the user. Unsafe file names, a file that vanishes mid-write, and any ZIP/stream error still
 * throw [IOException]. Invoices that only carry a legacy `attachmentUri` (no managed file) are
 * skipped by design and are not counted.
 */
object BackupZipExporter {

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create()

    @Throws(IOException::class)
    fun writeZip(
        outputStream: OutputStream,
        data: BackupData,
        metadata: BackupMetadata = BackupMetadata.create(),
        attachmentSource: BackupAttachmentSource = BackupAttachmentSource.None
    ): BackupExportResult {
        val referencedFileNames = data.invoices
            .mapNotNull { it.attachmentFileName?.takeIf(String::isNotBlank) }
            .distinct()

        // Pre-flight, before writing a single byte: an unsafe name is an integrity problem and
        // fails the backup; a missing/unreadable file is omitted (and counted) instead.
        val missingFileNames = mutableSetOf<String>()
        for (fileName in referencedFileNames) {
            if (!BackupFormat.isSafeAttachmentFileName(fileName)) {
                throw IOException("Unsafe attachment file name in database")
            }
            val probe = attachmentSource.open(fileName)
            if (probe == null) missingFileNames.add(fileName) else probe.close()
        }
        val fileNames = referencedFileNames.filterNot { it in missingFileNames }
        val skippedAttachmentCount = data.invoices.count { invoice ->
            val name = invoice.attachmentFileName
            !name.isNullOrBlank() && name in missingFileNames
        }

        val payload = BackupMapper.toPayload(data, metadata) { invoice ->
            invoice.attachmentFileName
                ?.takeIf { it.isNotBlank() && it !in missingFileNames }
                ?.let(BackupFormat::attachmentEntryNameFor)
        }
        val json = gson.toJson(payload)

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFormat.JSON_ENTRY_NAME))
            zip.write(json.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            for (fileName in fileNames) {
                val input = attachmentSource.open(fileName)
                    ?: throw IOException("Attachment file is missing or unreadable: $fileName")
                input.use {
                    zip.putNextEntry(ZipEntry(BackupFormat.attachmentEntryNameFor(fileName)))
                    it.copyTo(zip)
                    zip.closeEntry()
                }
            }
        }
        return BackupExportResult(skippedAttachmentCount)
    }
}

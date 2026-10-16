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
 * Pure Kotlin backup ZIP writer. No Android dependencies.
 *
 * Layout: `backup.json` (versioned JSON, written first) followed by one
 * `attachments/<managedFileName>` entry per distinct managed attachment file, streamed
 * straight from [BackupAttachmentSource] without loading files into memory.
 *
 * Integrity: a backup must never silently lose a referenced document. Before anything is
 * written, every referenced attachment file is checked for readability; if any is missing or
 * unreadable, [writeZip] throws [IOException] and the backup fails. Invoices that only carry a
 * legacy `attachmentUri` (no managed file) are skipped by design.
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
    ) {
        val fileNames = data.invoices
            .mapNotNull { it.attachmentFileName?.takeIf(String::isNotBlank) }
            .distinct()

        // Pre-flight: fail before writing a single byte if any referenced file is unusable.
        for (fileName in fileNames) {
            if (!BackupFormat.isSafeAttachmentFileName(fileName)) {
                throw IOException("Unsafe attachment file name in database")
            }
            val probe = attachmentSource.open(fileName)
                ?: throw IOException("Attachment file is missing or unreadable: $fileName")
            probe.close()
        }

        val payload = BackupMapper.toPayload(data, metadata) { invoice ->
            invoice.attachmentFileName
                ?.takeIf(String::isNotBlank)
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
    }
}

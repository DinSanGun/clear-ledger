package com.dinyairsadot.clearledger.core.util.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/**
 * Pure Kotlin backup ZIP reader. No Android dependencies.
 *
 * Reading is two independent streaming passes over the same ZIP (the caller re-opens it):
 * [readPayload] parses `backup.json`; [extractAttachments] streams the attachment entries to
 * caller-provided outputs. ZIP entry names are never used to build filesystem paths here.
 */
object BackupZipImporter {

    /** Streaming limits for [extractAttachments]; defaults come from [BackupFormat]. */
    data class Limits(
        val maxEntries: Int = BackupFormat.MAX_ZIP_ENTRIES,
        val maxJsonBytes: Long = BackupFormat.MAX_JSON_BYTES,
        val maxAttachmentBytes: Long = BackupFormat.MAX_ATTACHMENT_BYTES,
        val maxTotalAttachmentBytes: Long = BackupFormat.MAX_TOTAL_ATTACHMENT_BYTES
    )

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun readPayload(
        inputStream: InputStream,
        maxJsonBytes: Long = BackupFormat.MAX_JSON_BYTES
    ): BackupPayload {
        val json = readBackupJson(inputStream, maxJsonBytes)
        val payload = gson.fromJson(json, BackupPayload::class.java)
            ?: throw IllegalArgumentException("Backup JSON is empty or invalid")
        return payload
    }

    /**
     * Streams every attachment entry named in [referencedEntryNames] to the stream returned by
     * [openOutput] (called once per entry, with the entry name; the stream is closed here).
     *
     * Strict: throws [IllegalArgumentException] (or an [java.io.IOException] such as
     * [java.util.zip.ZipException] for CRC/truncation errors) when the ZIP
     * - has more than [Limits.maxEntries] entries, or any directory entry,
     * - contains a duplicate entry name,
     * - contains an entry that is neither `backup.json` nor a safe `attachments/<file>` name,
     * - contains an attachment entry that is not referenced,
     * - exceeds the per-file or total size limits (enforced while streaming, not from headers),
     * - or lacks any entry in [referencedEntryNames].
     *
     * On failure the caller is responsible for deleting anything [openOutput] created.
     */
    fun extractAttachments(
        inputStream: InputStream,
        referencedEntryNames: Set<String>,
        limits: Limits = Limits(),
        openOutput: (entryName: String) -> OutputStream
    ) {
        val seen = HashSet<String>()
        val extracted = HashSet<String>()
        var entryCount = 0
        var totalBytes = 0L

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (++entryCount > limits.maxEntries) {
                    throw IllegalArgumentException("Backup ZIP has too many entries")
                }
                val name = entry.name
                if (entry.isDirectory) {
                    throw IllegalArgumentException("Backup ZIP contains a directory entry")
                }
                if (!seen.add(name)) {
                    throw IllegalArgumentException("Backup ZIP contains a duplicate entry")
                }

                when {
                    name == BackupFormat.JSON_ENTRY_NAME -> {
                        copyLimited(zip, null, limits.maxJsonBytes)
                    }
                    BackupFormat.isSafeAttachmentEntryName(name) && name in referencedEntryNames -> {
                        val budget = minOf(
                            limits.maxAttachmentBytes,
                            limits.maxTotalAttachmentBytes - totalBytes
                        )
                        val written = openOutput(name).use { copyLimited(zip, it, budget) }
                        totalBytes += written
                        extracted.add(name)
                    }
                    else -> throw IllegalArgumentException("Unexpected entry in backup ZIP")
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (!extracted.containsAll(referencedEntryNames)) {
            throw IllegalArgumentException("Backup ZIP is missing a referenced attachment")
        }
    }

    private fun readBackupJson(inputStream: InputStream, maxJsonBytes: Long): String {
        return ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == BackupFormat.JSON_ENTRY_NAME) {
                    val buffer = ByteArrayOutputStream()
                    copyLimited(zip, buffer, maxJsonBytes)
                    return buffer.toByteArray().toString(StandardCharsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            throw IllegalArgumentException("ZIP does not contain ${BackupFormat.JSON_ENTRY_NAME}")
        }
    }

    /**
     * Copies [input] to [output] (or discards it if null) until end of stream, failing as soon
     * as more than [maxBytes] have been read. Reading to the end also makes
     * [ZipInputStream] verify the entry's CRC-32.
     */
    private fun copyLimited(input: InputStream, output: OutputStream?, maxBytes: Long): Long {
        val buffer = ByteArray(COPY_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) {
                throw IllegalArgumentException("Backup ZIP entry exceeds the size limit")
            }
            output?.write(buffer, 0, read)
        }
        return total
    }

    private const val COPY_BUFFER_SIZE = 16 * 1024
}

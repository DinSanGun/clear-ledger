package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Shared builders for the attachment backup/restore tests. */
internal object BackupTestSupport {

    // Pseudo-random (incompressible) so ZIP corruption/truncation tests hit entry data reliably.
    val pdfBytes: ByteArray = "%PDF-1.4\n".toByteArray() +
        ByteArray(6_000).also { java.util.Random(1).nextBytes(it) }
    val pngBytes: ByteArray = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47) +
        ByteArray(5_000).also { java.util.Random(2).nextBytes(it) }

    fun metadata() = BackupMetadata(
        exportedAt = "2026-06-11T12:00:00Z",
        dbSchemaVersion = BackupFormat.DB_SCHEMA_VERSION,
        producer = BackupFormat.PRODUCER
    )

    fun category(id: Long = 1L) =
        Category(id = id, name = "Utilities", colorHex = "#FF9800", orderIndex = 0)

    fun invoice(
        id: Long,
        fileName: String? = null,
        displayName: String? = null,
        mimeType: String? = null,
        legacyUri: String? = null
    ) = Invoice(
        id = id,
        categoryId = 1L,
        invoiceNumber = "INV-$id",
        amount = 10.0 + id,
        paymentStatus = PaymentStatus.NOT_PAID,
        servicePeriodMode = ServicePeriodMode.MONTH,
        attachmentUri = legacyUri,
        attachmentFileName = fileName,
        attachmentDisplayName = displayName,
        attachmentMimeType = mimeType
    )

    /** Builds a ZIP from raw (name, bytes) pairs, in the given order. Names must be unique. */
    fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            for ((name, bytes) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    /** JSON of a payload for [data]; [entryNameFor] decides each invoice's attachment entry. */
    fun payloadJson(
        data: BackupData,
        formatVersion: Int = BackupFormat.FORMAT_VERSION,
        entryNameFor: (Invoice) -> String? = { null }
    ): ByteArray {
        val payload = BackupMapper.toPayload(data, metadata(), entryNameFor)
            .copy(formatVersion = formatVersion)
        return GsonBuilder().serializeNulls().create().toJson(payload).toByteArray()
    }

    /** Writes a real backup ZIP, reading attachment files from [sourceDir]. */
    fun backupZip(data: BackupData, sourceDir: File): ByteArray {
        val output = ByteArrayOutputStream()
        BackupZipExporter.writeZip(
            output,
            data,
            metadata(),
            DirectoryBackupAttachmentSource(sourceDir)
        )
        return output.toByteArray()
    }

    fun entryNames(zipBytes: ByteArray): List<String> {
        val names = mutableListOf<String>()
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                names.add(entry.name)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return names
    }

    fun readEntry(zipBytes: ByteArray, name: String): ByteArray? {
        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == name) return zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }

    /** Replaces every occurrence of [from] with the equally long [to] in [bytes]. */
    fun replaceBytes(bytes: ByteArray, from: String, to: String): ByteArray {
        require(from.length == to.length)
        val source = from.toByteArray()
        val target = to.toByteArray()
        val result = bytes.copyOf()
        var i = 0
        while (i <= result.size - source.size) {
            if (source.indices.all { result[i + it] == source[it] }) {
                target.copyInto(result, i)
                i += source.size
            } else {
                i++
            }
        }
        return result
    }

    /** Names of regular files directly inside [dir] (empty if it doesn't exist). */
    fun fileNames(dir: File): Set<String> =
        dir.listFiles()?.filter { it.isFile }?.map { it.name }?.toSet() ?: emptySet()
}

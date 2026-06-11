package com.dinyairsadot.taxtracker.core.util.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Pure Kotlin backup ZIP writer. No Android dependencies.
 * Writes a single [BackupFormat.JSON_ENTRY_NAME] entry containing versioned JSON.
 */
object BackupZipExporter {

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .create()

    fun writeZip(
        outputStream: OutputStream,
        data: BackupData,
        metadata: BackupMetadata = BackupMetadata.create()
    ) {
        val payload = BackupMapper.toPayload(data, metadata)
        val json = gson.toJson(payload)

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
            zip.putNextEntry(ZipEntry(BackupFormat.JSON_ENTRY_NAME))
            zip.write(json.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }
    }
}

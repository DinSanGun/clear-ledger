package com.dinyairsadot.clearledger.core.util.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/**
 * Pure Kotlin backup ZIP reader. No Android dependencies.
 * Reads a single [BackupFormat.JSON_ENTRY_NAME] entry containing versioned JSON.
 */
object BackupZipImporter {

    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    fun readPayload(inputStream: InputStream): BackupPayload {
        val json = readBackupJson(inputStream)
        val payload = gson.fromJson(json, BackupPayload::class.java)
            ?: throw IllegalArgumentException("Backup JSON is empty or invalid")
        return payload
    }

    private fun readBackupJson(inputStream: InputStream): String {
        return ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == BackupFormat.JSON_ENTRY_NAME) {
                    return zip.readBytes().toString(StandardCharsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
            throw IllegalArgumentException("ZIP does not contain ${BackupFormat.JSON_ENTRY_NAME}")
        }
    }
}

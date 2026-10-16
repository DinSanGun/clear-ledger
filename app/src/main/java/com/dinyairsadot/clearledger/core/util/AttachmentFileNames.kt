package com.dinyairsadot.clearledger.core.util

import java.util.UUID

/**
 * Pure (Android-free) generator of collision-safe managed attachment file names, shared by
 * [AttachmentStorage] (new attachments) and backup restore (re-created attachments).
 *
 * The result is always `<random UUID>[.<extension>]`; the extension comes from [displayName]
 * if it is a short alphanumeric suffix, else from [mimeType]. No part of [displayName] other
 * than that suffix ever reaches the file name, so untrusted display names cannot influence
 * the path.
 */
object AttachmentFileNames {

    fun generate(displayName: String?, mimeType: String?): String {
        val extension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() && it.length <= 10 && it.all(Char::isLetterOrDigit) }
            ?: extensionForMimeType(mimeType)
        val base = UUID.randomUUID().toString()
        return if (extension != null) "$base.$extension" else base
    }

    private fun extensionForMimeType(mimeType: String?): String? = when (mimeType) {
        "application/pdf" -> "pdf"
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> null
    }
}

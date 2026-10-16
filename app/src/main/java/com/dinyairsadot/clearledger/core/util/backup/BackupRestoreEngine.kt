package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.util.AttachmentFileNames
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/** Persistence operations the [BackupRestoreEngine] needs. Implemented on top of Room. */
interface BackupRestoreStore {
    /**
     * Atomically replaces all categories and invoices. Must be all-or-nothing: if it throws,
     * previously stored data is unchanged. Returns the managed attachment file names that the
     * *replaced* invoices referenced.
     */
    suspend fun replaceAll(categories: List<Category>, invoices: List<Invoice>): Set<String>

    /** True if any currently stored invoice references the managed file [fileName]. */
    suspend fun isAttachmentFileReferenced(fileName: String): Boolean
}

/**
 * Staged, fail-safe restore of a backup ZIP including invoice attachments. Pure Kotlin (no
 * Android types) so the data-safety logic is unit-testable.
 *
 * Steps (everything before step 4 leaves existing data and files untouched):
 * 1. Re-read and re-validate `backup.json` from the ZIP (not trusting any earlier check).
 * 2. Stream every referenced attachment entry into [attachmentsDir] under a **newly generated**
 *    managed name (never overwriting anything; ZIP entry names are lookup keys only). The
 *    whole ZIP is verified in this pass: unsafe/duplicate/unexpected entries, size and count
 *    limits, CRC errors, and missing referenced entries all abort the restore.
 * 3. Map invoices to the new file names (invoices sharing an entry keep sharing one file).
 * 4. Atomically replace the stored data via [BackupRestoreStore.replaceAll].
 * 5. Only after that commit, delete old managed files that are no longer referenced.
 *
 * If anything fails before the commit completes, the newly staged files are removed (unless the
 * database turns out to reference them) and the exception is rethrown. Failures while deleting
 * old files never fail a restore that has already committed.
 */
class BackupRestoreEngine(
    private val store: BackupRestoreStore,
    private val attachmentsDir: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val limits: BackupZipImporter.Limits = BackupZipImporter.Limits()
) {

    /** [openZip] is invoked once per pass and each returned stream is closed by the engine. */
    suspend fun restore(openZip: () -> InputStream) = withContext(ioDispatcher) {
        val payload = openZip().use { BackupZipImporter.readPayload(it, limits.maxJsonBytes) }
        when (val validation = BackupValidator.validate(payload)) {
            is BackupValidationResult.Valid -> Unit
            is BackupValidationResult.UnsupportedVersion ->
                throw IllegalArgumentException("Unsupported backup version ${validation.version}")
            is BackupValidationResult.Invalid ->
                throw IllegalArgumentException("Invalid backup: ${validation.reason}")
        }

        val staged = stageAttachments(openZip, payload)
        val replacedFileNames: Set<String>
        try {
            val categories = payload.categories.map(BackupMapper::fromCategoryDto)
            val invoices = payload.invoices.map { dto ->
                val invoice = BackupMapper.fromInvoiceDto(dto)
                val attachment = dto.attachment ?: return@map invoice
                invoice.copy(
                    attachmentFileName = staged.fileNamesByEntry.getValue(attachment.entryName),
                    attachmentDisplayName = attachment.displayName,
                    attachmentMimeType = attachment.mimeType
                )
            }
            replacedFileNames = store.replaceAll(categories, invoices)
        } catch (t: Throwable) {
            withContext(NonCancellable) { discardStaged(staged) }
            throw t
        }

        // Committed. From here on nothing may turn this into a reported failure.
        withContext(NonCancellable) {
            AttachmentFileCleanup.deleteUnreferenced(
                attachmentsDir,
                replacedFileNames,
                store::isAttachmentFileReferenced
            )
        }
    }

    private class StagedAttachments(
        /** Backup ZIP entry name -> newly generated managed file name. */
        val fileNamesByEntry: Map<String, String>,
        val createdFiles: List<File>
    )

    private fun stageAttachments(openZip: () -> InputStream, payload: BackupPayload): StagedAttachments {
        val metadataByEntry = payload.invoices
            .mapNotNull { it.attachment }
            .distinctBy { it.entryName }
            .associateBy { it.entryName }

        if (!attachmentsDir.isDirectory && !attachmentsDir.mkdirs() && !attachmentsDir.isDirectory) {
            throw IOException("Cannot create attachments directory")
        }

        val fileNamesByEntry = LinkedHashMap<String, String>()
        val createdFiles = ArrayList<File>()
        try {
            openZip().use { zip ->
                BackupZipImporter.extractAttachments(zip, metadataByEntry.keys, limits) { entryName ->
                    val meta = metadataByEntry.getValue(entryName)
                    val fileName = AttachmentFileNames.generate(meta.displayName, meta.mimeType)
                    val file = File(attachmentsDir, fileName)
                    // CREATE_NEW: fails instead of ever overwriting an existing file.
                    val output = Files.newOutputStream(
                        file.toPath(),
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                    )
                    createdFiles.add(file)
                    fileNamesByEntry[entryName] = fileName
                    output
                }
            }
        } catch (t: Throwable) {
            createdFiles.forEach { runCatching { it.delete() } }
            throw t
        }
        return StagedAttachments(fileNamesByEntry, createdFiles)
    }

    /**
     * Removes staged files after a failed restore. A file is kept if the store reports it as
     * referenced (e.g. the commit actually succeeded and only the return path failed) and also
     * if that can't be determined.
     */
    private suspend fun discardStaged(staged: StagedAttachments) {
        for (file in staged.createdFiles) {
            val referenced = runCatching { store.isAttachmentFileReferenced(file.name) }
                .getOrDefault(true)
            if (!referenced) runCatching { file.delete() }
        }
    }
}

/** Best-effort deletion of managed attachment files that nothing references any more. */
object AttachmentFileCleanup {

    /**
     * Deletes each of [fileNames] from [directory] unless [isReferenced] says an invoice still
     * uses it. Names that are not plain file names are ignored. Failures for individual files
     * are swallowed (worst case: an unreferenced file stays on disk). Call sites run this
     * inside `NonCancellable`.
     */
    suspend fun deleteUnreferenced(
        directory: File,
        fileNames: Collection<String>,
        isReferenced: suspend (String) -> Boolean
    ) {
        for (fileName in fileNames) {
            if (!BackupFormat.isSafeAttachmentFileName(fileName)) continue
            runCatching {
                if (!isReferenced(fileName)) {
                    File(directory, fileName).takeIf { it.isFile }?.delete()
                }
            }
        }
    }
}

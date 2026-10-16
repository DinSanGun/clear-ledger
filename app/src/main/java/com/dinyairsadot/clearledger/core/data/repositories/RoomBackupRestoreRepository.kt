package com.dinyairsadot.clearledger.core.data.repositories

import android.content.Context
import androidx.room.withTransaction
import com.dinyairsadot.clearledger.core.data.DefaultCategorySeeder
import com.dinyairsadot.clearledger.core.data.SeedingPreferenceManager
import com.dinyairsadot.clearledger.core.data.ClearLedgerDatabase
import com.dinyairsadot.clearledger.core.data.entities.CategoryEntity
import com.dinyairsadot.clearledger.core.data.entities.InvoiceEntity
import com.dinyairsadot.clearledger.core.domain.BackupRestoreRepository
import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import com.dinyairsadot.clearledger.core.util.backup.AttachmentFileCleanup
import com.dinyairsadot.clearledger.core.util.backup.BackupRestoreEngine
import com.dinyairsadot.clearledger.core.util.backup.BackupRestoreStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class RoomBackupRestoreRepository(
    private val database: ClearLedgerDatabase,
    private val seedingPreferenceManager: SeedingPreferenceManager,
    /** The app-private `invoice_attachments/` directory holding managed attachment files. */
    private val attachmentsDir: File
) : BackupRestoreRepository {

    private val store = object : BackupRestoreStore {
        override suspend fun replaceAll(
            categories: List<Category>,
            invoices: List<Invoice>
        ): Set<String> {
            val categoryEntities = categories.map { CategoryEntity.fromDomain(it) }
            val invoiceEntities = invoices.map { InvoiceEntity.fromDomain(it) }
            return database.withTransaction {
                val replacedFileNames = managedFileNames(database.invoiceDao().getAll())
                database.invoiceDao().deleteAll()
                database.categoryDao().deleteAll()
                database.categoryDao().insertAll(categoryEntities)
                database.invoiceDao().insertAll(invoiceEntities)
                replacedFileNames
            }
        }

        override suspend fun isAttachmentFileReferenced(fileName: String): Boolean =
            database.invoiceDao().countByAttachmentFileName(fileName) > 0
    }

    override suspend fun restoreFromBackup(openZip: () -> InputStream) {
        BackupRestoreEngine(store, attachmentsDir).restore(openZip)

        seedingPreferenceManager.setHasSeededDefaultCategories(true)
        seedingPreferenceManager.setHasClearedSeededCustomFields(true)
    }

    override suspend fun resetAllData(context: Context) {
        val defaultEntities = DefaultCategorySeeder.buildDomainCategories(context)
            .map { CategoryEntity.fromDomain(it) }

        val replacedFileNames = database.withTransaction {
            val fileNames = managedFileNames(database.invoiceDao().getAll())
            database.invoiceDao().deleteAll()
            database.categoryDao().deleteAll()
            database.categoryDao().insertAll(defaultEntities)
            fileNames
        }

        // Committed: the deleted invoices' managed attachment files are now unreferenced.
        // Best-effort; a failure here only leaves files behind and never fails the reset.
        withContext(NonCancellable + Dispatchers.IO) {
            AttachmentFileCleanup.deleteUnreferenced(
                attachmentsDir,
                replacedFileNames,
                store::isAttachmentFileReferenced
            )
        }

        seedingPreferenceManager.setHasSeededDefaultCategories(true)
        seedingPreferenceManager.setHasClearedSeededCustomFields(true)
    }

    private fun managedFileNames(invoices: List<InvoiceEntity>): Set<String> =
        invoices.mapNotNull { it.attachmentFileName?.takeIf(String::isNotBlank) }.toSet()
}

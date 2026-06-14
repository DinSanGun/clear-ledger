package com.dinyairsadot.taxtracker.core.data.repositories

import androidx.room.withTransaction
import com.dinyairsadot.taxtracker.core.data.SeedingPreferenceManager
import com.dinyairsadot.taxtracker.core.data.TaxTrackerDatabase
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.data.entities.InvoiceEntity
import com.dinyairsadot.taxtracker.core.domain.BackupRestoreRepository
import com.dinyairsadot.taxtracker.core.util.backup.BackupMapper
import com.dinyairsadot.taxtracker.core.util.backup.BackupPayload

class RoomBackupRestoreRepository(
    private val database: TaxTrackerDatabase,
    private val seedingPreferenceManager: SeedingPreferenceManager
) : BackupRestoreRepository {

    override suspend fun restoreFromBackup(payload: BackupPayload) {
        val categoryEntities = payload.categories
            .map { BackupMapper.fromCategoryDto(it) }
            .map { CategoryEntity.fromDomain(it) }
        val invoiceEntities = payload.invoices
            .map { BackupMapper.fromInvoiceDto(it) }
            .map { InvoiceEntity.fromDomain(it) }

        database.withTransaction {
            database.invoiceDao().deleteAll()
            database.categoryDao().deleteAll()
            database.categoryDao().insertAll(categoryEntities)
            database.invoiceDao().insertAll(invoiceEntities)
        }

        seedingPreferenceManager.setHasSeededDefaultCategories(true)
        seedingPreferenceManager.setHasClearedSeededCustomFields(true)
    }
}

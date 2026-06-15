package com.dinyairsadot.clearledger.core.data.repositories

import androidx.room.withTransaction
import com.dinyairsadot.clearledger.core.data.SeedingPreferenceManager
import com.dinyairsadot.clearledger.core.data.ClearLedgerDatabase
import com.dinyairsadot.clearledger.core.data.entities.CategoryEntity
import com.dinyairsadot.clearledger.core.data.entities.InvoiceEntity
import com.dinyairsadot.clearledger.core.domain.BackupRestoreRepository
import com.dinyairsadot.clearledger.core.util.backup.BackupMapper
import com.dinyairsadot.clearledger.core.util.backup.BackupPayload

class RoomBackupRestoreRepository(
    private val database: ClearLedgerDatabase,
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

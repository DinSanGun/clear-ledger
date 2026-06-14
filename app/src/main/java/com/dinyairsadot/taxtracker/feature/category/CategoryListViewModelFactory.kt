package com.dinyairsadot.taxtracker.feature.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dinyairsadot.taxtracker.core.data.SeedingPreferenceManager
import com.dinyairsadot.taxtracker.core.data.TaxTrackerDatabase
import com.dinyairsadot.taxtracker.core.data.repositories.RoomBackupRestoreRepository
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository

class CategoryListViewModelFactory(
    private val categoryRepository: CategoryRepository,
    private val invoiceRepository: InvoiceRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            val database = TaxTrackerDatabase.getDatabase(context)
            val backupRestoreRepository = RoomBackupRestoreRepository(
                database = database,
                seedingPreferenceManager = SeedingPreferenceManager(context)
            )
            return CategoryListViewModel(
                categoryRepository,
                invoiceRepository,
                context,
                backupRestoreRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

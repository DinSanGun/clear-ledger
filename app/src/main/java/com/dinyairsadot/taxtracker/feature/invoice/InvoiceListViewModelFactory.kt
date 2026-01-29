package com.dinyairsadot.taxtracker.feature.invoice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository

class InvoiceListViewModelFactory(
    private val invoiceRepository: InvoiceRepository,
    private val categoryRepository: CategoryRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceListViewModel::class.java)) {
            return InvoiceListViewModel(invoiceRepository, categoryRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.dinyairsadot.taxtracker.feature.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
            return CategoryListViewModel(categoryRepository, invoiceRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

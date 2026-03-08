package com.dinyairsadot.taxtracker.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomCategoryRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomInvoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.dinyairsadot.taxtracker.R

data class CategoryUi(
    val id: Long,
    val name: String,
    val colorHex: String,
    val description: String,
    val customFieldTitle1: String? = null,
    val customFieldTitle2: String? = null,
    val customFieldTitle3: String? = null,
    val unpaidInvoicesCount: Int = 0,
    val totalInvoicesCount: Int = 0
)

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val errorMessage: String? = null
)

class CategoryListViewModel(
    private val categoryRepository: CategoryRepository,
    private val invoiceRepository: InvoiceRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState(isLoading = true))
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    init {
        loadInitialCategories()
    }

    private fun loadInitialCategories() {
        viewModelScope.launch {
            _uiState.value = CategoryListUiState(isLoading = true)

            try {
                val categories = categoryRepository.getCategories()
                val uiCategories = categories.map { category ->
                    val invoices = invoiceRepository.getInvoicesForCategory(category.id)
                    val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
                    val totalCount = invoices.size
                    category.toUi(unpaidCount, totalCount)
                }

                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = uiCategories,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = emptyList(),
                    errorMessage = context.getString(R.string.failed_to_load_categories)
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // Keep existing categories visible while refreshing counts
            val currentState = _uiState.value
            val hasExistingCategories = currentState.categories.isNotEmpty()
            
            // Only show loading if we have no categories yet
            if (!hasExistingCategories) {
                _uiState.value = currentState.copy(isLoading = true)
            }

            try {
                val categories = categoryRepository.getCategories()
                val uiCategories = categories.map { category ->
                    val invoices = invoiceRepository.getInvoicesForCategory(category.id)
                    val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
                    val totalCount = invoices.size
                    category.toUi(unpaidCount, totalCount)
                }

                // Update state without clearing existing data or showing loading
                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = uiCategories,
                    errorMessage = null
                )
            } catch (e: Exception) {
                // On error, only update error message, keep existing categories visible
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = context.getString(R.string.failed_to_load_categories)
                )
            }
        }
    }

    fun onCategoryClicked(id: Long) {
        // Add selection logic later
    }

    fun onAddCategoryClicked() {
        // Add analytics / logging later
    }

    fun addCategory(
        name: String,
        colorHex: String,
        description: String,
        customFieldTitles: List<String>
    ) {
        viewModelScope.launch {
            try {
                // ✅ If color is blank, use a default
                val safeColorHex = if (colorHex.isBlank()) "#FF9800" else colorHex.trim()

                // Use id=0 to let Room auto-generate the ID
                val newCategory = Category(
                    id = 0,
                    name = name,
                    colorHex = safeColorHex,
                    description = description.ifBlank { null },
                    customFieldTitles = customFieldTitles,
                    pinnedDefaults = emptyMap()
                )

                categoryRepository.addCategory(newCategory)

                val updated = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(
                    categories = updated.map { category ->
                        val invoices = invoiceRepository.getInvoicesForCategory(category.id)
                        val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
                        val totalCount = invoices.size
                        category.toUi(unpaidCount, totalCount)
                    },
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = context.getString(R.string.failed_to_add_category)
                )
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(id)

                val updated = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(
                    categories = updated.map { category ->
                        val invoices = invoiceRepository.getInvoicesForCategory(category.id)
                        val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
                        val totalCount = invoices.size
                        category.toUi(unpaidCount, totalCount)
                    },
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = context.getString(R.string.failed_to_delete_category)
                )
            }
        }
    }

    fun updateCategory(
        id: Long,
        name: String,
        colorHex: String,
        description: String,
        customFieldTitles: List<String>
    ) {
        viewModelScope.launch {
            try {
                // Reuse the same safe color logic as addCategory
                val safeColorHex = if (colorHex.isBlank()) "#FF9800" else colorHex.trim()

                val updatedCategory = Category(
                    id = id,
                    name = name.trim(),
                    colorHex = safeColorHex,
                    description = description.trim().ifBlank { null },
                    customFieldTitles = customFieldTitles,
                    pinnedDefaults = emptyMap()
                )

                categoryRepository.updateCategory(updatedCategory)

                val updatedList = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(
                    categories = updatedList.map { category ->
                        val invoices = invoiceRepository.getInvoicesForCategory(category.id)
                        val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
                        val totalCount = invoices.size
                        category.toUi(unpaidCount, totalCount)
                    },
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = context.getString(R.string.failed_to_update_category)
                )
            }
        }
    }

    /**
     * Checks if any invoices for the given category have data in the specified custom field index.
     * Returns true if at least one invoice has a non-blank value at that index.
     */
    suspend fun hasInvoicesWithFieldData(categoryId: Long, fieldIndex: Int): Boolean {
        val invoices = invoiceRepository.getInvoicesForCategory(categoryId)
        return invoices.any { invoice ->
            invoice.customFieldValues.getOrNull(fieldIndex)?.isNotBlank() == true
        }
    }

    /**
     * Gets a category by ID from the repository.
     */
    suspend fun getCategoryById(id: Long): Category? {
        return categoryRepository.getCategories().firstOrNull { it.id == id }
    }
}

// Mapping from domain model to UI model
private fun Category.toUi(unpaidCount: Int = 0, totalCount: Int = 0): CategoryUi {
    return CategoryUi(
        id = this.id.toLong(),
        name = this.name,
        colorHex = this.colorHex,
        description = this.description ?: "",
        customFieldTitle1 = this.customFieldTitles.getOrNull(0),
        customFieldTitle2 = this.customFieldTitles.getOrNull(1),
        customFieldTitle3 = this.customFieldTitles.getOrNull(2),
        unpaidInvoicesCount = unpaidCount,
        totalInvoicesCount = totalCount
    )
}

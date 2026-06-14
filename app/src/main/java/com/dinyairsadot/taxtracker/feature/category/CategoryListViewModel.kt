package com.dinyairsadot.taxtracker.feature.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.dinyairsadot.taxtracker.core.domain.BackupRestoreRepository
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository
import com.dinyairsadot.taxtracker.core.util.AllExportData
import com.dinyairsadot.taxtracker.core.util.backup.BackupData
import com.dinyairsadot.taxtracker.core.util.backup.BackupPayload
import com.dinyairsadot.taxtracker.core.util.backup.BackupValidationResult
import com.dinyairsadot.taxtracker.core.util.backup.BackupValidator
import com.dinyairsadot.taxtracker.core.util.backup.BackupZipImporter
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
    val totalInvoicesCount: Int = 0,
    val orderIndex: Int = 0
)

data class CategoryListUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val errorMessage: String? = null,
    val isReorderMode: Boolean = false
)

class CategoryListViewModel(
    private val categoryRepository: CategoryRepository,
    private val invoiceRepository: InvoiceRepository,
    private val context: Context,
    private val backupRestoreRepository: BackupRestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState(isLoading = true))
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    init {
        loadInitialCategories()
    }

    private fun loadInitialCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val categories = categoryRepository.getCategories()
                val uiCategories = buildUiCategories(categories)

                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = uiCategories,
                    errorMessage = null,
                    isReorderMode = _uiState.value.isReorderMode
                )
            } catch (e: Exception) {
                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = emptyList(),
                    errorMessage = context.getString(R.string.failed_to_load_categories),
                    isReorderMode = _uiState.value.isReorderMode
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
                val uiCategories = buildUiCategories(categories)

                // Update state without clearing existing data or showing loading
                _uiState.value = CategoryListUiState(
                    isLoading = false,
                    categories = uiCategories,
                    errorMessage = null,
                    isReorderMode = currentState.isReorderMode
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
                    pinnedDefaults = emptyMap(),
                    orderIndex = 0
                )

                categoryRepository.addCategory(newCategory)

                val updated = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(categories = buildUiCategories(updated), errorMessage = null)
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
                _uiState.value = _uiState.value.copy(categories = buildUiCategories(updated), errorMessage = null)
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
                    pinnedDefaults = emptyMap(),
                    orderIndex = _uiState.value.categories.firstOrNull { it.id == id }?.orderIndex ?: 0
                )

                categoryRepository.updateCategory(updatedCategory)

                val updatedList = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(categories = buildUiCategories(updatedList), errorMessage = null)
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

    fun enterReorderMode() {
        _uiState.value = _uiState.value.copy(isReorderMode = true)
    }

    fun exitReorderMode() {
        _uiState.value = _uiState.value.copy(isReorderMode = false)
    }

    fun moveCategoryUp(categoryId: Long) {
        moveCategoryByOffset(categoryId = categoryId, offset = -1)
    }

    fun moveCategoryDown(categoryId: Long) {
        moveCategoryByOffset(categoryId = categoryId, offset = 1)
    }

    private fun moveCategoryByOffset(categoryId: Long, offset: Int) {
        viewModelScope.launch {
            val current = _uiState.value.categories
            val currentIndex = current.indexOfFirst { it.id == categoryId }
            if (currentIndex == -1) return@launch
            val targetIndex = currentIndex + offset
            if (targetIndex !in current.indices) return@launch

            val reordered = current.toMutableList().apply {
                val item = removeAt(currentIndex)
                add(targetIndex, item)
            }
            _uiState.value = _uiState.value.copy(
                categories = reordered.mapIndexed { index, item -> item.copy(orderIndex = index) },
                errorMessage = null
            )
            categoryRepository.updateCategoryOrder(_uiState.value.categories.map { it.id })
        }
    }

    suspend fun loadAllDataForExport(): AllExportData {
        val categories = categoryRepository.getCategories()
        val invoicesByCategory = categories.associate { category ->
            category.id to invoiceRepository.getInvoicesForCategory(category.id)
        }
        return AllExportData(categories, invoicesByCategory)
    }

    suspend fun loadAllDataForBackup(): BackupData {
        val categories = categoryRepository.getCategories()
        val invoices = invoiceRepository.getAllInvoices()
        return BackupData(categories, invoices)
    }

    suspend fun validateAndParseBackup(uri: Uri): BackupValidationResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return BackupValidationResult.Invalid("Failed to open backup file")
            inputStream.use { stream ->
                val payload = BackupZipImporter.readPayload(stream)
                BackupValidator.validate(payload)
            }
        } catch (_: Exception) {
            BackupValidationResult.Invalid("Failed to read backup file")
        }
    }

    suspend fun performRestore(payload: BackupPayload) {
        backupRestoreRepository.restoreFromBackup(payload)
    }

    private suspend fun buildUiCategories(categories: List<Category>): List<CategoryUi> {
        return categories.map { category ->
            val invoices = invoiceRepository.getInvoicesForCategory(category.id)
            val unpaidCount = invoices.count { it.paymentStatus != com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID }
            val totalCount = invoices.size
            category.toUi(unpaidCount, totalCount)
        }
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
        totalInvoicesCount = totalCount,
        orderIndex = this.orderIndex
    )
}

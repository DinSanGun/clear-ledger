package com.dinyairsadot.taxtracker.feature.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinyairsadot.taxtracker.core.domain.Invoice
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomCategoryRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomInvoiceRepository
import android.content.Context
import com.dinyairsadot.taxtracker.R


enum class SortOption {
    DATE_DESCENDING,
    DATE_ASCENDING,
    AMOUNT_DESCENDING,
    AMOUNT_ASCENDING
}

data class InvoiceUi(
    val id: Long,
    val invoiceNumber: String,
    val amount: Double,
    val paymentStatus: PaymentStatus,
    val dueDateText: String?,
    val dueDate: LocalDate? = null, // Add for sorting
    val notes: String?,
    val customFieldValues: List<String> = emptyList()
)

data class InvoiceListUiState(
    val isLoading: Boolean = false,
    val categoryName: String? = null,
    val categoryColorHex: String? = null,
    val categoryCustomFieldTitles: List<String> = emptyList(),
    val invoices: List<InvoiceUi> = emptyList(),
    val errorMessage: String? = null,
    val sortOption: SortOption = SortOption.DATE_DESCENDING
)

class InvoiceListViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val categoryRepository: CategoryRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceListUiState(isLoading = true))
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()


    fun loadCategoryHeader(categoryId: Long) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategories().firstOrNull { it.id == categoryId }
                _uiState.value = _uiState.value.copy(
                    categoryName = category?.name,
                    categoryColorHex = category?.colorHex,
                    categoryCustomFieldTitles = category?.customFieldTitles ?: emptyList()
                )
            } catch (_: Exception) {
                // If category can't be loaded, keep title fallback in UI
                _uiState.value = _uiState.value.copy(
                    categoryName = null,
                    categoryColorHex = null,
                    categoryCustomFieldTitles = emptyList()
                )
            }
        }
    }

    /**
     * Load invoices for a given category.
     * This is called from the UI using LaunchedEffect(categoryId).
     */
    fun loadInvoices(categoryId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val invoices = invoiceRepository.getInvoicesForCategory(categoryId)
                val sortedInvoices = sortInvoices(invoices.map { it.toUi() }, _uiState.value.sortOption)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    invoices = sortedInvoices,
                    errorMessage = null
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    invoices = emptyList(),
                    errorMessage = context.getString(R.string.failed_to_load_invoices)
                )
            }
        }
    }
    
    fun setSortOption(sortOption: SortOption) {
        val currentInvoices = _uiState.value.invoices
        val sortedInvoices = sortInvoices(currentInvoices, sortOption)
        _uiState.value = _uiState.value.copy(
            sortOption = sortOption,
            invoices = sortedInvoices
        )
    }
    
    private fun sortInvoices(invoices: List<InvoiceUi>, sortOption: SortOption): List<InvoiceUi> {
        return when (sortOption) {
            SortOption.DATE_DESCENDING -> invoices.sortedWith(
                compareByDescending<InvoiceUi> { it.dueDate ?: LocalDate.MIN }
                    .thenByDescending { it.id }
            )
            SortOption.DATE_ASCENDING -> invoices.sortedWith(
                compareBy<InvoiceUi> { it.dueDate ?: LocalDate.MAX }
                    .thenBy { it.id }
            )
            SortOption.AMOUNT_DESCENDING -> invoices.sortedWith(
                compareByDescending<InvoiceUi> { it.amount }
                    .thenByDescending { it.id }
            )
            SortOption.AMOUNT_ASCENDING -> invoices.sortedWith(
                compareBy<InvoiceUi> { it.amount }
                    .thenBy { it.id }
            )
        }
    }

    fun addInvoice(
        categoryId: Long,
        amount: Double,
        dateText: String,
        paymentStatus: PaymentStatus,
        notes: String,
        customFieldValues: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val parsedDate = dateText
                .takeIf { it.isNotBlank() }
                ?.let { text ->
                    val trimmed = text.trim()
                    runCatching {
                        // Try DD/MM/YYYY format
                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }.getOrNull()
                        ?: runCatching {
                            // Fallback to YYYY-MM-DD format (for backward compatibility)
                            LocalDate.parse(trimmed)
                        }.getOrNull()
                }

            // Use id=0 to let Room auto-generate the ID
            val newInvoice = Invoice(
                id = 0,
                categoryId = categoryId,
                invoiceNumber = "", // can be expanded later
                amount = amount,
                paymentStatus = paymentStatus,
                dueDate = parsedDate,
                paymentDate = null,
                consumptionValue = null,
                consumptionUnit = null,
                notes = notes.ifBlank { null },
                customFieldValues = customFieldValues.map { it.trim() }.filter { it.isNotBlank() }
            )

            invoiceRepository.addInvoice(newInvoice)
            loadInvoices(categoryId)
        }
    }

    suspend fun getInvoice(invoiceId: Long): Invoice? {
        return invoiceRepository.getInvoiceById(invoiceId)
    }

    fun updateInvoice(
        invoiceId: Long,
        amount: Double,
        dateText: String,
        paymentStatus: PaymentStatus,
        notes: String,
        customFieldValues: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val existing = invoiceRepository.getInvoiceById(invoiceId) ?: return@launch

            val parsedDate = dateText
                .takeIf { it.isNotBlank() }
                ?.let { text ->
                    val trimmed = text.trim()
                    runCatching {
                        // Try DD/MM/YYYY format
                        LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }.getOrNull()
                        ?: runCatching {
                            // Fallback to YYYY-MM-DD format (for backward compatibility)
                            LocalDate.parse(trimmed)
                        }.getOrNull()
                }

            val updated = existing.copy(
                amount = amount,
                paymentStatus = paymentStatus,
                dueDate = parsedDate,
                notes = notes.ifBlank { null },
                customFieldValues = customFieldValues.map { it.trim() }.filter { it.isNotBlank() }
            )

            invoiceRepository.updateInvoice(updated)

            // Refresh list so InvoiceListScreen sees updated data
            loadInvoices(existing.categoryId)
        }
    }

    fun deleteInvoice(
        invoiceId: Long,
        categoryId: Long
    ) {
        viewModelScope.launch {
            invoiceRepository.deleteInvoice(invoiceId)
            // Refresh list so InvoiceListScreen sees updated data
            loadInvoices(categoryId)
        }
    }
}

// Mapping from domain model to UI model
private fun Invoice.toUi(): InvoiceUi {
    return InvoiceUi(
        id = this.id,
        invoiceNumber = this.invoiceNumber,
        amount = this.amount,
        paymentStatus = this.paymentStatus,
        dueDateText = this.dueDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
        dueDate = this.dueDate, // Include for sorting
        notes = this.notes,
        customFieldValues = this.customFieldValues
    )
}

package com.dinyairsadot.taxtracker.feature.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.DocumentType
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
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
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
    val vendorName: String? = null,
    val issueDateText: String? = null,
    val dueDateText: String?,
    val dueDate: LocalDate? = null, // Add for sorting
    val paymentDateText: String? = null,
    val servicePeriodStartText: String? = null,
    val servicePeriodEndText: String? = null,
    val notes: String?,
    val customFieldValues: List<String> = emptyList(),
    val documentType: DocumentType? = null,
    // New core fields
    val documentNumber: String = invoiceNumber,  // New field (fallback to invoiceNumber for compatibility)
    val amountDue: Double = amount,              // New field (fallback to amount for compatibility)
    val paymentMethod: String? = null,
    val confirmationNumber: String? = null,
    // Pinned snapshot
    val pinnedSnapshot: Map<String, String> = emptyMap(),
    // Explicit mode; never infer this from dates.
    val servicePeriodMode: ServicePeriodMode = ServicePeriodMode.MONTH
)

data class InvoiceListUiState(
    val isLoading: Boolean = false,
    val categoryName: String? = null,
    val categoryColorHex: String? = null,
    val categoryCustomFieldTitles: List<String> = emptyList(),
    val categoryPinnedSupplierName: String? = null,
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

    /**
     * Determines the default document type based on category name.
     * Returns null if no default should be set (user must select).
     */
    fun getDefaultDocumentType(categoryName: String?): DocumentType? {
        if (categoryName == null) return null
        
        val nameLower = categoryName.lowercase()
        
        // Utility bills -> BILL_DEMAND
        if (nameLower.contains("arnona") || 
            nameLower.contains("water") || 
            nameLower.contains("electricity") || 
            nameLower.contains("gas") || 
            nameLower.contains("phone") || 
            nameLower.contains("internet") ||
            nameLower.contains("national insurance") ||
            nameLower.contains("ביטוח לאומי")) {
            return DocumentType.BILL_DEMAND
        }
        
        // Business expenses -> TAX_INVOICE
        if (nameLower.contains("business") || 
            nameLower.contains("expense") ||
            nameLower.contains("הוצאות עסקיות") ||
            nameLower.contains("עסקי")) {
            return DocumentType.TAX_INVOICE
        }
        
        return null
    }

    fun loadCategoryHeader(categoryId: Long) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategories().firstOrNull { it.id == categoryId }
                _uiState.value = _uiState.value.copy(
                    categoryName = category?.name,
                    categoryColorHex = category?.colorHex,
                    categoryCustomFieldTitles = category?.customFieldTitles ?: emptyList(),
                    categoryPinnedSupplierName = category?.pinnedDefaults?.get(Category.PINNED_KEY_SUPPLIER_NAME)
                )
            } catch (_: Exception) {
                // If category can't be loaded, keep title fallback in UI
                _uiState.value = _uiState.value.copy(
                    categoryName = null,
                    categoryColorHex = null,
                    categoryCustomFieldTitles = emptyList(),
                    categoryPinnedSupplierName = null
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
        documentNumber: String,
        amountDue: Double,
        paymentStatus: PaymentStatus,
        servicePeriodStartText: String,
        servicePeriodEndText: String,
        servicePeriodMode: ServicePeriodMode,
        paymentMethod: String?,
        confirmationNumber: String?,
        notes: String,
        customFieldValues: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            fun parseDate(dateText: String): LocalDate? {
                val trimmed = dateText.trim()
                return runCatching {
                    LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrNull()
            }

            val servicePeriodStart = parseDate(servicePeriodStartText)
            val servicePeriodEnd = parseDate(servicePeriodEndText)

            // Snapshot category's pinned defaults at invoice creation time
            val category = categoryRepository.getCategories().firstOrNull { it.id == categoryId }
            val pinnedSnapshot = category?.pinnedDefaults ?: emptyMap()

            // Use id=0 to let Room auto-generate the ID
            val newInvoice = Invoice(
                id = 0,
                categoryId = categoryId,
                // Old fields (backward compatibility)
                invoiceNumber = documentNumber,  // Map new to old
                amount = amountDue,             // Map new to old
                paymentStatus = paymentStatus,
                vendorName = null,
                issueDate = null,
                dueDate = servicePeriodEnd,      // Use service period end as due date for sorting
                paymentDate = null,
                servicePeriodStart = servicePeriodStart,
                servicePeriodEnd = servicePeriodEnd,
                consumptionValue = null,
                consumptionUnit = null,
                notes = notes.ifBlank { null },
                customFieldValues = customFieldValues.map { it.trim() },
                documentType = null,
                // New fields
                amountDue = amountDue,
                documentNumber = documentNumber,
                paymentMethod = paymentMethod,
                confirmationNumber = confirmationNumber,
                // Pinned snapshot: capture category defaults at creation time
                pinnedSnapshot = pinnedSnapshot,
                servicePeriodMode = servicePeriodMode
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
        documentNumber: String,
        amountDue: Double,
        paymentStatus: PaymentStatus,
        servicePeriodStartText: String,
        servicePeriodEndText: String,
        servicePeriodMode: ServicePeriodMode,
        paymentMethod: String?,
        confirmationNumber: String?,
        notes: String,
        customFieldValues: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val existing = invoiceRepository.getInvoiceById(invoiceId) ?: return@launch

            fun parseDate(dateText: String): LocalDate? {
                val trimmed = dateText.trim()
                return runCatching {
                    LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrNull()
            }

            val servicePeriodStart = parseDate(servicePeriodStartText)
            val servicePeriodEnd = parseDate(servicePeriodEndText)

            val updated = existing.copy(
                // Old fields (backward compatibility)
                invoiceNumber = documentNumber,
                amount = amountDue,
                paymentStatus = paymentStatus,
                dueDate = servicePeriodEnd,  // Use service period end as due date for sorting
                servicePeriodStart = servicePeriodStart,
                servicePeriodEnd = servicePeriodEnd,
                notes = notes.ifBlank { null },
                customFieldValues = customFieldValues.map { it.trim() },
                // New fields
                amountDue = amountDue,
                documentNumber = documentNumber,
                paymentMethod = paymentMethod,
                confirmationNumber = confirmationNumber,
                servicePeriodMode = servicePeriodMode
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
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return InvoiceUi(
        id = this.id,
        invoiceNumber = this.invoiceNumber,
        amount = this.amount,
        paymentStatus = this.paymentStatus,
        vendorName = this.vendorName,
        issueDateText = this.issueDate?.format(dateFormatter),
        dueDateText = this.dueDate?.format(dateFormatter),
        dueDate = this.dueDate, // Include for sorting
        paymentDateText = this.paymentDate?.format(dateFormatter),
        servicePeriodStartText = this.servicePeriodStart?.format(dateFormatter),
        servicePeriodEndText = this.servicePeriodEnd?.format(dateFormatter),
        notes = this.notes,
        customFieldValues = this.customFieldValues,
        documentType = this.documentType,
        // New core fields
        documentNumber = this.documentNumber,
        amountDue = this.amountDue,
        paymentMethod = this.paymentMethod,
        confirmationNumber = this.confirmationNumber,
        pinnedSnapshot = this.pinnedSnapshot,
        servicePeriodMode = this.servicePeriodMode
    )
}

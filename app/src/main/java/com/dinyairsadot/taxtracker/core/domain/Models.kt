package com.dinyairsadot.taxtracker.core.domain

import java.time.Instant
import java.time.LocalDate

enum class PaymentStatus {
    PAID_FULL,
    NOT_PAID,
    PAID_CREDIT
}

enum class CustomFieldType {
    TEXT,
    NUMBER,
    DATE,
    BOOLEAN
}

enum class DocumentType {
    BILL_DEMAND,
    TAX_INVOICE,
    INVOICE_RECEIPT
}

data class Category(
    val id: Long,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val customFieldTitles: List<String> = emptyList(), // Up to 10 custom field titles
    val supplierName: String? = null,  // Kept for backward compatibility
    val pinnedDefaults: Map<String, String> = emptyMap(),  // Pinned default values (e.g., "supplierName" -> "Israel Electric Company")
    val seedKey: String? = null,       // Stable key for seeded categories (e.g. "arnona"); null for user-created
    val userEdited: Boolean = false     // True after user saves edits; prevents locale overwrite
) {
    // Backward compatibility: getters for old field names
    val customFieldTitle1: String? get() = customFieldTitles.getOrNull(0)
    val customFieldTitle2: String? get() = customFieldTitles.getOrNull(1)
    val customFieldTitle3: String? get() = customFieldTitles.getOrNull(2)
    
    companion object {
        const val MAX_CUSTOM_FIELDS = 10
        const val PINNED_KEY_SUPPLIER_NAME = "supplierName"
    }
}

/**
 * Defines a custom field in a given category's schema.
 * Example: "Meter ID" (TEXT), "Billing period start" (DATE)
 */
data class CustomFieldDefinition(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val type: CustomFieldType,
    val isRequired: Boolean,
    val isArchived: Boolean = false, // future soft-delete support
    val order: Int = 0
)

/**
 * Represents a single invoice / bill entry.
 * New minimal core fields (prepared for future migration):
 * - amountDue, documentNumber, servicePeriodStart, servicePeriodEnd (required in new model)
 * - paymentMethod (optional)
 * Old fields kept for backward compatibility during transition.
 */
data class Invoice(
    val id: Long,
    val categoryId: Long,
    // Old fields (kept for compatibility)
    val invoiceNumber: String,
    val amount: Double,
    val paymentStatus: PaymentStatus,
    val vendorName: String? = null,
    val issueDate: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val paymentDate: LocalDate? = null,
    val servicePeriodStart: LocalDate? = null,
    val servicePeriodEnd: LocalDate? = null,
    val consumptionValue: Double? = null,
    val consumptionUnit: String? = null,
    val notes: String? = null,
    val customFieldValues: List<String> = emptyList(),
    val documentType: DocumentType? = null,
    // New minimal core fields (added for future migration)
    val amountDue: Double = amount,  // defaults to old amount
    val documentNumber: String = invoiceNumber,  // defaults to old invoiceNumber
    val paymentMethod: String? = null,
    val confirmationNumber: String? = null,
    // Pinned snapshot: captures category.pinnedDefaults at invoice creation time
    val pinnedSnapshot: Map<String, String> = emptyMap()
) {
    // Backward compatibility: getters for old field names
    val customFieldValue1: String? get() = customFieldValues.getOrNull(0)
    val customFieldValue2: String? get() = customFieldValues.getOrNull(1)
    val customFieldValue3: String? get() = customFieldValues.getOrNull(2)
}

/**
 * Stores the value of a custom field for a specific invoice.
 * The value is stored as String and interpreted according to CustomFieldType.
 */
data class InvoiceCustomFieldValue(
    val id: Long,
    val invoiceId: Long,
    val fieldDefinitionId: Long,
    val value: String
)

/**
 * Attachment (photo / image) of an invoice.
 */
data class InvoiceImage(
    val id: Long,
    val invoiceId: Long,
    val uri: String,             // URI/path to the image in app storage
    val createdAt: Instant = Instant.now()
)

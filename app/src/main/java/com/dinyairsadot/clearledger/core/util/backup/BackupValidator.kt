package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.DocumentType
import com.dinyairsadot.clearledger.core.domain.InvoiceCurrency
import com.dinyairsadot.clearledger.core.domain.PaymentStatus
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Validates a [BackupPayload] before restore.
 *
 * Gson can produce null values for fields that Kotlin models as non-null when JSON omits them.
 * All checks use explicit null guards or [runCatching] before calling [Enum.valueOf] or
 * [LocalDate.parse]. Invalid input returns [BackupValidationResult.Invalid], never throws.
 *
 * [BackupMetadata.dbSchemaVersion] is informational only; restore is allowed when
 * [BackupPayload.formatVersion] matches [BackupFormat.FORMAT_VERSION].
 */
object BackupValidator {

    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val colorHexRegex = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

    fun validate(payload: BackupPayload): BackupValidationResult {
        if (payload.formatVersion != BackupFormat.FORMAT_VERSION) {
            return BackupValidationResult.UnsupportedVersion(payload.formatVersion)
        }

        val metadata = payload.metadata
            ?: return BackupValidationResult.Invalid("Missing metadata")
        if (metadata.dbSchemaVersion <= 0) {
            return BackupValidationResult.Invalid("Invalid dbSchemaVersion")
        }
        if (metadata.producer.isNullOrBlank()) {
            return BackupValidationResult.Invalid("Missing producer")
        }

        val categories = payload.categories
            ?: return BackupValidationResult.Invalid("Missing categories")
        val invoices = payload.invoices
            ?: return BackupValidationResult.Invalid("Missing invoices")

        val categoryIds = mutableSetOf<Long>()
        for (category in categories) {
            if (category.id <= 0) {
                return BackupValidationResult.Invalid("Invalid category id: ${category.id}")
            }
            if (!categoryIds.add(category.id)) {
                return BackupValidationResult.Invalid("Duplicate category id: ${category.id}")
            }
            if (category.name.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Category name is required")
            }
            if (category.colorHex.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Category colorHex is required")
            }
            if (!colorHexRegex.matches(category.colorHex)) {
                return BackupValidationResult.Invalid("Invalid category colorHex: ${category.colorHex}")
            }
            if (category.customFieldTitles == null) {
                return BackupValidationResult.Invalid("Category customFieldTitles is required")
            }
            if (category.pinnedDefaults == null) {
                return BackupValidationResult.Invalid("Category pinnedDefaults is required")
            }
        }

        val invoiceIds = mutableSetOf<Long>()
        for (invoice in invoices) {
            if (invoice.id <= 0) {
                return BackupValidationResult.Invalid("Invalid invoice id: ${invoice.id}")
            }
            if (!invoiceIds.add(invoice.id)) {
                return BackupValidationResult.Invalid("Duplicate invoice id: ${invoice.id}")
            }
            if (invoice.categoryId <= 0) {
                return BackupValidationResult.Invalid("Invalid invoice categoryId: ${invoice.categoryId}")
            }
            if (!categoryIds.contains(invoice.categoryId)) {
                return BackupValidationResult.Invalid(
                    "Invoice ${invoice.id} references unknown category ${invoice.categoryId}"
                )
            }
            if (invoice.invoiceNumber.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Invoice number is required")
            }
            if (invoice.documentNumber.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Document number is required")
            }
            if (invoice.amount < 0) {
                return BackupValidationResult.Invalid("Invoice amount cannot be negative")
            }
            if (invoice.amountDue < 0) {
                return BackupValidationResult.Invalid("Invoice amountDue cannot be negative")
            }
            if (invoice.paymentStatus.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Payment status is required")
            }
            if (runCatching { PaymentStatus.valueOf(invoice.paymentStatus) }.isFailure) {
                return BackupValidationResult.Invalid("Invalid payment status: ${invoice.paymentStatus}")
            }
            if (invoice.amountCurrency.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Amount currency is required")
            }
            if (runCatching { InvoiceCurrency.valueOf(invoice.amountCurrency) }.isFailure) {
                return BackupValidationResult.Invalid("Invalid amount currency: ${invoice.amountCurrency}")
            }
            if (invoice.servicePeriodMode.isNullOrBlank()) {
                return BackupValidationResult.Invalid("Service period mode is required")
            }
            if (runCatching { ServicePeriodMode.valueOf(invoice.servicePeriodMode) }.isFailure) {
                return BackupValidationResult.Invalid(
                    "Invalid service period mode: ${invoice.servicePeriodMode}"
                )
            }
            if (invoice.documentType != null &&
                runCatching { DocumentType.valueOf(invoice.documentType) }.isFailure
            ) {
                return BackupValidationResult.Invalid("Invalid document type: ${invoice.documentType}")
            }
            for (dateField in listOf(
                invoice.issueDate,
                invoice.dueDate,
                invoice.paymentDate,
                invoice.servicePeriodStart,
                invoice.servicePeriodEnd
            )) {
                if (dateField != null &&
                    runCatching { LocalDate.parse(dateField, isoDateFormatter) }.isFailure
                ) {
                    return BackupValidationResult.Invalid("Invalid date: $dateField")
                }
            }
            if (invoice.customFieldValues == null) {
                return BackupValidationResult.Invalid("Invoice customFieldValues is required")
            }
            if (invoice.pinnedSnapshot == null) {
                return BackupValidationResult.Invalid("Invoice pinnedSnapshot is required")
            }
        }

        return BackupValidationResult.Valid(payload)
    }
}

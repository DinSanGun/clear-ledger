package com.dinyairsadot.taxtracker.core.util

import com.dinyairsadot.taxtracker.core.domain.InvoiceCurrency
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import com.dinyairsadot.taxtracker.core.domain.ServicePeriodMode
import java.time.LocalDate

/**
 * Localized column headers and display values for user-facing CSV export.
 * Built in the UI layer (e.g. via [stringResource]) and passed to [InvoiceCsvExporter].
 */
data class InvoiceCsvExportLabels(
    val categoryNameHeader: String,
    val invoiceNumberHeader: String,
    val amountHeader: String,
    val currencyHeader: String,
    val statusHeader: String,
    val servicePeriodModeHeader: String,
    val servicePeriodStartHeader: String,
    val servicePeriodEndHeader: String,
    val dueDateHeader: String,
    val paymentDateHeader: String,
    val paymentMethodHeader: String,
    val confirmationNumberHeader: String,
    val notesHeader: String,
    /** Localized fallback when a category custom field title is blank (1-based index). */
    val customFieldFallbackHeader: (index: Int) -> String,
    val paymentStatusPaid: String,
    val paymentStatusNotPaid: String,
    val servicePeriodModeMonth: String,
    val servicePeriodModeDate: String,
    val currencyIls: String,
    val currencyUsd: String,
    val paymentMethodByStoredValue: Map<String, String>,
    val formatDate: (LocalDate?) -> String
) {
    fun paymentStatusLabel(status: PaymentStatus): String = when (status) {
        PaymentStatus.PAID -> paymentStatusPaid
        PaymentStatus.NOT_PAID -> paymentStatusNotPaid
    }

    fun servicePeriodModeLabel(mode: ServicePeriodMode): String = when (mode) {
        ServicePeriodMode.MONTH -> servicePeriodModeMonth
        ServicePeriodMode.DATE -> servicePeriodModeDate
    }

    fun currencyLabel(currency: InvoiceCurrency): String = when (currency) {
        InvoiceCurrency.ILS -> currencyIls
        InvoiceCurrency.USD -> currencyUsd
    }

    fun paymentMethodLabel(storedValue: String?): String {
        if (storedValue.isNullOrBlank()) return ""
        return paymentMethodByStoredValue[storedValue] ?: storedValue
    }

    fun customFieldColumnHeader(title: String?, oneBasedIndex: Int): String {
        val trimmed = title?.trim().orEmpty()
        return if (trimmed.isNotEmpty()) trimmed else customFieldFallbackHeader(oneBasedIndex)
    }
}

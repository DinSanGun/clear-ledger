package com.dinyairsadot.clearledger.core.util

import com.dinyairsadot.clearledger.core.domain.Invoice
import java.math.BigDecimal

/**
 * Pure Kotlin CSV generator for invoice export. No Android dependencies.
 */
object InvoiceCsvExporter {

    fun generate(
        invoices: List<Invoice>,
        categoryName: String,
        customFieldTitles: List<String>,
        labels: InvoiceCsvExportLabels
    ): String {
        val headers = buildHeaders(labels, customFieldTitles)
        val rows = invoices.map { invoice ->
            buildRow(invoice, categoryName, customFieldTitles, labels)
        }
        return (listOf(headers) + rows).joinToString("\n") { row ->
            row.joinToString(",") { escapeCsvField(it) }
        }
    }

    internal fun escapeCsvField(value: String): String {
        val needsQuotes = value.contains(',') ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
        if (!needsQuotes) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun buildHeaders(
        labels: InvoiceCsvExportLabels,
        customFieldTitles: List<String>
    ): List<String> {
        val base = listOf(
            labels.categoryNameHeader,
            labels.invoiceNumberHeader,
            labels.amountHeader,
            labels.currencyHeader,
            labels.statusHeader,
            labels.servicePeriodModeHeader,
            labels.servicePeriodStartHeader,
            labels.servicePeriodEndHeader,
            labels.dueDateHeader,
            labels.paymentDateHeader,
            labels.paymentMethodHeader,
            labels.confirmationNumberHeader,
            labels.notesHeader
        )
        val customHeaders = customFieldTitles.mapIndexed { index, title ->
            labels.customFieldColumnHeader(title, index + 1)
        }
        return base + customHeaders
    }

    private fun buildRow(
        invoice: Invoice,
        categoryName: String,
        customFieldTitles: List<String>,
        labels: InvoiceCsvExportLabels
    ): List<String> {
        val base = listOf(
            categoryName,
            invoice.invoiceNumber,
            formatAmount(invoice.amount),
            labels.currencyLabel(invoice.amountCurrency),
            labels.paymentStatusLabel(invoice.paymentStatus),
            labels.servicePeriodModeLabel(invoice.servicePeriodMode),
            labels.formatDate(invoice.servicePeriodStart),
            labels.formatDate(invoice.servicePeriodEnd),
            labels.formatDate(invoice.dueDate),
            labels.formatDate(invoice.paymentDate),
            labels.paymentMethodLabel(invoice.paymentMethod),
            invoice.confirmationNumber.orEmpty(),
            invoice.notes.orEmpty()
        )
        val customCells = customFieldTitles.indices.map { index ->
            invoice.customFieldValues.getOrElse(index) { "" }
        }
        return base + customCells
    }

    private fun formatAmount(amount: Double): String =
        BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
}

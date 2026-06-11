package com.dinyairsadot.taxtracker.core.util.backup

import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.Invoice
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Maps domain models to backup DTOs.
 *
 * Backup stores the app's effective domain state (post-[toDomain]), not a byte-for-byte
 * raw database dump. Restore will later go through [fromDomain], so the round-trip is
 * complete. Known implication: fields filled by domain fallbacks (e.g. [Invoice.amountDue]
 * defaults to legacy [Invoice.amount] when the DB column is null) are exported as their
 * effective values.
 */
object BackupMapper {

    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toPayload(data: BackupData, metadata: BackupMetadata): BackupPayload {
        return BackupPayload(
            formatVersion = BackupFormat.FORMAT_VERSION,
            metadata = metadata,
            categories = data.categories.map(::toCategoryDto),
            invoices = data.invoices.map(::toInvoiceDto)
        )
    }

    fun toCategoryDto(category: Category): BackupCategoryDto {
        return BackupCategoryDto(
            id = category.id,
            name = category.name,
            colorHex = category.colorHex,
            description = category.description,
            customFieldTitles = category.customFieldTitles,
            supplierName = category.supplierName,
            pinnedDefaults = category.pinnedDefaults,
            seedKey = category.seedKey,
            userEdited = category.userEdited,
            orderIndex = category.orderIndex
        )
    }

    fun toInvoiceDto(invoice: Invoice): BackupInvoiceDto {
        return BackupInvoiceDto(
            id = invoice.id,
            categoryId = invoice.categoryId,
            invoiceNumber = invoice.invoiceNumber,
            amount = invoice.amount,
            amountDue = invoice.amountDue,
            documentNumber = invoice.documentNumber,
            paymentStatus = invoice.paymentStatus.name,
            amountCurrency = invoice.amountCurrency.name,
            vendorName = invoice.vendorName,
            issueDate = formatDate(invoice.issueDate),
            dueDate = formatDate(invoice.dueDate),
            paymentDate = formatDate(invoice.paymentDate),
            servicePeriodStart = formatDate(invoice.servicePeriodStart),
            servicePeriodEnd = formatDate(invoice.servicePeriodEnd),
            servicePeriodMode = invoice.servicePeriodMode.name,
            documentType = invoice.documentType?.name,
            paymentMethod = invoice.paymentMethod,
            numberOfPayments = invoice.numberOfPayments,
            confirmationNumber = invoice.confirmationNumber,
            consumptionValue = invoice.consumptionValue,
            consumptionUnit = invoice.consumptionUnit,
            notes = invoice.notes,
            customFieldValues = invoice.customFieldValues,
            pinnedSnapshot = invoice.pinnedSnapshot
        )
    }

    private fun formatDate(date: LocalDate?): String? {
        return date?.format(isoDateFormatter)
    }
}

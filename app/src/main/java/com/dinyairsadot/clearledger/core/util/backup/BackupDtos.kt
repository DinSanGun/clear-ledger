package com.dinyairsadot.clearledger.core.util.backup

import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.domain.Invoice
import java.time.Instant

/** Input container for backup export (domain models before mapping). */
data class BackupData(
    val categories: List<Category>,
    val invoices: List<Invoice>
)

data class BackupMetadata(
    val exportedAt: String,
    val dbSchemaVersion: Int,
    val producer: String
) {
    companion object {
        fun create(): BackupMetadata = BackupMetadata(
            exportedAt = Instant.now().toString(),
            dbSchemaVersion = BackupFormat.DB_SCHEMA_VERSION,
            producer = BackupFormat.PRODUCER
        )
    }
}

data class BackupCategoryDto(
    val id: Long,
    val name: String,
    val colorHex: String,
    val description: String?,
    val customFieldTitles: List<String>?,
    val supplierName: String?,
    val pinnedDefaults: Map<String, String>?,
    val seedKey: String?,
    val userEdited: Boolean,
    val orderIndex: Int
)

/**
 * Reference from an invoice to its attachment file inside the backup ZIP (format version 2+).
 *
 * [entryName] is the ZIP entry (`attachments/<name>`) holding the file's bytes. It is only a
 * lookup key: on restore the file is re-created under a newly generated managed name, and the
 * entry name is never used to build a filesystem path. Several invoices may share an
 * [entryName]; they then share one restored file.
 */
data class BackupAttachmentDto(
    val entryName: String,
    val displayName: String?,
    val mimeType: String?
)

/**
 * Does not include the legacy `attachmentUri` (external `content://` references are neither
 * backed up nor restored). The managed attachment is described by [attachment]; it is null
 * for invoices without an attachment and absent in version-1 backups.
 */
data class BackupInvoiceDto(
    val id: Long,
    val categoryId: Long,
    val invoiceNumber: String,
    val amount: Double,
    val amountDue: Double,
    val documentNumber: String,
    val paymentStatus: String,
    val amountCurrency: String,
    val vendorName: String?,
    val issueDate: String?,
    val dueDate: String?,
    val paymentDate: String?,
    val servicePeriodStart: String?,
    val servicePeriodEnd: String?,
    val servicePeriodMode: String,
    val documentType: String?,
    val paymentMethod: String?,
    val numberOfPayments: String?,
    val confirmationNumber: String?,
    val consumptionValue: Double?,
    val consumptionUnit: String?,
    val notes: String?,
    val customFieldValues: List<String>?,
    val pinnedSnapshot: Map<String, String>?,
    val attachment: BackupAttachmentDto? = null
)

data class BackupPayload(
    val formatVersion: Int,
    val metadata: BackupMetadata,
    val categories: List<BackupCategoryDto>,
    val invoices: List<BackupInvoiceDto>
)

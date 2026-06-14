package com.dinyairsadot.taxtracker.core.util.backup

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupValidatorTest {

    private val gson = Gson()

    @Test
    fun validate_validFullPayload_returnsValid() {
        val result = BackupValidator.validate(validPayload())
        assertTrue(result is BackupValidationResult.Valid)
    }

    @Test
    fun validate_formatVersion2_returnsUnsupportedVersion() {
        val result = BackupValidator.validate(validPayload().copy(formatVersion = 2))
        assertEquals(BackupValidationResult.UnsupportedVersion(2), result)
    }

    @Test
    fun validate_formatVersion0_returnsUnsupportedVersion() {
        val result = BackupValidator.validate(validPayload().copy(formatVersion = 0))
        assertEquals(BackupValidationResult.UnsupportedVersion(0), result)
    }

    @Test
    fun validate_missingFormatVersion_returnsUnsupportedVersion() {
        val json = gson.toJson(validPayload()).replace("\"formatVersion\":1,", "")
        val payload = gson.fromJson(json, BackupPayload::class.java)
        val result = BackupValidator.validate(payload)
        assertEquals(BackupValidationResult.UnsupportedVersion(0), result)
    }

    @Test
    fun validate_missingCategories_returnsInvalid() {
        val payload = gson.fromJson(
            """
            {
              "formatVersion": 1,
              "metadata": {
                "exportedAt": "2026-06-11T12:00:00Z",
                "dbSchemaVersion": 13,
                "producer": "tax-tracker-android"
              },
              "invoices": []
            }
            """.trimIndent(),
            BackupPayload::class.java
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_missingInvoices_returnsInvalid() {
        val payload = gson.fromJson(
            """
            {
              "formatVersion": 1,
              "metadata": {
                "exportedAt": "2026-06-11T12:00:00Z",
                "dbSchemaVersion": 13,
                "producer": "tax-tracker-android"
              },
              "categories": []
            }
            """.trimIndent(),
            BackupPayload::class.java
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_categoryIdZero_returnsInvalid() {
        val payload = validPayload().copy(
            categories = listOf(validCategory().copy(id = 0L))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_categoryIdNegative_returnsInvalid() {
        val payload = validPayload().copy(
            categories = listOf(validCategory().copy(id = -1L))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_invoiceIdZero_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(id = 0L))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_invoiceCategoryIdZero_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(categoryId = 0L))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_blankCategoryName_returnsInvalid() {
        val payload = validPayload().copy(
            categories = listOf(validCategory().copy(name = ""))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_invalidColorHex_returnsInvalid() {
        val payload = validPayload().copy(
            categories = listOf(validCategory().copy(colorHex = "red"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_blankInvoiceNumber_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(invoiceNumber = ""))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_blankDocumentNumber_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(documentNumber = ""))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_negativeAmount_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(amount = -1.0))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_negativeAmountDue_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(amountDue = -1.0))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_unknownPaymentStatus_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(paymentStatus = "UNKNOWN_ENUM"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_unknownAmountCurrency_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(amountCurrency = "EUR"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_unknownServicePeriodMode_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(servicePeriodMode = "WEEKLY"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_unknownDocumentType_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(documentType = "RECEIPT"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_invalidDate_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(issueDate = "not-a-date"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_invalidMonthDate_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(dueDate = "2026-13-01"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_duplicateCategoryIds_returnsInvalid() {
        val payload = validPayload().copy(
            categories = listOf(validCategory(), validCategory())
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_duplicateInvoiceIds_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice(), validInvoice())
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_orphanInvoiceCategoryId_returnsInvalid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(categoryId = 99L))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Invalid)
    }

    @Test
    fun validate_emptyCategoriesAndInvoices_returnsValid() {
        val payload = validPayload().copy(categories = emptyList(), invoices = emptyList())
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Valid)
    }

    @Test
    fun validate_nullDates_returnsValid() {
        val payload = validPayload().copy(
            invoices = listOf(
                validInvoice().copy(
                    issueDate = null,
                    dueDate = null,
                    paymentDate = null,
                    servicePeriodStart = null,
                    servicePeriodEnd = null
                )
            )
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Valid)
    }

    @Test
    fun validate_blankCustomFieldValue_returnsValid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(customFieldValues = listOf("", "value")))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Valid)
    }

    @Test
    fun validate_numberOfPaymentsNonNumeric_returnsValid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(numberOfPayments = "abc"))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Valid)
    }

    @Test
    fun validate_numberOfPaymentsNull_returnsValid() {
        val payload = validPayload().copy(
            invoices = listOf(validInvoice().copy(numberOfPayments = null))
        )
        val result = BackupValidator.validate(payload)
        assertTrue(result is BackupValidationResult.Valid)
    }

    private fun validPayload(): BackupPayload = BackupPayload(
        formatVersion = BackupFormat.FORMAT_VERSION,
        metadata = BackupMetadata(
            exportedAt = "2026-06-11T12:00:00Z",
            dbSchemaVersion = BackupFormat.DB_SCHEMA_VERSION,
            producer = BackupFormat.PRODUCER
        ),
        categories = listOf(validCategory()),
        invoices = listOf(validInvoice())
    )

    private fun validCategory(): BackupCategoryDto = BackupCategoryDto(
        id = 1L,
        name = "Utilities",
        colorHex = "#FF9800",
        description = null,
        customFieldTitles = emptyList(),
        supplierName = null,
        pinnedDefaults = emptyMap(),
        seedKey = null,
        userEdited = false,
        orderIndex = 0
    )

    private fun validInvoice(): BackupInvoiceDto = BackupInvoiceDto(
        id = 10L,
        categoryId = 1L,
        invoiceNumber = "INV-1",
        amount = 100.0,
        amountDue = 100.0,
        documentNumber = "DOC-1",
        paymentStatus = "NOT_PAID",
        amountCurrency = "ILS",
        vendorName = null,
        issueDate = null,
        dueDate = null,
        paymentDate = null,
        servicePeriodStart = null,
        servicePeriodEnd = null,
        servicePeriodMode = "MONTH",
        documentType = null,
        paymentMethod = null,
        numberOfPayments = null,
        confirmationNumber = null,
        consumptionValue = null,
        consumptionUnit = null,
        notes = null,
        customFieldValues = emptyList(),
        pinnedSnapshot = emptyMap()
    )
}

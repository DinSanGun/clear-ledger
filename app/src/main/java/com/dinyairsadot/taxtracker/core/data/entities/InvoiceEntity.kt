package com.dinyairsadot.taxtracker.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dinyairsadot.taxtracker.core.data.converters.DocumentTypeConverter
import com.dinyairsadot.taxtracker.core.data.converters.LocalDateConverter
import com.dinyairsadot.taxtracker.core.data.converters.PaymentStatusConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringListConverter
import com.dinyairsadot.taxtracker.core.domain.Invoice

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(
    LocalDateConverter::class,
    PaymentStatusConverter::class,
    StringListConverter::class,
    DocumentTypeConverter::class
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val invoiceNumber: String,
    val amount: Double,
    val paymentStatus: com.dinyairsadot.taxtracker.core.domain.PaymentStatus,
    val vendorName: String? = null,
    val issueDateEpochDay: Long? = null,
    val dueDateEpochDay: Long? = null,
    val paymentDateEpochDay: Long? = null,
    val servicePeriodStartEpochDay: Long? = null,
    val servicePeriodEndEpochDay: Long? = null,
    val consumptionValue: Double? = null,
    val consumptionUnit: String? = null,
    val notes: String? = null,
    val customFieldValuesJson: String? = null, // Stored as JSON string, converted via StringListConverter
    val documentTypeString: String? = null // Stored as String, converted via DocumentTypeConverter
) {
    fun toDomain(): Invoice {
        val issueDate = issueDateEpochDay?.let { 
            LocalDateConverter().toLocalDate(it) 
        }
        val dueDate = dueDateEpochDay?.let { 
            LocalDateConverter().toLocalDate(it) 
        }
        val paymentDate = paymentDateEpochDay?.let { 
            LocalDateConverter().toLocalDate(it) 
        }
        val servicePeriodStart = servicePeriodStartEpochDay?.let {
            LocalDateConverter().toLocalDate(it)
        }
        val servicePeriodEnd = servicePeriodEndEpochDay?.let {
            LocalDateConverter().toLocalDate(it)
        }
        val customFieldValues = if (customFieldValuesJson.isNullOrBlank()) {
            emptyList()
        } else {
            StringListConverter().toStringList(customFieldValuesJson)
        }
        val documentType = documentTypeString?.let {
            DocumentTypeConverter().toDocumentType(it)
        }
        
        return Invoice(
            id = id,
            categoryId = categoryId,
            invoiceNumber = invoiceNumber,
            amount = amount,
            paymentStatus = paymentStatus,
            vendorName = vendorName,
            issueDate = issueDate,
            dueDate = dueDate,
            paymentDate = paymentDate,
            servicePeriodStart = servicePeriodStart,
            servicePeriodEnd = servicePeriodEnd,
            consumptionValue = consumptionValue,
            consumptionUnit = consumptionUnit,
            notes = notes,
            customFieldValues = customFieldValues,
            documentType = documentType
        )
    }
    
    companion object {
        fun fromDomain(invoice: Invoice): InvoiceEntity {
            val issueDateEpochDay = invoice.issueDate?.let { 
                LocalDateConverter().fromLocalDate(it) 
            }
            val dueDateEpochDay = invoice.dueDate?.let { 
                LocalDateConverter().fromLocalDate(it) 
            }
            val paymentDateEpochDay = invoice.paymentDate?.let { 
                LocalDateConverter().fromLocalDate(it) 
            }
            val servicePeriodStartEpochDay = invoice.servicePeriodStart?.let {
                LocalDateConverter().fromLocalDate(it)
            }
            val servicePeriodEndEpochDay = invoice.servicePeriodEnd?.let {
                LocalDateConverter().fromLocalDate(it)
            }
            val customFieldValuesJson = if (invoice.customFieldValues.isEmpty()) {
                null
            } else {
                StringListConverter().fromStringList(invoice.customFieldValues)
            }
            val documentTypeString = invoice.documentType?.let {
                DocumentTypeConverter().fromDocumentType(it)
            }
            
            return InvoiceEntity(
                id = invoice.id,
                categoryId = invoice.categoryId,
                invoiceNumber = invoice.invoiceNumber,
                amount = invoice.amount,
                paymentStatus = invoice.paymentStatus,
                vendorName = invoice.vendorName,
                issueDateEpochDay = issueDateEpochDay,
                dueDateEpochDay = dueDateEpochDay,
                paymentDateEpochDay = paymentDateEpochDay,
                servicePeriodStartEpochDay = servicePeriodStartEpochDay,
                servicePeriodEndEpochDay = servicePeriodEndEpochDay,
                consumptionValue = invoice.consumptionValue,
                consumptionUnit = invoice.consumptionUnit,
                notes = invoice.notes,
                customFieldValuesJson = customFieldValuesJson,
                documentTypeString = documentTypeString
            )
        }
    }
}

package com.dinyairsadot.taxtracker.core.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
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
    StringListConverter::class
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val invoiceNumber: String,
    val amount: Double,
    val paymentStatus: com.dinyairsadot.taxtracker.core.domain.PaymentStatus,
    val dueDateEpochDay: Long? = null,
    val paymentDateEpochDay: Long? = null,
    val consumptionValue: Double? = null,
    val consumptionUnit: String? = null,
    val notes: String? = null,
    val customFieldValuesJson: String? = null // Stored as JSON string, converted via StringListConverter
) {
    fun toDomain(): Invoice {
        val dueDate = dueDateEpochDay?.let { 
            LocalDateConverter().toLocalDate(it) 
        }
        val paymentDate = paymentDateEpochDay?.let { 
            LocalDateConverter().toLocalDate(it) 
        }
        val customFieldValues = if (customFieldValuesJson.isNullOrBlank()) {
            emptyList()
        } else {
            StringListConverter().toStringList(customFieldValuesJson)
        }
        
        return Invoice(
            id = id,
            categoryId = categoryId,
            invoiceNumber = invoiceNumber,
            amount = amount,
            paymentStatus = paymentStatus,
            dueDate = dueDate,
            paymentDate = paymentDate,
            consumptionValue = consumptionValue,
            consumptionUnit = consumptionUnit,
            notes = notes,
            customFieldValues = customFieldValues
        )
    }
    
    companion object {
        fun fromDomain(invoice: Invoice): InvoiceEntity {
            val dueDateEpochDay = invoice.dueDate?.let { 
                LocalDateConverter().fromLocalDate(it) 
            }
            val paymentDateEpochDay = invoice.paymentDate?.let { 
                LocalDateConverter().fromLocalDate(it) 
            }
            val customFieldValuesJson = if (invoice.customFieldValues.isEmpty()) {
                null
            } else {
                StringListConverter().fromStringList(invoice.customFieldValues)
            }
            
            return InvoiceEntity(
                id = invoice.id,
                categoryId = invoice.categoryId,
                invoiceNumber = invoice.invoiceNumber,
                amount = invoice.amount,
                paymentStatus = invoice.paymentStatus,
                dueDateEpochDay = dueDateEpochDay,
                paymentDateEpochDay = paymentDateEpochDay,
                consumptionValue = invoice.consumptionValue,
                consumptionUnit = invoice.consumptionUnit,
                notes = invoice.notes,
                customFieldValuesJson = customFieldValuesJson
            )
        }
    }
}

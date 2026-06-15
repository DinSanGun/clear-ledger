package com.dinyairsadot.clearledger.core.data.converters

import androidx.room.TypeConverter
import com.dinyairsadot.clearledger.core.domain.PaymentStatus

class PaymentStatusConverter {
    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? {
        return when (value) {
            "PAID" -> PaymentStatus.PAID
            "NOT_PAID" -> PaymentStatus.NOT_PAID
            // Legacy values from pre-migration
            "PAID_FULL", "PAID_CREDIT" -> PaymentStatus.PAID
            else -> value?.let { runCatching { PaymentStatus.valueOf(it) }.getOrNull() }
        }
    }
}

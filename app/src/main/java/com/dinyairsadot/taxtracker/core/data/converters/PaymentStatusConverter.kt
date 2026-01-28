package com.dinyairsadot.taxtracker.core.data.converters

import androidx.room.TypeConverter
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus

class PaymentStatusConverter {
    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? {
        return value?.let { PaymentStatus.valueOf(it) }
    }
}

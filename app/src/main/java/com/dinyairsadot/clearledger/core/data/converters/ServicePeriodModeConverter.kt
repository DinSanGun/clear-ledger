package com.dinyairsadot.clearledger.core.data.converters

import androidx.room.TypeConverter
import com.dinyairsadot.clearledger.core.domain.ServicePeriodMode

class ServicePeriodModeConverter {

    @TypeConverter
    fun fromServicePeriodMode(value: ServicePeriodMode?): String? = value?.name

    /**
     * Converts a stored string back to [ServicePeriodMode].
     * Falls back to [ServicePeriodMode.MONTH] for any unrecognised value so that
     * future enum additions do not crash existing installs.
     */
    @TypeConverter
    fun toServicePeriodMode(value: String?): ServicePeriodMode? =
        value?.let { runCatching { ServicePeriodMode.valueOf(it) }.getOrDefault(ServicePeriodMode.MONTH) }
}

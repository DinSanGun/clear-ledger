package com.dinyairsadot.taxtracker.core.data.converters

import androidx.room.TypeConverter
import com.dinyairsadot.taxtracker.core.domain.DocumentType

class DocumentTypeConverter {
    @TypeConverter
    fun fromDocumentType(value: DocumentType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toDocumentType(value: String?): DocumentType? {
        return value?.let { DocumentType.valueOf(it) }
    }
}

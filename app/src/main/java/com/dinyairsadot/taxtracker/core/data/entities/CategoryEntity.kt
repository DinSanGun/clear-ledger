package com.dinyairsadot.taxtracker.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dinyairsadot.taxtracker.core.data.converters.StringListConverter
import com.dinyairsadot.taxtracker.core.data.converters.StringMapConverter
import com.dinyairsadot.taxtracker.core.domain.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val customFieldTitlesJson: String? = null, // Stored as JSON string, converted via StringListConverter
    val supplierName: String? = null, // Kept for backward compatibility
    val pinnedDefaultsJson: String? = null // Stored as JSON Map<String, String>, converted via StringMapConverter
) {
    fun toDomain(): Category {
        val customFieldTitles = if (customFieldTitlesJson.isNullOrBlank()) {
            emptyList()
        } else {
            StringListConverter().toStringList(customFieldTitlesJson)
        }
        
        val pinnedDefaults = if (pinnedDefaultsJson.isNullOrBlank()) {
            emptyMap()
        } else {
            StringMapConverter().toStringMap(pinnedDefaultsJson)
        }
        
        return Category(
            id = id,
            name = name,
            colorHex = colorHex,
            description = description,
            customFieldTitles = customFieldTitles,
            supplierName = supplierName,
            pinnedDefaults = pinnedDefaults
        )
    }
    
    companion object {
        fun fromDomain(category: Category): CategoryEntity {
            val customFieldTitlesJson = if (category.customFieldTitles.isEmpty()) {
                null
            } else {
                StringListConverter().fromStringList(category.customFieldTitles)
            }
            
            val pinnedDefaultsJson = if (category.pinnedDefaults.isEmpty()) {
                null
            } else {
                StringMapConverter().fromStringMap(category.pinnedDefaults)
            }
            
            return CategoryEntity(
                id = category.id,
                name = category.name,
                colorHex = category.colorHex,
                description = category.description,
                customFieldTitlesJson = customFieldTitlesJson,
                supplierName = category.supplierName,
                pinnedDefaultsJson = pinnedDefaultsJson
            )
        }
    }
}

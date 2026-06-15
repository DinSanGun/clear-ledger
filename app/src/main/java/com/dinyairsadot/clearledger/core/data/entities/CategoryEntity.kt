package com.dinyairsadot.clearledger.core.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dinyairsadot.clearledger.core.data.converters.StringListConverter
import com.dinyairsadot.clearledger.core.data.converters.StringMapConverter
import com.dinyairsadot.clearledger.core.domain.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val description: String? = null,
    val customFieldTitlesJson: String? = null, // Stored as JSON string, converted via StringListConverter
    val supplierName: String? = null, // Kept for backward compatibility
    val pinnedDefaultsJson: String? = null, // Stored as JSON Map<String, String>, converted via StringMapConverter
    val seedKey: String? = null,       // Stable key for seeded categories; null for user-created (no backfill)
    val userEdited: Boolean = false,   // True after user saves; prevents locale overwrite
    val orderIndex: Int = 0            // Stable persisted category ordering
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
            pinnedDefaults = pinnedDefaults,
            seedKey = seedKey,
            userEdited = userEdited,
            orderIndex = orderIndex
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
                pinnedDefaultsJson = pinnedDefaultsJson,
                seedKey = category.seedKey,
                userEdited = category.userEdited,
                orderIndex = category.orderIndex
            )
        }
    }
}

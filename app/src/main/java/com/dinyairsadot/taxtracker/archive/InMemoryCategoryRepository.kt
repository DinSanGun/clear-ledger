package com.dinyairsadot.taxtracker.archive

import android.content.Context
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository

/**
 * Archived: No longer used. App uses Room (RoomCategoryRepository) for persistence.
 * Kept for reference only.
 */
object InMemoryCategoryRepository : CategoryRepository {

    // Mutable list so we can add items at runtime
    private val categories = mutableListOf(
        Category(
            id = 1,
            name = "Electricity",
            colorHex = "#FF9800",
            description = "Electricity provider bills",
            customFieldTitles = emptyList(),
            orderIndex = 0
        ),
        Category(
            id = 2,
            name = "Water",
            colorHex = "#2196F3",
            description = "Water and sewage",
            customFieldTitles = emptyList(),
            orderIndex = 1
        ),
        Category(
            id = 3,
            name = "City Taxes",
            colorHex = "#4CAF50",
            description = "Arnona / city hall payments",
            customFieldTitles = emptyList(),
            orderIndex = 2
        )
    )

    override suspend fun getCategories(): List<Category> {
        // Return a copy to avoid exposing the mutable list
        return categories.toList()
    }

    override suspend fun addCategory(category: Category) {
        categories.add(category)
    }

    override suspend fun deleteCategory(id: Long) {
        categories.removeAll { it.id == id }
    }

    override suspend fun updateCategory(category: Category) {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categories[index] = category
        }
    }

    override suspend fun updateLocalizedSeededCategories(context: Context) {
        // No-op: in-memory repo has no seeded rows to update
    }

    override suspend fun clearCustomFieldsForSeededCategories(): Int = 0

    override suspend fun updateCategoryOrder(orderedIds: List<Long>) {
        val byId = categories.associateBy { it.id }
        val reordered = orderedIds.mapIndexedNotNull { index, id ->
            byId[id]?.copy(orderIndex = index)
        }
        if (reordered.size == categories.size) {
            categories.clear()
            categories.addAll(reordered)
        }
    }
}

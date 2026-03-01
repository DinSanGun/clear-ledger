package com.dinyairsadot.taxtracker.core.domain

import android.content.Context

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(id: Long)
    suspend fun updateCategory(category: Category)
    /** Updates name/description only for seeded, unedited categories to match the given locale. */
    suspend fun updateLocalizedSeededCategories(context: Context)
    /** Clears custom fields for all seeded, unedited categories (one-time migration). */
    suspend fun clearCustomFieldsForSeededCategories(): Int
}
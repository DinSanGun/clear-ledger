package com.dinyairsadot.taxtracker.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)
    
    @Update
    suspend fun update(category: CategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    /** Categories that were seeded (have seedKey) and have not been edited by the user. */
    @Query("SELECT * FROM categories WHERE seedKey IS NOT NULL AND userEdited = 0")
    suspend fun getSeededUnedited(): List<CategoryEntity>

    /** Clear custom fields for all seeded, unedited categories (one-time migration). */
    @Query("UPDATE categories SET customFieldTitlesJson = NULL WHERE seedKey IS NOT NULL AND userEdited = 0")
    suspend fun clearCustomFieldsForSeededUnedited(): Int

    /** Update name, description, and clear custom fields (for locale refresh); does not touch color or other fields. */
    @Query("UPDATE categories SET name = :name, description = :description, customFieldTitlesJson = NULL WHERE id = :id")
    suspend fun updateNameDescriptionAndCustomFields(id: Long, name: String, description: String?)
}

package com.dinyairsadot.taxtracker.core.data.repositories

import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository

class RoomCategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    
    override suspend fun getCategories(): List<Category> {
        return categoryDao.getAll().map { it.toDomain() }
    }
    
    override suspend fun addCategory(category: Category) {
        // Room will auto-generate ID if id=0 (due to @PrimaryKey(autoGenerate = true))
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.insert(entity)
    }
    
    override suspend fun deleteCategory(id: Long) {
        categoryDao.deleteById(id)
    }
    
    override suspend fun updateCategory(category: Category) {
        val entity = CategoryEntity.fromDomain(category)
        categoryDao.update(entity)
    }
}

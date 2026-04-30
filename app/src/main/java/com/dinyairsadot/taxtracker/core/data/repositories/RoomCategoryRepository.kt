package com.dinyairsadot.taxtracker.core.data.repositories

import android.content.Context
import android.util.Log
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.data.dao.CategoryDao
import com.dinyairsadot.taxtracker.core.data.entities.CategoryEntity
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository

/** Single source of truth: seedKey -> (name string res id, description string res id). */
private val SEED_KEY_TO_RES_IDS: Map<String, Pair<Int, Int>> = mapOf(
    "arnona" to (R.string.default_category_arnona to R.string.default_category_arnona_description),
    "electricity" to (R.string.default_category_electricity to R.string.default_category_electricity_description),
    "water" to (R.string.default_category_water to R.string.default_category_water_description),
    "gas" to (R.string.default_category_gas to R.string.default_category_gas_description),
    "phone_internet" to (R.string.default_category_phone_internet to R.string.default_category_phone_internet_description),
    "national_insurance" to (R.string.default_category_national_insurance to R.string.default_category_national_insurance_description),
    "health_fund" to (R.string.default_category_health_fund to R.string.default_category_health_fund_description),
    "car_insurance" to (R.string.default_category_car_insurance to R.string.default_category_car_insurance_description),
)

/** Fixed display order for built-in categories, independent of localized names. */
private val SEEDED_CATEGORY_ORDER: Map<String, Int> = listOf(
    "arnona",
    "electricity",
    "water",
    "gas",
    "phone_internet",
    "national_insurance",
    "health_fund",
    "car_insurance"
).withIndex().associate { (index, key) -> key to index }

class RoomCategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override suspend fun getCategories(): List<Category> {
        return categoryDao.getAll()
            .map { it.toDomain() }
    }

    override suspend fun addCategory(category: Category) {
        // Room will auto-generate ID if id=0 (due to @PrimaryKey(autoGenerate = true))
        val nextOrderIndex = categoryDao.getMaxOrderIndex() + 1
        val entity = CategoryEntity.fromDomain(category).copy(orderIndex = nextOrderIndex)
        categoryDao.insert(entity)
    }

    override suspend fun deleteCategory(id: Long) {
        categoryDao.deleteById(id)
    }

    override suspend fun updateCategory(category: Category) {
        val existing = categoryDao.getById(category.id)
        val entity = CategoryEntity.fromDomain(category).copy(
            seedKey = existing?.seedKey,
            userEdited = true,
            orderIndex = existing?.orderIndex ?: category.orderIndex
        )
        categoryDao.update(entity)
    }

    override suspend fun updateCategoryOrder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            categoryDao.updateOrderIndex(id = id, orderIndex = index)
        }
    }

    override suspend fun updateLocalizedSeededCategories(context: Context) {
        val entities = categoryDao.getSeededUnedited()
        var updated = 0
        for (entity in entities) {
            val key = entity.seedKey ?: continue
            val resIds = SEED_KEY_TO_RES_IDS[key] ?: continue
            val name = context.getString(resIds.first)
            val description = context.getString(resIds.second)
            categoryDao.updateNameDescriptionAndCustomFields(entity.id, name, description)
            updated++
        }
        if (updated > 0) {
            Log.d(TAG, "updateLocalizedSeededCategories: updated $updated seeded categories for current locale")
        }
    }

    override suspend fun clearCustomFieldsForSeededCategories(): Int {
        val count = categoryDao.clearCustomFieldsForSeededUnedited()
        if (count > 0) {
            Log.d(TAG, "clearCustomFieldsForSeededCategories: cleared custom fields from $count seeded categories")
        }
        return count
    }

    companion object {
        private const val TAG = "RoomCategoryRepo"
    }
}

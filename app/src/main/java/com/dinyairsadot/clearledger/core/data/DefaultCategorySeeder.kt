package com.dinyairsadot.clearledger.core.data

import android.content.Context
import com.dinyairsadot.clearledger.R
import com.dinyairsadot.clearledger.core.domain.Category

/**
 * Single source of truth for the 8 default seeded categories.
 * Used by both initial seeding in MainActivity and reset in RoomBackupRestoreRepository,
 * so a fresh install and a reset always produce identical default categories.
 */
object DefaultCategorySeeder {

    fun buildDomainCategories(context: Context): List<Category> = listOf(
        Category(
            id = 0,
            name = context.getString(R.string.default_category_arnona),
            colorHex = "#4CAF50",
            description = context.getString(R.string.default_category_arnona_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "arnona",
            userEdited = false,
            orderIndex = 0
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_electricity),
            colorHex = "#FF9800",
            description = context.getString(R.string.default_category_electricity_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "electricity",
            userEdited = false,
            orderIndex = 1
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_water),
            colorHex = "#2196F3",
            description = context.getString(R.string.default_category_water_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "water",
            userEdited = false,
            orderIndex = 2
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_gas),
            colorHex = "#9C27B0",
            description = context.getString(R.string.default_category_gas_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "gas",
            userEdited = false,
            orderIndex = 3
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_phone_internet),
            colorHex = "#00BCD4",
            description = context.getString(R.string.default_category_phone_internet_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "phone_internet",
            userEdited = false,
            orderIndex = 4
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_national_insurance),
            colorHex = "#795548",
            description = context.getString(R.string.default_category_national_insurance_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "national_insurance",
            userEdited = false,
            orderIndex = 5
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_health_fund),
            colorHex = "#3F51B5",
            description = context.getString(R.string.default_category_health_fund_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "health_fund",
            userEdited = false,
            orderIndex = 6
        ),
        Category(
            id = 0,
            name = context.getString(R.string.default_category_car_insurance),
            colorHex = "#FF5722",
            description = context.getString(R.string.default_category_car_insurance_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "car_insurance",
            userEdited = false,
            orderIndex = 7
        )
    )
}

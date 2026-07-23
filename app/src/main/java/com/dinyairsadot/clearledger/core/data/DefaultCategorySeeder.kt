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
            colorHex = "#66BB6A",
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
            colorHex = "#FFA726",
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
            colorHex = "#42A5F5",
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
            colorHex = "#AB47BC",
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
            colorHex = "#26C6DA",
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
            colorHex = "#8D6E63",
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
            colorHex = "#5C6BC0",
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
            colorHex = "#FF7043",
            description = context.getString(R.string.default_category_car_insurance_description),
            customFieldTitles = emptyList(),
            pinnedDefaults = emptyMap(),
            seedKey = "car_insurance",
            userEdited = false,
            orderIndex = 7
        )
    )
}

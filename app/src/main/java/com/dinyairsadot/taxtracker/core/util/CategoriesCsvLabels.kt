package com.dinyairsadot.taxtracker.core.util

/**
 * Localized column headers for categories.csv in the all-data ZIP export.
 */
data class CategoriesCsvLabels(
    val categoryNameHeader: String,
    val descriptionHeader: String,
    val orderHeader: String,
    val customFieldTitleHeader: (oneBasedIndex: Int) -> String
)

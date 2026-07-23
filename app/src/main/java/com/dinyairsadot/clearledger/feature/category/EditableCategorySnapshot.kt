package com.dinyairsadot.clearledger.feature.category

/**
 * Normalized snapshot of meaningful category form fields for unsaved-change detection.
 * Excludes UI-only state such as validation errors, snackbars, and empty pending inputs.
 */
internal data class EditableCategorySnapshot(
    val name: String,
    val colorHex: String,
    val description: String,
    val customFieldTitles: List<String>
)

internal fun editableCategorySnapshot(
    name: String,
    colorHex: String,
    description: String,
    customFieldTitles: List<String>,
    pendingNewFieldName: String = ""
): EditableCategorySnapshot {
    val resolved = resolveCustomFieldTitlesForSave(
        customFieldTitles = customFieldTitles,
        pendingNewFieldName = pendingNewFieldName,
        onDuplicatePendingField = {}
    ) ?: customFieldTitles.map { it.trim() }.filter { it.isNotBlank() }
    return EditableCategorySnapshot(
        name = name.trim(),
        colorHex = colorHex.trim(),
        description = description.trim(),
        customFieldTitles = resolved
    )
}

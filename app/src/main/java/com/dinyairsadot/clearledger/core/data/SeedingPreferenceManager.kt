package com.dinyairsadot.clearledger.core.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages the persistent flag that tracks whether default categories have been seeded.
 * This ensures seeding only happens once on first app launch, even if the user later
 * deletes all categories.
 */
class SeedingPreferenceManager(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "seeding_prefs"
        private const val KEY_HAS_SEEDED_DEFAULT_CATEGORIES = "has_seeded_default_categories"
        private const val KEY_HAS_CLEARED_SEEDED_CUSTOM_FIELDS = "has_cleared_seeded_custom_fields"
    }

    /** Whether custom fields have been cleared from seeded categories (one-time migration). */
    fun hasClearedSeededCustomFields(): Boolean =
        sharedPrefs.getBoolean(KEY_HAS_CLEARED_SEEDED_CUSTOM_FIELDS, false)

    fun setHasClearedSeededCustomFields(cleared: Boolean = true) {
        sharedPrefs.edit()
            .putBoolean(KEY_HAS_CLEARED_SEEDED_CUSTOM_FIELDS, cleared)
            .apply()
    }

    /**
     * Check if default categories have already been seeded.
     * @return true if seeding has been completed, false otherwise
     */
    fun hasSeededDefaultCategories(): Boolean {
        return sharedPrefs.getBoolean(KEY_HAS_SEEDED_DEFAULT_CATEGORIES, false)
    }

    /**
     * Mark that default categories have been seeded.
     * This should be called immediately after seeding completes successfully.
     */
    fun setHasSeededDefaultCategories(hasSeeded: Boolean = true) {
        sharedPrefs.edit()
            .putBoolean(KEY_HAS_SEEDED_DEFAULT_CATEGORIES, hasSeeded)
            .apply()
    }
}

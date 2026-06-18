package com.dinyairsadot.clearledger.core.data

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class LanguagePreferenceManager(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_LAST_APPLIED_LANGUAGE = "last_applied_language_for_seeded_data"

        /**
         * Normalize language code: Android's Locale converts "he" to "iw" for backward compatibility.
         * We normalize "iw" back to "he" to match our UI language codes.
         */
        private fun normalizeLanguageCode(code: String): String {
            return if (code == "iw") "he" else code
        }
    }

    /**
     * Get the current saved language code ("en" or "he"). Default is Hebrew when no preference is set.
     */
    fun getCurrentLanguage(): String {
        val rawResult = sharedPrefs.getString(KEY_LANGUAGE, "he") ?: "he"
        return normalizeLanguageCode(rawResult)
    }

    /**
     * Save the selected language preference
     */
    fun setLanguage(locale: Locale) {
        val normalizedLangCode = normalizeLanguageCode(locale.language)
        sharedPrefs.edit().putString(KEY_LANGUAGE, normalizedLangCode).commit()
    }

    /**
     * Get the saved Locale, or return device default if none saved
     */
    fun getSavedLocale(): Locale {
        val languageCode = getCurrentLanguage()
        return when (languageCode) {
            "he" -> Locale.forLanguageTag("he")
            "en" -> Locale.forLanguageTag("en")
            else -> Locale.getDefault()
        }
    }

    /**
     * Check if a language preference has been saved
     */
    fun hasLanguagePreference(): Boolean {
        return sharedPrefs.contains(KEY_LANGUAGE)
    }

    /** Language code last applied to seeded categories (for locale sync). Null if never applied. */
    fun getLastAppliedLanguage(): String? {
        val raw = sharedPrefs.getString(KEY_LAST_APPLIED_LANGUAGE, null) ?: return null
        return if (raw == "iw") "he" else raw
    }

    /** Mark that seeded categories have been updated for the given language. */
    fun setLastAppliedLanguage(languageCode: String) {
        val normalized = if (languageCode == "iw") "he" else languageCode
        sharedPrefs.edit().putString(KEY_LAST_APPLIED_LANGUAGE, normalized).apply()
    }
}

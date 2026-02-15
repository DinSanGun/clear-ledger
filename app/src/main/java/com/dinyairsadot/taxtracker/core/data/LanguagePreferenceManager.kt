package com.dinyairsadot.taxtracker.core.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
        private const val TAG = "LanguageDebug"
        
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
        // #region agent log
        val defaultValue = sharedPrefs.getString(KEY_LANGUAGE, "he")
        val rawResult = defaultValue ?: "he"
        val result = normalizeLanguageCode(rawResult)
        Log.d(TAG, "[A] getCurrentLanguage: rawResult='$rawResult', normalizedResult='$result' (length=${result.length}), key='$KEY_LANGUAGE', allKeys=${sharedPrefs.all.keys.toList()}")
        // #endregion
        return result
    }

    /**
     * Save the selected language preference
     */
    fun setLanguage(locale: Locale) {
        // #region agent log
        val rawLangCode = locale.language
        val normalizedLangCode = normalizeLanguageCode(rawLangCode)
        val beforeValue = sharedPrefs.getString(KEY_LANGUAGE, null)
        Log.d(TAG, "[A] setLanguage BEFORE write: rawLangCode='$rawLangCode', normalizedLangCode='$normalizedLangCode' (length=${normalizedLangCode.length}), key='$KEY_LANGUAGE', beforeValue='$beforeValue'")
        // #endregion
        val editor = sharedPrefs.edit()
        editor.putString(KEY_LANGUAGE, normalizedLangCode)
        val success = editor.commit() // Changed from apply() to commit() for synchronous write
        // #region agent log
        val readBack = sharedPrefs.getString(KEY_LANGUAGE, null)
        Log.d(TAG, "[A] setLanguage AFTER write: normalizedLangCode='$normalizedLangCode', writeSuccess=$success, readBack='$readBack' (length=${readBack?.length}), match=${readBack == normalizedLangCode}, allKeys=${sharedPrefs.all.keys.toList()}")
        // #endregion
    }

    /**
     * Get the saved Locale, or return device default if none saved
     */
    fun getSavedLocale(): Locale {
        val languageCode = getCurrentLanguage()
        return when (languageCode) {
            "he" -> Locale("he")
            "en" -> Locale("en")
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

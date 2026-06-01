package com.dinyairsadot.taxtracker.feature.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.dinyairsadot.taxtracker.core.data.LanguagePreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguageViewModel(private val context: Context) : ViewModel() {
    private val languageManager = LanguagePreferenceManager(context)

    private val _currentLanguage = MutableStateFlow(languageManager.getCurrentLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    /**
     * Change the app language and recreate the activity
     */
    fun changeLanguage(locale: Locale, activity: Activity) {
        languageManager.setLanguage(locale)
        // Normalize the language code: Android converts "he" to "iw", so we normalize it back
        val normalizedLangCode = if (locale.language == "iw") "he" else locale.language
        _currentLanguage.value = normalizedLangCode
        // Recreate activity to apply language changes
        activity.recreate()
    }

    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<LanguageOption> {
        return listOf(
            LanguageOption("en", "English", "English"),
            LanguageOption("he", "עברית", "Hebrew")
        )
    }
}

data class LanguageOption(
    val code: String,
    val displayName: String,
    val englishName: String
)

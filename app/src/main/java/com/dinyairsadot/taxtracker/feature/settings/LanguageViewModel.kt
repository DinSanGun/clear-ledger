package com.dinyairsadot.taxtracker.feature.settings

import android.app.Activity
import android.content.Context
import android.util.Log
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
    
    companion object {
        private const val TAG = "LanguageDebug"
    }
    
    init {
        // #region agent log
        val initLang = languageManager.getCurrentLanguage()
        Log.d(TAG, "[B] ViewModel init: initialLanguage='$initLang', stateFlowValue='${_currentLanguage.value}', contextClass='${context.javaClass.simpleName}'")
        // Update StateFlow if it doesn't match what we read from preferences
        if (_currentLanguage.value != initLang) {
            Log.d(TAG, "[B] StateFlow mismatch detected - updating: stateFlowValue='${_currentLanguage.value}', prefsValue='$initLang'")
            _currentLanguage.value = initLang
        }
        // #endregion
    }

    /**
     * Change the app language and recreate the activity
     */
    fun changeLanguage(locale: Locale, activity: Activity) {
        // #region agent log
        Log.d(TAG, "[C] changeLanguage BEFORE setLanguage: localeLanguage='${locale.language}' (length=${locale.language.length}), currentStateFlow='${_currentLanguage.value}' (length=${_currentLanguage.value.length}), activityClass='${activity.javaClass.simpleName}'")
        // #endregion
        languageManager.setLanguage(locale)
        // #region agent log
        val afterSetLang = languageManager.getCurrentLanguage()
        Log.d(TAG, "[C] changeLanguage AFTER setLanguage: localeLanguage='${locale.language}', readBackFromPrefs='$afterSetLang' (length=${afterSetLang.length}), stateFlowBeforeUpdate='${_currentLanguage.value}', matchAfterWrite=${afterSetLang == locale.language}")
        // #endregion
        // Normalize the language code: Android converts "he" to "iw", so we normalize it back
        val normalizedLangCode = if (locale.language == "iw") "he" else locale.language
        _currentLanguage.value = normalizedLangCode
        // #region agent log
        Log.d(TAG, "[C] changeLanguage AFTER StateFlow update: localeLanguage='${locale.language}', normalizedLangCode='$normalizedLangCode', stateFlowAfterUpdate='${_currentLanguage.value}', matchAfterUpdate=${_currentLanguage.value == normalizedLangCode}")
        // #endregion
        
        // Recreate activity to apply language changes
        // The MainActivity will read the new preference and apply it
        // #region agent log
        Log.d(TAG, "[D] About to recreate activity: localeLanguage='${locale.language}', finalStateFlow='${_currentLanguage.value}', finalPrefsValue='${languageManager.getCurrentLanguage()}'")
        // #endregion
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

package com.dinyairsadot.clearledger.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LanguageViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanguageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanguageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

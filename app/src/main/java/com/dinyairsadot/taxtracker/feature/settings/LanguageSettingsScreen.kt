package com.dinyairsadot.taxtracker.feature.settings

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.dinyairsadot.taxtracker.R
import java.util.Locale

private const val TAG = "LanguageDebug"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onNavigateBack: () -> Unit,
    activity: Activity? = null
) {
    val context = LocalContext.current
    val viewModel: LanguageViewModel = viewModel(
        factory = LanguageViewModelFactory(context)
    )
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val availableLanguages = viewModel.getAvailableLanguages()

    // #region agent log - Track StateFlow changes
    LaunchedEffect(currentLanguage) {
        Log.d(TAG, "[F] StateFlow value changed: currentLanguage='$currentLanguage', availableLanguages=${availableLanguages.map { it.code }}")
    }
    // #endregion

    // #region agent log - Initial composition
    LaunchedEffect(Unit) {
        Log.d(TAG, "[F] Screen composed: currentLanguage='$currentLanguage', activityIsNull=${activity == null}, activityClass='${activity?.javaClass?.simpleName}'")
    }
    // #endregion

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            availableLanguages.forEach { language ->
                // #region agent log
                val isSelected = currentLanguage == language.code
                val comparison = currentLanguage == language.code
                Log.d(TAG, "[E] Rendering language option: languageCode='${language.code}' (length=${language.code.length}), currentLanguage='$currentLanguage' (length=${currentLanguage.length}), isSelected=$isSelected, comparisonResult=$comparison")
                // #endregion
                LanguageOptionItem(
                    language = language,
                    isSelected = isSelected,
                    onClick = {
                        // #region agent log
                        Log.d(TAG, "[E] Language option clicked: languageCode='${language.code}', currentLanguageBeforeClick='$currentLanguage', activityIsNull=${activity == null}, activityClass='${activity?.javaClass?.simpleName}'")
                        // #endregion
                        if (activity != null) {
                            val locale = Locale(language.code)
                            viewModel.changeLanguage(locale, activity)
                        } else {
                            // #region agent log
                            Log.d(TAG, "[H] Activity is null - cannot change language: languageCode='${language.code}'")
                            // #endregion
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // #region agent log - Item rendering
    LaunchedEffect(language.code, isSelected) {
        Log.d("LanguageDebug", "[G] Item rendered: languageCode='${language.code}', isSelected=$isSelected")
    }
    // #endregion
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = language.englishName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

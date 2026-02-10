package com.dinyairsadot.taxtracker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dinyairsadot.taxtracker.core.data.LanguagePreferenceManager
import com.dinyairsadot.taxtracker.core.data.TaxTrackerDatabase
import com.dinyairsadot.taxtracker.core.data.repositories.RoomCategoryRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomInvoiceRepository
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.ui.TaxTrackerNavHost
import com.dinyairsadot.taxtracker.ui.theme.TaxInvoiceTrackerTheme
import kotlinx.coroutines.launch
import com.dinyairsadot.taxtracker.R
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "LanguageDebug"
    }
    
    override fun attachBaseContext(newBase: Context?) {
        val updatedContext = newBase?.let { updateBaseContextLocale(it) }
        Log.d(TAG, "[MAIN] attachBaseContext: newBase=${newBase?.javaClass?.simpleName}, updatedContext=${updatedContext?.javaClass?.simpleName}")
        super.attachBaseContext(updatedContext)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val languageManager = LanguagePreferenceManager(context)
        val savedLocale = languageManager.getSavedLocale()
        val savedLanguage = languageManager.getCurrentLanguage()
        
        Log.d(TAG, "[MAIN] updateBaseContextLocale BEFORE: savedLocale=$savedLocale, savedLanguage='$savedLanguage', contextLocale=${context.resources.configuration.locales[0]}")
        
        // minSdk is 26, so createConfigurationContext + setLocale/setLayoutDirection are always available
        val config = Configuration(context.resources.configuration)
        config.setLocale(savedLocale)
        config.setLayoutDirection(savedLocale)
        Log.d(TAG, "[MAIN] updateBaseContextLocale: Set layout direction for locale=${savedLocale.language}")
        val newContext = context.createConfigurationContext(config)
        val layoutDir = newContext.resources.configuration.layoutDirection
        Log.d(TAG, "[MAIN] updateBaseContextLocale AFTER: newContextLocale=${newContext.resources.configuration.locales[0]}, layoutDirection=$layoutDir")
        val result = newContext

        // Verify the locale was set correctly
        val finalLocale = result.resources.configuration.locales[0]
        val finalLayoutDirection = result.resources.configuration.layoutDirection
        Log.d(TAG, "[MAIN] updateBaseContextLocale FINAL: finalLocale=$finalLocale, finalLanguage='${finalLocale.language}', layoutDirection=$finalLayoutDirection")
        
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // #region agent log - Check locale in onCreate
        val languageManager = LanguagePreferenceManager(this)
        val savedLocale = languageManager.getSavedLocale()
        val savedLanguage = languageManager.getCurrentLanguage()
        val currentConfigLocale = resources.configuration.locales[0]
        val currentConfigLanguage = currentConfigLocale.language
        
        val layoutDir = resources.configuration.layoutDirection
        val baseLayoutDir = baseContext.resources.configuration.layoutDirection
        
        Log.d(TAG, "[MAIN] onCreate: savedLocale=$savedLocale, savedLanguage='$savedLanguage'")
        Log.d(TAG, "[MAIN] onCreate: resources.configuration.locale=$currentConfigLocale, language='$currentConfigLanguage', layoutDirection=$layoutDir")
        Log.d(TAG, "[MAIN] onCreate: baseContext=${baseContext.javaClass.simpleName}, baseContextLocale=${baseContext.resources.configuration.locales[0]}, baseLayoutDirection=$baseLayoutDir")
        
        // Test string resource loading
        val testStringEn = getString(R.string.app_name)
        Log.d(TAG, "[MAIN] onCreate: getString(R.string.app_name)='$testStringEn'")
        // #endregion
        
        enableEdgeToEdge()
        
        // Language preference is already applied via attachBaseContext()
        // Initialize Room database
        val database = TaxTrackerDatabase.getDatabase(this)
        val categoryRepository = RoomCategoryRepository(database.categoryDao())
        val invoiceRepository = RoomInvoiceRepository(database.invoiceDao())
        
        // Seed initial data if database is empty
        lifecycleScope.launch {
            seedInitialDataIfNeeded(categoryRepository, invoiceRepository)
        }
        
        setContent {
            // Debug: observe locale & layout direction via Compose configuration

            val configuration = LocalConfiguration.current

            val composeLocale = configuration.locales[0]
            val composeLanguage = composeLocale.language
            val composeLayoutDir = configuration.layoutDirection

            // Determine layout direction from configuration
            val layoutDirection = if (composeLayoutDir == android.view.View.LAYOUT_DIRECTION_RTL) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }
            
            LaunchedEffect(composeLocale) {
                Log.d(TAG, "[MAIN] Compose setContent: LocalContext.current.resources.configuration.locale=$composeLocale, language='$composeLanguage', layoutDirection=$composeLayoutDir, ComposeLayoutDirection=$layoutDirection")
            }
            // #endregion
            
            // Explicitly provide layout direction to Compose
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                TaxInvoiceTrackerTheme {
                    val navController = rememberNavController()
                    
                    // Remember repositories to avoid recreating them on recomposition
                    val rememberedCategoryRepo = remember { categoryRepository }
                    val rememberedInvoiceRepo = remember { invoiceRepository }

                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TaxTrackerNavHost(
                            navController = navController,
                            categoryRepository = rememberedCategoryRepo,
                            invoiceRepository = rememberedInvoiceRepo
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun seedInitialDataIfNeeded(
        categoryRepository: RoomCategoryRepository,
        invoiceRepository: RoomInvoiceRepository
    ) {
        val categories = categoryRepository.getCategories()
        
        // Only seed if database is empty
        if (categories.isEmpty()) {
            // Seed 8 default categories with their custom fields
            val defaultCategories = listOf(
                // Arnona
                Category(
                    id = 0, // Room will auto-generate
                    name = getString(R.string.default_category_arnona),
                    colorHex = "#4CAF50",
                    description = getString(R.string.default_category_arnona_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_property_id),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Electricity
                Category(
                    id = 0,
                    name = getString(R.string.default_category_electricity),
                    colorHex = "#FF9800",
                    description = getString(R.string.default_category_electricity_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_meter_id),
                        getString(R.string.field_consumption_kwh),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Water
                Category(
                    id = 0,
                    name = getString(R.string.default_category_water),
                    colorHex = "#2196F3",
                    description = getString(R.string.default_category_water_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_meter_id),
                        getString(R.string.field_consumption_cubic_meters),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Gas
                Category(
                    id = 0,
                    name = getString(R.string.default_category_gas),
                    colorHex = "#9C27B0",
                    description = getString(R.string.default_category_gas_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_meter_id),
                        getString(R.string.field_consumption_cubic_meters),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Phone/Internet
                Category(
                    id = 0,
                    name = getString(R.string.default_category_phone_internet),
                    colorHex = "#00BCD4",
                    description = getString(R.string.default_category_phone_internet_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_account_number),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // National Insurance
                Category(
                    id = 0,
                    name = getString(R.string.default_category_national_insurance),
                    colorHex = "#795548",
                    description = getString(R.string.default_category_national_insurance_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_id_number),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Business Expenses
                Category(
                    id = 0,
                    name = getString(R.string.default_category_business_expenses),
                    colorHex = "#F44336",
                    description = getString(R.string.default_category_business_expenses_description),
                    customFieldTitles = listOf(
                        getString(R.string.field_vendor_name),
                        getString(R.string.field_invoice_number),
                        getString(R.string.field_vat_number),
                        getString(R.string.field_billing_period_start),
                        getString(R.string.field_billing_period_end)
                    )
                ),
                // Other
                Category(
                    id = 0,
                    name = getString(R.string.default_category_other),
                    colorHex = "#9E9E9E",
                    description = getString(R.string.default_category_other_description),
                    customFieldTitles = emptyList()
                )
            )
            
            defaultCategories.forEach { category ->
                categoryRepository.addCategory(category)
            }
        }
    }
}


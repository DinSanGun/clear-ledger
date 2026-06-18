package com.dinyairsadot.clearledger

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.rememberNavController
import com.dinyairsadot.clearledger.core.data.LanguagePreferenceManager
import com.dinyairsadot.clearledger.core.data.SeedingPreferenceManager
import com.dinyairsadot.clearledger.core.data.ClearLedgerDatabase
import com.dinyairsadot.clearledger.core.data.repositories.RoomCategoryRepository
import com.dinyairsadot.clearledger.core.data.repositories.RoomInvoiceRepository
import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.ui.LoadingScreen
import com.dinyairsadot.clearledger.core.ui.ClearLedgerNavHost
import com.dinyairsadot.clearledger.ui.theme.TaxInvoiceTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val updatedContext = newBase?.let { updateBaseContextLocale(it) }
        super.attachBaseContext(updatedContext)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val languageManager = LanguagePreferenceManager(context)
        val savedLocale = languageManager.getSavedLocale()
        // minSdk is 26, so createConfigurationContext + setLocale/setLayoutDirection are always available
        val config = Configuration(context.resources.configuration)
        config.setLocale(savedLocale)
        config.setLayoutDirection(savedLocale)
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Language preference is already applied via attachBaseContext()
        // Initialize Room database
        val database = ClearLedgerDatabase.getDatabase(this)
        val categoryRepository = RoomCategoryRepository(database.categoryDao())
        val invoiceRepository = RoomInvoiceRepository(database.invoiceDao())
        val seedingPreferenceManager = SeedingPreferenceManager(this)
        
        setContent {
            val configuration = LocalConfiguration.current
            val composeLayoutDir = configuration.layoutDirection

            // Determine layout direction from configuration
            val layoutDirection = if (composeLayoutDir == android.view.View.LAYOUT_DIRECTION_RTL) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            // Explicitly provide layout direction to Compose
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ClearLedgerTheme {
                    val navController = rememberNavController()

                    // Remember repositories and preference manager to avoid recreating them on recomposition
                    val rememberedCategoryRepo = remember { categoryRepository }
                    val rememberedInvoiceRepo = remember { invoiceRepository }
                    val rememberedSeedingPrefs = remember { seedingPreferenceManager }
                    val languageManager = remember { LanguagePreferenceManager(this@MainActivity) }

                    // Track initialization state: true = checking/seeding, false = ready
                    // Start with true to show loading screen while we check if seeding is needed
                    var isInitializing by remember { mutableStateOf(true) }

                    // Check if seeding is needed and perform it if necessary
                    LaunchedEffect(Unit) {
                        val hasSeeded = rememberedSeedingPrefs.hasSeededDefaultCategories()

                        if (!hasSeeded) {
                            // Flag indicates seeding hasn't been done - check database
                            val categories = rememberedCategoryRepo.getCategories()
                            val needsSeeding = categories.isEmpty()

                            if (needsSeeding) {
                                // Database is empty - seed now (loading screen already showing)
                                seedInitialDataIfNeeded(rememberedCategoryRepo, rememberedInvoiceRepo, rememberedSeedingPrefs)
                            }
                        }

                        // Mark as ready (seeding complete or not needed)
                        isInitializing = false
                    }

                    // After init: one-time migration to clear custom fields from seeded categories
                    LaunchedEffect(isInitializing) {
                        if (isInitializing) return@LaunchedEffect
                        if (!rememberedSeedingPrefs.hasClearedSeededCustomFields()) {
                            rememberedCategoryRepo.clearCustomFieldsForSeededCategories()
                            rememberedSeedingPrefs.setHasClearedSeededCustomFields(true)
                        }
                        val current = languageManager.getCurrentLanguage()
                        val lastApplied = languageManager.getLastAppliedLanguage()
                        if (lastApplied != current) {
                            rememberedCategoryRepo.updateLocalizedSeededCategories(this@MainActivity)
                            languageManager.setLastAppliedLanguage(current)
                        }
                    }

                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Show loading screen while checking/seeding, then show NavHost
                        if (isInitializing) {
                            LoadingScreen()
                        } else {
                            ClearLedgerNavHost(
                                navController = navController,
                                categoryRepository = rememberedCategoryRepo,
                                invoiceRepository = rememberedInvoiceRepo
                            )
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun seedInitialDataIfNeeded(
        categoryRepository: RoomCategoryRepository,
        invoiceRepository: RoomInvoiceRepository,
        seedingPreferenceManager: SeedingPreferenceManager
    ) {
        // Check flag first - if already seeded, don't seed again even if database is empty
        if (seedingPreferenceManager.hasSeededDefaultCategories()) {
            return
        }
        
        val categories = categoryRepository.getCategories()
        
        // Only seed if database is empty AND flag indicates seeding hasn't been done
        if (categories.isEmpty()) {
            // Seed 8 default categories with minimal custom fields
            val defaultCategories = listOf(
                // Arnona
                Category(
                    id = 0, // Room will auto-generate
                    name = getString(R.string.default_category_arnona),
                    colorHex = "#4CAF50",
                    description = getString(R.string.default_category_arnona_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "arnona",
                    userEdited = false,
                    orderIndex = 0
                ),
                // Electricity
                Category(
                    id = 0,
                    name = getString(R.string.default_category_electricity),
                    colorHex = "#FF9800",
                    description = getString(R.string.default_category_electricity_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "electricity",
                    userEdited = false,
                    orderIndex = 1
                ),
                // Water
                Category(
                    id = 0,
                    name = getString(R.string.default_category_water),
                    colorHex = "#2196F3",
                    description = getString(R.string.default_category_water_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "water",
                    userEdited = false,
                    orderIndex = 2
                ),
                // Gas
                Category(
                    id = 0,
                    name = getString(R.string.default_category_gas),
                    colorHex = "#9C27B0",
                    description = getString(R.string.default_category_gas_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "gas",
                    userEdited = false,
                    orderIndex = 3
                ),
                // Phone/Internet
                Category(
                    id = 0,
                    name = getString(R.string.default_category_phone_internet),
                    colorHex = "#00BCD4",
                    description = getString(R.string.default_category_phone_internet_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "phone_internet",
                    userEdited = false,
                    orderIndex = 4
                ),
                // National Insurance
                Category(
                    id = 0,
                    name = getString(R.string.default_category_national_insurance),
                    colorHex = "#795548",
                    description = getString(R.string.default_category_national_insurance_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "national_insurance",
                    userEdited = false,
                    orderIndex = 5
                ),
                // Health Fund
                Category(
                    id = 0,
                    name = getString(R.string.default_category_health_fund),
                    colorHex = "#3F51B5",
                    description = getString(R.string.default_category_health_fund_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "health_fund",
                    userEdited = false,
                    orderIndex = 6
                ),
                // Car Insurance
                Category(
                    id = 0,
                    name = getString(R.string.default_category_car_insurance),
                    colorHex = "#FF5722",
                    description = getString(R.string.default_category_car_insurance_description),
                    customFieldTitles = emptyList(),
                    pinnedDefaults = emptyMap(),
                    seedKey = "car_insurance",
                    userEdited = false,
                    orderIndex = 7
                )
            )
            
            defaultCategories.forEach { category ->
                categoryRepository.addCategory(category)
            }
            
            // Mark seeding as complete - this ensures we never seed again,
            // even if the user deletes all categories later
            seedingPreferenceManager.setHasSeededDefaultCategories(true)
        }
    }
}


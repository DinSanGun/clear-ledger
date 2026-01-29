package com.dinyairsadot.taxtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dinyairsadot.taxtracker.core.data.TaxTrackerDatabase
import com.dinyairsadot.taxtracker.core.data.repositories.RoomCategoryRepository
import com.dinyairsadot.taxtracker.core.data.repositories.RoomInvoiceRepository
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.ui.TaxTrackerNavHost
import com.dinyairsadot.taxtracker.ui.theme.TaxInvoiceTrackerTheme
import kotlinx.coroutines.launch
import com.dinyairsadot.taxtracker.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room database
        val database = TaxTrackerDatabase.getDatabase(this)
        val categoryRepository = RoomCategoryRepository(database.categoryDao())
        val invoiceRepository = RoomInvoiceRepository(database.invoiceDao())
        
        // Seed initial data if database is empty
        lifecycleScope.launch {
            seedInitialDataIfNeeded(categoryRepository, invoiceRepository)
        }
        
        setContent {
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
    
    private suspend fun seedInitialDataIfNeeded(
        categoryRepository: RoomCategoryRepository,
        invoiceRepository: RoomInvoiceRepository
    ) {
        val categories = categoryRepository.getCategories()
        
        // Only seed if database is empty
        if (categories.isEmpty()) {
            // Seed 3 default categories
            val defaultCategories = listOf(
                Category(
                    id = 0, // Room will auto-generate
                    name = getString(R.string.default_category_electricity),
                    colorHex = "#FF9800",
                    description = getString(R.string.default_category_electricity_description),
                    customFieldTitles = emptyList()
                ),
                Category(
                    id = 0,
                    name = getString(R.string.default_category_water),
                    colorHex = "#2196F3",
                    description = getString(R.string.default_category_water_description),
                    customFieldTitles = emptyList()
                ),
                Category(
                    id = 0,
                    name = getString(R.string.default_category_city_taxes),
                    colorHex = "#4CAF50",
                    description = getString(R.string.default_category_city_taxes_description),
                    customFieldTitles = emptyList()
                )
            )
            
            defaultCategories.forEach { category ->
                categoryRepository.addCategory(category)
            }
        }
    }
}


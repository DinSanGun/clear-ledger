package com.dinyairsadot.taxtracker.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dinyairsadot.taxtracker.feature.category.AddCategoryScreen
import com.dinyairsadot.taxtracker.feature.category.CategoryListRoute
import com.dinyairsadot.taxtracker.feature.category.CategoryListViewModel
import com.dinyairsadot.taxtracker.feature.category.EditCategoryScreen
import com.dinyairsadot.taxtracker.feature.invoice.InvoiceListScreen
import com.dinyairsadot.taxtracker.feature.invoice.AddInvoiceScreen

import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.stringResource
import com.dinyairsadot.taxtracker.R


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import com.dinyairsadot.taxtracker.feature.invoice.InvoiceDetailsScreen
import com.dinyairsadot.taxtracker.feature.invoice.InvoiceListViewModel

import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dinyairsadot.taxtracker.feature.invoice.EditInvoiceScreen
import com.dinyairsadot.taxtracker.feature.invoice.InvoiceListUiState
import com.dinyairsadot.taxtracker.feature.invoice.InvoiceUi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.core.domain.CategoryRepository
import com.dinyairsadot.taxtracker.core.domain.InvoiceRepository
import com.dinyairsadot.taxtracker.feature.category.CategoryListViewModelFactory
import com.dinyairsadot.taxtracker.feature.invoice.InvoiceListViewModelFactory
import com.dinyairsadot.taxtracker.feature.settings.LanguageSettingsScreen
import androidx.activity.ComponentActivity



// Adjust if your Screen definitions live elsewhere
sealed class Screen(val route: String) {
    object CategoryList : Screen("category_list")
    object AddCategory : Screen("add_category")
    object EditCategory : Screen("edit_category/{categoryId}") {
        const val ARG_CATEGORY_ID = "categoryId"
        fun routeWithId(id: Long): String = "edit_category/$id"
    }

    object InvoiceList : Screen("invoice_list/{categoryId}") {
        fun routeWithCategoryId(categoryId: Long) = "invoice_list/$categoryId"
    }

    object AddInvoice : Screen("add_invoice/{categoryId}") {
        fun routeWithCategoryId(categoryId: Long) = "add_invoice/$categoryId"
    }

    object InvoiceDetails : Screen("invoice_details/{invoiceId}") {
        fun routeWithId(invoiceId: Long) = "invoice_details/$invoiceId"
    }

    object EditInvoice : Screen("edit_invoice/{invoiceId}") {
        fun routeWithId(invoiceId: Long) = "edit_invoice/$invoiceId"
    }

    object LanguageSettings : Screen("language_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxTrackerNavHost(
    navController: NavHostController,
    categoryRepository: CategoryRepository,
    invoiceRepository: InvoiceRepository,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CategoryList.route,
        modifier = modifier
    ) {
        // -------------------------
        // Category list screen
        // -------------------------
        composable(Screen.CategoryList.route) { backStackEntry ->
            val context = LocalContext.current
            val viewModel: CategoryListViewModel = viewModel(
                backStackEntry,
                factory = CategoryListViewModelFactory(categoryRepository, invoiceRepository, context)
            )

            // Read "category_added" flag from saved state (for snackbar)
            val categoryAdded =
                backStackEntry.savedStateHandle.get<Boolean>("category_added") == true

            CategoryListRoute(
                onAddCategoryClick = {
                    navController.navigate(Screen.AddCategory.route)
                },
                onCategoryClick = { id ->
                    navController.navigate(Screen.InvoiceList.routeWithCategoryId(id))
                },
                onEditCategoryClick = { id ->
                    navController.navigate(Screen.EditCategory.routeWithId(id))
                },
                onLanguageSettingsClick = {
                    navController.navigate(Screen.LanguageSettings.route)
                },
                viewModel = viewModel,
                showCategoryAddedMessage = categoryAdded,
                onCategoryAddedMessageShown = {
                    backStackEntry.savedStateHandle.remove<Boolean>("category_added")
                }
            )
        }

        // -------------------------
        // Add category screen
        // -------------------------
        composable(Screen.AddCategory.route) { backStackEntry ->
            // Share the same ViewModel instance as CategoryList
            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CategoryList.route)
            }
            val viewModel: CategoryListViewModel = viewModel(
                parentEntry,
                factory = CategoryListViewModelFactory(categoryRepository, invoiceRepository, context)
            )

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val existingNamesLower = uiState.categories
                .map { it.name.trim().lowercase() }
                .toSet()

            AddCategoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveCategory = { name, colorHex, description, customFieldTitles ->
                    viewModel.addCategory(name, colorHex, description, customFieldTitles)
                },
                existingNamesLower = existingNamesLower,
                onCategorySaved = {
                    // Set flag so CategoryList can show "Category added" snackbar
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("category_added", true)
                }
            )
        }

        // -------------------------
        // Edit category screen
        // -------------------------
        composable(
            route = Screen.EditCategory.route,
            arguments = listOf(
                navArgument(Screen.EditCategory.ARG_CATEGORY_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val categoryId =
                backStackEntry.arguments?.getLong(Screen.EditCategory.ARG_CATEGORY_ID)
                    ?: return@composable

            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CategoryList.route)
            }
            val viewModel: CategoryListViewModel = viewModel(
                parentEntry,
                factory = CategoryListViewModelFactory(categoryRepository, invoiceRepository, context)
            )

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val categoryUi = uiState.categories.firstOrNull { it.id == categoryId }
            if (categoryUi == null) {
                // If category is missing (e.g. deleted), go back
                navController.popBackStack()
                return@composable
            }

            val otherNamesLower = uiState.categories
                .filter { it.id != categoryId }
                .map { it.name.trim().lowercase() }
                .toSet()

            // Get the full category from repository to access all custom fields
            var fullCategory by remember { mutableStateOf<Category?>(null) }
            LaunchedEffect(categoryId) {
                fullCategory = viewModel.getCategoryById(categoryId)
            }

            if (fullCategory == null) {
                // Loading or category not found
                return@composable
            }

            // Extract non-null category for smart cast
            val category = fullCategory!!

            EditCategoryScreen(
                categoryId = categoryUi.id,
                initialName = categoryUi.name,
                initialColorHex = categoryUi.colorHex,
                initialDescription = categoryUi.description,
                categoryColorHex = categoryUi.colorHex,
                initialCustomFieldTitles = category.customFieldTitles,
                otherNamesLower = otherNamesLower,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveCategory = { name, colorHex, description, customFieldTitles ->
                    viewModel.updateCategory(
                        id = categoryUi.id,
                        name = name,
                        colorHex = colorHex,
                        description = description,
                        customFieldTitles = customFieldTitles
                    )
                },
                viewModel = viewModel
                )
        }
        // -------------------------
        // Invoice list screen
        // -------------------------
        composable(
            route = Screen.InvoiceList.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: return@composable

            val context = LocalContext.current
            val viewModel: InvoiceListViewModel = viewModel(
                backStackEntry,
                factory = InvoiceListViewModelFactory(invoiceRepository, categoryRepository, context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Load invoices whenever the categoryId changes (or first time we enter)
            androidx.compose.runtime.LaunchedEffect(categoryId) {
                viewModel.loadCategoryHeader(categoryId)
                viewModel.loadInvoices(categoryId)
            }

            InvoiceListScreen(
                categoryId = categoryId,
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onAddInvoiceClick = {
                    navController.navigate(Screen.AddInvoice.routeWithCategoryId(categoryId))
                },
                onInvoiceClick = { invoiceId ->
                    navController.navigate(
                        Screen.InvoiceDetails.routeWithId(invoiceId)
                    )
                },
                onDeleteInvoice = { invoiceId ->
                    viewModel.deleteInvoice(invoiceId = invoiceId, categoryId = categoryId)
                },
                categoryColorHex = uiState.categoryColorHex,
                onSortOptionChange = { sortOption ->
                    viewModel.setSortOption(sortOption)
                },
                onSearchQueryChange = { query ->
                    viewModel.setSearchQuery(query)
                },
                onSearchModeChange = { mode ->
                    viewModel.setSearchMode(mode)
                },
                onServicePeriodStartFilterChange = { date ->
                    viewModel.setServicePeriodStartFilter(date)
                },
                onServicePeriodEndFilterChange = { date ->
                    viewModel.setServicePeriodEndFilter(date)
                },
                onStatusFilterChange = { status ->
                    viewModel.setStatusFilter(status)
                },
                onClearFilters = {
                    viewModel.clearFilters()
                }
            )
        }
        // -------------------------
        // Invoice add screen
        // -------------------------
        composable(
            route = Screen.AddInvoice.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: return@composable

            // We don't need uiState here, just the ability to add an invoice.
            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceList.route)
            }
            val viewModel: InvoiceListViewModel = viewModel(
                parentEntry,
                factory = InvoiceListViewModelFactory(invoiceRepository, categoryRepository, context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AddInvoiceScreen(
                categoryId = categoryId,
                categoryName = uiState.categoryName,
                categoryColorHex = uiState.categoryColorHex,
                categoryCustomFieldTitles = uiState.categoryCustomFieldTitles,
                onSaveInvoice = { documentNumber, amountDue, paymentStatus, servicePeriodStartText, servicePeriodEndText, servicePeriodMode, paymentDate, dueDate, paymentMethod, numberOfPayments, confirmationNumber, vendorName, notes, customFieldValues, amountCurrency ->
                    viewModel.addInvoice(
                        categoryId = categoryId,
                        documentNumber = documentNumber,
                        amountDue = amountDue,
                        paymentStatus = paymentStatus,
                        servicePeriodStartText = servicePeriodStartText,
                        servicePeriodEndText = servicePeriodEndText,
                        servicePeriodMode = servicePeriodMode,
                        paymentDate = paymentDate,
                        dueDate = dueDate,
                        paymentMethod = paymentMethod,
                        numberOfPayments = numberOfPayments,
                        confirmationNumber = confirmationNumber,
                        vendorName = vendorName,
                        notes = notes,
                        customFieldValues = customFieldValues,
                        amountCurrency = amountCurrency
                    )
                    // AddInvoiceScreen will also call onNavigateBack() after this
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // -------------------------
        // Invoice detail screen
        // -------------------------
        composable(
            route = Screen.InvoiceDetails.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable

            // Share InvoiceListViewModel between list and details,
            // just like you share CategoryListViewModel across screens.
            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceList.route)
            }
            val viewModel: InvoiceListViewModel = viewModel(
                parentEntry,
                factory = InvoiceListViewModelFactory(invoiceRepository, categoryRepository, context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val invoice = uiState.visibleInvoices.firstOrNull { it.id == invoiceId }

            if (invoice == null) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.invoice_not_found)) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back)
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.invoice_could_not_be_loaded))
                    }
                }
            } else {
                InvoiceDetailsScreen(
                    invoice = invoice,
                    categoryCustomFieldTitles = uiState.categoryCustomFieldTitles,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        navController.navigate(
                            Screen.EditInvoice.routeWithId(invoice.id)
                        )
                    },
                    categoryColorHex = uiState.categoryColorHex
                )
            }
        }
        // -------------------------
        // Invoice edit screen
        // -------------------------
        composable(
            route = Screen.EditInvoice.route,
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable

            // Share the same InvoiceListViewModel as list/details
            val context = LocalContext.current
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceList.route)
            }
            val viewModel: InvoiceListViewModel = viewModel(
                parentEntry,
                factory = InvoiceListViewModelFactory(invoiceRepository, categoryRepository, context)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val invoiceUi = uiState.visibleInvoices.firstOrNull { it.id == invoiceId }

            if (invoiceUi == null) {
                // If we somehow got here without an invoice, just go back
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }

            EditInvoiceScreen(
                invoiceId = invoiceUi.id,
                categoryColorHex = uiState.categoryColorHex,
                categoryCustomFieldTitles = uiState.categoryCustomFieldTitles,
                initialServicePeriodMode = invoiceUi.servicePeriodMode,
                initialDocumentNumber = invoiceUi.documentNumber,
                initialAmount = invoiceUi.amountDue.toString(),
                initialPaymentStatus = invoiceUi.paymentStatus,
                initialServicePeriodStartText = invoiceUi.servicePeriodStartText ?: "",
                initialServicePeriodEndText = invoiceUi.servicePeriodEndText ?: "",
                initialPaymentDateText = invoiceUi.paymentDateText,
                initialDueDateText = invoiceUi.dueDateText,
                initialPaymentMethod = invoiceUi.paymentMethod,
                initialNumberOfPayments = invoiceUi.numberOfPayments,
                initialConfirmationNumber = invoiceUi.confirmationNumber,
                initialVendorName = invoiceUi.vendorName,
                initialNotes = invoiceUi.notes ?: "",
                initialCustomFieldValues = invoiceUi.customFieldValues,
                initialAmountCurrency = invoiceUi.amountCurrency,
                onNavigateBack = { navController.popBackStack() },
                onSaveInvoice = { documentNumber, amountDue, paymentStatus, servicePeriodStartText, servicePeriodEndText, servicePeriodMode, paymentDate, dueDate, paymentMethod, numberOfPayments, confirmationNumber, vendorName, notes, customFieldValues, amountCurrency ->
                    viewModel.updateInvoice(
                        invoiceId = invoiceUi.id,
                        documentNumber = documentNumber,
                        amountDue = amountDue,
                        paymentStatus = paymentStatus,
                        servicePeriodStartText = servicePeriodStartText,
                        servicePeriodEndText = servicePeriodEndText,
                        servicePeriodMode = servicePeriodMode,
                        paymentDate = paymentDate,
                        dueDate = dueDate,
                        paymentMethod = paymentMethod,
                        numberOfPayments = numberOfPayments,
                        confirmationNumber = confirmationNumber,
                        vendorName = vendorName,
                        notes = notes,
                        customFieldValues = customFieldValues,
                        amountCurrency = amountCurrency
                    )
                    // EditInvoiceScreen itself calls onNavigateBack()
                }
            )
        }
        // -------------------------
        // Language Settings screen
        // -------------------------
        composable(Screen.LanguageSettings.route) { backStackEntry ->
            val context = LocalContext.current
            val activity = context as? ComponentActivity
            
            LanguageSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                activity = activity
            )
        }
    }
}

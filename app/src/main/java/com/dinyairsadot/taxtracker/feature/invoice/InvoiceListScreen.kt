package com.dinyairsadot.taxtracker.feature.invoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import com.dinyairsadot.taxtracker.feature.invoice.SortOption
import com.dinyairsadot.taxtracker.feature.invoice.formatServicePeriodForDisplay
import com.dinyairsadot.taxtracker.R

private const val SORT_MENU_ANIM_MS = 420

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceListScreen(
    categoryId: Long,
    uiState: InvoiceListUiState,
    onBackClick: () -> Unit,
    onEditCategoryClick: () -> Unit,
    onAddInvoiceClick: () -> Unit,
    onInvoiceClick: (Long) -> Unit,
    onDeleteInvoice: (Long) -> Unit,
    categoryColorHex: String?,
    onSortOptionChange: (SortOption) -> Unit
    ) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val invoiceDeletedMessage = stringResource(R.string.invoice_deleted)
    var pendingDeleteInvoiceId by remember { mutableStateOf<Long?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    val sortMenuVisibility = remember { MutableTransitionState(false) }

    LaunchedEffect(sortMenuVisibility.isIdle, sortMenuVisibility.currentState, showSortMenu) {
        // Keep popup mounted while exiting; dismiss only after exit animation completes.
        if (showSortMenu && sortMenuVisibility.isIdle && !sortMenuVisibility.currentState) {
            showSortMenu = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    val headerColor = uiState.categoryColorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val onHeaderColor = contentColorFor(headerColor)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        topBar = {
            TopAppBar(
                title = { Text(uiState.categoryName ?: stringResource(R.string.invoices)) },
                colors = categoryTopAppBarColors(categoryColorHex),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Sort menu
                    Box {
                        IconButton(onClick = {
                            showSortMenu = true
                            sortMenuVisibility.targetState = true
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { sortMenuVisibility.targetState = false },
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            AnimatedVisibility(
                                visibleState = sortMenuVisibility,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing)),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing))
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shadowElevation = 2.dp,
                                    tonalElevation = 2.dp
                                ) {
                                    Column {
                                        DropdownMenuItem(
                                        text = { Text(stringResource(R.string.date_newest_first)) },
                                        onClick = {
                                            onSortOptionChange(SortOption.DATE_DESCENDING)
                                            sortMenuVisibility.targetState = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.date_oldest_first)) },
                                        onClick = {
                                            onSortOptionChange(SortOption.DATE_ASCENDING)
                                            sortMenuVisibility.targetState = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.amount_highest_first)) },
                                        onClick = {
                                            onSortOptionChange(SortOption.AMOUNT_DESCENDING)
                                            sortMenuVisibility.targetState = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.amount_lowest_first)) },
                                        onClick = {
                                            onSortOptionChange(SortOption.AMOUNT_ASCENDING)
                                            sortMenuVisibility.targetState = false
                                        }
                                    )
                                    }
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = onEditCategoryClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = LocalContentColor.current
                        )
                    ) {
                        Text(stringResource(R.string.edit_category))
                    }
                }
                )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddInvoiceClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_invoice)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    ErrorState(
                        message = uiState.errorMessage,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.invoices.isEmpty() -> {
                    EmptyInvoicesState(
                        categoryId = categoryId,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    InvoiceListContent(
                        invoices = uiState.invoices,
                        modifier = Modifier.fillMaxSize(),
                        onInvoiceClick = onInvoiceClick,
                        onRequestDeleteInvoice = { id -> pendingDeleteInvoiceId = id }
                    )
                }
            }
        }
    }

    pendingDeleteInvoiceId?.let { invoiceId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteInvoiceId = null },
            title = { Text(stringResource(R.string.delete_invoice)) },
            text = { Text(stringResource(R.string.delete_invoice_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteInvoice(invoiceId)
                        pendingDeleteInvoiceId = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(invoiceDeletedMessage)
                        }
                    }
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteInvoiceId = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = stringResource(R.string.please_try_again_later),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun EmptyInvoicesState(
    categoryId: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_invoices_yet),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.padding(top = 4.dp))
        Text(
            text = stringResource(R.string.tap_to_add_first_invoice),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InvoiceListContent(
    invoices: List<InvoiceUi>,
    modifier: Modifier = Modifier,
    onInvoiceClick: (Long) -> Unit,
    onRequestDeleteInvoice: (Long) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            items(invoices) { invoice ->
                InvoiceItem(
                    invoice = invoice,
                    onClick = { onInvoiceClick(invoice.id) },
                    onDeleteClick = { onRequestDeleteInvoice(invoice.id) },
                    context = context
                )
            }
    }
}

@Composable
private fun InvoiceItem(
    invoice: InvoiceUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    context: android.content.Context
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Left content (everything that should flow top->bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 84.dp) // reserve space for amount + delete so they never overlap text
            ) {
                Text(
                    text = invoice.invoiceNumber.ifBlank { stringResource(R.string.invoice_number_fallback, invoice.id) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.padding(top = 4.dp))

                Text(
                    text = when (invoice.paymentStatus) {
                        com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID_FULL -> stringResource(R.string.paid_in_full)
                        com.dinyairsadot.taxtracker.core.domain.PaymentStatus.NOT_PAID -> stringResource(R.string.not_paid)
                        com.dinyairsadot.taxtracker.core.domain.PaymentStatus.PAID_CREDIT -> stringResource(R.string.paid_with_credit)
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                invoice.dueDateText?.let { due ->
                    Text(
                        text = stringResource(R.string.due, due),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                formatServicePeriodForDisplay(
                    invoice.servicePeriodStartText,
                    invoice.servicePeriodEndText,
                    invoice.servicePeriodMode,
                    context.resources.configuration.locales[0]
                )?.let { servicePeriod ->
                    Text(
                        text = servicePeriod,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Right content pinned to the vertical center of the entire card content
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatAmountILS(invoice.amount, context),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_invoice),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatAmountILS(amount: Double, context: android.content.Context): String {
    return context.getString(R.string.amount_format_ils, amount)
}

/**
 * Helper to convert #RRGGBB to a Color.
 */
private fun parseColor(hex: String): Color {
    return try {
        if (hex.isBlank()) {
            Color.Gray
        } else {
            Color(hex.toColorInt())
        }
    } catch (_: IllegalArgumentException) {
        Color.Gray
    }
}



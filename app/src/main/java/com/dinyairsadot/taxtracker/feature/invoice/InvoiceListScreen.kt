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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.RadioButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.dinyairsadot.taxtracker.core.ui.SwipeDismissSnackbarHost
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import com.dinyairsadot.taxtracker.feature.invoice.SortOption
import com.dinyairsadot.taxtracker.feature.invoice.formatServicePeriodForDisplay
import com.dinyairsadot.taxtracker.R
import com.dinyairsadot.taxtracker.core.domain.PaymentStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val SORT_MENU_ANIM_MS = 420
private val LIST_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Same width for From/To so the date strips start on one vertical line (LTR/RTL). */
private val FilterSheetDateLabelColumnWidth = 88.dp

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
    onSortOptionChange: (SortOption) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchModeChange: (SearchMode) -> Unit,
    onServicePeriodStartFilterChange: (LocalDate?) -> Unit,
    onServicePeriodEndFilterChange: (LocalDate?) -> Unit,
    onStatusFilterChange: (PaymentStatus?) -> Unit,
    onClearFilters: () -> Unit
) {

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val invoiceDeletedMessage = stringResource(R.string.invoice_deleted)
    var pendingDeleteInvoiceId by remember { mutableStateOf<Long?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    val sortMenuVisibility = remember { MutableTransitionState(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filtersActive = remember(
        uiState.servicePeriodStartFilter,
        uiState.servicePeriodEndFilter,
        uiState.statusFilter
    ) {
        uiState.servicePeriodStartFilter != null ||
            uiState.servicePeriodEndFilter != null ||
            uiState.statusFilter != null
    }

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
        snackbarHost = { SwipeDismissSnackbarHost(hostState = snackbarHostState) },

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
                                    initialOffsetY = { -it },
                                    animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(SORT_MENU_ANIM_MS, easing = FastOutSlowInEasing)),
                                exit = slideOutVertically(
                                    targetOffsetY = { -it },
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
                                        Text(
                                            text = stringResource(R.string.sort_by),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 6.dp)
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
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

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        SearchBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = onSearchQueryChange,
                            searchMode = uiState.searchMode,
                            onSearchModeChange = onSearchModeChange,
                            filtersActive = filtersActive,
                            onFilterClick = { showFilterSheet = true }
                        )

                        if (uiState.visibleInvoices.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyInvoicesState(
                                    categoryId = categoryId
                                )
                            }
                        } else {
                            InvoiceListContent(
                                invoices = uiState.visibleInvoices,
                                modifier = Modifier.weight(1f),
                                onInvoiceClick = onInvoiceClick,
                                onRequestDeleteInvoice = { id -> pendingDeleteInvoiceId = id }
                            )
                        }
                    }
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

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            FilterSheetContent(
                servicePeriodStart = uiState.servicePeriodStartFilter,
                servicePeriodEnd = uiState.servicePeriodEndFilter,
                statusFilter = uiState.statusFilter,
                onServicePeriodStartChange = onServicePeriodStartFilterChange,
                onServicePeriodEndChange = onServicePeriodEndFilterChange,
                onStatusFilterChange = onStatusFilterChange,
                onReset = onClearFilters
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchMode: SearchMode,
    onSearchModeChange: (SearchMode) -> Unit,
    filtersActive: Boolean,
    onFilterClick: () -> Unit
) {
    val placeholderRes = when (searchMode) {
        SearchMode.INVOICE_NUMBER -> R.string.search_by_invoice_number_placeholder
        SearchMode.AMOUNT -> R.string.search_by_amount_placeholder
    }

    val modeIndicator = when (searchMode) {
        SearchMode.INVOICE_NUMBER -> "# ▾"
        SearchMode.AMOUNT -> "₪ ▾"
    }

    var showModeMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(placeholderRes)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clickable { showModeMenu = true }
                                        .padding(start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = modeIndicator,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(onClick = onFilterClick) {
                                    Icon(
                                        imageVector = Icons.Filled.FilterList,
                                        contentDescription = stringResource(R.string.filter),
                                        tint = if (filtersActive) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showModeMenu,
                                onDismissRequest = { showModeMenu = false }
                            ) {
                                Text(
                                    text = stringResource(R.string.search_by_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.search_mode_invoice_number)) },
                                    onClick = {
                                        onSearchModeChange(SearchMode.INVOICE_NUMBER)
                                        showModeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.search_mode_amount)) },
                                    onClick = {
                                        onSearchModeChange(SearchMode.AMOUNT)
                                        showModeMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheetContent(
    servicePeriodStart: LocalDate?,
    servicePeriodEnd: LocalDate?,
    statusFilter: PaymentStatus?,
    onServicePeriodStartChange: (LocalDate?) -> Unit,
    onServicePeriodEndChange: (LocalDate?) -> Unit,
    onStatusFilterChange: (PaymentStatus?) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.reset))
            }
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = stringResource(R.string.service_period),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.padding(top = 6.dp))

        DateFilterRow(
            label = stringResource(R.string.from_date),
            date = servicePeriodStart,
            onDateChange = onServicePeriodStartChange
        )
        DateFilterRow(
            label = stringResource(R.string.to_date),
            date = servicePeriodEnd,
            onDateChange = onServicePeriodEndChange
        )

        Spacer(modifier = Modifier.padding(top = 12.dp))

        Text(
            text = stringResource(R.string.status),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.padding(top = 6.dp))

        StatusOptionRow(
            label = stringResource(R.string.all),
            selected = statusFilter == null,
            onSelect = { onStatusFilterChange(null) }
        )
        StatusOptionRow(
            label = stringResource(R.string.paid),
            selected = statusFilter == PaymentStatus.PAID,
            onSelect = { onStatusFilterChange(PaymentStatus.PAID) }
        )
        StatusOptionRow(
            label = stringResource(R.string.not_paid),
            selected = statusFilter == PaymentStatus.NOT_PAID,
            onSelect = { onStatusFilterChange(PaymentStatus.NOT_PAID) }
        )

        Spacer(modifier = Modifier.padding(bottom = 24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterRow(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val zoneId = remember { ZoneId.systemDefault() }

    val initialMillis = remember(date) {
        date?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(FilterSheetDateLabelColumnWidth),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clickable(
                    onClickLabel = stringResource(R.string.pick_date),
                    role = Role.Button,
                    onClick = { showPicker = true }
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (date != null) {
                        date.format(LIST_DATE_FORMATTER)
                    } else {
                        stringResource(R.string.format_hint_dd_mm_yyyy)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (date != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        IconButton(
            onClick = { onDateChange(null) },
            enabled = date != null
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.clear_filter_date_cd)
            )
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        val picked = millis?.let {
                            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
                        }
                        onDateChange(picked)
                        showPicker = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun StatusOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
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
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Invoice data block
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                val invoiceNumberText = invoice.invoiceNumber.ifBlank {
                    stringResource(R.string.invoice_number_fallback, invoice.id)
                }
                Text(
                    text = stringResource(R.string.invoice_number_label, invoiceNumberText.truncateForList()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.padding(top = 2.dp))

                val statusText = when (invoice.paymentStatus) {
                    PaymentStatus.PAID -> stringResource(R.string.paid)
                    PaymentStatus.NOT_PAID -> stringResource(R.string.not_paid)
                }
                val statusColor = invoice.paymentStatus.toDisplayColor()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val servicePeriod = formatServicePeriodForDisplay(
                        invoice.servicePeriodStartText,
                        invoice.servicePeriodEndText,
                        invoice.servicePeriodMode,
                        context.resources.configuration.locales[0]
                    )

                    servicePeriod?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = " \u2022 ",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.padding(top = 2.dp))

                Text(
                    text = formatAmountWithCurrency(context, invoice.amount, invoice.amountCurrency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                invoice.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Spacer(modifier = Modifier.padding(top = 2.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

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

private fun String.truncateForList(maxChars: Int = 12): String {
    if (this.length <= maxChars) return this
    return this.take(maxChars) + "…"
}

@Composable
private fun PaymentStatus.toDisplayColor(): Color {
    return when (this) {
        PaymentStatus.PAID -> Color(0xFF4CAF50)
        PaymentStatus.NOT_PAID -> MaterialTheme.colorScheme.error
    }
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



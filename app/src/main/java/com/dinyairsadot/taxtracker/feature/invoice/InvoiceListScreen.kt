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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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

/** Search-bar funnel: 80% of 28dp, then 90% of that (user-tuned). */
private val SearchBarFilterIconSizeDp = (28f * 0.8f * 0.9f).dp

/** # / ₪ next to search field: labelMedium × prior 1.3 × 1.2 (120% bump). */
private const val SEARCH_BAR_MODE_SYMBOL_SCALE = 1.3f * 1.2f

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
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            FilterSheetContent(
                servicePeriodStart = uiState.servicePeriodStartFilter,
                servicePeriodEnd = uiState.servicePeriodEndFilter,
                statusFilter = uiState.statusFilter,
                onServicePeriodStartChange = onServicePeriodStartFilterChange,
                onServicePeriodEndChange = onServicePeriodEndFilterChange,
                onStatusFilterChange = onStatusFilterChange,
                onClearFilters = onClearFilters,
                onApply = { showFilterSheet = false }
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

    val labelMedium = MaterialTheme.typography.labelMedium
    val modeIndicatorSymbolStyle = remember(
        labelMedium.fontSize.value,
        labelMedium.fontSize.type
    ) {
        val fs = labelMedium.fontSize
        val scaled = when (fs.type) {
            TextUnitType.Sp -> (fs.value * SEARCH_BAR_MODE_SYMBOL_SCALE).sp
            TextUnitType.Em -> (fs.value * SEARCH_BAR_MODE_SYMBOL_SCALE).em
            else -> (14f * SEARCH_BAR_MODE_SYMBOL_SCALE).sp
        }
        val line = when (scaled.type) {
            TextUnitType.Sp -> (scaled.value * 1.35f).sp
            TextUnitType.Em -> (scaled.value * 1.35f).em
            else -> TextUnit.Unspecified
        }
        labelMedium.copy(
            fontSize = scaled,
            lineHeight = if (line != TextUnit.Unspecified) line else labelMedium.lineHeight
        )
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
                                        .padding(start = 4.dp)
                                        .heightIn(min = 32.dp)
                                        .wrapContentHeight(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when (searchMode) {
                                        SearchMode.INVOICE_NUMBER -> {
                                            Text(
                                                text = "#",
                                                style = modeIndicatorSymbolStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = " ▾",
                                                style = labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        SearchMode.AMOUNT -> {
                                            Text(
                                                text = "₪",
                                                style = modeIndicatorSymbolStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = " ▾",
                                                style = labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = onFilterClick) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_filter_svgrepo),
                                        contentDescription = stringResource(R.string.filter),
                                        modifier = Modifier.size(SearchBarFilterIconSizeDp),
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
    onClearFilters: () -> Unit,
    onApply: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 16.dp
        )
    ) {
        item {
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        item {
            Text(
                text = stringResource(R.string.service_period),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        item { Spacer(modifier = Modifier.padding(top = 6.dp)) }

        item {
            DateFilterRow(
                label = stringResource(R.string.from_date),
                date = servicePeriodStart,
                onDateChange = onServicePeriodStartChange
            )
        }
        item {
            DateFilterRow(
                label = stringResource(R.string.to_date),
                date = servicePeriodEnd,
                onDateChange = onServicePeriodEndChange
            )
        }

        item { Spacer(modifier = Modifier.padding(top = 12.dp)) }

        item {
            Text(
                text = stringResource(R.string.status),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        item { Spacer(modifier = Modifier.padding(top = 6.dp)) }

        item {
            StatusFilterHorizontalRow(
                statusFilter = statusFilter,
                onStatusFilterChange = onStatusFilterChange
            )
        }

        item { Spacer(modifier = Modifier.padding(top = 16.dp)) }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        item { Spacer(modifier = Modifier.padding(top = 12.dp)) }

        item {
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.clear_filters))
            }
        }

        item { Spacer(modifier = Modifier.padding(top = 8.dp)) }

        item {
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.apply_filters))
            }
        }
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
private fun StatusFilterHorizontalRow(
    statusFilter: PaymentStatus?,
    onStatusFilterChange: (PaymentStatus?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = statusFilter == null,
            onClick = { onStatusFilterChange(null) },
            label = {
                Text(
                    text = stringResource(R.string.all),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = statusFilter == PaymentStatus.PAID,
            onClick = { onStatusFilterChange(PaymentStatus.PAID) },
            label = {
                Text(
                    text = stringResource(R.string.paid),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = statusFilter == PaymentStatus.NOT_PAID,
            onClick = { onStatusFilterChange(PaymentStatus.NOT_PAID) },
            label = {
                Text(
                    text = stringResource(R.string.not_paid),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            },
            modifier = Modifier.weight(1f)
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
            items(invoices, key = { invoice -> invoice.id }) { invoice ->
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



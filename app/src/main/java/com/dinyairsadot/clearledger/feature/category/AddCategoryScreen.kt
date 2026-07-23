@file:OptIn(ExperimentalFoundationApi::class)

package com.dinyairsadot.clearledger.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.dinyairsadot.clearledger.core.domain.Category
import com.dinyairsadot.clearledger.core.ui.SwipeDismissSnackbarHost
import com.dinyairsadot.clearledger.core.ui.UnsavedChangesDialog
import com.dinyairsadot.clearledger.core.ui.categoryTopAppBarColors
import com.dinyairsadot.clearledger.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    onNavigateBack: () -> Unit,
    onSaveCategory: (
        name: String,
        colorHex: String,
        description: String,
        customFieldTitles: List<String>
    ) -> Unit,
    existingNamesLower: Set<String>,
    onCategorySaved: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var colorHex by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    var customFieldTitles by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var pendingRemoveFieldIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    
    var showPendingNewFieldInput by rememberSaveable { mutableStateOf(false) }
    var newFieldName by rememberSaveable { mutableStateOf("") }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }
    var showUnsavedChangesDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val nameScrollAnchor = remember { BringIntoViewRequester() }
    val colorSectionScrollAnchor = remember { BringIntoViewRequester() }
    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }
    var fieldExistsError by remember { mutableStateOf<String?>(null) }

    fun removeCustomFieldAt(index: Int) {
        val newList = customFieldTitles.toMutableList()
        newList.removeAt(index)
        customFieldTitles = newList
    }
    
    fun onShowPendingNewFieldClick() {
        if (showPendingNewFieldInput) {
            val trimmed = newFieldName.trim()
            if (trimmed.isNotBlank()) {
                val committed = customFieldTitles.map { it.trim() }.filter { it.isNotBlank() }
                if (committed.any { it.equals(trimmed, ignoreCase = true) }) {
                    fieldExistsError = context.getString(R.string.field_already_exists)
                    return
                }
                if (committed.size >= Category.MAX_CUSTOM_FIELDS) {
                    showPendingNewFieldInput = false
                    newFieldName = ""
                    return
                }
                customFieldTitles = committed + trimmed
                newFieldName = ""
                fieldExistsError = null
            }
        }
        showPendingNewFieldInput = customFieldTitles.size < Category.MAX_CUSTOM_FIELDS
    }

    fun addFieldFromCatalog(fieldName: String) {
        val trimmed = fieldName.trim()
        if (trimmed.isBlank()) return
        val committed = customFieldTitles.map { it.trim() }.filter { it.isNotBlank() }
        if (committed.any { it.equals(trimmed, ignoreCase = true) }) {
            fieldExistsError = context.getString(R.string.field_already_exists)
            return
        }
        if (committed.size >= Category.MAX_CUSTOM_FIELDS) {
            return
        }
        customFieldTitles = committed + trimmed
        fieldExistsError = null
        if (customFieldTitles.size >= Category.MAX_CUSTOM_FIELDS) {
            showPendingNewFieldInput = false
            newFieldName = ""
        }
    }

    fun onSaveClicked() {
        var hasError = false

        if (name.isBlank()) {
            nameError = context.getString(R.string.name_required)
            hasError = true
        } else if (existingNamesLower.contains(name.trim().lowercase())) {
            nameError = context.getString(R.string.name_must_be_unique)
            hasError = true
        } else {
            nameError = null
        }

        if (colorHex.isNotBlank()) {
            val regex = Regex("^#[0-9A-Fa-f]{6}$")
            if (!regex.matches(colorHex.trim())) {
                colorError = context.getString(R.string.color_must_be_rrggbb_format)
                hasError = true
            } else {
                colorError = null
            }
        } else {
            colorError = null
        }

        if (hasError) {
            coroutineScope.launch {
                delay(50)
                if (nameError != null) {
                    nameScrollAnchor.bringIntoView()
                } else if (colorError != null) {
                    colorSectionScrollAnchor.bringIntoView()
                }
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.please_fix_highlighted_fields),
                    withDismissAction = true
                )
            }
            return
        }

        val trimmedTitles = resolveCustomFieldTitlesForSave(
            customFieldTitles = customFieldTitles,
            pendingNewFieldName = newFieldName,
            onDuplicatePendingField = {
                fieldExistsError = context.getString(R.string.field_already_exists)
                showPendingNewFieldInput = true
            }
        ) ?: return

        onSaveCategory(
            name.trim(),
            colorHex.trim(),
            description.trim(),
            trimmedTitles
        )

        onCategorySaved()
        onNavigateBack()
    }

    val originalSnapshot = remember {
        editableCategorySnapshot(
            name = "",
            colorHex = "",
            description = "",
            customFieldTitles = emptyList()
        )
    }
    val hasUnsavedChanges = editableCategorySnapshot(
        name = name,
        colorHex = colorHex,
        description = description,
        customFieldTitles = customFieldTitles,
        pendingNewFieldName = newFieldName
    ) != originalSnapshot

    fun onBackRequested() {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    BackHandler {
        onBackRequested()
    }

    val formState = CategoryFormState(
        name = name,
        nameError = nameError,
        colorHex = colorHex,
        colorError = colorError,
        description = description,
        customFieldTitles = customFieldTitles,
        showPendingNewFieldInput = showPendingNewFieldInput,
        newFieldName = newFieldName,
        selectedTopicId = selectedTopicId,
        fieldExistsError = fieldExistsError
    )

    val formCallbacks = CategoryFormCallbacks(
        onNameChange = { newName ->
            name = newName
            if (nameError != null) nameError = null
        },
        onColorHexChange = { newColor ->
            colorHex = newColor
            if (colorError != null) colorError = null
        },
        onDescriptionChange = { newDesc ->
            description = newDesc
        },
        onSaveClick = { onSaveClicked() },
        onCustomFieldTitleChange = { index, value ->
            val newList = customFieldTitles.toMutableList()
            if (index < newList.size) {
                newList[index] = value
            } else {
                newList.add(value)
            }
            customFieldTitles = newList
        },
        onShowPendingNewFieldClick = { onShowPendingNewFieldClick() },
        onRequestRemoveCustomField = { index ->
            pendingRemoveFieldIndex = index
        },
        onNewFieldNameChange = { newName ->
            newFieldName = newName
            if (fieldExistsError != null) fieldExistsError = null
        },
        onTopicSelected = { topicId ->
            selectedTopicId = topicId
        },
        onAddFieldFromCatalog = { fieldName ->
            addFieldFromCatalog(fieldName)
        }
    )

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        snackbarHost = { SwipeDismissSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_category_title)) },
                colors = categoryTopAppBarColors(colorHex.takeIf { it.isNotBlank() }),
                navigationIcon = {
                    IconButton(onClick = ::onBackRequested) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showUnsavedChangesDialog) {
            UnsavedChangesDialog(
                onSave = {
                    showUnsavedChangesDialog = false
                    onSaveClicked()
                },
                onDiscard = {
                    showUnsavedChangesDialog = false
                    onNavigateBack()
                },
                onDismiss = { showUnsavedChangesDialog = false }
            )
        }
        if (pendingRemoveFieldIndex != null) {
            val fieldIndex = pendingRemoveFieldIndex!!
            val fieldTitle = customFieldTitles.getOrNull(fieldIndex)?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.field_number, fieldIndex + 1)
            
            AlertDialog(
                onDismissRequest = { pendingRemoveFieldIndex = null },
                title = { Text(stringResource(R.string.remove_custom_field)) },
                text = {
                    Text(
                        context.getString(R.string.remove_custom_field_confirmation, fieldTitle)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            removeCustomFieldAt(fieldIndex)
                            pendingRemoveFieldIndex = null
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { pendingRemoveFieldIndex = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        CategoryForm(
            state = formState,
            callbacks = formCallbacks,
            saveButtonLabel = stringResource(R.string.add_category),
            nameScrollAnchor = nameScrollAnchor,
            colorSectionScrollAnchor = colorSectionScrollAnchor,
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
        )
    }
}

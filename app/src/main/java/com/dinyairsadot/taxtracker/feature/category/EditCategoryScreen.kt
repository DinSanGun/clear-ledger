package com.dinyairsadot.taxtracker.feature.category

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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dinyairsadot.taxtracker.feature.category.CategoryListViewModel
import com.dinyairsadot.taxtracker.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    categoryId: Long,
    initialName: String,
    initialColorHex: String,
    categoryColorHex: String?,
    initialDescription: String?,
    initialCustomFieldTitles: List<String>,
    otherNamesLower: Set<String>,
    onNavigateBack: () -> Unit,
    onSaveCategory: (
        name: String,
        colorHex: String,
        description: String,
        customFieldTitles: List<String>
    ) -> Unit,
    viewModel: CategoryListViewModel
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var colorHex by rememberSaveable { mutableStateOf(initialColorHex) }
    var description by rememberSaveable { mutableStateOf(initialDescription.orEmpty()) }

    var customFieldTitles by rememberSaveable { mutableStateOf(initialCustomFieldTitles) }
    var pendingRemoveFieldIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var hasFieldData by rememberSaveable { mutableStateOf(false) }
    
    // New field input state
    var newFieldName by rememberSaveable { mutableStateOf("") }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }
    var fieldExistsError by remember { mutableStateOf<String?>(null) }

    // Check if invoices have data in the field when removal is requested
    LaunchedEffect(pendingRemoveFieldIndex) {
        if (pendingRemoveFieldIndex != null) {
            hasFieldData = viewModel.hasInvoicesWithFieldData(categoryId, pendingRemoveFieldIndex!!)
        }
    }

    fun removeCustomFieldAt(index: Int) {
        val newList = customFieldTitles.toMutableList()
        newList.removeAt(index)
        customFieldTitles = newList
    }
    
    fun addFieldFromInput() {
        val trimmed = newFieldName.trim()
        if (trimmed.isBlank()) return
        
        // Check for duplicates (case-insensitive)
        if (customFieldTitles.any { it.trim().equals(trimmed, ignoreCase = true) }) {
            fieldExistsError = context.getString(R.string.field_already_exists)
            return
        }
        
        customFieldTitles = customFieldTitles + trimmed
        newFieldName = ""
        fieldExistsError = null
    }
    
    fun addFieldFromCatalog(fieldName: String) {
        // Check for duplicates (case-insensitive)
        if (customFieldTitles.any { it.trim().equals(fieldName.trim(), ignoreCase = true) }) {
            fieldExistsError = context.getString(R.string.field_already_exists)
            return
        }
        
        customFieldTitles = customFieldTitles + fieldName
        fieldExistsError = null
    }

    fun onSaveClicked() {
        var hasError = false

        if (name.isBlank()) {
            nameError = context.getString(R.string.name_required)
            hasError = true
        } else if (otherNamesLower.contains(name.trim().lowercase())) {
            nameError = context.getString(R.string.name_must_be_unique)
            hasError = true
        }

        if (colorHex.isNotBlank()) {
            val regex = Regex("^#[0-9A-Fa-f]{6}$")
            if (!regex.matches(colorHex.trim())) {
                colorError = context.getString(R.string.color_must_be_rrggbb_format)
                hasError = true
            }
        }

        if (!hasError) {
            val trimmedTitles = customFieldTitles.map { it.trim() }.filter { it.isNotBlank() }
            onSaveCategory(
                name.trim(),
                colorHex.trim(),
                description.trim(),
                trimmedTitles
            )
            onNavigateBack()
        }
    }

    val formState = CategoryFormState(
        name = name,
        nameError = nameError,
        colorHex = colorHex,
        colorError = colorError,
        description = description,
        customFieldTitles = customFieldTitles,
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
        onAddCustomFieldClick = {
            customFieldTitles = customFieldTitles + ""
        },
        onRequestRemoveCustomField = { index ->
            pendingRemoveFieldIndex = index
        },
        onNewFieldNameChange = { newName ->
            newFieldName = newName
            if (fieldExistsError != null) fieldExistsError = null
        },
        onAddNewFieldFromInput = {
            addFieldFromInput()
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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_category_title)) },
                colors = categoryTopAppBarColors(colorHex.takeIf { it.isNotBlank() }),
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
    ) { innerPadding ->
        if (pendingRemoveFieldIndex != null) {
            val fieldIndex = pendingRemoveFieldIndex!!
            val fieldTitle = customFieldTitles.getOrNull(fieldIndex)?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.field_number, fieldIndex + 1)
            
            AlertDialog(
                onDismissRequest = { pendingRemoveFieldIndex = null },
                title = { Text(stringResource(R.string.remove_custom_field)) },
                text = {
                    Text(context.getString(R.string.remove_custom_field_confirmation, fieldTitle))
                },
                confirmButton = {
                    TextButton(onClick = {
                        removeCustomFieldAt(fieldIndex)
                        pendingRemoveFieldIndex = null
                    }) {
                        Text(stringResource(R.string.remove))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRemoveFieldIndex = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        CategoryForm(
            state = formState,
            callbacks = formCallbacks,
            saveButtonLabel = stringResource(R.string.save_changes),
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
        )
    }
}
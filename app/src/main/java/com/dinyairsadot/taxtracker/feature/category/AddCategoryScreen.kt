package com.dinyairsadot.taxtracker.feature.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton


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

    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }

    fun removeCustomFieldAt(index: Int) {
        val newList = customFieldTitles.toMutableList()
        newList.removeAt(index)
        customFieldTitles = newList
    }

    fun onSaveClicked() {
        var hasError = false

        if (name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        } else if (existingNamesLower.contains(name.trim().lowercase())) {
            nameError = "Name must be unique"
            hasError = true
        }

        if (colorHex.isNotBlank()) {
            val regex = Regex("^#[0-9A-Fa-f]{6}$")
            if (!regex.matches(colorHex.trim())) {
                colorError = "Color must be in #RRGGBB format"
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

            onCategorySaved()
            onNavigateBack()
        }
    }

    val formState = CategoryFormState(
        name = name,
        nameError = nameError,
        colorHex = colorHex,
        colorError = colorError,
        description = description,
        customFieldTitles = customFieldTitles
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
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add category") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pendingRemoveFieldIndex != null) {
            val fieldIndex = pendingRemoveFieldIndex!!
            val fieldTitle = customFieldTitles.getOrNull(fieldIndex)?.takeIf { it.isNotBlank() }
                ?: "Field ${fieldIndex + 1}"
            
            AlertDialog(
                onDismissRequest = { pendingRemoveFieldIndex = null },
                title = { Text("Remove custom field?") },
                text = {
                    Text(
                        "Removing \"$fieldTitle\" will delete any information stored in invoices for this field. " +
                                "Are you sure you want to remove it?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        removeCustomFieldAt(fieldIndex)
                        pendingRemoveFieldIndex = null
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRemoveFieldIndex = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        CategoryForm(
            state = formState,
            callbacks = formCallbacks,
            saveButtonLabel = "Add category",
            modifier = Modifier.padding(innerPadding)   // 👈 important
        )
    }
}

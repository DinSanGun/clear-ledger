package com.dinyairsadot.taxtracker.feature.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.dinyairsadot.taxtracker.core.ui.categoryTopAppBarColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    initialName: String,
    initialColorHex: String,
    categoryColorHex: String?,
    initialDescription: String?,
    initialCustomFieldTitle1: String?,
    initialCustomFieldTitle2: String?,
    initialCustomFieldTitle3: String?,
    otherNamesLower: Set<String>,
    onNavigateBack: () -> Unit,
    onSaveCategory: (
        name: String,
        colorHex: String,
        description: String,
        customFieldTitle1: String?,
        customFieldTitle2: String?,
        customFieldTitle3: String?
    ) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var colorHex by rememberSaveable { mutableStateOf(initialColorHex) }
    var description by rememberSaveable { mutableStateOf(initialDescription.orEmpty()) }

    var customFieldTitle1 by rememberSaveable { mutableStateOf(initialCustomFieldTitle1.orEmpty()) }
    var customFieldTitle2 by rememberSaveable { mutableStateOf(initialCustomFieldTitle2.orEmpty()) }
    var customFieldTitle3 by rememberSaveable { mutableStateOf(initialCustomFieldTitle3.orEmpty()) }

    var visibleCustomFieldCount by rememberSaveable {
        mutableStateOf(
            listOf(customFieldTitle1, customFieldTitle2, customFieldTitle3)
                .count { it.isNotBlank() }
        )
    }

    var pendingRemoveFieldIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var colorError by remember { mutableStateOf<String?>(null) }

    fun removeCustomFieldAt(index: Int) {
        when (index) {
            1 -> {
                customFieldTitle1 = customFieldTitle2
                customFieldTitle2 = customFieldTitle3
                customFieldTitle3 = ""
            }
            2 -> {
                customFieldTitle2 = customFieldTitle3
                customFieldTitle3 = ""
            }
            3 -> {
                customFieldTitle3 = ""
            }
        }
        visibleCustomFieldCount = (visibleCustomFieldCount - 1).coerceAtLeast(0)
    }

    fun onSaveClicked() {
        var hasError = false

        if (name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        } else if (otherNamesLower.contains(name.trim().lowercase())) {
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
            onSaveCategory(
                name.trim(),
                colorHex.trim(),
                description.trim(),
                customFieldTitle1.trim().ifBlank { null },
                customFieldTitle2.trim().ifBlank { null },
                customFieldTitle3.trim().ifBlank { null }
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
        visibleCustomFieldCount = visibleCustomFieldCount,
        customFieldTitle1 = customFieldTitle1,
        customFieldTitle2 = customFieldTitle2,
        customFieldTitle3 = customFieldTitle3
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
        onCustomFieldTitle1Change = { customFieldTitle1 = it },
        onCustomFieldTitle2Change = { customFieldTitle2 = it },
        onCustomFieldTitle3Change = { customFieldTitle3 = it },
        onAddCustomFieldClick = {
            visibleCustomFieldCount = (visibleCustomFieldCount + 1).coerceAtMost(3)
        },
        onRequestRemoveCustomField = { index ->
            pendingRemoveFieldIndex = index
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit category") },
                colors = categoryTopAppBarColors(categoryColorHex),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (pendingRemoveFieldIndex != null) {
            AlertDialog(
                onDismissRequest = { pendingRemoveFieldIndex = null },
                title = { Text("Remove custom field?") },
                text = {
                    Text(
                        "Removing a custom field may delete information stored in invoices for this category. " +
                                "Are you sure you want to remove it?"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        removeCustomFieldAt(pendingRemoveFieldIndex!!)
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
            saveButtonLabel = "Save changes",
            modifier = Modifier.padding(innerPadding)
        )
    }
}
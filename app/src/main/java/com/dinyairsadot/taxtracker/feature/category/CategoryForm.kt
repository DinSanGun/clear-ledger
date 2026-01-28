package com.dinyairsadot.taxtracker.feature.category

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinyairsadot.taxtracker.core.domain.Category

data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val colorHex: String = "",
    val colorError: String? = null,
    val description: String = "",
    val customFieldTitles: List<String> = emptyList() // List of custom field titles (up to 10)
)

data class CategoryFormCallbacks(
    val onNameChange: (String) -> Unit,
    val onColorHexChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSaveClick: () -> Unit,
    val onCustomFieldTitleChange: (index: Int, value: String) -> Unit,
    val onAddCustomFieldClick: () -> Unit,
    val onRequestRemoveCustomField: (index: Int) -> Unit
)

@Composable
fun CategoryForm(
    state: CategoryFormState,
    callbacks: CategoryFormCallbacks,
    saveButtonLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name
        OutlinedTextField(
            value = state.name,
            onValueChange = callbacks.onNameChange,
            label = { Text("Name") },
            isError = state.nameError != null,
            supportingText = if (state.nameError != null) {
                { Text(state.nameError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Color + preview (center vertically so the circle isn't "floating")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.colorHex,
                onValueChange = callbacks.onColorHexChange,
                label = { Text("Color hex (#RRGGBB, optional)") },
                placeholder = { Text("#FF9800") },
                isError = state.colorError != null,
                supportingText = if (state.colorError != null) {
                    { Text(state.colorError) }
                } else {
                    null
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            CategoryColorPreview(
                colorHex = state.colorHex,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text(text = "Quick color presets")

        CategoryColorOptionsRow(
            selectedColorHex = state.colorHex,
            onColorSelected = callbacks.onColorHexChange
        )

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = callbacks.onDescriptionChange,
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Custom fields (titles only; up to 10)
        Text(text = "Custom fields (up to ${Category.MAX_CUSTOM_FIELDS})")

        if (state.customFieldTitles.size < Category.MAX_CUSTOM_FIELDS) {
            TextButton(onClick = callbacks.onAddCustomFieldClick) {
                Text("Add custom field")
            }
        }

        // Render all custom fields dynamically
        state.customFieldTitles.forEachIndexed { index, title ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { callbacks.onCustomFieldTitleChange(index, it) },
                    label = { Text("Field ${index + 1} title (optional)") },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { callbacks.onRequestRemoveCustomField(index) }) {
                    Text("Remove")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = callbacks.onSaveClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(saveButtonLabel)
        }
    }
}


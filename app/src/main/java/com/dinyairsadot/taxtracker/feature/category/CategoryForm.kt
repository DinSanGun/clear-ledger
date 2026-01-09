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

data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val colorHex: String = "",
    val colorError: String? = null,
    val description: String = "",
    val visibleCustomFieldCount: Int = 0,
    val customFieldTitle1: String = "",
    val customFieldTitle2: String = "",
    val customFieldTitle3: String = ""
)

data class CategoryFormCallbacks(
    val onNameChange: (String) -> Unit,
    val onColorHexChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSaveClick: () -> Unit,
    val onCustomFieldTitle1Change: (String) -> Unit,
    val onCustomFieldTitle2Change: (String) -> Unit,
    val onCustomFieldTitle3Change: (String) -> Unit,
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

        // Custom fields (titles only; up to 3)
        Text(text = "Custom fields (up to 3)")

        if (state.visibleCustomFieldCount < 3) {
            TextButton(onClick = callbacks.onAddCustomFieldClick) {
                Text("Add custom field")
            }
        }

        if (state.visibleCustomFieldCount >= 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.customFieldTitle1,
                    onValueChange = callbacks.onCustomFieldTitle1Change,
                    label = { Text("Field 1 title (optional)") },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { callbacks.onRequestRemoveCustomField(1) }) {
                    Text("Remove")
                }
            }
        }

        if (state.visibleCustomFieldCount >= 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.customFieldTitle2,
                    onValueChange = callbacks.onCustomFieldTitle2Change,
                    label = { Text("Field 2 title (optional)") },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { callbacks.onRequestRemoveCustomField(2) }) {
                    Text("Remove")
                }
            }
        }

        if (state.visibleCustomFieldCount >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.customFieldTitle3,
                    onValueChange = callbacks.onCustomFieldTitle3Change,
                    label = { Text("Field 3 title (optional)") },
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { callbacks.onRequestRemoveCustomField(3) }) {
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


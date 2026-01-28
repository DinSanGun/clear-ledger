package com.dinyairsadot.taxtracker.feature.category

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
            .verticalScroll(rememberScrollState())
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

        // Color selection
        Text(text = "Theme color")

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
                    label = { Text("Field ${index + 1} title") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { callbacks.onRequestRemoveCustomField(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove field"
                    )
                }
            }
        }

        // Add custom field button (below existing fields)
        if (state.customFieldTitles.size < Category.MAX_CUSTOM_FIELDS) {
            OutlinedButton(
                onClick = callbacks.onAddCustomFieldClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Add custom field")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = callbacks.onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50) // Green color
            )
        ) {
            Text(saveButtonLabel)
        }
    }
}


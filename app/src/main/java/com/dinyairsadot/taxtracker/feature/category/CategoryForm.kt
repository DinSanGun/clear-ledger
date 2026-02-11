package com.dinyairsadot.taxtracker.feature.category

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.clickable
import com.dinyairsadot.taxtracker.core.domain.Category
import com.dinyairsadot.taxtracker.R

data class CategoryFormState(
    val name: String = "",
    val nameError: String? = null,
    val colorHex: String = "",
    val colorError: String? = null,
    val description: String = "",
    val customFieldTitles: List<String> = emptyList(), // List of custom field titles (up to 10)
    val pinnedSupplierName: String = "", // Pinned default supplier name for this category
    val newFieldName: String = "", // New field name input
    val selectedTopicId: String? = null, // Selected topic for catalog browsing
    val fieldExistsError: String? = null // Error when trying to add duplicate field
)

data class CategoryFormCallbacks(
    val onNameChange: (String) -> Unit,
    val onColorHexChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSaveClick: () -> Unit,
    val onCustomFieldTitleChange: (index: Int, value: String) -> Unit,
    val onAddCustomFieldClick: () -> Unit,
    val onRequestRemoveCustomField: (index: Int) -> Unit,
    val onPinnedSupplierNameChange: (String) -> Unit,
    val onNewFieldNameChange: (String) -> Unit,
    val onAddNewFieldFromInput: () -> Unit,
    val onTopicSelected: (String?) -> Unit,
    val onAddFieldFromCatalog: (String) -> Unit
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
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name
        OutlinedTextField(
            value = state.name,
            onValueChange = callbacks.onNameChange,
            label = { Text(stringResource(R.string.name)) },
            isError = state.nameError != null,
            supportingText = if (state.nameError != null) {
                { Text(state.nameError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Color selection
        Text(text = stringResource(R.string.theme_color))

        CategoryColorOptionsRow(
            selectedColorHex = state.colorHex,
            onColorSelected = callbacks.onColorHexChange
        )

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = callbacks.onDescriptionChange,
            label = { Text(stringResource(R.string.description_optional)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Pinned default supplier name
        OutlinedTextField(
            value = state.pinnedSupplierName,
            onValueChange = callbacks.onPinnedSupplierNameChange,
            label = { Text(stringResource(R.string.pinned_supplier_name)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Custom fields section
        if (state.customFieldTitles.isNotEmpty()) {
            Text(
                text = stringResource(R.string.add_custom_field),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
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
                    label = { Text(stringResource(R.string.field_title, index + 1)) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { callbacks.onRequestRemoveCustomField(index) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_field)
                    )
                }
            }
        }

        // Add new field UI (if space available)
        if (state.customFieldTitles.size < Category.MAX_CUSTOM_FIELDS) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 1) FIRST: Text input + Add button (easiest path)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.newFieldName,
                    onValueChange = callbacks.onNewFieldNameChange,
                    label = { Text(stringResource(R.string.new_field_name_hint)) },
                    modifier = Modifier.weight(1f),
                    isError = state.fieldExistsError != null,
                    supportingText = state.fieldExistsError?.let { { Text(it) } }
                )
                IconButton(
                    onClick = callbacks.onAddNewFieldFromInput,
                    enabled = state.newFieldName.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add)
                    )
                }
            }
            
            // 2) THEN: Catalog selection
            FieldCatalogPicker(
                selectedTopicId = state.selectedTopicId,
                onTopicSelected = callbacks.onTopicSelected,
                onFieldSelected = callbacks.onAddFieldFromCatalog,
                modifier = Modifier.fillMaxWidth()
            )
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

@Composable
fun FieldCatalogPicker(
    selectedTopicId: String?,
    onTopicSelected: (String?) -> Unit,
    onFieldSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // "Or choose from list" text
        Text(
            text = stringResource(R.string.or_choose_from_list),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Topic selector dropdown
        var topicExpanded by remember { mutableStateOf(false) }
        val topics = remember { FieldCatalog.getTopics() }
        
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedTopicId?.let { id ->
                    topics.firstOrNull { it.id == id }?.let { stringResource(it.nameResId) }
                } ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { topicExpanded = true },
                label = { Text(stringResource(R.string.select_topic)) },
                trailingIcon = {
                    IconButton(onClick = { topicExpanded = !topicExpanded }) {
                        Icon(
                            imageVector = if (topicExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = topicExpanded,
                onDismissRequest = { topicExpanded = false }
            ) {
                // Clear selection option
                DropdownMenuItem(
                    text = { Text("-") },
                    onClick = {
                        onTopicSelected(null)
                        topicExpanded = false
                    }
                )
                topics.forEach { topic ->
                    DropdownMenuItem(
                        text = { Text(stringResource(topic.nameResId)) },
                        onClick = {
                            onTopicSelected(topic.id)
                            topicExpanded = false
                        }
                    )
                }
            }
        }
        
        // Show fields for selected topic
        selectedTopicId?.let { topicId ->
            val selectedTopic = topics.firstOrNull { it.id == topicId }
            selectedTopic?.fields?.forEach { fieldName ->
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { onFieldSelected(fieldName) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fieldName)
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}


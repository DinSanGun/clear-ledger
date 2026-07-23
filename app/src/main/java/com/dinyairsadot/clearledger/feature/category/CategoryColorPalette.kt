package com.dinyairsadot.clearledger.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.dinyairsadot.clearledger.R


private data class PresetCategoryColor(
    val hex: String,
    val color: Color
)

// Shared preset colors for categories (balanced middle shades from the extended palette).
// Hex values are valid #RRGGBB and work with existing validation.
// Spectrum order: red → orange → yellow → green → cyan → blue → purple.
private val presetCategoryColors = listOf(
    PresetCategoryColor("#EF5350", Color(0xFFEF5350)), // red
    PresetCategoryColor("#FFA726", Color(0xFFFFA726)), // orange
    PresetCategoryColor("#FFEE58", Color(0xFFFFEE58)), // yellow
    PresetCategoryColor("#66BB6A", Color(0xFF66BB6A)), // green
    PresetCategoryColor("#26C6DA", Color(0xFF26C6DA)), // cyan
    PresetCategoryColor("#42A5F5", Color(0xFF42A5F5)), // blue
    PresetCategoryColor("#AB47BC", Color(0xFFAB47BC))  // purple
)

// 7x7 predefined extended palette (49 colors)
private val extendedCategoryColorHexes = listOf(
    // Row 1 - Reds/Pinks
    "#FFCDD2", "#EF9A9A", "#E57373", "#EF5350", "#F44336", "#E53935", "#D32F2F",
    // Row 2 - Oranges/Deep Orange
    "#FFE0B2", "#FFCC80", "#FFB74D", "#FFA726", "#FF9800", "#FB8C00", "#F57C00",
    // Row 3 - Yellows/Amber
    "#FFF9C4", "#FFF59D", "#FFF176", "#FFEE58", "#FFEB3B", "#FDD835", "#FBC02D",
    // Row 4 - Greens
    "#C8E6C9", "#A5D6A7", "#81C784", "#66BB6A", "#4CAF50", "#43A047", "#388E3C",
    // Row 5 - Cyans/Teals
    "#B2EBF2", "#80DEEA", "#4DD0E1", "#26C6DA", "#00BCD4", "#00ACC1", "#0097A7",
    // Row 6 - Blues
    "#BBDEFB", "#90CAF9", "#64B5F6", "#42A5F5", "#2196F3", "#1E88E5", "#1976D2",
    // Row 7 - Purples
    "#E1BEE7", "#CE93D8", "#BA68C8", "#AB47BC", "#9C27B0", "#8E24AA", "#7B1FA2"
)

private fun selectionIndicatorColor(backgroundColor: Color): Color {
    return if (backgroundColor.luminance() > 0.5f) {
        Color(0xFF2B2B2B)
    } else {
        Color.White
    }
}

@Composable
private fun CategoryColorCircle(
    color: Color,
    selected: Boolean,
    checkIconSize: Dp,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val indicatorColor = selectionIndicatorColor(color)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) indicatorColor else outlineColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            selected -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = indicatorColor,
                    modifier = Modifier.size(checkIconSize)
                )
            }
            content != null -> content()
        }
    }
}

@Composable
fun CategoryColorOptionsRow(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreColors by remember { mutableStateOf(false) }

    // Check if selected color is from extended palette (not in preset colors)
    val isExtendedPaletteSelected = selectedColorHex.isNotBlank() &&
        !presetCategoryColors.any { it.hex.equals(selectedColorHex, ignoreCase = true) } &&
        extendedCategoryColorHexes.any { it.equals(selectedColorHex, ignoreCase = true) }
    val selectedExtendedColor = if (isExtendedPaletteSelected) {
        parseCategoryColorOrNull(selectedColorHex)
    } else {
        null
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presetCategoryColors.forEach { preset ->
            val isSelected = selectedColorHex.equals(preset.hex, ignoreCase = true)

            CategoryColorCircle(
                color = preset.color,
                selected = isSelected,
                checkIconSize = 18.dp,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clickable { onColorSelected(preset.hex) }
            )
        }
        // Extra circle to open extended palette
        CategoryColorCircle(
            color = selectedExtendedColor ?: MaterialTheme.colorScheme.surfaceVariant,
            selected = isExtendedPaletteSelected,
            checkIconSize = 18.dp,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clickable { showMoreColors = true },
            content = {
                Text(stringResource(R.string.plus))
            }
        )
    }
    if (showMoreColors) {
        AlertDialog(
            onDismissRequest = { showMoreColors = false },
            title = { Text(stringResource(R.string.choose_a_color)) },
            text = {
                Column {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        items(extendedCategoryColorHexes) { hex ->
                            val color = parseCategoryColorOrNull(hex) ?: return@items
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                            CategoryColorCircle(
                                color = color,
                                selected = isSelected,
                                checkIconSize = 16.dp,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        onColorSelected(hex)
                                        showMoreColors = false
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoreColors = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}

private fun parseCategoryColorOrNull(hex: String): androidx.compose.ui.graphics.Color? {
    val trimmed = hex.trim()
    val regex = Regex("^#[0-9A-Fa-f]{6}$")
    if (!regex.matches(trimmed)) return null

    return try {
        val intColor = AndroidColor.parseColor(trimmed)
        androidx.compose.ui.graphics.Color(intColor)
    } catch (e: IllegalArgumentException) {
        null
    }
}

@Composable
fun CategoryColorPreview(
    colorHex: String,
    modifier: Modifier = Modifier
) {
    val parsedColor = parseCategoryColorOrNull(colorHex)
    val fillColor = parsedColor ?: MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(fillColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
    )
}

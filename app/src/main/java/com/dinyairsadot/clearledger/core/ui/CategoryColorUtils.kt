package com.dinyairsadot.clearledger.core.ui

import androidx.compose.ui.graphics.Color
import android.graphics.Color.parseColor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance


const val DEFAULT_CATEGORY_COLOR_HEX = "#424242" // dark grey

fun parseCategoryColorOrDefault(hex: String?): Color {
    return try {
        Color(parseColor(hex ?: DEFAULT_CATEGORY_COLOR_HEX))
    } catch (e: IllegalArgumentException) {
        Color(parseColor(DEFAULT_CATEGORY_COLOR_HEX))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun categoryTopAppBarColors(categoryColorHex: String?): TopAppBarColors {
    val container = parseCategoryColorOrDefault(categoryColorHex)
    val content = if (container.luminance() < 0.5f) Color.White else Color.Black

    return TopAppBarDefaults.topAppBarColors(
        containerColor = container,
        titleContentColor = content,
        navigationIconContentColor = content,
        actionIconContentColor = content
    )
}

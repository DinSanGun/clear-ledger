package com.dinyairsadot.taxtracker.core.ui

import androidx.compose.foundation.relocation.BringIntoViewRequester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DROPDOWN_OPEN_DELAY_MS = 80L

/**
 * Opens dropdowns more reliably in scrollable containers by first bringing
 * the anchor field into view. This reduces cases where the popup overlaps the anchor.
 */
fun requestAnchoredDropdownExpansion(
    shouldExpand: Boolean,
    scope: CoroutineScope,
    bringIntoViewRequester: BringIntoViewRequester,
    onExpandedChange: (Boolean) -> Unit
) {
    if (!shouldExpand) {
        onExpandedChange(false)
        return
    }

    scope.launch {
        bringIntoViewRequester.bringIntoView()
        // Let layout settle after scroll before opening popup.
        delay(DROPDOWN_OPEN_DELAY_MS)
        onExpandedChange(true)
    }
}

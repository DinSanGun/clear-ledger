package com.dinyairsadot.clearledger.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

/**
 * [SnackbarHost] whose snackbars can be dismissed with a horizontal swipe (both directions),
 * in addition to the usual timeout and action dismiss behavior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDismissSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { Snackbar(snackbarData = it) },
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            key(data) {
                @Suppress("DEPRECATION")
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { target ->
                        when (target) {
                            SwipeToDismissBoxValue.StartToEnd,
                            SwipeToDismissBoxValue.EndToStart -> {
                                data.dismiss()
                                true
                            }
                            SwipeToDismissBoxValue.Settled -> false
                            else -> false
                        }
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth(),
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = { },
                    content = { snackbar(data) }
                )
            }
        }
    )
}

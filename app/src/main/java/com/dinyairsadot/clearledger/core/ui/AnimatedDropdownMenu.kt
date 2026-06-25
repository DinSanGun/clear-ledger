package com.dinyairsadot.clearledger.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

const val DROPDOWN_MENU_ANIM_MS = 280

private val menuFadeTween = tween<Float>(DROPDOWN_MENU_ANIM_MS, easing = FastOutSlowInEasing)
private val menuSlideTween = tween<IntOffset>(DROPDOWN_MENU_ANIM_MS, easing = FastOutSlowInEasing)

@Stable
class AnimatedDropdownMenuState internal constructor() {
    internal var showMenu by mutableStateOf(false)
    internal val visibility = MutableTransitionState(false)

    val isExpanded: Boolean
        get() = showMenu

    fun open() {
        showMenu = true
        visibility.targetState = true
    }

    fun dismiss() {
        visibility.targetState = false
    }
}

@Composable
fun rememberAnimatedDropdownMenuState(): AnimatedDropdownMenuState {
    val state = remember { AnimatedDropdownMenuState() }
    LaunchedEffect(state.visibility.isIdle, state.visibility.currentState, state.showMenu) {
        if (state.showMenu && state.visibility.isIdle && !state.visibility.currentState) {
            state.showMenu = false
        }
    }
    return state
}

/**
 * [DropdownMenu] with a short fade + slide animation, matching the sort-menu polish
 * already used on the invoice list. Keeps the popup mounted until the exit animation finishes.
 */
@Composable
fun AnimatedDropdownMenu(
    state: AnimatedDropdownMenuState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = state.showMenu,
        onDismissRequest = {
            state.dismiss()
            onDismissRequest()
        },
        modifier = modifier,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        AnimatedVisibility(
            visibleState = state.visibility,
            enter = slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = menuSlideTween
            ) + fadeIn(animationSpec = menuFadeTween),
            exit = slideOutVertically(
                targetOffsetY = { -it / 4 },
                animationSpec = menuSlideTween
            ) + fadeOut(animationSpec = menuFadeTween)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 2.dp,
                tonalElevation = 2.dp
            ) {
                Column(content = content)
            }
        }
    }
}

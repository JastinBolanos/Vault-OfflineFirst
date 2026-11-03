package com.jastin.boveda.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/* =========================================================================
 * NAVIGATION INTERCEPTOR (NATIVE ANDROID)
 * Wraps AndroidX BackHandler for composable navigation control.
 * ========================================================================= */
@Composable
fun BackPressHandler(enabled: Boolean = true, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

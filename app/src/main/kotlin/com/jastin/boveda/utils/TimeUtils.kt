package com.jastin.boveda.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* =========================================================================
 * TIME PROVIDER (NATIVE ANDROID / JVM)
 * Implements native timestamp and date formatting for Android.
 * ========================================================================= */
fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

fun getCurrentTimeFormatted(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.US)
    return formatter.format(Date())
}

fun getCurrentDateFormatted(): String {
    return "Today"
}

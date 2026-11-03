package com.jastin.boveda

import android.os.Build

/* =========================================================================
 * PLATFORM IDENTIFIER (NATIVE ANDROID)
 * Exposes OS information to the application layers.
 * ========================================================================= */
interface Platform {
    val name: String
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

fun getPlatform(): Platform = AndroidPlatform()

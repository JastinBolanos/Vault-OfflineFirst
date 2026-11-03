package com.jastin.boveda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jastin.boveda.database.DatabaseDriverFactory
import com.jastin.boveda.utils.AndroidPlatformContext

/* =========================================================================
 * NATIVE APPLICATION HOST (ANDROID)
 * Base entry point for the shared interface system (Compose MP):
 * * System Boundary: Architectural limit where direct interaction
 * with the OS resides.
 * * Dependency Injection: Captures and provides the [Context] to the agnostic
 * domain, facilitating the initialization of persistence (SQL).
 * ========================================================================= */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidPlatformContext.applicationContext = this.applicationContext

        setContent {
            // ✅ MEMORY SHIELD: We pass applicationContext instead of 'this'
            // This ensures that SQLDelight doesn't retain the Activity if the user rotates the screen.
            App(driverFactory = DatabaseDriverFactory(this.applicationContext))
        }
    }
}

/* =========================================================================
 * ARCHITECTURAL NOTE: PREVIEW LIMITATIONS
 * =========================================================================
 * The root preview is disabled: the Compose engine operates
 * in a static environment (Mock) without access to Context or the file system.
 * * * Implication: Attempting to instantiate the full hierarchy triggers
 * SQLite access errors.
 * * Strategy: Visual tests should be limited to presentational
 * components (Dumb) isolated from persistence dependencies.
 */
@Preview
@Composable
fun AppAndroidPreview() {
}
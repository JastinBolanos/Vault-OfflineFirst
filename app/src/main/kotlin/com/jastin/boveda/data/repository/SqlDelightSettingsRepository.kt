package com.jastin.boveda.data.repository

import com.jastin.boveda.database.BovedaDatabase
import com.jastin.boveda.domain.repository.SettingsRepository

/* =========================================================================
 * SETTINGS REPOSITORY (SQLDELIGHT)
 * Manages the persistence of global configurations:
 * * Synchronous Writing: Ensures immediate saving via transactions.
 * * Fault Tolerance: Catches I/O errors to guarantee
 * UI startup continuity without losing traceability in logs.
 * ========================================================================= */
class SqlDelightSettingsRepository(
    private val database: BovedaDatabase
) : SettingsRepository {

    private val queries = database.appConfigQueries

    init {
        try {
            val savedTheme = queries.getThemePreference().executeAsOneOrNull()
            if (savedTheme != null) {
                com.jastin.boveda.globalIsDarkMode.value = savedTheme == 1L
            }
        } catch (e: Exception) {
            // We avoid the "Swallowed Exception".
            // We don't stop the app, but leave a log for the developer.
            println("⚙️ Database Warning: Could not load the theme -> ${e.message}")
        }
    }

    override fun updateThemePreference(isDark: Boolean) {
        com.jastin.boveda.globalIsDarkMode.value = isDark
        try {
            database.transaction {
                queries.saveThemePreference(if (isDark) 1L else 0L)
            }
        } catch (e: Exception) {
            println("⚙️ Database Warning: Could not save the theme -> ${e.message}")
        }
    }
}
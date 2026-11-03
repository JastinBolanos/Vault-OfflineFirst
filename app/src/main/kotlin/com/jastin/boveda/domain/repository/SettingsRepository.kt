package com.jastin.boveda.domain.repository

/* =========================================================================
 * SETTINGS REPOSITORY (REPOSITORY PATTERN)
 * Defines the contract for persisting global configurations,
 * decoupling the domain layer from the storage infrastructure.
 * ========================================================================= */
interface SettingsRepository {
    fun updateThemePreference(isDark: Boolean)
}
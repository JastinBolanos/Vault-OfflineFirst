package com.jastin.boveda.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/* =========================================================================
 * DATABASE DRIVER FACTORY (NATIVE ANDROID)
 * Implements the initialization of the SQLite engine using AndroidSqliteDriver.
 * ========================================================================= */
class DatabaseDriverFactory(private val context: Context) {
    fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = BovedaDatabase.Schema,
            context = context,
            name = "boveda_local.db"
        )
    }
}

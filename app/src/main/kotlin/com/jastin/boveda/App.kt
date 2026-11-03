package com.jastin.boveda

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.Navigator
import com.jastin.boveda.data.repository.SqlDelightSettingsRepository
import com.jastin.boveda.database.BovedaDatabase
import com.jastin.boveda.database.DatabaseDriverFactory
import com.jastin.boveda.data.repository.SqlDelightTransactionRepository
import com.jastin.boveda.domain.repository.SettingsRepository
import com.jastin.boveda.domain.repository.TransactionRepository
import com.jastin.boveda.presentation.screens.welcome.WelcomeScreen
import com.jastin.boveda.presentation.theme.BovedaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

/* =========================================================================
 * COMPOSITION ROOT
 * Initializes dependencies (manual DI), global theme, and navigation (Voyager).
 * ========================================================================= */

// --- 1. SERVICE LOCATOR ---
lateinit var globalTransactionRepository: TransactionRepository
lateinit var globalSettingsRepository: SettingsRepository

val globalIsDarkMode = MutableStateFlow(false)

@Composable
fun App(driverFactory: DatabaseDriverFactory) {

    // --- 2. SECURED DATABASE BOOTSTRAP ---
    remember {
        if (!::globalTransactionRepository.isInitialized) {
            val driver = driverFactory.createDriver()
            val database = BovedaDatabase(driver)
            val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

            globalTransactionRepository = SqlDelightTransactionRepository(
                database = database,
                scope = appScope
            )

            globalSettingsRepository = SqlDelightSettingsRepository(
                database = database,
            )
        }
    }

    // --- 3. CONFIGURACIÓN VISUAL GLOBAL ---
    BovedaTheme(isDarkTheme = true) {

        // Caja principal que envuelve absolutamente todo
        Box(modifier = Modifier.fillMaxSize()) {

            // LA IMAGEN MAESTRA (Se verá cuando pases al MainScreen)
            Image(
                painter = painterResource(R.drawable.app_background_dark),
                contentDescription = "Fondo Base de Bóveda",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // --- 4. ROOT NAVIGATOR ---
            // ¡EL CAMBIO MAESTRO! Ahora arranca en WelcomeScreen
            Navigator(WelcomeScreen())
        }
    }
}

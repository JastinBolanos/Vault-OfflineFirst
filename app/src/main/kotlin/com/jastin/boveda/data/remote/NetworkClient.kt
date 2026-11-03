package com.jastin.boveda.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.delay

/* =========================================================================
 * NETWORK CLIENT SIMULATOR
 * Emulates the HTTP layer to validate the offline synchronization logic.
 * ========================================================================= */
class NetworkClient {

    // MEMORY SHIELD (SINGLETON)
    // By using 'companion object', we guarantee that only ONE HTTP engine exists
    // throughout the app's lifecycle, regardless of how many times you instantiate NetworkClient.
    // This prevents RAM collapse and Thread Leaks during Worker retries.
    companion object {
        private val httpClient = HttpClient()
    }

    suspend fun processPendingTransaction(transactionId: String): Boolean {
        // --- THE PING OF TRUTH ---
        // It's super fast because it returns an empty response. If there is no internet, this fails.
        try {
            httpClient.get("https://clients3.google.com/generate_204")
        } catch (e: Exception) {
            throw Exception("Device without real internet connection: ${e.message}")
        }

        // --- BANK SIMULATOR ---
        // If the code reaches here, IT'S BECAUSE THERE IS INTERNET.
        // Now, we simulate the time it would take the bank to process the payment.
        delay(1500)

        return true
    }
}
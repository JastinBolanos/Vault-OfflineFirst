package com.jastin.boveda.domain.usecase

import com.jastin.boveda.data.remote.NetworkClient
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.domain.repository.TransactionRepository

/* =========================================================================
 * USE CASE: OFFLINE SYNCHRONIZATION
 * Orchestrates the retrieval of local payments and their transmission to the server.
 * ========================================================================= */

class SyncPendingTransactionsUseCase(
    private val repository: TransactionRepository,
    private val networkClient: NetworkClient
) {

    // --- 1. PROCESSING ENGINE ---
    suspend operator fun invoke() {
        val allTransactions = repository.transactions.value

        val pendingTransactions = allTransactions.filter { it.status == TransactionStatus.PENDING }

        if (pendingTransactions.isEmpty()) return

        for (tx in pendingTransactions) {
            try {
                val isSuccess = networkClient.processPendingTransaction(tx.id)

                if (isSuccess) {
                    repository.updateTransactionStatus(tx.id, TransactionStatus.COMPLETED)
                }
            } catch (e: Exception) {
                println("Worker Error: Failed to sync tx ${tx.id} -> ${e.message}")
            }
        }
    }
}
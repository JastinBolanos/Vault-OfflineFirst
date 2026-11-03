package com.jastin.boveda.domain.repository

import com.jastin.boveda.domain.model.Transaction
import com.jastin.boveda.domain.model.TransactionStatus
import kotlinx.coroutines.flow.StateFlow

/* =========================================================================
 * PORT: TransactionRepository
 * Persistence contract (DIP). Isolates the domain from databases or network.
 * ========================================================================= */
interface TransactionRepository {

    // --- 1. REACTIVE READS (Single Source of Truth) ---
    val transactions: StateFlow<List<Transaction>>
    val currentBalance: StateFlow<Double>

    // --- 2. LOCAL MUTATIONS ---
    fun saveTransaction(transaction: Transaction)

    // --- 3. SYNCHRONIZATION MUTATIONS ---
    fun updateTransactionStatus(id: String, status: TransactionStatus)
}
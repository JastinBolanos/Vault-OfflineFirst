package com.jastin.boveda.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jastin.boveda.database.BovedaDatabase
import com.jastin.boveda.domain.model.Transaction
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.domain.repository.TransactionRepository
import com.jastin.boveda.utils.getCurrentTimeMillis
import com.jastin.boveda.utils.BovedaSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* =========================================================================
 * TRANSACTION REPOSITORY (SINGLE SOURCE OF TRUTH)
 * * [TransactionRepository] implementation using SQLDelight:
 * 1. Mapping: Extracts data from SQLite and STRICTLY converts it to
 * pure Domain entities, isolating the DB from the User Interface.
 * ========================================================================= */
class SqlDelightTransactionRepository(
    database: BovedaDatabase,
    private val scope: CoroutineScope
) : TransactionRepository {

    private val queries = database.transactionEntityQueries

    // --- 1. LAYER READING AND MAPPING (ENTITY -> DOMAIN) ---
    override val transactions: StateFlow<List<Transaction>> = queries.selectAllTransactions()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { entityList ->
            entityList.map { entity ->
                Transaction(
                    id = entity.id,
                    amount = -entity.amount,
                    receiverName = entity.title,
                    receiverAccount = "Local Account",
                    status = if (entity.status == TransactionStatus.PENDING.name) TransactionStatus.PENDING else TransactionStatus.COMPLETED,
                    timestamp = entity.timestamp
                )
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- BALANCE CALCULATION ENGINE ---
    override val currentBalance: StateFlow<Double> = transactions.map { list ->
        // ¡CAPITAL CORPORATIVO INYECTADO AQUI!
        val initialBalance = 35000.00
        val totalSpent = list.sumOf { it.amount }
        initialBalance + totalSpent
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 35000.00
    )

    // --- WRITING (DOMAIN -> ENTITY) ---
    override fun saveTransaction(transaction: Transaction) {
        scope.launch(Dispatchers.IO) {
            try {
                val currentTimestamp = getCurrentTimeMillis()
                val uniqueId = transaction.id.ifBlank { currentTimestamp.toString() }

                queries.insertTransaction(
                    id = uniqueId,
                    title = transaction.receiverName,
                    amount = kotlin.math.abs(transaction.amount),
                    status = transaction.status.name,
                    timestamp = currentTimestamp
                )
                BovedaSyncWorker().enqueueSync()
            } catch (e: Exception) {
                println("⚠️ SQL / Idempotency Error: ${e.message}")
            }
        }
    }

    override fun updateTransactionStatus(id: String, status: TransactionStatus) {
        scope.launch(Dispatchers.IO) {
            queries.updateTransactionStatus(status.name, id)
        }
    }
}
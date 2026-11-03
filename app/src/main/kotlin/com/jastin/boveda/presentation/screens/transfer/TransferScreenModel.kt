package com.jastin.boveda.presentation.screens.transfer

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.jastin.boveda.domain.model.Transaction
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.globalTransactionRepository
import com.jastin.boveda.utils.getCurrentTimeMillis
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/* =========================================================================
 * TRANSFER SCREEN MODEL (MVI PATTERN)
 * State manager and business logic for the payments screen.
 * ========================================================================= */

// --- 1. MVI CONTRACT (STATE & INTENTS) ---
data class TransferState(
    val amount: String = "",
    val recipient: String = "",
    val balance: Double = 35000.00,
    val isLoading: Boolean = false,
    val successTransactionId: String? = null
)

sealed class TransferIntent {
    data class UpdateAmount(val amount: String) : TransferIntent()
    data class UpdateRecipient(val recipient: String) : TransferIntent()
    object ClearNavigation : TransferIntent()
}

class TransferScreenModel : StateScreenModel<TransferState>(TransferState()) {

    private val repository = globalTransactionRepository

    init {
        mutableState.update { it.copy(balance = repository.currentBalance.value) }
    }

    // --- 2. ACTION PROCESSING (REDUCER) ---
    fun onIntent(intent: TransferIntent) {
        when (intent) {
            is TransferIntent.UpdateAmount -> {
                var input = intent.amount.replace(",", ".")

                if (input.startsWith(".")) {
                    input = "0$input"
                }

                // FINANCIAL SHIELD (REGEX):
                // ^(0|[1-9]\d*) -> Starts with 0, or with a number 1-9
                // (\.\d{0,2})?$ -> Can have an optional point with max 2 decimals.
                val isValidMoneyFormat = input.isEmpty() || input.matches(Regex("""^(0|[1-9]\d*)(\.\d{0,2})?$"""))

                if (isValidMoneyFormat) {
                    mutableState.update { it.copy(amount = input) }
                }
            }
            is TransferIntent.UpdateRecipient -> mutableState.update { it.copy(recipient = intent.recipient) }
            is TransferIntent.ClearNavigation -> mutableState.update { it.copy(successTransactionId = null) }
        }
    }

    // --- 3. TRANSACTION EXECUTION (FOREGROUND + FALLBACK) ---
    fun executeTransfer() {
        val amountNum = state.value.amount.toDoubleOrNull() ?: return

        if (amountNum <= 0 || amountNum > state.value.balance || amountNum > 5000.00) return

        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            val safeId = "tx_${kotlin.random.Random.nextLong(100000, 999999)}"

            val newTx = Transaction(
                id = safeId,
                amount = -amountNum,
                receiverName = state.value.recipient,
                receiverAccount = "Local Account",
                status = TransactionStatus.PENDING,
                timestamp = getCurrentTimeMillis()
            )
            repository.saveTransaction(newTx)

            try {
                val networkClient = com.jastin.boveda.data.remote.NetworkClient()
                val isSuccess = networkClient.processPendingTransaction(safeId)

                if (isSuccess) {
                    repository.updateTransactionStatus(safeId, TransactionStatus.COMPLETED)
                }
            } catch (_: Exception) {
                println("Foreground failure. The Worker will handle it.")
            }

            mutableState.update {
                it.copy(
                    isLoading = false,
                    amount = "",
                    recipient = "",
                    successTransactionId = safeId
                )
            }
        }
    }
}
package com.jastin.boveda.presentation.model

/* =========================================================================
 * VIEW STATE MODELS
 * Optimized visual representation of data.
 * * Separation of Concerns: Contains pre-processed data (e.g., formatted strings),
 *   isolating domain entities and DB schema.
 * * UI Performance: By delegating formatting to lower layers, we ensure
 *   Compose acts as a passive UI, avoiding heavy logic during recomposition.
 * ========================================================================= */

// --- 1. INTERFACE STATES ---
// Dictionary of possible visual states for a transaction.
enum class TxUiStatus { COMPLETED, PENDING }

// --- 2. NESTED SUB-MODELS ---
// Information nodes that compose complex lists within the main view (e.g., audit trails).
data class TimelineEventUi(
    val status: String,
    val time: String,
    val done: Boolean
)

// --- 3. MAIN PAYLOAD ---
// The final "processed" DTO consumed by Jetpack Compose to render the detail view.
data class TransactionUiModel(
    val id: String,
    val title: String,
    val amount: Double,
    val status: TxUiStatus,
    val date: String,
    val time: String,
    val method: String,
    val recipient: String,
    val reference: String,
    val timeline: List<TimelineEventUi>
)
package com.jastin.boveda.domain.model

import kotlinx.serialization.Serializable

/* =========================================================================
 * DOMAIN ENTITY: TRANSACTION
 * Represents the business core (Core Domain) in Clean Architecture.
 * This class is strictly agnostic to the UI and databases.
 * ========================================================================= */

// --- 1. MAIN DATA MODEL ---
// Serializable entity that acts as a DTO to the backend and base for persistence.
@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val receiverName: String,
    val receiverAccount: String,
    val status: TransactionStatus,
    val timestamp: Long
)

// --- 2. OPERATION LIFECYCLE ---
// Defines the possible states of the financial flow to guarantee
// integrity and consistency in distributed systems.
enum class TransactionStatus {
    PENDING,    // Initial state in offline mode
    COMPLETED,  // Confirmed by the server
    FAILED,     // Business rejection (e.g., insufficient funds)
    EXPIRED     // Time-to-live exceeded (TTL) without synchronization
}
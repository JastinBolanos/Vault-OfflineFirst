package com.jastin.boveda.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney

/* =========================================================================
 * DESIGN SYSTEM (STATELESS COMPONENTS)
 * =========================================================================
 * Presentation components (Dumb).
 * * State Hoisting: Inject state and events via parameters.
 * * Decoupling: Consume only [TransactionUiModel], delegating
 *   all business logic to upper layers.
 */

// --- 1. BUTTONS AND ACTIONS ---
@Composable
fun BovedaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isGhost) Slate800.copy(alpha = 0.5f) else Slate950,
            contentColor = Color.White,
            disabledContainerColor = Slate100,
            disabledContentColor = Slate400
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// --- 2. CONTAINERS (SURFACES) ---
@Composable
fun BovedaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

// --- 3. LISTS AND DATA ROWS ---
@Composable
fun TransactionRow(tx: TransactionUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(
                if (tx.status == TxUiStatus.PENDING) Amber500.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background,
                RoundedCornerShape(12.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (tx.status == TxUiStatus.PENDING) Icons.Default.Schedule else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (tx.status == TxUiStatus.PENDING) Amber500 else Emerald500
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${tx.date} • ${tx.time}", fontSize = 12.sp, color = Slate400)
        }
        Column(horizontalAlignment = Alignment.End) {
            val isPositive = tx.amount > 0
            Text(
                text = "${if(isPositive) "+" else ""}${formatMoney(tx.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isPositive) Emerald500 else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (tx.status == TxUiStatus.PENDING) "Pending" else "Successful",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (tx.status == TxUiStatus.PENDING) Amber500 else Slate400
            )
        }
    }
}
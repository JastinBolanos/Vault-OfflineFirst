package com.jastin.boveda.presentation.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.jastin.boveda.R
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.presentation.components.TransactionRow
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.screens.activity.ActivityTab
import com.jastin.boveda.presentation.screens.detail.DetailScreen
import com.jastin.boveda.presentation.screens.transfer.TransferScreen
import com.jastin.boveda.presentation.screens.main.LocalMenuDrawerState
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney
import kotlinx.coroutines.launch
import kotlin.math.max

/* =========================================================================
 * MAIN SCREEN (DASHBOARD)
 * Main tab of the application. Acts as the nerve center of the UI,
 * observing balances in real time and providing quick access to transfers
 * and recent movements.
 * ========================================================================= */

object HomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Home", icon = icon) }
        }

    @Composable
    override fun Content() {

        // --- ROUTERS AND BRIDGES (COMPOSITION LOCALS) ---
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current

        // NAVIGATION PROVIDER (DRAWER)
        val drawerState = LocalMenuDrawerState.current
        val scope = rememberCoroutineScope()

        // --- DOMAIN OBSERVATION ---
        val repository = com.jastin.boveda.globalTransactionRepository
        val balanceState = repository.currentBalance.collectAsState()
        val balance = balanceState.value
        val allTransactionsState = repository.transactions.collectAsState()

        // --- 1. DATOS REALES (DE LA BASE DE DATOS) ---
        val realTransactions = allTransactionsState.value.map { tx ->
            TransactionUiModel(
                id = tx.id,
                title = tx.receiverName,
                amount = tx.amount,
                status = if (tx.status == TransactionStatus.PENDING) TxUiStatus.PENDING else TxUiStatus.COMPLETED,
                date = "Today",
                time = "00:00",
                method = "Vault Balance",
                recipient = tx.receiverName,
                reference = "REF-${tx.id.take(6)}",
                timeline = emptyList()
            )
        }

        // --- 2. DATOS DE RELLENO (MOCK DATA) ---
        val mockPlaceholders = remember {
            listOf(
                TransactionUiModel(
                    id = "mock_stripe_in",
                    title = "Stripe Payout",
                    amount = 12450.00,
                    status = TxUiStatus.COMPLETED,
                    date = "Today",
                    time = "10:30",
                    method = "Bank Transfer",
                    recipient = "Stripe Payments Company",
                    reference = "REF-STRP449",
                    timeline = emptyList()
                ),
                TransactionUiModel(
                    id = "mock_aws_01",
                    title = "Amazon Web Services",
                    amount = -1450.75,
                    status = TxUiStatus.COMPLETED,
                    date = "Today",
                    time = "08:15",
                    method = "Corporate Card",
                    recipient = "AWS EMEA SARL",
                    reference = "REF-AWS9920",
                    timeline = emptyList()
                ),
                TransactionUiModel(
                    id = "mock_apple_pending",
                    title = "Apple Enterprise",
                    amount = -4299.00,
                    status = TxUiStatus.PENDING,
                    date = "Yesterday",
                    time = "16:45",
                    method = "Wire Transfer",
                    recipient = "Apple Inc.",
                    reference = "REF-APPL09X",
                    timeline = emptyList()
                )
            )
        }
        val displayTransactions = (realTransactions + mockPlaceholders).take(3)

        // --- VISUAL BUSINESS RULES ---
        val displayBalance = max(0.0, balance)
        val hasInsufficientFunds = balance <= 0

        // =====================================================================
        // 💎 ANIMACIONES PREMIUM (SHIMMERS & WAVES)
        // =====================================================================
        val infiniteTransition = rememberInfiniteTransition(label = "home_animations")

        // 1. Shimmer/Ola brillante para el saldo
        val shimmerAnim by infiniteTransition.animateFloat(
            initialValue = -200f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ), label = "balance_shimmer"
        )
        val balanceBrush = Brush.linearGradient(
            colors = listOf(Color.White, Emerald500, Color.White),
            start = Offset(shimmerAnim, shimmerAnim),
            end = Offset(shimmerAnim + 250f, shimmerAnim + 250f)
        )

        // 2. Ola de luz para el botón Transfer
        val buttonWaveAnim by infiniteTransition.animateFloat(
            initialValue = -500f,
            targetValue = 1500f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2500, delayMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "button_wave"
        )
        val buttonWaveBrush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
            start = Offset(buttonWaveAnim, 0f),
            end = Offset(buttonWaveAnim + 200f, 200f)
        )

        // 3. Serpiente de luz en movimiento (Bordes de las transacciones)
        val borderAnim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1500f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "border_anim"
        )
        val borderBrush = Brush.linearGradient(
            colors = listOf(
                Slate800.copy(alpha = 0.3f),
                Emerald500.copy(alpha = 0.9f),
                Color(0xFF8B5CF6).copy(alpha = 0.9f),
                Slate800.copy(alpha = 0.3f)
            ),
            start = Offset(borderAnim, borderAnim),
            end = Offset(borderAnim + 300f, borderAnim + 300f)
        )
        // =====================================================================


        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.avatar_corporate),
                            contentDescription = "User Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Welcome back", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Text("Alexander Sterling", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()
            ) {

                // --- 4. MAIN BALANCE CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate950)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Text("Available Balance", color = Slate400, fontSize = 14.sp)

                        // TEXTO ANIMADO DEL BALANCE
                        Text(
                            text = formatMoney(displayBalance),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = if (hasInsufficientFunds) TextStyle(color = Red500) else TextStyle(brush = balanceBrush),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        // BOTÓN ANIMADO DE TRANSFERENCIA
                        Button(
                            onClick = { navigator.push(TransferScreen(balance)) },
                            enabled = !hasInsufficientFunds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .drawWithContent {
                                    drawContent()
                                    drawRect(brush = buttonWaveBrush)
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500, contentColor = Slate950,
                                disabledContainerColor = Slate800, disabledContentColor = Slate400
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(if (hasInsufficientFunds) "Insufficient Funds" else "Transfer", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- 5. RECENT ACTIVITY SECTION ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = { tabNavigator.current = ActivityTab }) {
                        Text("See all", color = Emerald500, fontWeight = FontWeight.SemiBold)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(displayTransactions) { tx ->
                        // CAJA ENVOLVENTE PARA EL BORDE ANIMADO
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, borderBrush, RoundedCornerShape(16.dp))
                        ) {
                            TransactionRow(tx) { navigator.push(DetailScreen(tx.id)) }
                        }
                    }
                }
            }
        }
    }
}

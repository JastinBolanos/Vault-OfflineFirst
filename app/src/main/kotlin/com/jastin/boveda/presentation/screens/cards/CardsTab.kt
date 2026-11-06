package com.jastin.boveda.presentation.screens.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.jastin.boveda.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* =========================================================================
 * BÓVEDA PRIVATE BANKING - CARDS TAB (FULL SCREEN EXPERIENCE)
 * 
 * - Full Screen Titanium Black Metal and Virtual Cloud Cards Console
 * - Direct freeze/unfreeze controls with instant visual state
 * - Spending limit tracker and multi-currency travel settings
 * ========================================================================= */

object CardsTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.CreditCard)
            return remember { TabOptions(index = 1u, title = "Cards", icon = icon) }
        }

    @Composable
    override fun Content() {
        val clipboardManager = LocalClipboardManager.current
        val scope = rememberCoroutineScope()

        var isPhysicalCardFrozen by remember { mutableStateOf(false) }
        var isOnlineTransactionsEnabled by remember { mutableStateOf(true) }
        var isContactlessEnabled by remember { mutableStateOf(true) }
        var isInternationalEnabled by remember { mutableStateOf(true) }
        var isAtmEnabled by remember { mutableStateOf(true) }

        var isDetailsRevealed by remember { mutableStateOf(false) }
        var isCopiedNotification by remember { mutableStateOf(false) }
        var selectedCardType by remember { mutableStateOf(0) } // 0: Titanium Metal, 1: Virtual Cloud
        var monthlyLimit by remember { mutableStateOf(50000.0) }

        val spentAmount = 18450.75
        val limitProgress = (spentAmount / monthlyLimit).toFloat().coerceIn(0f, 1f)

        // Animación suave de respiración en la tarjeta metálica
        val infiniteTransition = rememberInfiniteTransition(label = "card_screen_motion")
        val cardFloatY by infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "card_float"
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Cards & Vault",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Titanium & Sovereign Payment Engines",
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald500.copy(alpha = 0.15f))
                                .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "1 Physical • 2 Virtual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald400
                            )
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 135.dp)
            ) {

                // --- 1. SELECTOR DE TIPO DE TARJETA ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedCardType == 0) Emerald500 else Color.Transparent)
                                .clickable { selectedCardType = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Titanium Metal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCardType == 0) Slate950 else Slate400
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedCardType == 1) Emerald500 else Color.Transparent)
                                .clickable { selectedCardType = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Virtual Cloud Card",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCardType == 1) Slate950 else Slate400
                            )
                        }
                    }
                }

                // --- 2. SHOWCASE DE LA TARJETA SELECCIONADA ---
                item {
                    val cardGradient = if (selectedCardType == 0) {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF263345),
                                Color(0xFF141C2A),
                                Color(0xFF0A0F18)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF312E81),
                                Color(0xFF0F172A)
                            )
                        )
                    }

                    val borderGradient = if (selectedCardType == 0) {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFD4AF37),
                                Color(0xFFF3E5AB),
                                Color(0xFFAA771C)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF818CF8),
                                Color(0xFFC084FC),
                                Color.Transparent
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .offset(y = cardFloatY.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardGradient)
                            .border(1.2.dp, borderGradient, RoundedCornerShape(24.dp))
                            .padding(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Header de la tarjeta
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Microchip metálico
                                    Box(
                                        modifier = Modifier
                                            .size(width = 28.dp, height = 20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFD4AF37), Color(0xFFF3E5AB), Color(0xFFAA771C))
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (selectedCardType == 0) "VAULT TITANIUM METAL" else "VAULT CLOUD DISPOSABLE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.4.sp,
                                        color = if (selectedCardType == 0) Color(0xFFD4AF37) else Color(0xFFA5B4FC)
                                    )
                                }

                                if (isPhysicalCardFrozen && selectedCardType == 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Red500.copy(alpha = 0.25f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "FROZEN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Red500
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Nfc,
                                        contentDescription = "Contactless",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Número de tarjeta
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isDetailsRevealed) {
                                        if (selectedCardType == 0) "4920  8849  0129  8849" else "5204  9102  4481  9021"
                                    } else {
                                        "••••  ••••  ••••  8849"
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                IconButton(
                                    onClick = { isDetailsRevealed = !isDetailsRevealed },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                ) {
                                    Icon(
                                        imageVector = if (isDetailsRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle card details",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Datos inferiores
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(text = "CARDHOLDER", fontSize = 9.sp, color = Slate400, letterSpacing = 1.sp)
                                    Text(text = "ALEXANDER STERLING", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "EXPIRES", fontSize = 9.sp, color = Slate400, letterSpacing = 1.sp)
                                        Text(text = if (selectedCardType == 0) "09 / 29" else "12 / 27", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "CVV", fontSize = 9.sp, color = Slate400, letterSpacing = 1.sp)
                                        Text(text = if (isDetailsRevealed) "842" else "•••", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 3. ACCIONES RÁPIDAS DE TARJETA ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardActionButton(
                            title = if (isPhysicalCardFrozen) "Unfreeze Card" else "Freeze Card",
                            icon = if (isPhysicalCardFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                            isWarning = !isPhysicalCardFrozen,
                            modifier = Modifier.weight(1f),
                            onClick = { isPhysicalCardFrozen = !isPhysicalCardFrozen }
                        )

                        CardActionButton(
                            title = if (isCopiedNotification) "Copied!" else "Copy Details",
                            icon = if (isCopiedNotification) Icons.Default.Check else Icons.Default.ContentCopy,
                            isWarning = false,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                clipboardManager.setText(AnnotatedString("4920884901298849"))
                                isCopiedNotification = true
                                scope.launch {
                                    delay(2000)
                                    isCopiedNotification = false
                                }
                            }
                        )

                        CardActionButton(
                            title = "Change PIN",
                            icon = Icons.Default.Pin,
                            isWarning = false,
                            modifier = Modifier.weight(1f),
                            onClick = { /* PIN modal */ }
                        )
                    }
                }

                // --- 4. CONTROL DE LÍMITES MENSUALES ---
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Monthly Spending Limit",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "36.9% utilized this billing cycle",
                                        fontSize = 11.sp,
                                        color = Slate400
                                    )
                                }

                                Text(
                                    text = "$50,000 / mo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald400
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Barra de progreso
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(limitProgress)
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(Emerald500, Teal400)))
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Spent: $18,450.75", fontSize = 11.sp, color = Slate400)
                                Text(text = "Available: $31,549.25", fontSize = 11.sp, color = Emerald400, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // --- 5. INTERRUPTORES DE SEGURIDAD Y PERMISOS ---
                item {
                    Text(
                        text = "SECURITY & PERMISSIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Slate400,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        CardToggleRow(
                            icon = Icons.Default.Public,
                            title = "International & Travel Spend",
                            subtitle = "Zero FX markup across 140+ currencies",
                            checked = isInternationalEnabled,
                            onCheckedChange = { isInternationalEnabled = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        CardToggleRow(
                            icon = Icons.Default.ShoppingBag,
                            title = "Online Transactions",
                            subtitle = "E-commerce and cloud billing authorization",
                            checked = isOnlineTransactionsEnabled,
                            onCheckedChange = { isOnlineTransactionsEnabled = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        CardToggleRow(
                            icon = Icons.Default.Nfc,
                            title = "Contactless & Mobile Payments",
                            subtitle = "Tap to pay and Google Wallet tokenization",
                            checked = isContactlessEnabled,
                            onCheckedChange = { isContactlessEnabled = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        CardToggleRow(
                            icon = Icons.Default.Atm,
                            title = "ATM Cash Withdrawals",
                            subtitle = "Global ATM access with daily $5,000 limit",
                            checked = isAtmEnabled,
                            onCheckedChange = { isAtmEnabled = it }
                        )
                    }
                }
            }
        }
    }

    // --- COMPONENTE: BOTÓN RÁPIDO DE ACCIÓN ---
    @Composable
    private fun CardActionButton(
        title: String,
        icon: ImageVector,
        isWarning: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val bg = if (isWarning) Red500.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f)
        val borderCol = if (isWarning) Red500.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.10f)
        val tintCol = if (isWarning) Red500 else Color.White

        Box(
            modifier = modifier
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintCol,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tintCol,
                    maxLines = 1
                )
            }
        }
    }

    // --- COMPONENTE: FILA DE INTERRUPTOR ---
    @Composable
    private fun CardToggleRow(
        icon: ImageVector,
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (checked) Emerald500.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) Emerald400 else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Emerald400,
                    checkedTrackColor = Emerald500.copy(alpha = 0.40f)
                )
            )
        }
    }
}

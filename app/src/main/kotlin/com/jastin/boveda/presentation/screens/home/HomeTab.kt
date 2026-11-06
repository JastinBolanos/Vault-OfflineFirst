package com.jastin.boveda.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.screens.activity.ActivityTab
import com.jastin.boveda.presentation.screens.cards.CardsTab
import com.jastin.boveda.presentation.screens.detail.DetailScreen
import com.jastin.boveda.presentation.screens.main.LocalMenuDrawerState
import com.jastin.boveda.presentation.screens.security.SecurityTab
import com.jastin.boveda.presentation.screens.transfer.TransferScreen
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/* =========================================================================
 * BÓVEDA PRIVATE BANKING - HOME TAB (REDESIGNED ULTRA-PREMIUM NEOBANK)
 * 
 * - Executive Vault Titanium Card with ambient glow and balance visibility toggle.
 * - 5 Unique Central Action Hub (Transfer, Deposit, Analytics, Token, Cards).
 * - Luminous Recent Movements with vibrant neobank merchant identifiers.
 * - Interactive User Profile, Deposit Wire Instructions, and Card Manager sheets.
 * ========================================================================= */

object HomeTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Home", icon = icon) }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val drawerState = LocalMenuDrawerState.current
        val scope = rememberCoroutineScope()
        val clipboardManager = LocalClipboardManager.current

        // Estados interactivos
        var showProfileSheet by remember { mutableStateOf(false) }
        val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var showDepositSheet by remember { mutableStateOf(false) }
        val depositSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var showCardSheet by remember { mutableStateOf(false) }
        val cardSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var isBalanceHidden by remember { mutableStateOf(false) }
        var isAccountCopiedQuick by remember { mutableStateOf(false) }

        // --- ANIMACIÓN DE LEVITACIÓN SUAVE DE LA TARJETA DORADA ("UN POCO DE MOVIMIENTO NADA MÁS") ---
        val goldCardMotionTransition = rememberInfiniteTransition(label = "gold_card_motion")
        val goldCardFloatY by goldCardMotionTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gold_float_y"
        )
        val goldCardTilt by goldCardMotionTransition.animateFloat(
            initialValue = -0.6f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(3800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gold_tilt"
        )

        // --- DOMAIN OBSERVATION ---
        val repository = com.jastin.boveda.globalTransactionRepository
        val balanceState = repository.currentBalance.collectAsState()
        val balance = balanceState.value
        val allTransactionsState = repository.transactions.collectAsState()

        // --- DATOS REALES ---
        val realTransactions = allTransactionsState.value.map { tx ->
            TransactionUiModel(
                id = tx.id,
                title = tx.receiverName,
                amount = tx.amount,
                status = if (tx.status == TransactionStatus.PENDING) TxUiStatus.PENDING else TxUiStatus.COMPLETED,
                date = "Today",
                time = "00:00",
                method = "Vault Transfer",
                recipient = tx.receiverName,
                reference = "REF-${tx.id.take(6)}",
                timeline = emptyList()
            )
        }

        // --- DATOS DE DEMOSTRACIÓN VIBRANTES Y NEOBANK ---
        val mockPlaceholders = remember {
            listOf(
                TransactionUiModel(
                    id = "mock_stripe_in",
                    title = "Stripe Payout",
                    amount = 12450.00,
                    status = TxUiStatus.COMPLETED,
                    date = "Today",
                    time = "10:30 AM",
                    method = "Payment Inflow",
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
                    time = "08:15 AM",
                    method = "Cloud Infra",
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
                    time = "04:45 PM",
                    method = "Hardware",
                    recipient = "Apple Inc.",
                    reference = "REF-APPL09X",
                    timeline = emptyList()
                )
            )
        }
        val displayTransactions = (realTransactions + mockPlaceholders).take(4)
        val displayBalance = max(0.0, balance)
        val hasInsufficientFunds = balance <= 0

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // --- 1. TOP BAR ELEGANTE CON AVATAR INTERACTIVO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showProfileSheet = true }
                            .padding(end = 8.dp)
                    ) {
                        // Avatar con borde en gradiente de halo
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(Emerald400, Teal400, Color(0xFF6366F1), Emerald400)
                                    )
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.avatar_corporate),
                                contentDescription = "Profile Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Good day,",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate400
                            )
                            Text(
                                text = "Alexander Sterling",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Botón de menú lateral estilizado
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 130.dp)
            ) {

                // --- 2. EXECUTIVE 24K GOLD RESERVE CARD (DEGRADÉ POTENTE DE ORO Y MÁXIMO LUJO) ---
                item {
                    val cardSurfaceGradient = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFA8A), // Oro blanco reflectivo / Champagne
                            Color(0xFFFFD54F), // Oro brillante 24K espejo
                            Color(0xFFF59E0B), // Oro ámbar puro
                            Color(0xFFD97706), // Oro bronce imperial
                            Color(0xFF92400E), // Oro quemado profundo
                            Color(0xFF451A03)  // Sombra de obsidiana áurea
                        )
                    )
                    val cardBorderGradient = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFDE7), // Reflejo blanco-dorado puro
                            Color(0xFFFFE082), // Oro luminoso
                            Color(0xFFF59E0B), // Oro ámbar
                            Color(0xFF78350F), // Profundidad bronce
                            Color(0xFFFFF9C4)  // Luz especular
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = goldCardFloatY.dp)
                            .graphicsLayer {
                                rotationZ = goldCardTilt
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .background(cardSurfaceGradient)
                            .border(1.4.dp, cardBorderGradient, RoundedCornerShape(26.dp))
                            .padding(22.dp)
                    ) {
                        Column {
                            // Fila superior: Chip microprocesador de obsidiana/oro, Identificador y Switch de Ocultar Saldo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Microchip de la tarjeta dorada
                                    Box(
                                        modifier = Modifier
                                            .size(width = 26.dp, height = 19.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF1E1402), Color(0xFF382506), Color(0xFF0F0A01))
                                                )
                                            )
                                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 14.dp, height = 10.dp)
                                                .border(0.8.dp, Color(0xFFFFD54F).copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(9.dp))
                                    Text(
                                        text = "VAULT GOLD RESERVE",
                                        fontSize = 11.sp,
                                        letterSpacing = 1.6.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1F1403)
                                    )
                                }

                                // Botón para ocultar/mostrar saldo confidencial grabado
                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E1402).copy(alpha = 0.20f))
                                        .border(1.dp, Color(0xFF1E1402).copy(alpha = 0.25f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Balance",
                                        tint = Color(0xFF1E1402),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Etiqueta de balance
                            Text(
                                text = "TOTAL AVAILABLE BALANCE",
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF382506).copy(alpha = 0.90f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Monto de saldo con altura fija para evitar cualquier salto o expansión de la tarjeta
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Crossfade(
                                    targetState = isBalanceHidden,
                                    animationSpec = tween(150),
                                    label = "balance_toggle"
                                ) { hidden ->
                                    Text(
                                        text = if (hidden) "$ ••••••••••" else formatMoney(displayBalance),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (!hidden && hasInsufficientFunds) Red500 else Color(0xFF120C02),
                                        letterSpacing = if (hidden) 1.5.sp else (-0.5).sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Fila inferior de métricas con indicador de flujo y cuenta rápida copiable
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Píldora de crecimiento financiero
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1A1202).copy(alpha = 0.22f))
                                        .border(1.dp, Color(0xFF1A1202).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 11.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF120C02),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "+4.8% net cash flow",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF120C02)
                                        )
                                    }
                                }

                                // Copiado rápido de cuenta
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString("VAULT-8849-0129-US"))
                                            isAccountCopiedQuick = true
                                            scope.launch {
                                                delay(1800)
                                                isAccountCopiedQuick = false
                                            }
                                        }
                                        .background(Color(0xFF1A1202).copy(alpha = 0.22f))
                                        .border(1.dp, Color(0xFF1A1202).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isAccountCopiedQuick) "Copied!" else "•••• 8849",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAccountCopiedQuick) Color(0xFF064E3B) else Color(0xFF120C02)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = if (isAccountCopiedQuick) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy account number",
                                        tint = if (isAccountCopiedQuick) Color(0xFF064E3B) else Color(0xFF120C02),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. BOTONES CENTRALES: 5 ACCIONES ÚNICAS Y EXCLUSIVAS (SIN TEXTO PARTIDO) ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Transfer (Transferir / Enviar)
                        CentralActionButton(
                            title = "Transfer",
                            icon = Icons.Default.SwapHoriz,
                            gradient = Brush.linearGradient(listOf(Emerald400, Teal400)),
                            iconTint = Slate950,
                            modifier = Modifier.weight(1f),
                            onClick = { navigator.push(TransferScreen(balance)) }
                        )

                        // 2. Deposit (Depositar / Wire In)
                        CentralActionButton(
                            title = "Deposit",
                            icon = Icons.Default.Add,
                            gradient = Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7))),
                            iconTint = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = { showDepositSheet = true }
                        )

                        // 3. Analytics (Inteligencia financiera)
                        CentralActionButton(
                            title = "Analytics",
                            icon = Icons.Default.Insights,
                            gradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                            iconTint = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = { tabNavigator.current = ActivityTab }
                        )

                        // 4. Token / Security (Pantalla completa de Seguridad 2FA)
                        CentralActionButton(
                            title = "Security",
                            icon = Icons.Default.Shield,
                            gradient = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                            iconTint = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = { tabNavigator.current = SecurityTab }
                        )

                        // 5. Cards (Pantalla completa de Tarjetas y Límites)
                        CentralActionButton(
                            title = "Cards",
                            icon = Icons.Default.CreditCard,
                            gradient = Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
                            iconTint = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = { tabNavigator.current = CardsTab }
                        )
                    }
                }

                // --- 4. ENCABEZADO DE ACTIVIDAD RECIENTE (LIMPIO, SIN ETIQUETA "LIVE") ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Movements",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        TextButton(
                            onClick = { tabNavigator.current = ActivityTab },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "View all",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald400
                            )
                        }
                    }
                }

                // --- 5. MOVIMIENTOS RECIENTES: DISEÑO NEOBANK MODERNO Y CONTINUO ---
                item {
                    val feedBorderGradient = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.70f))
                            .border(1.dp, feedBorderGradient, RoundedCornerShape(22.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            displayTransactions.forEachIndexed { index, tx ->
                                NeobankTransactionItem(
                                    tx = tx,
                                    onClick = { navigator.push(DetailScreen(tx.id, tx)) }
                                )

                                if (index < displayTransactions.lastIndex) {
                                    HorizontalDivider(
                                        color = Color.White.copy(alpha = 0.05f),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. MODAL BOTTOM SHEET: PERFIL ULTRA-PREMIUM ---
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                sheetState = profileSheetState,
                containerColor = Color(0xFF0F172A),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 44.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                tonalElevation = 16.dp
            ) {
                UserProfileTrayContent(
                    onDismiss = {
                        scope.launch {
                            profileSheetState.hide()
                            showProfileSheet = false
                        }
                    }
                )
            }
        }

        // --- 7. MODAL BOTTOM SHEET: DEPOSIT / WIRE INSTRUCTIONS (NUEVO BOTÓN 1) ---
        if (showDepositSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDepositSheet = false },
                sheetState = depositSheetState,
                containerColor = Color(0xFF0F172A),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 44.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                DepositInstructionsSheet(
                    onDismiss = {
                        scope.launch {
                            depositSheetState.hide()
                            showDepositSheet = false
                        }
                    }
                )
            }
        }

        // --- 8. MODAL BOTTOM SHEET: CARD CONTROLS & LIMITS (NUEVO BOTÓN 2) ---
        if (showCardSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCardSheet = false },
                sheetState = cardSheetState,
                containerColor = Color(0xFF0F172A),
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 44.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                CardManagerSheet(
                    onDismiss = {
                        scope.launch {
                            cardSheetState.hide()
                            showCardSheet = false
                        }
                    }
                )
            }
        }
    }

    // --- COMPONENTE: BOTÓN CENTRAL DE ACCIÓN INDIVIDUAL (5 ITEMS) ---
    @Composable
    private fun CentralActionButton(
        title: String,
        icon: ImageVector,
        gradient: Brush,
        iconTint: Color,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        Column(
            modifier = modifier
                .clickable { onClick() }
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono redondeado con fondo degradado luminoso
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(gradient)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Etiqueta tipográfica en una sola línea limpia
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.90f),
                maxLines = 1
            )
        }
    }

    // --- COMPONENTE: MODAL SHEET DE INSTRUCCIONES DE DEPÓSITO ---
    @Composable
    private fun DepositInstructionsSheet(onDismiss: () -> Unit) {
        val clipboardManager = LocalClipboardManager.current
        var copiedKey by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        fun copyWithNotice(label: String, value: String) {
            clipboardManager.setText(AnnotatedString(value))
            copiedKey = label
            scope.launch {
                delay(2000)
                if (copiedKey == label) copiedKey = null
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deposit & Wire Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time incoming settlement routing",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista de campos copiables
            DepositDetailRow(
                label = "Beneficiary Name",
                value = "Alexander Sterling",
                isCopied = copiedKey == "Beneficiary Name",
                onCopy = { copyWithNotice("Beneficiary Name", "Alexander Sterling") }
            )

            DepositDetailRow(
                label = "Bank Name",
                value = "Vault Federal Custody NA",
                isCopied = copiedKey == "Bank Name",
                onCopy = { copyWithNotice("Bank Name", "Vault Federal Custody NA") }
            )

            DepositDetailRow(
                label = "Routing Number (ABA / Fedwire)",
                value = "021000021",
                isCopied = copiedKey == "Routing",
                onCopy = { copyWithNotice("Routing", "021000021") }
            )

            DepositDetailRow(
                label = "Account Identifier",
                value = "8849-0129-03",
                isCopied = copiedKey == "Account",
                onCopy = { copyWithNotice("Account", "8849-0129-03") }
            )

            DepositDetailRow(
                label = "SWIFT / BIC Code",
                value = "VLTUS33XXX",
                isCopied = copiedKey == "SWIFT",
                onCopy = { copyWithNotice("SWIFT", "VLTUS33XXX") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Funds deposited via Fedwire settle in 30 seconds. Domestic ACH clears next morning.",
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun DepositDetailRow(
        label: String,
        value: String,
        isCopied: Boolean,
        onCopy: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .clickable { onCopy() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, fontSize = 11.sp, color = Slate400)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCopied) Emerald500.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = if (isCopied) Emerald400 else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied" else "Copy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCopied) Emerald400 else Color.White
                    )
                }
            }
        }
    }

    // --- COMPONENTE: MODAL SHEET DE GESTIÓN DE TARJETA TITANIUM ---
    @Composable
    private fun CardManagerSheet(onDismiss: () -> Unit) {
        var isCardFrozen by remember { mutableStateOf(false) }
        var isInternationalEnabled by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Card Controls",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Titanium Black Metal Debit Card",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Representación visual de la tarjeta Titanium
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1F2937),
                                Color(0xFF111827),
                                Color(0xFF030712)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VAULT METAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD4AF37),
                            letterSpacing = 1.4.sp
                        )

                        if (isCardFrozen) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Red500.copy(alpha = 0.20f))
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
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "••••  ••••  ••••  8849",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "CARDHOLDER", fontSize = 9.sp, color = Slate400, letterSpacing = 1.sp)
                            Text(text = "ALEXANDER STERLING", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "EXPIRES", fontSize = 9.sp, color = Slate400, letterSpacing = 1.sp)
                            Text(text = "09 / 29", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Switch: Congelar tarjeta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCardFrozen) Red500.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCardFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isCardFrozen) Red500 else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Freeze Physical & Virtual Card", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Instantly disable unauthorized transactions", fontSize = 11.sp, color = Slate400)
                    }
                }

                Switch(
                    checked = isCardFrozen,
                    onCheckedChange = { isCardFrozen = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Red500,
                        checkedTrackColor = Red500.copy(alpha = 0.35f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Switch: Compras internacionales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Global Currencies & Travel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Zero FX markup in 140+ countries", fontSize = 11.sp, color = Slate400)
                    }
                }

                Switch(
                    checked = isInternationalEnabled,
                    onCheckedChange = { isInternationalEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Emerald400,
                        checkedTrackColor = Emerald500.copy(alpha = 0.40f)
                    )
                )
            }
        }
    }

    // --- COMPONENTE: BANDEJA ULTRA-PREMIUM DEL PERFIL ---
    @Composable
    private fun UserProfileTrayContent(onDismiss: () -> Unit) {
        val clipboardManager = LocalClipboardManager.current
        var isAccountCopied by remember { mutableStateOf(false) }
        var biometricsEnabled by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        // Animación de halo pulsante para el avatar de perfil
        val infiniteTransition = rememberInfiniteTransition(label = "profile_halo")
        val haloScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "halo_scale"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con botón de cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Executive Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar principal con efecto Aura Glow animado
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(haloScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Emerald400.copy(alpha = 0.45f), Teal400.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .border(2.5.dp, Brush.sweepGradient(listOf(Emerald400, Teal400, Color(0xFF8B5CF6), Emerald400)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.avatar_corporate),
                    contentDescription = "Alexander Sterling Profile",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Nombre y badge de verificación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Alexander Sterling",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Member",
                    tint = Emerald400,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Founding Member • Black Metal Tier",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Emerald400
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de IBAN / Account ID con botón de copiar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACCOUNT IDENTIFIER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "VAULT-8849-0129-US",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Botón de copia
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAccountCopied) Emerald500.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                            .clickable {
                                clipboardManager.setText(AnnotatedString("VAULT-8849-0129-US"))
                                isAccountCopied = true
                                coroutineScope.launch {
                                    delay(2000)
                                    isAccountCopied = false
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAccountCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isAccountCopied) Emerald400 else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAccountCopied) "Copied" else "Copy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAccountCopied) Emerald400 else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Métricas rápidas de cuenta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileMetricCard(
                    title = "Daily Limit",
                    value = "$50,000",
                    subtitle = "100% Available",
                    modifier = Modifier.weight(1f)
                )

                ProfileMetricCard(
                    title = "Offline Engine",
                    value = "Autonomous",
                    subtitle = "SQLite Encrypted",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch de Biometría / Quick Security
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Biometric Security",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Fingerprint / Face ID for transfers",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Switch(
                    checked = biometricsEnabled,
                    onCheckedChange = { biometricsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Emerald400,
                        checkedTrackColor = Emerald500.copy(alpha = 0.45f)
                    )
                )
            }
        }
    }

    // --- COMPONENTE: TARJETA DE MÉTRICAS DEL PERFIL ---
    @Composable
    private fun ProfileMetricCard(
        title: String,
        value: String,
        subtitle: String,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Emerald400,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // --- COMPONENTE: ITEM DE MOVIMIENTO ESTILO NEOBANK VIVO Y PROFESIONAL ---
    @Composable
    private fun NeobankTransactionItem(
        tx: TransactionUiModel,
        onClick: () -> Unit
    ) {
        val isPending = tx.status == TxUiStatus.PENDING
        val isPositive = tx.amount > 0

        // Configuración viva según la naturaleza del movimiento (estilo Neobank)
        val (badgeGradient, badgeIcon, categoryLabel, categoryColor) = when {
            tx.title.contains("Stripe", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                    Icons.Default.CreditCard,
                    "Payment Inflow",
                    Color(0xFFA5B4FC)
                )
            }
            tx.title.contains("Amazon", ignoreCase = true) || tx.title.contains("AWS", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))),
                    Icons.Default.Cloud,
                    "Cloud Infra",
                    Color(0xFFFDBA74)
                )
            }
            tx.title.contains("Apple", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
                    Icons.Default.Devices,
                    "Hardware",
                    Color(0xFFCBD5E1)
                )
            }
            isPositive -> {
                Quad(
                    Brush.linearGradient(listOf(Emerald500, Teal400)),
                    Icons.Default.ArrowDownward,
                    "Deposit",
                    Emerald400
                )
            }
            else -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
                    Icons.Default.ArrowUpward,
                    "Vault Transfer",
                    Color(0xFF93C5FD)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono distintivo y vibrante con sombra sutil
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Datos del comerciante y categoría viva
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = categoryLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )

                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = Slate400
                    )

                    Text(
                        text = tx.time,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            // Monto y píldora de estado
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else ""}${formatMoney(tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) Emerald400 else Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isPending) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Amber500.copy(alpha = 0.16f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Amber500,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Pending",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber500
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald500.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Completed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald400
                        )
                    }
                }
            }
        }
    }

    // Helper data holder para el diseño neobank
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

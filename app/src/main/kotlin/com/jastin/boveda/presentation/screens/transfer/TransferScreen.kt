package com.jastin.boveda.presentation.screens.transfer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney

/* =========================================================================
 * ULTRA-PREMIUM NEOBANK TRANSFER SCREEN (BÓVEDA PRIVATE BANKING)
 *
 * - Ambient obsidian glow & luminous glassmorphism.
 * - Mini 24K Gold Reserve card badge & real-time balance badge.
 * - Massive high-precision dynamic currency amount input.
 * - Quick amount pills (+50, +100, +500, MAX) with luminous accent borders.
 * - 1-Tap Frequent Beneficiaries carousel (instant autofill).
 * - Polished glassmorphic input cards with leading status icons.
 * - Instant Fedwire / Zero-Fee settlement breakdown.
 * - High-gloss emerald/gold action button (no flat grey disabled state).
 * ========================================================================= */

private data class QuickPayee(
    val name: String,
    val account: String,
    val initials: String,
    val gradient: List<Color>,
    val tag: String
)

data class TransferScreen(val currentBalance: Double) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { TransferScreenModel() }
        val state by screenModel.state.collectAsState()

        val clipboardManager = LocalClipboardManager.current
        var accountNumber by remember { mutableStateOf("") }
        var transferMessage by remember { mutableStateOf("") }
        var showLimitInfoDialog by remember { mutableStateOf(false) }

        val amountNum = state.amount.toDoubleOrNull() ?: 0.0
        val isOverdraft = amountNum > currentBalance
        val exceedsLimit = amountNum > 5000.00
        val hasError = isOverdraft || exceedsLimit
        val hasTrailingDot = state.amount.endsWith(".")
        val isFormValid = amountNum > 0 && !hasError && !hasTrailingDot && state.recipient.isNotBlank() && accountNumber.isNotBlank() && !state.isLoading

        // Handle hardware back press cleanly
        BackHandler(enabled = !state.isLoading) {
            navigator.pop()
        }

        LaunchedEffect(state.successTransactionId) {
            state.successTransactionId?.let { txId ->
                screenModel.onIntent(TransferIntent.ClearNavigation)
                navigator.replace(com.jastin.boveda.presentation.screens.detail.DetailScreen(txId))
            }
        }

        // Beneficiarios frecuentes para autofill instantáneo de 1 toque
        val quickPayees = remember {
            listOf(
                QuickPayee(
                    name = "Stripe Corporate",
                    account = "US89-STRP-99201",
                    initials = "ST",
                    gradient = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)),
                    tag = "Payout"
                ),
                QuickPayee(
                    name = "Amazon Web Services",
                    account = "US44-AMZN-88102",
                    initials = "AW",
                    gradient = listOf(Color(0xFFFF9900), Color(0xFFD97706)),
                    tag = "Cloud"
                ),
                QuickPayee(
                    name = "Apple Enterprise",
                    account = "US12-APPL-00441",
                    initials = "AP",
                    gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    tag = "Hardware"
                ),
                QuickPayee(
                    name = "Elena Rostova",
                    account = "+1 (555) 234-8901",
                    initials = "ER",
                    gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    tag = "Treasury"
                ),
                QuickPayee(
                    name = "Alexander Wright",
                    account = "+1 (555) 876-5432",
                    initials = "AW",
                    gradient = listOf(Color(0xFFF59E0B), Color(0xFFB45309)),
                    tag = "Partner"
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
        ) {
            // --- FONDO ATMOSFÉRICO NEOBANK (AURA AMBIENTAL) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Slate950,
                                Color(0xFF020617)
                            )
                        )
                    )
            )

            // Resplandor superior sutil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Emerald500.copy(alpha = 0.12f),
                                Color(0xFF3B82F6).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botón de regreso en cristal esmerilado
                        IconButton(
                            onClick = { navigator.pop() },
                            enabled = !state.isLoading,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Título y estado del enrutamiento
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Transfer Funds",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Emerald400)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Instant Fedwire Routing",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Botón de info de límites
                        IconButton(
                            onClick = { showLimitInfoDialog = true },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Transfer Limits",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp)
                        .imePadding()
                ) {
                    // Contenido desplazable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // --- 1. TARJETA DE ORIGEN (VAULT GOLD RESERVE PREVIEW) ---
                        SourceAccountCard(currentBalance = currentBalance)

                        // --- 2. HERO DISPLAY DE MONTO CON ESTILO NEOBANK ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
                                .padding(vertical = 22.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = "AMOUNT TO SEND (USD)",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )

                            // Monto en tipografía masiva con símbolo $
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "$",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasError) Red500 else Emerald400,
                                    modifier = Modifier.padding(end = 6.dp)
                                )

                                BasicTextField(
                                    value = state.amount,
                                    onValueChange = { screenModel.onIntent(TransferIntent.UpdateAmount(it)) },
                                    textStyle = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (hasError) Red500 else Color.White,
                                        textAlign = TextAlign.Start
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    enabled = !state.isLoading,
                                    cursorBrush = SolidColor(Emerald400),
                                    modifier = Modifier.width(IntrinsicSize.Min),
                                    visualTransformation = VisualTransformation { text ->
                                        if (text.text.isEmpty()) {
                                            TransformedText(
                                                text = AnnotatedString("0", spanStyle = SpanStyle(color = Slate400.copy(alpha = 0.5f))),
                                                offsetMapping = object : OffsetMapping {
                                                    override fun originalToTransformed(offset: Int): Int = 1
                                                    override fun transformedToOriginal(offset: Int): Int = 0
                                                }
                                            )
                                        } else {
                                            TransformedText(AnnotatedString(text.text), OffsetMapping.Identity)
                                        }
                                    }
                                )
                            }

                            // Alerta animada en caso de error
                            AnimatedVisibility(
                                visible = hasError,
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Red500.copy(alpha = 0.15f))
                                        .border(1.dp, Red500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isOverdraft) "Insufficient balance in Vault" else "Limit is $5,000.00 per wire",
                                        color = Red500,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Pastillas de montos rápidos (+50, +100, +500, MAX)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                QuickAmountChip(
                                    label = "+$50",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val newTotal = amountNum + 50.0
                                    val cleanTotal = if (newTotal % 1.0 == 0.0) newTotal.toLong().toString() else newTotal.toString()
                                    screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                                }
                                QuickAmountChip(
                                    label = "+$100",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val newTotal = amountNum + 100.0
                                    val cleanTotal = if (newTotal % 1.0 == 0.0) newTotal.toLong().toString() else newTotal.toString()
                                    screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                                }
                                QuickAmountChip(
                                    label = "+$500",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val newTotal = amountNum + 500.0
                                    val cleanTotal = if (newTotal % 1.0 == 0.0) newTotal.toLong().toString() else newTotal.toString()
                                    screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                                }
                                QuickAmountChip(
                                    label = "MAX",
                                    isMax = true,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val maxAllowed = minOf(currentBalance, 5000.00)
                                    val cleanTotal = if (maxAllowed % 1.0 == 0.0) maxAllowed.toLong().toString() else maxAllowed.toString()
                                    screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                                }
                            }
                        }

                        // --- 3. BENEFICIARIOS FRECUENTES (AUTOFILL DE 1 TOQUE) ---
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FREQUENT BENEFICIARIES",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                                Text(
                                    text = "1-Tap Autofill",
                                    fontSize = 11.sp,
                                    color = Emerald400,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                quickPayees.forEach { payee ->
                                    val isSelected = state.recipient == payee.name
                                    QuickPayeeCard(
                                        payee = payee,
                                        isSelected = isSelected,
                                        onClick = {
                                            screenModel.onIntent(TransferIntent.UpdateRecipient(payee.name))
                                            accountNumber = payee.account
                                        }
                                    )
                                }
                            }
                        }

                        // --- 4. FORMULARIO MODERNO DE RECEPTOR (CRISTAL OSCURO PULIDO) ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(24.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "RECIPIENT & SETTLEMENT INFO",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            )

                            // Campo 1: Nombre o Alias
                            ModernGlassInput(
                                value = state.recipient,
                                onValueChange = { screenModel.onIntent(TransferIntent.UpdateRecipient(it)) },
                                label = "Recipient Full Name or Alias",
                                placeholder = "e.g. Stripe Payments or Elena R.",
                                icon = Icons.Default.Person,
                                iconTint = Emerald400,
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Words,
                                enabled = !state.isLoading
                            )

                            // Campo 2: Cuenta o Teléfono con botón "Paste"
                            ModernGlassInput(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                label = "Account, IBAN or Routing Number",
                                placeholder = "e.g. US89-STRP-99201",
                                icon = Icons.Default.AccountBalance,
                                iconTint = Color(0xFF60A5FA),
                                keyboardType = KeyboardType.Text,
                                enabled = !state.isLoading,
                                trailingAction = {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .clickable {
                                                clipboardManager.getText()?.text?.let { clipText ->
                                                    accountNumber = clipText.trim()
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Paste",
                                            color = Emerald400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            )

                            // Campo 3: Mensaje / Referencia con chips sugeridos
                            ModernGlassInput(
                                value = transferMessage,
                                onValueChange = { transferMessage = it },
                                label = "Transfer Memo / Reference (Optional)",
                                placeholder = "e.g. Invoice #2049, Rent, Advisory",
                                icon = Icons.Default.Edit,
                                iconTint = Color(0xFFA78BFA),
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Sentences,
                                enabled = !state.isLoading
                            )

                            // Sugerencias rápidas de referencia
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                listOf("Invoice", "Payroll", "Services", "Settlement", "Personal").forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .clickable { transferMessage = tag }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // --- 5. RESUMEN DE SEGURIDAD Y TARIFAS (FEDWIRE PROTOCOL) ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Transfer Network Fee", color = Slate400, fontSize = 13.sp)
                                }
                                Text("Free (Bóveda VIP)", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = Amber500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Estimated Settlement", color = Slate400, fontSize = 13.sp)
                                }
                                Text("Instant (Fedwire)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Security Standard", color = Slate400, fontSize = 13.sp)
                                }
                                Text("256-bit AES + Offline Sync", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- 6. BOTÓN DE CONFIRMACIÓN ULTRA-PREMIUM (SIN GRIS PLANO) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        val buttonGradient = if (isFormValid) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF059669),
                                    Color(0xFF10B981),
                                    Color(0xFF2DD4BF)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A)
                                )
                            )
                        }

                        val borderStroke = if (isFormValid) {
                            BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.5f))
                        } else {
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        }

                        Surface(
                            onClick = { if (isFormValid) screenModel.executeTransfer() },
                            enabled = isFormValid,
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            border = borderStroke,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(buttonGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Broadcasting Wire...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (isFormValid) Color.White else Slate400.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (amountNum > 0 && !hasError) {
                                                "Confirm Transfer • $${state.amount}"
                                            } else {
                                                "Confirm Transfer"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isFormValid) Color.White else Slate400.copy(alpha = 0.7f)
                                        )
                                        if (isFormValid) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 7. MODAL DE INFORMACIÓN DE LÍMITES ---
            if (showLimitInfoDialog) {
                Dialog(onDismissRequest = { showLimitInfoDialog = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Emerald400, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Transfer Limits", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                                }
                                IconButton(onClick = { showLimitInfoDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                                }
                            }

                            Text(
                                text = "Bóveda Private Banking protects your assets with real-time biometric safeguards and per-transaction limits.",
                                color = Slate400,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Single Wire Maximum", color = Slate400, fontSize = 12.sp)
                                    Text("$5,000.00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Daily Outflow Limit", color = Slate400, fontSize = 12.sp)
                                    Text("$50,000.00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Offline Sync Queue", color = Slate400, fontSize = 12.sp)
                                    Text("Active & Encrypted", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { showLimitInfoDialog = false },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Understood", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // --- 8. OVERLAY DE PROCESAMIENTO BIOMÉTRICO / CRIPTOGRÁFICO ---
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 32.dp, vertical = 28.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Emerald400,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Authorizing Transfer",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Securing transaction in Vault ledger...",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // --- COMPONENTES AUXILIARES PULIDOS ---

    @Composable
    private fun SourceAccountCard(currentBalance: Double) {
        val goldChipGradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFA8A),
                Color(0xFFFFD54F),
                Color(0xFFF59E0B),
                Color(0xFFD97706)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini chip de tarjeta dorada
                Box(
                    modifier = Modifier
                        .size(38.dp, 26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(goldChipGradient)
                        .border(1.dp, Color(0xFFFFFDE7), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF451A03),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vault Gold Reserve",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Emerald500.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = Emerald400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(
                        text = "Source Account •••• 8849",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMoney(currentBalance),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Available Balance",
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun QuickAmountChip(
        label: String,
        isMax: Boolean = false,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val backgroundBrush = if (isMax) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF59E0B).copy(alpha = 0.25f),
                    Color(0xFFD97706).copy(alpha = 0.15f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.03f)
                )
            )
        }

        val borderBrush = if (isMax) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFD54F),
                    Color(0xFFF59E0B)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        }

        Box(
            modifier = modifier
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundBrush)
                .border(1.dp, borderBrush, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isMax) Color(0xFFFFD54F) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }

    @Composable
    private fun QuickPayeeCard(
        payee: QuickPayee,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val borderColor = if (isSelected) Emerald400 else Color.White.copy(alpha = 0.08f)
        val containerBg = if (isSelected) Emerald500.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f)

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(containerBg)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(payee.gradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = payee.initials,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = payee.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = payee.tag,
                    color = if (isSelected) Emerald400 else Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun ModernGlassInput(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        placeholder: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        iconTint: Color,
        keyboardType: KeyboardType = KeyboardType.Text,
        capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
        enabled: Boolean = true,
        trailingAction: (@Composable () -> Unit)? = null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate400
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Slate400.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            capitalization = capitalization
                        ),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(Emerald400),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (trailingAction != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingAction()
                }
            }
        }
    }
}
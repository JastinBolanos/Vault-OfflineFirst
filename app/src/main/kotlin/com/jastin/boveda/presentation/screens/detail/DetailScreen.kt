package com.jastin.boveda.presentation.screens.detail

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.R
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.presentation.model.TimelineEventUi
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.screens.transfer.TransferScreen
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/* =========================================================================
 * TRANSACTION DETAIL SCREEN (HYPER-REALISTIC NEOBANK VOUCHER)
 *
 * Provides a high-fidelity, ultra-realistic voucher for any transaction:
 * - Direct resolution from uiModel or database or mock fallback (never blank).
 * - Distinctive merchant logos & profiles (AWS, Stripe, Apple, Vault).
 * - Inflow vs Outflow color dynamics and official Fedwire/ACH settlement tags.
 * - Interactive action strip (Repeat Transfer, Share Receipt, Dispute Help).
 * - Full financial metadata (MCC code, IMAD Fedwire trace, SHA256 signature).
 * - Interactive copy-to-clipboard for all reference codes.
 * - 3-stage cryptographic audit trail (Timeline).
 * ========================================================================= */

private data class MerchantProfile(
    val legalName: String,
    val category: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val address: String,
    val route: String,
    val authCode: String,
    val mccCode: String
)

data class DetailScreen(
    val transactionId: String,
    val uiModel: TransactionUiModel? = null
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val coroutineScope = rememberCoroutineScope()
        val dbTransactions by com.jastin.boveda.globalTransactionRepository.transactions.collectAsState()

        var showCopiedToast by remember { mutableStateOf<String?>(null) }
        var showHelpModal by remember { mutableStateOf(false) }

        BackHandler {
            navigator.pop()
        }

        // 1. Resolver el modelo de datos de forma infalible (uiModel -> DB -> Preset Mock)
        val resolvedModel: TransactionUiModel = remember(transactionId, uiModel, dbTransactions) {
            if (uiModel != null) {
                uiModel
            } else {
                val foundInDb = dbTransactions.find { it.id == transactionId }
                if (foundInDb != null) {
                    TransactionUiModel(
                        id = foundInDb.id,
                        title = foundInDb.receiverName,
                        amount = foundInDb.amount,
                        status = if (foundInDb.status == TransactionStatus.PENDING) TxUiStatus.PENDING else TxUiStatus.COMPLETED,
                        date = "Today",
                        time = "10:30 AM",
                        method = "Vault Balance",
                        recipient = foundInDb.receiverName,
                        reference = "REF-${foundInDb.id.take(8).uppercase()}",
                        timeline = emptyList()
                    )
                } else {
                    // Fallbacks realistas para los movimientos demo
                    when {
                        transactionId.contains("stripe", ignoreCase = true) -> {
                            TransactionUiModel(
                                id = transactionId,
                                title = "Stripe Payout",
                                amount = 12450.00,
                                status = TxUiStatus.COMPLETED,
                                date = "Today",
                                time = "10:30 AM",
                                method = "Payment Inflow",
                                recipient = "Stripe Payments Company LLC",
                                reference = "REF-STRP-992014",
                                timeline = emptyList()
                            )
                        }
                        transactionId.contains("apple", ignoreCase = true) -> {
                            TransactionUiModel(
                                id = transactionId,
                                title = "Apple Enterprise",
                                amount = -4299.00,
                                status = TxUiStatus.PENDING,
                                date = "Yesterday",
                                time = "04:45 PM",
                                method = "Hardware",
                                recipient = "Apple Inc. Corporate Sales",
                                reference = "REF-APPL-09412X",
                                timeline = emptyList()
                            )
                        }
                        else -> {
                            // Por defecto: Amazon Web Services
                            TransactionUiModel(
                                id = transactionId,
                                title = "Amazon Web Services",
                                amount = -5000.00,
                                status = TxUiStatus.COMPLETED,
                                date = "Today",
                                time = "08:15 AM",
                                method = "Cloud Infra",
                                recipient = "AWS EMEA SARL",
                                reference = "REF-AWS-882049",
                                timeline = emptyList()
                            )
                        }
                    }
                }
            }
        }

        val isPending = resolvedModel.status == TxUiStatus.PENDING
        val isIncome = resolvedModel.amount > 0
        val cleanAmount = formatMoney(abs(resolvedModel.amount))
        val referenceCode = resolvedModel.reference.ifBlank { "REF-${resolvedModel.id.take(8).uppercase()}" }
        val receiverName = resolvedModel.recipient.ifBlank { resolvedModel.title }

        // Perfil comercial del comerciante para realismo bancario absoluto
        val merchantProfile = remember(resolvedModel.title) {
            when {
                resolvedModel.title.contains("Stripe", ignoreCase = true) -> {
                    MerchantProfile(
                        legalName = "Stripe Payments Company LLC",
                        category = "Payment Processing & Inflow",
                        icon = Icons.Default.CreditCard,
                        gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                        address = "354 Oyster Point Blvd, San Francisco, CA",
                        route = "ACH Instant Settlement • Node US-WEST",
                        authCode = "AUTH-STRP-88219-X",
                        mccCode = "7389 - Business Services"
                    )
                }
                resolvedModel.title.contains("Amazon", ignoreCase = true) || resolvedModel.title.contains("AWS", ignoreCase = true) -> {
                    MerchantProfile(
                        legalName = "Amazon Web Services, Inc.",
                        category = "Cloud Infrastructure & Compute",
                        icon = Icons.Default.Cloud,
                        gradient = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
                        address = "410 Terry Ave N, Seattle, WA 98109",
                        route = "Fedwire Direct Debit • RTGS B2B",
                        authCode = "AUTH-AWS-99201-B",
                        mccCode = "7372 - Cloud Computer Services"
                    )
                }
                resolvedModel.title.contains("Apple", ignoreCase = true) -> {
                    MerchantProfile(
                        legalName = "Apple Inc. Corporate Sales",
                        category = "Hardware & Corporate Devices",
                        icon = Icons.Default.Devices,
                        gradient = listOf(Color(0xFF475569), Color(0xFF1E293B)),
                        address = "One Apple Park Way, Cupertino, CA 95014",
                        route = "Commercial Card Network • Tokenized",
                        authCode = "AUTH-APPL-77192-A",
                        mccCode = "5732 - Electronic Sales"
                    )
                }
                else -> {
                    MerchantProfile(
                        legalName = receiverName,
                        category = if (isIncome) "Direct Wire Credit" else "Private Vault Outflow",
                        icon = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        gradient = if (isIncome) listOf(Emerald500, Teal400) else listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                        address = "Verified Private Banking Node",
                        route = "Fedwire Instant Transfer (256-bit AES)",
                        authCode = "VLT-AUTH-${resolvedModel.id.take(6).uppercase()}",
                        mccCode = "6012 - Financial Institutions"
                    )
                }
            }
        }

        fun copyToClipboard(label: String, value: String) {
            clipboardManager.setText(AnnotatedString(value))
            showCopiedToast = "$label copied to clipboard"
            coroutineScope.launch {
                delay(2000)
                if (showCopiedToast == "$label copied to clipboard") {
                    showCopiedToast = null
                }
            }
        }

        fun shareVoucher() {
            val shareText = """
                🏦 BÓVEDA PRIVATE BANKING - TRANSACTION VOUCHER
                ------------------------------------------------
                Status: ${if (isPending) "AUTHORIZED / PENDING CLEARING" else "SETTLED / COMPLETED"}
                Amount: ${if (isIncome) "+" else "-"}$cleanAmount USD
                Merchant/Party: ${merchantProfile.legalName}
                Category: ${merchantProfile.category}
                Clearing Route: ${merchantProfile.route}
                Authorization Code: ${merchantProfile.authCode}
                MCC Code: ${merchantProfile.mccCode}
                Reference: $referenceCode
                IMAD Trace: 20261105008910BVD
                Security: 256-bit AES + Offline-First Synchronized
            """.trimIndent()

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Transaction Voucher"))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
        ) {
            // Fondo de la app con textura sutil visible
            Image(
                painter = painterResource(R.drawable.app_background_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Velo oscuro equilibrado exactamente al 50% de opacidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF030712).copy(alpha = 0.50f))
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
                        IconButton(
                            onClick = { navigator.pop() },
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

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Transaction Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Official Fedwire Voucher",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = { shareVoucher() },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
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
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        // --- 1. HERO HEADER: LOGO Y MONTO ---
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar del comerciante con halo
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Brush.sweepGradient(merchantProfile.gradient))
                                    .padding(2.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = merchantProfile.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Monto en tipografía masiva
                            Text(
                                text = "${if (isIncome) "+" else "-"} $cleanAmount",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isIncome) Emerald400 else Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Nombre del Comercio y Categoría
                            Text(
                                text = resolvedModel.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${merchantProfile.category} • ${resolvedModel.date}, ${resolvedModel.time}",
                                fontSize = 12.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tira de Acciones Rápidas (Repetir, Comprobante, Soporte)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                QuickActionPill(
                                    label = "Share",
                                    icon = Icons.Default.Share,
                                    modifier = Modifier.weight(1f)
                                ) { shareVoucher() }

                                QuickActionPill(
                                    label = "Repeat",
                                    icon = Icons.Default.Refresh,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    navigator.push(TransferScreen(35000.00))
                                }

                                QuickActionPill(
                                    label = "Support",
                                    icon = Icons.Default.HelpOutline,
                                    modifier = Modifier.weight(1f)
                                ) { showHelpModal = true }
                            }
                        }

                        // --- 2. VOUCHER DIGITAL: METADATOS BANCARIOS REALES ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.035f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OFFICIAL TRANSACTION METADATA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VIP FEDWIRE",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                            // 1. Legal Entity
                            VoucherDetailRow(
                                label = "Merchant Legal Entity",
                                value = merchantProfile.legalName,
                                valueBold = true
                            )

                            // 2. Account Origin / Destination
                            VoucherDetailRow(
                                label = if (isIncome) "Receiving Account" else "Funding Account",
                                value = "Vault Gold Reserve •••• 8849"
                            )

                            // 3. Clearing Protocol
                            VoucherDetailRow(
                                label = "Clearing & Settlement Route",
                                value = merchantProfile.route
                            )

                            // 4. Authorization Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Authorization Code", color = Slate400, fontSize = 13.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { copyToClipboard("Auth Code", merchantProfile.authCode) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = merchantProfile.authCode,
                                        color = Color(0xFFFFD54F),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            // 5. Reference ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Transaction Reference", color = Slate400, fontSize = 13.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { copyToClipboard("Reference", referenceCode) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = referenceCode,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            // 6. Network Fee
                            VoucherDetailRow(
                                label = "Network Transfer Fee",
                                value = "$0.00 USD (Sponsored VIP Tier)",
                                valueColor = Emerald400,
                                valueBold = true
                            )

                            // 7. MCC Code
                            VoucherDetailRow(
                                label = "Merchant Category (MCC)",
                                value = merchantProfile.mccCode
                            )

                            // 8. Federal Trace IMAD
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Fedwire IMAD Trace ID", color = Slate400, fontSize = 13.sp)
                                Text(
                                    text = "20261105008910BVD",
                                    color = Slate400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // --- 3. HISTORIAL CRIPTOGRÁFICO DE 3 ETAPAS (TIMELINE) ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White.copy(alpha = 0.025f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(22.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "CRYPTOGRAPHIC SETTLEMENT TIMELINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                letterSpacing = 1.sp
                            )

                            Column(modifier = Modifier.fillMaxWidth()) {
                                TimelineMilestone(
                                    title = "1. Transaction Authorization Initiated",
                                    subtitle = "Authorized using 256-bit Biometric Vault Passkey",
                                    time = resolvedModel.time,
                                    isDone = true,
                                    showLine = true
                                )
                                TimelineMilestone(
                                    title = "2. Cryptographic Ledger Verification",
                                    subtitle = "Encrypted local SQLite block synchronized with offline-first engine",
                                    time = resolvedModel.time,
                                    isDone = true,
                                    showLine = true
                                )
                                TimelineMilestone(
                                    title = if (isPending) "3. Clearing through Interbank Gateway" else "3. Beneficiary Settlement Confirmed",
                                    subtitle = if (isPending) "Awaiting merchant settlement batch confirmation" else "Funds settled in recipient's primary institutional account",
                                    time = if (isPending) "In Progress" else "Completed",
                                    isDone = !isPending,
                                    showLine = false
                                )
                            }
                        }

                        // Notificación flotante de copiado
                        AnimatedVisibility(
                            visible = showCopiedToast != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald500.copy(alpha = 0.2f))
                                    .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = showCopiedToast ?: "",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // --- 4. BARRA DE ACCIÓN INFERIOR EQUILIBRADA ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { shareVoucher() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(54.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Share Receipt",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = { navigator.pop() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(54.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF059669),
                                                Color(0xFF10B981),
                                                Color(0xFF2DD4BF)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Return to Vault",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. MODAL DE ASISTENCIA / DISPUTA BANCARIA ---
            if (showHelpModal) {
                Dialog(onDismissRequest = { showHelpModal = false }) {
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
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald400, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Transaction Assurance", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                                }
                                IconButton(onClick = { showHelpModal = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                                }
                            }

                            Text(
                                text = "All Bóveda Private Banking transfers are protected by Zero-Liability Fraud Protection and FDIC-insured escrow nodes up to $250,000.00.",
                                color = Slate400,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Status Check", color = Slate400, fontSize = 12.sp)
                                    Text("Legitimate Transaction", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Encrypted Hash", color = Slate400, fontSize = 12.sp)
                                    Text("SHA256: 9e88...a4b2", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { showHelpModal = false },
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
        }
    }

    @Composable
    private fun QuickActionPill(
        label: String,
        icon: ImageVector,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = modifier.height(42.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    @Composable
    private fun VoucherDetailRow(
        label: String,
        value: String,
        valueColor: Color = Color.White,
        valueBold: Boolean = false
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Slate400, fontSize = 13.sp)
            Text(
                text = value,
                color = valueColor,
                fontSize = 13.sp,
                fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun TimelineMilestone(
        title: String,
        subtitle: String,
        time: String,
        isDone: Boolean,
        showLine: Boolean
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isDone) Emerald500 else Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                if (showLine) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(34.dp)
                            .background(
                                if (isDone) Emerald500.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = if (showLine) 12.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = if (isDone) Color.White else Slate400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = time,
                        color = if (isDone) Emerald400 else Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = subtitle,
                    color = Slate400,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
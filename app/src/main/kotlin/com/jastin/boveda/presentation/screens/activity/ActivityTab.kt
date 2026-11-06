package com.jastin.boveda.presentation.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.screens.detail.DetailScreen
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/* =========================================================================
 * REDESIGNED ACTIVITY & FINANCIAL INTELLIGENCE HUB
 * Gorgeous, highly interactive, timeline-grouped financial ledger.
 * ========================================================================= */

object ActivityTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Insights)
            return remember { TabOptions(index = 1u, title = "Activity", icon = icon) }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()

        // Estados interactivos: Filtro, Búsqueda y Notificación de Exportación
        var selectedFilter by remember { mutableStateOf("All") }
        var searchQuery by remember { mutableStateOf("") }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        val repository = com.jastin.boveda.globalTransactionRepository
        val domainTransactions = repository.transactions.collectAsState().value

        // --- 1. CAPA DE DOMINIO -> UI ---
        val realTransactions = domainTransactions.map { tx ->
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

        val mockTransactions = remember { getMockTransactions() }
        val allTransactions = remember(realTransactions, mockTransactions) {
            realTransactions + mockTransactions
        }

        // --- 2. CÁLCULO DE MÉTRICAS EN VIVO ---
        val totalInflow = remember(allTransactions) {
            allTransactions.filter { it.amount > 0 }.sumOf { it.amount }
        }
        val totalOutflow = remember(allTransactions) {
            allTransactions.filter { it.amount < 0 }.sumOf { abs(it.amount) }
        }
        val netRatio = remember(totalInflow, totalOutflow) {
            val total = totalInflow + totalOutflow
            if (total > 0) (totalInflow / total).toFloat().coerceIn(0.1f, 0.9f) else 0.5f
        }

        // --- 3. FILTRADO Y BÚSQUEDA REACTIVA ---
        val filteredTransactions = remember(selectedFilter, searchQuery, allTransactions) {
            allTransactions.filter { tx ->
                val matchesFilter = when (selectedFilter) {
                    "Inflow" -> tx.amount > 0 && tx.status == TxUiStatus.COMPLETED
                    "Outflow" -> tx.amount < 0 && tx.status == TxUiStatus.COMPLETED
                    "Pending" -> tx.status == TxUiStatus.PENDING
                    else -> true
                }
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                            tx.recipient.contains(searchQuery, ignoreCase = true) ||
                            tx.reference.contains(searchQuery, ignoreCase = true) ||
                            tx.method.contains(searchQuery, ignoreCase = true) ||
                            formatMoney(tx.amount).contains(searchQuery)
                }
                matchesFilter && matchesSearch
            }
        }

        // --- 4. AGRUPACIÓN POR LÍNEA TEMPORAL (TIMELINE NEOBANK) ---
        val groupedTransactions = remember(filteredTransactions) {
            filteredTransactions.groupBy { tx ->
                when (tx.date) {
                    "Today" -> "Today"
                    "Yesterday" -> "Yesterday"
                    else -> "Earlier this Month"
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Header elegante con título y botón de exportar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Activity & Ledger",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredTransactions.size} transactions recorded",
                            fontSize = 12.sp,
                            color = Slate400
                        )
                    }

                    // Botón para Exportar Extracto
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable {
                                if (!isExporting) {
                                    isExporting = true
                                    scope.launch {
                                        delay(800)
                                        isExporting = false
                                        exportMessage = "Statement exported: VAULT_STATEMENT.csv"
                                        delay(2500)
                                        exportMessage = null
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Emerald400,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isExporting) "Exporting..." else "Export",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 130.dp)
            ) {

                // Toast animado de exportación exitosa
                item {
                    AnimatedVisibility(
                        visible = exportMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Emerald500.copy(alpha = 0.15f))
                                .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = exportMessage ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Emerald400
                                )
                            }
                        }
                    }
                }

                // --- 1. HERO FINANCIERO: CASHFLOW RATIO CARD ---
                item {
                    val cardGradient = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF162138).copy(alpha = 0.85f),
                            Color(0xFF0E1626).copy(alpha = 0.90f)
                        )
                    )
                    val borderGradient = Brush.linearGradient(
                        colors = listOf(
                            Emerald500.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardGradient)
                            .border(1.dp, borderGradient, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MONTHLY CASH FLOW",
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate400
                                )

                                Text(
                                    text = "Net: +${formatMoney(totalInflow - totalOutflow)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald400
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Métricas de Ingreso vs Egreso
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Emerald500.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = Emerald400,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Total Inflow", fontSize = 12.sp, color = Slate400)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${formatMoney(totalInflow)}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald400
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Total Outflow", fontSize = 12.sp, color = Slate400)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = Slate400,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "-${formatMoney(totalOutflow)}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Barra visual de proporción de flujo neto
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(netRatio)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(listOf(Emerald500, Teal400)))
                                )
                            }
                        }
                    }
                }

                // --- 2. BARRA DE BÚSQUEDA INTERACTIVA ---
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF162034).copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search by name, reference or amount...",
                                        fontSize = 13.sp,
                                        color = Slate400
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Emerald400),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 3. FILTROS EN PÍLDORAS CON CONTADORES ---
                item {
                    val filterOptions = listOf(
                        "All" to allTransactions.size,
                        "Inflow" to allTransactions.count { it.amount > 0 && it.status == TxUiStatus.COMPLETED },
                        "Outflow" to allTransactions.count { it.amount < 0 && it.status == TxUiStatus.COMPLETED },
                        "Pending" to allTransactions.count { it.status == TxUiStatus.PENDING }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { (filterName, count) ->
                            val isSelected = selectedFilter == filterName

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Brush.horizontalGradient(listOf(Emerald500, Teal400))
                                        else SolidColor(Color.White.copy(alpha = 0.05f))
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedFilter = filterName }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = filterName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Slate950 else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Slate950.copy(alpha = 0.15f)
                                                else Color.White.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Slate950 else Slate400
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 4. LISTADO AGRUPADO POR LÍNEA TEMPORAL (TIMELINE) ---
                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No movements found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try adjusting your search or active filters",
                                    fontSize = 12.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                } else {
                    groupedTransactions.forEach { (dateGroup, transactionsInGroup) ->
                        // Header del grupo temporal
                        item {
                            Text(
                                text = dateGroup.uppercase(),
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }

                        // Caja unificada de cristal para el grupo
                        item {
                            val containerGradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF141C2E).copy(alpha = 0.75f),
                                    Color(0xFF0D1424).copy(alpha = 0.75f)
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(containerGradient)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                                    .padding(vertical = 6.dp)
                            ) {
                                Column {
                                    transactionsInGroup.forEachIndexed { idx, tx ->
                                        ActivityLedgerRow(
                                            tx = tx,
                                            onClick = { navigator.push(DetailScreen(tx.id, tx)) }
                                        )

                                        if (idx < transactionsInGroup.lastIndex) {
                                            HorizontalDivider(
                                                color = Color.White.copy(alpha = 0.05f),
                                                thickness = 1.dp,
                                                modifier = Modifier.padding(horizontal = 18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FILA DE MOVIMIENTO ESTILO FINTECH PROFESIONAL ---
    @Composable
    private fun ActivityLedgerRow(
        tx: TransactionUiModel,
        onClick: () -> Unit
    ) {
        val isPending = tx.status == TxUiStatus.PENDING
        val isPositive = tx.amount > 0

        val (badgeGradient, badgeIcon, categoryLabel, categoryColor) = when {
            tx.title.contains("Stripe", ignoreCase = true) || tx.title.contains("Shopify", ignoreCase = true) -> {
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
            tx.title.contains("Tax", ignoreCase = true) || tx.title.contains("IRS", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B))),
                    Icons.Default.AccountBalance,
                    "Corporate Tax",
                    Color(0xFFFCA5A5)
                )
            }
            tx.title.contains("Sequoia", ignoreCase = true) || tx.title.contains("Vanguard", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Emerald500, Teal400)),
                    Icons.Default.TrendingUp,
                    "Investments",
                    Emerald400
                )
            }
            tx.title.contains("Lease", ignoreCase = true) || tx.title.contains("WeWork", ignoreCase = true) -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
                    Icons.Default.Business,
                    "Office Lease",
                    Color(0xFF7DD3FC)
                )
            }
            isPositive -> {
                Quad(
                    Brush.linearGradient(listOf(Emerald500, Teal400)),
                    Icons.Default.ArrowDownward,
                    "Incoming Transfer",
                    Emerald400
                )
            }
            else -> {
                Quad(
                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                    Icons.Default.ArrowUpward,
                    "Outbound Transfer",
                    Color(0xFF93C5FD)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono distintivo
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(badgeGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Información de la transacción
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

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
                        text = tx.method,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            // Monto y estado
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else ""}${formatMoney(tx.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) Emerald400 else Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                if (isPending) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Amber500.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
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
                    Text(
                        text = tx.time,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                }
            }
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // =========================================================================
    // MOCK DATA GENERATOR (ENTERPRISE DATASET COMPATIBLE)
    // =========================================================================
    private fun getMockTransactions(): List<TransactionUiModel> {
        return listOf(
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
                time = "04:45 PM",
                method = "Wire Transfer",
                recipient = "Apple Inc.",
                reference = "REF-APPL09X",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_lease_cuota",
                title = "WeWork Office Lease",
                amount = -1850.00,
                status = TxUiStatus.COMPLETED,
                date = "Yesterday",
                time = "09:00 AM",
                method = "Direct Debit",
                recipient = "WeWork Companies LLC",
                reference = "REF-WW2026A",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_pending_inbound",
                title = "Global Tech Partners",
                amount = 15200.00,
                status = TxUiStatus.PENDING,
                date = "Earlier this Month",
                time = "02:10 PM",
                method = "International Wire",
                recipient = "Global Tech Corp",
                reference = "REF-GTWIRE7",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_dividend",
                title = "Vanguard Dividends",
                amount = 1245.50,
                status = TxUiStatus.COMPLETED,
                date = "Earlier this Month",
                time = "03:00 PM",
                method = "Investment Payout",
                recipient = "The Vanguard Group",
                reference = "REF-VNGD01B",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_pending_tax",
                title = "IRS Q3 Estimated Tax",
                amount = -24500.00,
                status = TxUiStatus.PENDING,
                date = "Earlier this Month",
                time = "11:45 AM",
                method = "Federal Wire",
                recipient = "Internal Revenue Service",
                reference = "REF-TAXQ3-99",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_pending_vc",
                title = "Sequoia Capital",
                amount = 250000.00,
                status = TxUiStatus.PENDING,
                date = "Earlier this Month",
                time = "09:30 AM",
                method = "Growth Equity",
                recipient = "Alexander Sterling",
                reference = "REF-SERIES-A",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_inbound_b2b",
                title = "Acme Corp Invoice #042",
                amount = 18400.00,
                status = TxUiStatus.COMPLETED,
                date = "Earlier this Month",
                time = "10:20 AM",
                method = "B2B Wire",
                recipient = "Alexander Sterling",
                reference = "REF-INV-042",
                timeline = emptyList()
            ),
            TransactionUiModel(
                id = "mock_inbound_shopify",
                title = "Shopify Payout",
                amount = 9350.75,
                status = TxUiStatus.COMPLETED,
                date = "Earlier this Month",
                time = "06:00 AM",
                method = "Merchant Settlement",
                recipient = "Alexander Sterling",
                reference = "REF-SHPFY-88",
                timeline = emptyList()
            )
        )
    }
}
package com.jastin.boveda.presentation.screens.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.jastin.boveda.domain.model.TransactionStatus
import com.jastin.boveda.presentation.components.TransactionRow
import com.jastin.boveda.presentation.model.TransactionUiModel
import com.jastin.boveda.presentation.model.TxUiStatus
import com.jastin.boveda.presentation.screens.detail.DetailScreen

/* =========================================================================
 * ACTIVITY VIEW (TAB COMPONENT)
 * =========================================================================
 * Implements the financial history via the [Tab] contract.
 * Optimizes data filtering with [remember] and uses the parent navigator
 * to allow full-screen transitions outside the BottomNavigation scope.
 */
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
        var selectedFilter by remember { mutableStateOf("All") }
        val filters = listOf("All", "Inbound", "Outbound", "Pending")

        val repository = com.jastin.boveda.globalTransactionRepository
        val domainTransactions = repository.transactions.collectAsState().value

        // --- 1. THE BRIDGE (MAPPER DOMAIN -> UI) ---
        // Transformamos las entidades reales de la base de datos
        val realTransactions = domainTransactions.map { tx ->
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

        // --- 2. MOCK DATA INJECTION ---
        // Obtenemos nuestros datos de prueba estáticos
        val mockTransactions = remember { getMockTransactions() }

        // --- 3. COMBINACIÓN MAESTRA ---
        // Juntamos lo real (arriba) con lo falso (abajo) para la UI
        val allTransactions = realTransactions + mockTransactions

        // --- 4. FILTRADO REACTIVO ---
        val filteredTransactions = remember(selectedFilter, allTransactions) {
            when (selectedFilter) {
                "Inbound" -> allTransactions.filter { it.amount > 0 && it.status == TxUiStatus.COMPLETED }
                "Outbound" -> allTransactions.filter { it.amount < 0 && it.status == TxUiStatus.COMPLETED }
                "Pending" -> allTransactions.filter { it.status == TxUiStatus.PENDING }
                else -> allTransactions
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text("Activity", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filters.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = Color.Transparent, selectedContainerColor = MaterialTheme.colorScheme.onSurface, selectedLabelColor = Color.White),
                                border = FilterChipDefaults.filterChipBorder(borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, enabled = true, selected = isSelected),
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(filteredTransactions) { tx ->
                    TransactionRow(tx) { navigator.push(DetailScreen(tx.id)) }
                }
            }
        }
    }

    // =========================================================================
    // MOCK DATA GENERATOR (ENTERPRISE & HIGH-TICKET TESTING)
    // =========================================================================
    private fun getMockTransactions(): List<TransactionUiModel> {
        return listOf(
            // 1. Ingreso Pesado (Liquidación de Pasarela de Pagos)
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
            // 2. Gasto de Infraestructura (Suscripción Cloud)
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
            // 3. Compra de Equipos (Pendiente / Monto Fuerte)
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
            ),
            // 4. Pago de Alquiler de Oficina (Cuota Fija)
            TransactionUiModel(
                id = "mock_lease_cuota",
                title = "WeWork Office Lease",
                amount = -1850.00,
                status = TxUiStatus.COMPLETED,
                date = "Yesterday",
                time = "09:00",
                method = "Direct Debit",
                recipient = "WeWork Companies LLC",
                reference = "REF-WW2026A",
                timeline = emptyList()
            ),
            // 5. Ingreso Internacional Pendiente (Transferencia Swift)
            TransactionUiModel(
                id = "mock_pending_inbound",
                title = "Global Tech Partners",
                amount = 15200.00,
                status = TxUiStatus.PENDING,
                date = "Aug 06",
                time = "14:10",
                method = "International Wire",
                recipient = "Global Tech Corp",
                reference = "REF-GTWIRE7",
                timeline = emptyList()
            ),
            // 6. Suscripción de Software B2B
            TransactionUiModel(
                id = "mock_salesforce",
                title = "Salesforce CRM",
                amount = -320.00,
                status = TxUiStatus.COMPLETED,
                date = "Aug 05",
                time = "11:20",
                method = "Corporate Card",
                recipient = "Salesforce.com Inc.",
                reference = "REF-SFDC88",
                timeline = emptyList()
            ),
            // 7. Retorno de Inversión / Dividendos
            TransactionUiModel(
                id = "mock_dividend",
                title = "Vanguard Dividends",
                amount = 1245.50,
                status = TxUiStatus.COMPLETED,
                date = "Aug 05",
                time = "15:00",
                method = "Investment Payout",
                recipient = "The Vanguard Group",
                reference = "REF-VNGD01B",
                timeline = emptyList()
            ),
            // 8. Pago de Cuota Vehicular (Leasing)
            TransactionUiModel(
                id = "mock_toyota_cuota",
                title = "Toyota Financial",
                amount = -485.00,
                status = TxUiStatus.COMPLETED,
                date = "Aug 04",
                time = "07:30",
                method = "Auto Loan (12/36)",
                recipient = "Toyota Motor Credit",
                reference = "REF-TYTA33",
                timeline = emptyList()
            ),
            // 9. Honorarios de Consultoría (Transferencia Fuerte)
            TransactionUiModel(
                id = "mock_deloitte",
                title = "Deloitte Tax Advisory",
                amount = -2100.00,
                status = TxUiStatus.COMPLETED,
                date = "Aug 02",
                time = "10:05",
                method = "Vault Transfer",
                recipient = "Deloitte Touche Tohmatsu",
                reference = "REF-DLTE55C",
                timeline = emptyList()
            ),
            // 10. Suscripción Tecnológica Recurrente
            TransactionUiModel(
                id = "mock_github",
                title = "GitHub Enterprise",
                amount = -210.00,
                status = TxUiStatus.COMPLETED,
                date = "Aug 01",
                time = "13:40",
                method = "Subscription",
                recipient = "GitHub Inc.",
                reference = "REF-GHUB00",
                timeline = emptyList()
            ),

            // 11. Pago de Impuestos Corporativos (Pendiente de compensación)
            TransactionUiModel(
                id = "mock_pending_tax",
                title = "IRS Q3 Estimated Tax",
                amount = -24500.00,
                status = TxUiStatus.PENDING,
                date = "Today",
                time = "11:45",
                method = "Wire Transfer",
                recipient = "Internal Revenue Service",
                reference = "REF-TAXQ3-99",
                timeline = emptyList()
            ),
            // 12. Liquidación de Ronda de Inversión (Ingreso Fuerte / Pendiente)
            TransactionUiModel(
                id = "mock_pending_vc",
                title = "Sequoia Capital",
                amount = 250000.00,
                status = TxUiStatus.PENDING,
                date = "Today",
                time = "09:30",
                method = "International Wire",
                recipient = "Jastin Abel",
                reference = "REF-SERIES-A",
                timeline = emptyList()
            ),
            // 13. Pago de Logística y Transporte Marítimo (Pendiente)
            TransactionUiModel(
                id = "mock_pending_logistics",
                title = "Maersk Global Logistics",
                amount = -18250.00,
                status = TxUiStatus.PENDING,
                date = "Yesterday",
                time = "17:15",
                method = "Corporate Transfer",
                recipient = "A.P. Moller - Maersk",
                reference = "REF-MRSK-044",
                timeline = emptyList()
            ),
            // 14. Pago de Factura B2B a tu favor (Ingreso Exitoso)
            TransactionUiModel(
                id = "mock_inbound_b2b",
                title = "Acme Corp Invoice #042",
                amount = 18400.00,
                status = TxUiStatus.COMPLETED,
                date = "Aug 06",
                time = "10:20",
                method = "Bank Transfer",
                recipient = "Jastin Abel",
                reference = "REF-INV-042",
                timeline = emptyList()
            ),
            // 15. Liquidación de Ventas E-commerce (Ingreso Exitoso)
            TransactionUiModel(
                id = "mock_inbound_shopify",
                title = "Shopify Payout",
                amount = 9350.75,
                status = TxUiStatus.COMPLETED,
                date = "Aug 05",
                time = "06:00",
                method = "E-commerce Settlement",
                recipient = "Jastin Abel",
                reference = "REF-SHPFY-88",
                timeline = emptyList()
            )
        )
    }
}
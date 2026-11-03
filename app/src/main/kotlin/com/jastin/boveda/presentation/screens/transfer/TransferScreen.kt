package com.jastin.boveda.presentation.screens.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.presentation.components.BovedaButton
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.formatMoney

/* =========================================================================
 * TRANSFER FORM (VISIONARY FINTECH SCREEN)
 * ========================================================================= */
data class TransferScreen(val currentBalance: Double) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { TransferScreenModel() }
        val state by screenModel.state.collectAsState()
        var accountNumber by remember { mutableStateOf("") }
        var transferMessage by remember { mutableStateOf("") }
        val amountNum = state.amount.toDoubleOrNull() ?: 0.0
        val isOverdraft = amountNum > currentBalance
        val exceedsLimit = amountNum > 5000.00
        val hasError = isOverdraft || exceedsLimit
        val hasTrailingDot = state.amount.endsWith(".")

        val errorColor = Red500

        LaunchedEffect(state.successTransactionId) {
            state.successTransactionId?.let { txId ->
                screenModel.onIntent(TransferIntent.ClearNavigation)
                navigator.replace(com.jastin.boveda.presentation.screens.detail.DetailScreen(txId))
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }, enabled = !state.isLoading) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = "Send Money",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                containerColor = Color.Transparent
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .fillMaxSize()
                        .imePadding()
                ) {
                    // --- 1. CONTEXTO DE ORIGEN (BALANCE PILL) ---
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Main Vault • ", color = Slate400, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = formatMoney(currentBalance),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- 2. INPUT DE MONTO DINÁMICO ---
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("AMOUNT TO SEND", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Bold)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth()
                        ) {
                            // EL SÍMBOLO DE DÓLAR ($) EN BLANCO SÓLIDO (SIN ANIMACIÓN)
                            Text(
                                text = "$",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light,
                                color = if (hasError) errorColor else Color.White,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            // CAMPO DE TEXTO DEL NÚMERO
                            BasicTextField(
                                value = state.amount,
                                onValueChange = { screenModel.onIntent(TransferIntent.UpdateAmount(it)) },

                                textStyle = TextStyle(
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (hasError) errorColor else Color.White
                                ),

                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                enabled = !state.isLoading,
                                cursorBrush = SolidColor(Emerald500),
                                modifier = Modifier.width(IntrinsicSize.Min),

                                visualTransformation = VisualTransformation { text ->
                                    if (text.text.isEmpty()) {
                                        TransformedText(
                                            text = AnnotatedString("0", spanStyle = SpanStyle(color = Color.White)),
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

                        // --- 3. BOTONES DE MONTOS RÁPIDOS ---
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            QuickAmountChip("+ $50") {
                                val newTotal = amountNum + 50.0
                                val cleanTotal = if (newTotal % 1.0 == 0.0) newTotal.toLong().toString() else newTotal.toString()
                                screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                            }
                            QuickAmountChip("+ $100") {
                                val newTotal = amountNum + 100.0
                                val cleanTotal = if (newTotal % 1.0 == 0.0) newTotal.toLong().toString() else newTotal.toString()
                                screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                            }
                            QuickAmountChip("MAX", isAccent = true) {
                                val cleanTotal = if (currentBalance % 1.0 == 0.0) currentBalance.toLong().toString() else currentBalance.toString()
                                screenModel.onIntent(TransferIntent.UpdateAmount(cleanTotal))
                            }
                        }

                        if (isOverdraft) {
                            Text("Insufficient balance", color = Red500, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        } else if (exceedsLimit) {
                            Text("The maximum transfer limit is $5000.00", color = Red500, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- 4 Y 5. METADATOS Y RESUMEN UNIFICADOS (SCROLLABLE) ---
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                        Text("RECIPIENT DETAILS", fontSize = 12.sp, color = Slate400, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                        OutlinedTextField(
                            value = state.recipient,
                            onValueChange = { screenModel.onIntent(TransferIntent.UpdateRecipient(it)) },
                            label = { Text("Full Name or Alias") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !state.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words),
                            colors = getGlassmorphismTextFieldColors()
                        )

                        OutlinedTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            label = { Text("Account or Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !state.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = getGlassmorphismTextFieldColors()
                        )

                        OutlinedTextField(
                            value = transferMessage,
                            onValueChange = { transferMessage = it },
                            label = { Text("Message (Optional)") },
                            placeholder = { Text("e.g. Dinner, Rent...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            enabled = !state.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Sentences),
                            colors = getGlassmorphismTextFieldColors()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Network Fee", color = Slate400, fontSize = 14.sp)
                                Text("Free", color = Emerald500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimated Arrival", color = Slate400, fontSize = 14.sp)
                                Text("Instant", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- EXECUTION ACTION (SUBMIT FIJO) ---
                    BovedaButton(
                        text = "Confirm Transfer",
                        enabled = amountNum > 0 && !hasError && !hasTrailingDot && state.recipient.isNotBlank() && accountNumber.isNotBlank() && !state.isLoading,
                        onClick = { screenModel.executeTransfer() },
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )
                }
            }

            // --- LOADING OVERLAY ---
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Emerald500, strokeWidth = 4.dp, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Processing payment...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }

    // --- COMPONENTES VISUALES INTERNOS ---

    @Composable
    private fun getGlassmorphismTextFieldColors() = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
        focusedBorderColor = Emerald500,
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        focusedLabelColor = Emerald500,
        unfocusedLabelColor = Slate400
    )

    @Composable
    private fun QuickAmountChip(label: String, isAccent: Boolean = false, onClick: () -> Unit) {
        val bgColor = if (isAccent) Emerald500.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        val textColor = if (isAccent) Emerald500 else Color.White

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
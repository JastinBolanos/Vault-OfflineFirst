package com.jastin.boveda.presentation.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.R
import com.jastin.boveda.presentation.screens.main.MainScreen
import com.jastin.boveda.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* =========================================================================
 * BÓVEDA PRIVATE BANKING - REGISTER SCREEN (CORPORATE ACCOUNT CREATION)
 *
 * Flujo de creación de cuenta simulado (falso pero 100% realista e interactivo).
 * Permite configurar perfil empresarial, clave de seguridad y emitir la
 * tarjeta 24K Gold Reserve con aprobación instantánea.
 * ========================================================================= */

class RegisterScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // Pasos del asistente: 1 = Perfil, 2 = Seguridad, 3 = Tarjeta 24K
        var currentStep by remember { mutableStateOf(1) }

        // Paso 1: Perfil Empresarial
        var fullName by remember { mutableStateOf("Alexander Sterling") }
        var corporateEmail by remember { mutableStateOf("alexander@sterlingholdings.com") }
        var companyName by remember { mutableStateOf("Sterling Capital LLC") }
        var selectedJurisdiction by remember { mutableStateOf("Delaware (USA)") }

        // Paso 2: Seguridad y PIN
        var masterPassword by remember { mutableStateOf("BovedaMaster2026#") }
        var transferPin by remember { mutableStateOf("884920") }
        var biometricEnrollment by remember { mutableStateOf(true) }

        // Estado del proceso de aprovisionamiento
        var isProvisioning by remember { mutableStateOf(false) }
        var provisioningStageText by remember { mutableStateOf("") }
        var showSuccessDialog by remember { mutableStateOf(false) }

        val jurisdictions = listOf("Delaware (USA)", "Zurich (Switzerland)", "London (UK)", "Singapore (SG)")

        BackHandler(enabled = !isProvisioning) {
            if (currentStep > 1) {
                currentStep--
            } else {
                navigator.pop()
            }
        }

        // Simulación hiper-realista del aprovisionamiento
        fun startVaultProvisioning() {
            if (isProvisioning) return
            coroutineScope.launch {
                isProvisioning = true
                provisioningStageText = "Minting offline cryptographic keypairs..."
                delay(800)
                provisioningStageText = "Registering 24K Gold Vault ledger..."
                delay(800)
                provisioningStageText = "Issuing Platinum Tier Liquidity Line ($35,000.00)..."
                delay(800)
                provisioningStageText = "Vault Initialized Successfully!"
                delay(500)
                isProvisioning = false
                showSuccessDialog = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
        ) {
            // Fondo
            Image(
                painter = painterResource(R.drawable.app_background_dark),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Resplandor superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.18f),
                                Color(0xFF10B981).copy(alpha = 0.12f),
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
                        IconButton(
                            onClick = {
                                if (currentStep > 1) currentStep-- else navigator.pop()
                            },
                            enabled = !isProvisioning,
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

                        // Indicador de pasos (1 de 3)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (step in 1..3) {
                                val isActive = step <= currentStep
                                val isCurrent = step == currentStep
                                Box(
                                    modifier = Modifier
                                        .height(4.dp)
                                        .width(if (isCurrent) 28.dp else 16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isActive) {
                                                Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFF59E0B)))
                                            } else {
                                                SolidColor(Color.White.copy(alpha = 0.15f))
                                            }
                                        )
                                )
                            }
                        }

                        Text(
                            text = "Step $currentStep/3",
                            color = Slate400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // --- TÍTULOS SEGÚN EL PASO ---
                        when (currentStep) {
                            1 -> {
                                StepHeader(
                                    title = "Open Corporate Vault",
                                    subtitle = "Establish your multi-currency corporate treasury account with offline-first protection."
                                )
                            }
                            2 -> {
                                StepHeader(
                                    title = "Security & Encryption",
                                    subtitle = "Configure your 256-bit master key and fast biometric transfer authorization."
                                )
                            }
                            3 -> {
                                StepHeader(
                                    title = "24K Gold Reserve Issuance",
                                    subtitle = "Instant issuance of your corporate physical & digital reserve card."
                                )
                            }
                        }

                        // --- CONTENIDO SEGÚN EL PASO ---
                        when (currentStep) {
                            1 -> {
                                // PASO 1: PERFIL EMPRESARIAL
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    RegisterInputField(
                                        label = "LEGAL FOUNDER / EXECUTIVE NAME",
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        placeholder = "e.g. Alexander Sterling",
                                        icon = Icons.Default.Person,
                                        iconTint = Emerald400,
                                        capitalization = KeyboardCapitalization.Words
                                    )

                                    RegisterInputField(
                                        label = "CORPORATE BUSINESS EMAIL",
                                        value = corporateEmail,
                                        onValueChange = { corporateEmail = it },
                                        placeholder = "e.g. alexander@enterprise.com",
                                        icon = Icons.Default.Email,
                                        iconTint = Color(0xFF60A5FA),
                                        keyboardType = KeyboardType.Email
                                    )

                                    RegisterInputField(
                                        label = "LEGAL ENTITY OR COMPANY NAME",
                                        value = companyName,
                                        onValueChange = { companyName = it },
                                        placeholder = "e.g. Sterling Capital Holdings LLC",
                                        icon = Icons.Default.Business,
                                        iconTint = Color(0xFFF59E0B),
                                        capitalization = KeyboardCapitalization.Words
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "JURISDICTION & INCORPORATION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate400,
                                            letterSpacing = 1.sp
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            jurisdictions.forEach { jur ->
                                                val isSelected = jur == selectedJurisdiction
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            if (isSelected) Emerald500.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) Emerald400 else Color.White.copy(alpha = 0.1f),
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { selectedJurisdiction = jur }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = jur,
                                                        color = if (isSelected) Emerald400 else Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // PASO 2: SEGURIDAD
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    RegisterInputField(
                                        label = "MASTER ENCRYPTED PASSCODE",
                                        value = masterPassword,
                                        onValueChange = { masterPassword = it },
                                        placeholder = "Create master key",
                                        icon = Icons.Default.Lock,
                                        iconTint = Color(0xFF60A5FA),
                                        isPassword = true
                                    )

                                    // Indicador de fortaleza de clave
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            for (i in 0 until 4) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(Emerald400)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Entropy: Military-Grade AES",
                                            color = Emerald400,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    RegisterInputField(
                                        label = "6-DIGIT FAST TRANSFER PIN",
                                        value = transferPin,
                                        onValueChange = { if (it.length <= 6) transferPin = it },
                                        placeholder = "6-digit authorization PIN",
                                        icon = Icons.Default.Pin,
                                        iconTint = Color(0xFFA78BFA),
                                        keyboardType = KeyboardType.NumberPassword
                                    )

                                    // Biometría
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = null,
                                                tint = Emerald400,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Biometric Authentication", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text("Authorize transfers with Touch ID", color = Slate400, fontSize = 11.sp)
                                            }
                                        }
                                        Switch(
                                            checked = biometricEnrollment,
                                            onCheckedChange = { biometricEnrollment = it },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Emerald500
                                            )
                                        )
                                    }
                                }
                            }

                            3 -> {
                                // PASO 3: PREVIEW DE LA TARJETA DORADA EMITIDA
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // Tarjeta Dorada Interactiva
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFFFFA8A),
                                                        Color(0xFFFFD54F),
                                                        Color(0xFFF59E0B),
                                                        Color(0xFFD97706),
                                                        Color(0xFF92400E),
                                                        Color(0xFF451A03)
                                                    )
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFF78350F))
                                                ),
                                                RoundedCornerShape(22.dp)
                                            )
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
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp, 22.dp)
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFF1E1E1E).copy(alpha = 0.6f))
                                                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(4.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CreditCard,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFFD54F),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "VAULT GOLD RESERVE",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF1E1E1E),
                                                        letterSpacing = 1.sp
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF1E1E1E).copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "TIER 1 VIP",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1E1E1E)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "••••  ••••  ••••  8849",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp,
                                                color = Color(0xFF261805)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "CARDHOLDER",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF451A03)
                                                    )
                                                    Text(
                                                        text = fullName.ifBlank { "ALEXANDER STERLING" }.uppercase(),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF1A0A00)
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "EXPIRES",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF451A03)
                                                    )
                                                    Text(
                                                        text = "12/30",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1A0A00)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Especificaciones del Vault aprobado
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Approved Cash Liquidity", color = Slate400, fontSize = 12.sp)
                                            Text("$35,000.00 USD", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Offline-First Protection", color = Slate400, fontSize = 12.sp)
                                            Text("Active (Zero-Knowledge)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Assigned IBAN", color = Slate400, fontSize = 12.sp)
                                            Text("US89-BOVEDA-8849-01", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Indicador de aprovisionamiento si está procesando
                        if (isProvisioning) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFFFD54F),
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = provisioningStageText,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // --- BOTÓN DE AVANCE O ACTIVACIÓN ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    startVaultProvisioning()
                                }
                            },
                            enabled = !isProvisioning,
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            if (currentStep == 3) {
                                                listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFFD54F))
                                            } else {
                                                listOf(Color(0xFF6D28D9), Color(0xFF7C3AED), Color(0xFF8B5CF6))
                                            }
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isProvisioning) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Provisioning Vault...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text(
                                            text = if (currentStep < 3) "Continue to Step ${currentStep + 1} →" else "Activate & Provision Vault",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (currentStep == 3) Color(0xFF1E1E1E) else Color.White
                                        )
                                        if (currentStep == 3) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF1E1E1E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Enlace para volver a Login
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already an authorized partner? ",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Log in to your Vault",
                                color = Emerald400,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(enabled = !isProvisioning) {
                                    navigator.push(LoginScreen())
                                }
                            )
                        }
                    }
                }
            }

            // --- MODAL DE ÉXITO: VAULT APROVISIONADO CON BOTÓN "Log in to your Vault" ---
            if (showSuccessDialog) {
                Dialog(onDismissRequest = { /* Modal bloqueante de éxito */ }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), RoundedCornerShape(26.dp))
                            .padding(26.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(Color(0xFFFFD54F), Emerald400, Color(0xFF6366F1), Color(0xFFFFD54F))
                                        )
                                    )
                                    .padding(3.dp),
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
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Corporate Vault Ready",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your 24K Gold Reserve has been provisioned and is ready for offline-first banking.",
                                    color = Slate400,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Account Holder", color = Slate400, fontSize = 12.sp)
                                    Text(fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Initial Liquidity", color = Slate400, fontSize = 12.sp)
                                    Text("$35,000.00", color = Emerald400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Security Token", color = Slate400, fontSize = 12.sp)
                                    Text("Active • 256-Bit", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            // BOTÓN REQUERIDO: "Log in to your Vault" -> envía directamente al Home
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    navigator.replaceAll(MainScreen())
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Log in to your Vault",
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
        }
    }

    @Composable
    private fun StepHeader(title: String, subtitle: String) {
        Column {
            Text(
                text = title,
                fontFamily = FontFamily(Font(R.font.inria_serif_bold, FontWeight.Bold)),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Slate400,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }

    @Composable
    private fun RegisterInputField(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        placeholder: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        iconTint: Color,
        isPassword: Boolean = false,
        keyboardType: KeyboardType = KeyboardType.Text,
        capitalization: KeyboardCapitalization = KeyboardCapitalization.None
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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
        }
    }
}

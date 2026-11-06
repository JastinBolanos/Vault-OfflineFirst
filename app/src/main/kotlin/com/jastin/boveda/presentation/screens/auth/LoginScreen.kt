package com.jastin.boveda.presentation.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.R
import com.jastin.boveda.presentation.screens.main.MainScreen
import com.jastin.boveda.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* =========================================================================
 * BÓVEDA PRIVATE BANKING - LOGIN SCREEN
 *
 * Hyper-realistic, high-security neobank login flow.
 * Pressing "Log in to your Vault" triggers an authentic biometric/ledger
 * decrypt sequence and routes seamlessly to MainScreen (Home).
 * ========================================================================= */

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var emailOrVaultId by remember { mutableStateOf("alexander.sterling@sterlingcorp.com") }
        var masterPasscode by remember { mutableStateOf("VaultSecure2026#") }
        var isPasswordVisible by remember { mutableStateOf(false) }
        var rememberDevice by remember { mutableStateOf(true) }

        // Estados del flujo de autenticación falso pero hiper-realista
        var authStage by remember { mutableStateOf(0) } // 0 = idle, 1 = verifying, 2 = biometric, 3 = decrypting, 4 = success
        val isAuthenticating = authStage > 0

        val authStageMessage = when (authStage) {
            1 -> "Verifying 256-bit Master Key..."
            2 -> "Biometric Token Authenticated ✓"
            3 -> "Decrypting Offline-First Ledger..."
            4 -> "Access Granted. Welcome, Alexander."
            else -> ""
        }

        BackHandler(enabled = !isAuthenticating) {
            navigator.pop()
        }

        // Ejecución del login simulado
        fun executeVaultLogin() {
            if (isAuthenticating) return
            coroutineScope.launch {
                authStage = 1
                delay(600)
                authStage = 2
                delay(600)
                authStage = 3
                delay(600)
                authStage = 4
                delay(400)
                navigator.replaceAll(MainScreen())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
        ) {
            // Fondo de la app
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
                    .height(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6D28D9).copy(alpha = 0.25f),
                                Color(0xFF10B981).copy(alpha = 0.15f),
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
                            onClick = { navigator.pop() },
                            enabled = !isAuthenticating,
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Emerald400)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BÓVEDA SECURE NODE",
                                color = Slate400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(42.dp))
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
                        Spacer(modifier = Modifier.height(10.dp))

                        // --- CABECERA Y TÍTULO ---
                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    append("Log in to your\n")
                                    withStyle(
                                        style = SpanStyle(
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFFFFA8A),
                                                    Color(0xFFFFD54F),
                                                    Color(0xFFF59E0B)
                                                )
                                            )
                                        )
                                    ) {
                                        append("Private Vault")
                                    }
                                },
                                fontFamily = FontFamily(
                                    Font(R.font.inria_serif_bold, FontWeight.Bold)
                                ),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 40.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter your enterprise credentials or biometric passkey to access your offline-first financial reserves.",
                                color = Slate400,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }

                        // --- TARJETA DE CREDENCIALES (GLASSMORPHIC) ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Campo 1: Vault ID o Email
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "VAULT ID OR CORPORATE EMAIL",
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
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    BasicTextField(
                                        value = emailOrVaultId,
                                        onValueChange = { emailOrVaultId = it },
                                        singleLine = true,
                                        enabled = !isAuthenticating,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                            // Campo 2: Master Security Key
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MASTER SECURITY PASSCODE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate400,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "256-bit Encrypted",
                                        fontSize = 10.sp,
                                        color = Emerald400,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

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
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    BasicTextField(
                                        value = masterPasscode,
                                        onValueChange = { masterPasscode = it },
                                        singleLine = true,
                                        enabled = !isAuthenticating,
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        cursorBrush = SolidColor(Emerald400),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { isPasswordVisible = !isPasswordVisible },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password",
                                            tint = Slate400,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Switch Recordar Dispositivo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFFA78BFA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Remember this device securely",
                                        color = Slate400,
                                        fontSize = 12.sp
                                    )
                                }
                                Switch(
                                    checked = rememberDevice,
                                    onCheckedChange = { rememberDevice = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Emerald500,
                                        uncheckedThumbColor = Slate400,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                                    )
                                )
                            }
                        }

                        // --- PILL DE BIOMETRÍA INSTANTÁNEA ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                                .clickable(enabled = !isAuthenticating) { executeVaultLogin() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
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
                                        text = "Biometric Passkey Active",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Touch to authenticate with Fingerprint",
                                        color = Slate400,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Indicador de estado si está autenticando
                        if (isAuthenticating) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Emerald500.copy(alpha = 0.12f))
                                    .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Emerald400,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = authStageMessage,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // --- BOTÓN PRINCIPAL REQUERIDO POR EL USUARIO: "Log in to your Vault" ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            onClick = { executeVaultLogin() },
                            enabled = !isAuthenticating,
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
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
                                    if (isAuthenticating) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Authenticating...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Log in to your Vault",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
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

                        // Enlace hacia creación de cuenta
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Don't have a Vault account yet? ",
                                color = Slate400,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Open Corporate Account",
                                color = Emerald400,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(enabled = !isAuthenticating) {
                                    navigator.push(RegisterScreen())
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

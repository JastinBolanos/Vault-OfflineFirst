package com.jastin.boveda.presentation.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jastin.boveda.R
import com.jastin.boveda.presentation.screens.main.MainScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        // --- ESTADOS DE ANIMACIÓN INDEPENDIENTES ---
        var loadingAction by remember { mutableStateOf("") }
        val isAnyLoading = loadingAction.isNotEmpty()

        val buttonPurple = Color(0xFF6D28D9)

        val vaultGradient = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF899CFF),
                0.23f to Color(0xFF924FFF),
                0.68f to Color(0xFFF442B5),
                1.00f to Color(0xFFD61672)
            )
        )

        // --- ACCIÓN DE ENTRADA A LA BÓVEDA ---
        val onEnterVault = { action: String ->
            if (!isAnyLoading) {
                loadingAction = action
                coroutineScope.launch {
                    delay(1200)
                    navigator.replaceAll(MainScreen())
                    loadingAction = ""
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            // --- 1. FONDO PRINCIPAL ---
            Image(
                painter = painterResource(R.drawable.splash_background),
                contentDescription = "Welcome Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
            ) {
                Spacer(modifier = Modifier.weight(0.6f))

                // --- 2. SECCIÓN SUPERIOR (TEXTOS) ---
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("Enterprise banking\non ")
                            withStyle(style = SpanStyle(brush = vaultGradient)) {
                                append("Vault")
                            }
                        },
                        fontFamily = FontFamily(
                            Font(R.font.inria_serif_bold, FontWeight.Bold)
                        ),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 44.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No borders, no downtime, no compromises.\nYour offline-first financial engine.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.weight(0.4f))

                // --- 3. TARJETA CORPORATIVA ---
                Image(
                    painter = painterResource(R.drawable.splash_card_bg),
                    contentDescription = "Card Background",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.weight(1.2f))

                // --- 4. BOTONES DE ACCIÓN ---
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                    // BOTÓN 1: CORPORATE ACCOUNT
                    Button(
                        onClick = { onEnterVault("CORPORATE") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonPurple,
                            disabledContainerColor = buttonPurple,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnyLoading
                    ) {
                        if (loadingAction == "CORPORATE") {
                            PremiumLoadingDots()
                        } else {
                            Text(
                                text = "Open Corporate Account",
                                color = if (loadingAction == "LOGIN") Color.White.copy(alpha = 0.5f) else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BOTÓN 2: LOG IN TO YOUR VAULT
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = !isAnyLoading) { onEnterVault("LOGIN") }
                            .padding(vertical = 12.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (loadingAction == "LOGIN") {
                            PremiumLoadingDots()
                        } else {
                            Text(
                                text = "Log in to your Vault",
                                color = if (loadingAction == "CORPORATE") Color.White.copy(alpha = 0.5f) else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // --- COMPONENTE: TRES PUNTITOS PODEROSOS Y VELOCES ---
    @Composable
    private fun PremiumLoadingDots() {
        val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 3) {
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 300,
                            delayMillis = i * 100,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_scale_$i"
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

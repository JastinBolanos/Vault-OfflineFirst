package com.jastin.boveda.presentation.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.jastin.boveda.R
import com.jastin.boveda.globalTransactionRepository
import com.jastin.boveda.presentation.screens.activity.ActivityTab
import com.jastin.boveda.presentation.screens.cards.CardsTab
import com.jastin.boveda.presentation.screens.home.HomeTab
import com.jastin.boveda.presentation.screens.security.SecurityTab
import com.jastin.boveda.presentation.screens.transfer.TransferScreen
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.BackPressHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalMenuDrawerState = compositionLocalOf<DrawerState> { error("DrawerState not provided") }

enum class DrawerFeature(val title: String, val subtitle: String, val icon: ImageVector) {
    DIGITAL_TOKEN("Digital Token", "Dynamic TOTP authorization", Icons.Default.Security),
    SECURITY_PRIVACY("Security & Privacy", "Zero-Knowledge & Encryption", Icons.Default.Lock),
    CARDS("Cards & Limits", "Titanium Metal & Freeze Controls", Icons.Default.CreditCard),
    SERVICE_POINTS("Service Points", "VIP Lounges & Desks", Icons.Default.Place),
    CONTACT_US("Contact Us", "Private Banker & Concierge", Icons.Default.Headset),
    ABOUT_VAULT("About Vault", "Engine Architecture & Legal", Icons.Default.Info)
}

class MainScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // Estado para la ventana modal interactiva del feature seleccionado
        var selectedFeature by remember { mutableStateOf<DrawerFeature?>(null) }

        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalMenuDrawerState provides drawerState
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                scrimColor = Slate950.copy(alpha = 0.65f),
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            drawerContainerColor = Color.Transparent,
                            windowInsets = WindowInsets(0.dp),
                            modifier = Modifier.width(320.dp).fillMaxHeight()
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Fondo del panel lateral
                                Image(
                                    painter = painterResource(R.drawable.drawer_background),
                                    contentDescription = "Drawer Background",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Contenido del menú interactivo
                                DrawerMenuContent(
                                    onSelectFeature = { feature ->
                                        scope.launch {
                                            drawerState.close()
                                            selectedFeature = feature
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    TabNavigator(HomeTab) {
                        val tabNavigator = LocalTabNavigator.current

                        BackPressHandler(enabled = drawerState.isOpen) {
                            scope.launch { drawerState.close() }
                        }
                        BackPressHandler(enabled = tabNavigator.current != HomeTab && !drawerState.isOpen) {
                            tabNavigator.current = HomeTab
                        }

                        // Contenedor principal donde el contenido corre a pantalla completa y pasa por debajo de la pastilla flotante
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 1. Contenido de la pestaña activa a pantalla completa (los movimientos recientes pasan por debajo)
                            Box(modifier = Modifier.fillMaxSize()) {
                                CurrentTab()
                            }

                            // 2. Pastilla de los 5 botones flotando POR ENCIMA del contenido
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 16.dp, vertical = 18.dp)
                                    .navigationBarsPadding()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                tonalElevation = 12.dp,
                                shadowElevation = 16.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // 1. Home
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TabIconItem(
                                            icon = Icons.Default.Home,
                                            label = "Home",
                                            isSelected = tabNavigator.current == HomeTab,
                                            tabType = BottomTabType.HOME
                                        ) {
                                            tabNavigator.current = HomeTab
                                        }
                                    }

                                    // 2. Cards (Pantalla completa CardsTab)
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TabIconItem(
                                            icon = Icons.Default.CreditCard,
                                            label = "Cards",
                                            isSelected = tabNavigator.current == CardsTab,
                                            tabType = BottomTabType.CARDS
                                        ) {
                                            tabNavigator.current = CardsTab
                                        }
                                    }

                                    // 3. Botón Central Flotante de Transferencias
                                    Box(modifier = Modifier.weight(1.15f), contentAlignment = Alignment.Center) {
                                        FloatingTransferButton {
                                            val currentBalance = globalTransactionRepository.currentBalance.value
                                            rootNavigator.push(TransferScreen(currentBalance))
                                        }
                                    }

                                    // 4. Activity (Pantalla completa ActivityTab)
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TabIconItem(
                                            icon = Icons.Default.Insights,
                                            label = "Activity",
                                            isSelected = tabNavigator.current == ActivityTab,
                                            tabType = BottomTabType.ACTIVITY
                                        ) {
                                            tabNavigator.current = ActivityTab
                                        }
                                    }

                                    // 5. Security (Pantalla completa SecurityTab)
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        TabIconItem(
                                            icon = Icons.Default.Shield,
                                            label = "Security",
                                            isSelected = tabNavigator.current == SecurityTab,
                                            tabType = BottomTabType.SECURITY
                                        ) {
                                            tabNavigator.current = SecurityTab
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIÁLOGO MODAL ULTRA-PREMIUM CON ANIMACIONES PARA CADA FUNCIÓN ---
        selectedFeature?.let { feature ->
            DrawerFeatureDialog(
                feature = feature,
                onDismiss = { selectedFeature = null }
            )
        }
    }

    // --- MENÚ LATERAL INTERACTIVO ---
    @Composable
    private fun DrawerMenuContent(onSelectFeature: (DrawerFeature) -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Cabecera del usuario en el Drawer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Emerald400, CircleShape)
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_corporate),
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Alexander Sterling",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Private Client • Tier 1",
                        fontSize = 12.sp,
                        color = Emerald400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "SERVICES & CONTROL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Slate400,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Los 5 botones interactivos del menú lateral
            val drawerItems = listOf(
                DrawerFeature.DIGITAL_TOKEN,
                DrawerFeature.SECURITY_PRIVACY,
                DrawerFeature.SERVICE_POINTS,
                DrawerFeature.CONTACT_US,
                DrawerFeature.ABOUT_VAULT
            )
            drawerItems.forEach { feature ->
                InteractiveDrawerItem(
                    feature = feature,
                    onClick = { onSelectFeature(feature) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tarjeta de estado de seguridad del sistema (Sustituye nota de desarrollo antigua)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Vault Core Security",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AES-256 • Offline Sovereign",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun InteractiveDrawerItem(
        feature: DrawerFeature,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = feature.subtitle,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    // --- DIÁLOGO MODAL UNIFORME Y ULTRA-PREMIUM PARA CADA FUNCIÓN ---
    @Composable
    private fun DrawerFeatureDialog(
        feature: DrawerFeature,
        onDismiss: () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF162138),
                                    Color(0xFF0D1424)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Emerald500.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(28.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Evita propagación de clicks */ }
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header del diálogo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Emerald500.copy(alpha = 0.25f),
                                                    Teal400.copy(alpha = 0.15f)
                                                )
                                            )
                                        )
                                        .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = feature.icon,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = feature.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Vault Verified Security",
                                        fontSize = 11.sp,
                                        color = Emerald400,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
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

                        // Contenido dinámico según el botón seleccionado
                        when (feature) {
                            DrawerFeature.DIGITAL_TOKEN -> DigitalTokenDialogContent()
                            DrawerFeature.SECURITY_PRIVACY -> SecurityPrivacyDialogContent()
                            DrawerFeature.CARDS -> CardsDialogContent()
                            DrawerFeature.SERVICE_POINTS -> ServicePointsDialogContent()
                            DrawerFeature.CONTACT_US -> ContactUsDialogContent()
                            DrawerFeature.ABOUT_VAULT -> AboutVaultDialogContent()
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Botón de acción final
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                contentColor = Slate950
                            )
                        ) {
                            Text(
                                text = "Understood",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // --- CONTENIDO 1: DIGITAL TOKEN ---
    @Composable
    private fun DigitalTokenDialogContent() {
        val clipboardManager = LocalClipboardManager.current
        var isCopied by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        var currentToken by remember { mutableStateOf("849 201") }
        var progress by remember { mutableStateOf(1f) }

        // Contador regresivo animado de 30 segundos para renovar el token
        LaunchedEffect(Unit) {
            while (true) {
                for (i in 30 downTo 0) {
                    progress = i / 30f
                    delay(1000)
                }
                currentToken = "${(100..999).random()} ${(100..999).random()}"
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Use this one-time dynamic code to authenticate high-volume wire transfers exceeding $10,000 USD.",
                fontSize = 13.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Caja del Token con animación
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                    .border(1.dp, Emerald400.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DYNAMIC SECURITY CODE",
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentToken,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Barra de progreso del tiempo restante
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Emerald400,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Refreshes automatically in ${(progress * 30).toInt()}s",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botón de copiar
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(currentToken.replace(" ", "")))
                    isCopied = true
                    scope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = if (isCopied) Emerald400 else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCopied) "Token Copied to Clipboard" else "Copy Security Token",
                    color = if (isCopied) Emerald400 else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // --- CONTENIDO 2: SECURITY & PRIVACY ---
    @Composable
    private fun SecurityPrivacyDialogContent() {
        var biometricLock by remember { mutableStateOf(true) }
        var autoLockBackground by remember { mutableStateOf(true) }
        var maskBalances by remember { mutableStateOf(false) }

        Column {
            Text(
                text = "Hardware-grade cryptographic protection. Your private keys never leave this physical Android device.",
                fontSize = 13.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecurityToggleRow(
                title = "Biometric Lock",
                subtitle = "Require fingerprint / face unlock for all transactions",
                checked = biometricLock,
                onCheckedChange = { biometricLock = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                title = "Zero-Knowledge Mode",
                subtitle = "Store offline database with AES-256-GCM cipher",
                checked = autoLockBackground,
                onCheckedChange = { autoLockBackground = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecurityToggleRow(
                title = "Privacy Screen Shield",
                subtitle = "Mask financial amounts in Android task switcher",
                checked = maskBalances,
                onCheckedChange = { maskBalances = it }
            )
        }
    }

    @Composable
    private fun SecurityToggleRow(
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Slate400, lineHeight = 14.sp)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Emerald500,
                    uncheckedThumbColor = Slate400,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }

    // --- CONTENIDO 3: SERVICE POINTS ---
    @Composable
    private fun ServicePointsDialogContent() {
        Column {
            Text(
                text = "Priority depository access across exclusive international financial centers:",
                fontSize = 13.sp,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(14.dp))

            ServicePointCard(
                city = "New York • 5th Ave Hub",
                address = "767 Fifth Avenue, Manhattan",
                status = "Open 24/7 • Private Lockers"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ServicePointCard(
                city = "Zurich • Private Depository",
                address = "Bahnhofstrasse 14, Zurich",
                status = "Open 24/7 • High Security Vaults"
            )

            Spacer(modifier = Modifier.height(10.dp))

            ServicePointCard(
                city = "London • Mayfair Suites",
                address = "12 Berkeley Square, London",
                status = "Open 24/7 • Executive Concierge"
            )
        }
    }

    @Composable
    private fun ServicePointCard(city: String, address: String, status: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                .padding(14.dp)
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
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(city, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(address, fontSize = 12.sp, color = Slate400)
                    Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Emerald400)
                }
            }
        }
    }

    // --- CONTENIDO 4: CONTACT US ---
    @Composable
    private fun ContactUsDialogContent() {
        val clipboardManager = LocalClipboardManager.current
        var toastMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        Column {
            Text(
                text = "Your dedicated private banking officer is available 24 hours a day, 7 days a week.",
                fontSize = 13.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta del Ejecutivo Privado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                    .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Emerald400, Teal400))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MV", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Slate950)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Marcus Vance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Emerald400, modifier = Modifier.size(16.dp))
                        }
                        Text("Senior Executive Private Banker", fontSize = 12.sp, color = Slate400)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald400))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Now • < 60s reply", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Emerald400)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contacto por Teléfono
            ContactActionCard(
                icon = Icons.Default.Phone,
                label = "Direct VIP Line",
                value = "+1 (800) 828-5800",
                onClick = {
                    clipboardManager.setText(AnnotatedString("+1 (800) 828-5800"))
                    toastMessage = "Phone copied to clipboard"
                    scope.launch { delay(2000); toastMessage = null }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contacto por Email cifrado
            ContactActionCard(
                icon = Icons.Default.Email,
                label = "Encrypted Email",
                value = "concierge@vault.financial",
                onClick = {
                    clipboardManager.setText(AnnotatedString("concierge@vault.financial"))
                    toastMessage = "Email copied to clipboard"
                    scope.launch { delay(2000); toastMessage = null }
                }
            )

            toastMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, fontSize = 11.sp, color = Emerald400, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun ContactActionCard(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(label, fontSize = 11.sp, color = Slate400)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Slate400, modifier = Modifier.size(16.dp))
        }
    }

    // --- CONTENIDO 5: ABOUT VAULT ---
    @Composable
    private fun AboutVaultDialogContent() {
        Column {
            Text(
                text = "Vault is a sovereign, offline-first financial ledger engine built natively for Android using modern Jetpack Compose.",
                fontSize = 13.sp,
                color = Slate400,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            AboutSpecRow(label = "Application Version", value = "v1.2.0 (Build 36)")
            AboutSpecRow(label = "Persistence Engine", value = "SQLDelight SQLite Local")
            AboutSpecRow(label = "UI Architecture", value = "Jetpack Compose + Clean Architecture")
            AboutSpecRow(label = "Background Worker", value = "AndroidX WorkManager Sync")
            AboutSpecRow(label = "Cryptography", value = "AES-256 Hardware Enclave")
            AboutSpecRow(label = "Open Source License", value = "MIT License")
        }
    }

    @Composable
    private fun AboutSpecRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = Slate400)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.8.dp)
    }

    // --- CONTENIDO: GESTIÓN DE TARJETA TITANIUM Y LÍMITES ---
    @Composable
    private fun CardsDialogContent() {
        var isCardFrozen by remember { mutableStateOf(false) }
        var isInternationalEnabled by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Representación visual de la tarjeta Titanium Black Metal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
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
                    .padding(18.dp)
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
                            Text(
                                text = "CONTACTLESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400,
                                letterSpacing = 1.sp
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

            // Switch: Congelar tarjeta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                        Text(text = "Freeze Card", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = "Instantly disable unauthorized charges", fontSize = 11.sp, color = Slate400)
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

            // Switch: Compras internacionales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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

    // --- ENUM DE PESTAÑAS INFERIORES PARA ANIMACIONES ESPECÍFICAS ---
    private enum class BottomTabType { HOME, CARDS, ACTIVITY, SECURITY }

    // --- BOTTOM BAR HELPERS CON ANIMACIONES INTERACTIVAS Y CONTINUAS ---
    @Composable
    private fun TabIconItem(
        icon: ImageVector,
        label: String,
        isSelected: Boolean,
        tabType: BottomTabType,
        onClick: () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "tab_icon_${label}_motion")

        // 1. Animación continua de levitación / flotación suave para Home
        val homeFloatY by infiniteTransition.animateFloat(
            initialValue = if (isSelected && tabType == BottomTabType.HOME) -2.5f else 0f,
            targetValue = if (isSelected && tabType == BottomTabType.HOME) 2.5f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "home_float_y"
        )

        // 2. Animación continua de balanceo de tarjeta para Cards
        val cardTilt by infiniteTransition.animateFloat(
            initialValue = if (isSelected && tabType == BottomTabType.CARDS) -6f else 0f,
            targetValue = if (isSelected && tabType == BottomTabType.CARDS) 6f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1900, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "card_tilt"
        )

        // 3. Animación continua de onda ascendente para Activity (gráfica)
        val activityFloatY by infiniteTransition.animateFloat(
            initialValue = if (isSelected && tabType == BottomTabType.ACTIVITY) 1.5f else 0f,
            targetValue = if (isSelected && tabType == BottomTabType.ACTIVITY) -2.5f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "activity_float_y"
        )

        // 4. Animación continua de respiración protectora para Security (escudo)
        val securityPulse by infiniteTransition.animateFloat(
            initialValue = if (isSelected && tabType == BottomTabType.SECURITY) 0.96f else 1.0f,
            targetValue = if (isSelected && tabType == BottomTabType.SECURITY) 1.14f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "security_pulse"
        )

        // Escala elástica reactiva a la selección
        val selectionScale by animateFloatAsState(
            targetValue = if (isSelected) 1.15f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "selection_scale"
        )

        val totalScale = selectionScale * (if (tabType == BottomTabType.SECURITY && isSelected) securityPulse else 1.0f)
        val totalOffsetY = when (tabType) {
            BottomTabType.HOME -> homeFloatY
            BottomTabType.ACTIVITY -> activityFloatY
            else -> 0f
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp)
        ) {
            // Contenedor del icono con halo sutil luminoso cuando está activo
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Emerald500.copy(alpha = 0.16f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Emerald400 else Slate400,
                    modifier = Modifier
                        .size(23.dp)
                        .graphicsLayer {
                            scaleX = totalScale
                            scaleY = totalScale
                            translationY = totalOffsetY
                            rotationZ = if (tabType == BottomTabType.CARDS) cardTilt else 0f
                        }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) Emerald400 else Slate400,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )

            // Micro-indicador de punto activo animado
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Emerald400)
                )
            }
        }
    }

    @Composable
    private fun FloatingTransferButton(onClick: () -> Unit) {
        val infiniteTransition = rememberInfiniteTransition(label = "arrows_breathe")

        // Animación dinámica de flechas adelante y hacia atrás
        val arrowTranslateX by infiniteTransition.animateFloat(
            initialValue = -3.5f,
            targetValue = 3.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrows_translate_x"
        )

        // Pulsación sutil de respiración del icono
        val scaleAnim by infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrows_stretch"
        )

        // Halo pulsante exterior expansivo
        val haloScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "halo_scale"
        )
        val haloAlpha by infiniteTransition.animateFloat(
            initialValue = 0.40f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "halo_alpha"
        )

        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anillo de halo exterior con desvanecimiento expansivo
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        scaleX = haloScale
                        scaleY = haloScale
                        alpha = haloAlpha
                    }
                    .clip(CircleShape)
                    .background(Emerald400)
            )

            // Botón principal interactivo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Emerald400, Teal400))
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Transfer",
                    tint = Slate950,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            translationX = arrowTranslateX
                        }
                )
            }
        }
    }
}

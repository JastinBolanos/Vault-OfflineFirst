package com.jastin.boveda.presentation.screens.main

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.jastin.boveda.R
import com.jastin.boveda.globalTransactionRepository
import com.jastin.boveda.presentation.screens.activity.ActivityTab
import com.jastin.boveda.presentation.screens.home.HomeTab
import com.jastin.boveda.presentation.screens.transfer.TransferScreen
import com.jastin.boveda.presentation.theme.*
import com.jastin.boveda.utils.BackPressHandler
import kotlinx.coroutines.launch

val LocalMenuDrawerState = compositionLocalOf<DrawerState> { error("DrawerState not provided") }

class MainScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalMenuDrawerState provides drawerState
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                scrimColor = Slate950.copy(alpha = 0.5f),
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            drawerContainerColor = Color.Transparent,
                            windowInsets = WindowInsets(0.dp),
                            modifier = Modifier.width(320.dp).fillMaxHeight()
                        ) {
                            // CAJA PRINCIPAL DEL MENÚ
                            Box(modifier = Modifier.fillMaxSize()) {

                                // EL FONDO DEL PANEL LATERAL
                                Image(
                                    painter = painterResource(R.drawable.drawer_background),
                                    contentDescription = "Drawer Background",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // EL CONTENIDO DEL MENÚ
                                DrawerMenuContent()
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

                        // --- SCAFFOLD PRINCIPAL TRANSPARENTE ---
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = {
                                Surface(
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp, vertical = 24.dp)
                                        .navigationBarsPadding()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(32.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp,
                                    shadowElevation = 12.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            TabIconItem(icon = Icons.Default.Home, label = "Home", isSelected = tabNavigator.current == HomeTab) { tabNavigator.current = HomeTab }
                                        }
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            FloatingTransferButton {
                                                val currentBalance = globalTransactionRepository.currentBalance.value
                                                rootNavigator.push(TransferScreen(currentBalance))
                                            }
                                        }
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            TabIconItem(icon = Icons.Default.Insights, label = "Activity", isSelected = tabNavigator.current == ActivityTab) { tabNavigator.current = ActivityTab }
                                        }
                                    }
                                }
                            }
                        ) { padding ->
                            Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                                CurrentTab()
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MENÚ PRIVADO ---
    @Composable
    private fun DrawerMenuContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                "Alexander Sterling",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )

            DrawerItem(icon = Icons.Default.Security, text = "Digital Token")
            DrawerItem(icon = Icons.Default.Lock, text = "Security & Privacy")
            DrawerItem(icon = Icons.Default.Place, text = "Service Points")
            DrawerItem(icon = Icons.Default.Headset, text = "Contact Us")
            DrawerItem(icon = Icons.Default.Info, text = "About Vault")

            Spacer(modifier = Modifier.weight(1f))

            // Developer Note (Glassmorphism sutil)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Developer Note",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "This software was built as a native Android app. This section is illustrative to showcase architecture and UI/UX; therefore, the options above are not connected to production flows.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    @Composable
    private fun DrawerItem(icon: ImageVector, text: String) {
        Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    private fun TabIconItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }) {
            Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Emerald500 else Slate400, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 11.sp, color = if (isSelected) Emerald500 else Slate400, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
        }
    }

    @Composable
    private fun FloatingTransferButton(onClick: () -> Unit) {
        val infiniteTransition = rememberInfiniteTransition(label = "arrows_breathe")

        val scaleXAnim by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrows_stretch"
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Transfer",
                tint = Emerald500,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { scaleX = scaleXAnim }
            )
        }
    }
}

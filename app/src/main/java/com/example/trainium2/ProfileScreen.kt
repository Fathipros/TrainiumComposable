package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    nombre: String,
    isAdmin: Boolean,
    idUsuario: Int,
    isPremium: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToMaquinas: (Boolean, Int) -> Unit,
    onNavigateToPlatos: () -> Unit,
    onNavigateToRegistro: (Int) -> Unit,
    onNavigateToReservas: (Boolean, Int) -> Unit,
    onNavigateToEditProfile: (Int) -> Unit
) {
    var headerVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); headerVisible = true
        delay(250); buttonsVisible = true
    }

    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "ha")
    val buttonsAlpha by animateFloatAsState(if (buttonsVisible) 1f else 0f, tween(500), label = "ba")

    // Theme-aware colors
    val bgBrush = if (isDarkTheme) {
        Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF0F4FF), Color(0xFFE3ECFF), Color(0xFFD6E4FF)))
    }
    val cardBg = if (isDarkTheme) Color(0xFF162347) else Color.White
    val cardIconBg = if (isDarkTheme) BlueAccent.copy(0.12f) else BlueAccent.copy(0.08f)
    val titleColor = if (isDarkTheme) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (isDarkTheme) Color.White.copy(0.4f) else Color(0xFF6B7B99)
    val topIconTint = if (isDarkTheme) BlueSoft else BlueAccent

    Box(
        Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Tema",
                        tint = topIconTint
                    )
                }
                IconButton(onClick = { onNavigateToEditProfile(idUsuario) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = topIconTint)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Avatar + Name
            Column(
                modifier = Modifier.alpha(headerAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(16.dp, CircleShape, ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(BlueAccent, BlueElectric)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nombre.take(1).uppercase(),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Hola, $nombre",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    textAlign = TextAlign.Center
                )

                if (isAdmin) {
                    Spacer(Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueAccent.copy(0.15f))
                    ) {
                        Text(
                            text = "👑 ADMINISTRADOR",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = BlueAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                if (isPremium) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "⭐ PREMIUM",
                        fontSize = 13.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // Menu buttons
            Column(
                modifier = Modifier.alpha(buttonsAlpha),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileMenuCard(
                    text = "Visualizar máquinas",
                    icon = Icons.Default.FitnessCenter,
                    subtitle = "Explora el equipamiento disponible",
                    cardBg = cardBg,
                    iconBg = cardIconBg,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    isDark = isDarkTheme,
                    onClick = { onNavigateToMaquinas(isAdmin, idUsuario) }
                )
                ProfileMenuCard(
                    text = "Máquinas reservadas",
                    icon = Icons.Default.CalendarMonth,
                    subtitle = "Tus reservas activas",
                    cardBg = cardBg,
                    iconBg = cardIconBg,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    isDark = isDarkTheme,
                    onClick = { onNavigateToReservas(isAdmin, idUsuario) }
                )
                ProfileMenuCard(
                    text = "Recomendación de platos",
                    icon = Icons.Default.Restaurant,
                    subtitle = "Nutrición personalizada",
                    cardBg = cardBg,
                    iconBg = cardIconBg,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    isDark = isDarkTheme,
                    onClick = { onNavigateToPlatos() }
                )
                ProfileMenuCard(
                    text = "Mi registro de peso",
                    icon = Icons.Default.MonitorWeight,
                    subtitle = "Seguimiento de tu progreso",
                    cardBg = cardBg,
                    iconBg = cardIconBg,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    isDark = isDarkTheme,
                    onClick = { onNavigateToRegistro(idUsuario) }
                )
            }

            Spacer(Modifier.height(32.dp))

            // Logout button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .alpha(buttonsAlpha),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFF6B6B).copy(0.5f))
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileMenuCard(
    text: String,
    icon: ImageVector,
    subtitle: String,
    cardBg: Color,
    iconBg: Color,
    titleColor: Color,
    subtitleColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                if (isDark) 8.dp else 4.dp,
                RoundedCornerShape(18.dp),
                ambientColor = if (isDark) BlueAccent.copy(0.08f) else Color.Black.copy(0.06f),
                spotColor = if (isDark) BlueAccent.copy(0.08f) else Color.Black.copy(0.06f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(if (isDark) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BlueAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = BlueAccent.copy(0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
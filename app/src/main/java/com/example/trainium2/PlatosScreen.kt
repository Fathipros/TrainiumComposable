package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PlatosScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var calorias by remember { mutableStateOf("") }
    var proteinas by remember { mutableStateOf("") }
    var carbohidratos by remember { mutableStateOf("") }
    var grasas by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var sinDatos by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Staggered animations
    var headerVisible by remember { mutableStateOf(false) }
    var plateVisible by remember { mutableStateOf(false) }
    var card1Visible by remember { mutableStateOf(false) }
    var card2Visible by remember { mutableStateOf(false) }
    var btnVisible by remember { mutableStateOf(false) }

    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val plateAlpha by animateFloatAsState(if (plateVisible) 1f else 0f, tween(600), label = "p")
    val plateScale by animateFloatAsState(if (plateVisible) 1f else 0.7f, tween(600, easing = FastOutSlowInEasing), label = "ps")
    val card1Alpha by animateFloatAsState(if (card1Visible) 1f else 0f, tween(500), label = "c1")
    val card2Alpha by animateFloatAsState(if (card2Visible) 1f else 0f, tween(500), label = "c2")
    val btnAlpha by animateFloatAsState(if (btnVisible) 1f else 0f, tween(500), label = "b")

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.15f, 0.35f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ga")

    fun cargarPlato() {
        cargando = true; error = false; sinDatos = false
        headerVisible = false; plateVisible = false; card1Visible = false; card2Visible = false; btnVisible = false
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT * FROM PLATOS ORDER BY RAND() LIMIT 1").executeQuery()
                    if (rs.next()) {
                        nombre = rs.getString("NOMBRE") ?: ""; calorias = rs.getString("CALORIAS") ?: "0"
                        proteinas = "0"; carbohidratos = "0"
                        grasas = "0"; autor = rs.getString("DESCRIPCION") ?: ""
                    } else { withContext(Dispatchers.Main) { sinDatos = true } }
                    conn.close()
                } else { withContext(Dispatchers.Main) { error = true } }
            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { error = true } }
            withContext(Dispatchers.Main) {
                cargando = false
                delay(80); headerVisible = true
                delay(120); plateVisible = true
                delay(150); card1Visible = true
                delay(150); card2Visible = true
                delay(120); btnVisible = true
            }
        }
    }

    LaunchedEffect(Unit) { cargarPlato() }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {

            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                Column(Modifier.weight(1f)) {
                    Text("Nutrición", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Tu plato recomendado del día", fontSize = 12.sp, color = Color.White.copy(0.35f))
                }
                IconButton(onClick = { cargarPlato() }) { Icon(Icons.Default.Refresh, null, tint = BlueAccent) }
            }
            Spacer(Modifier.height(20.dp))

            if (cargando) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueAccent, strokeWidth = 3.dp)
                }
            } else if (error || sinDatos) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (error) "⚠️" else "🍽️", fontSize = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(if (error) "Error de conexión" else "Sin platos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if (error) "Verifica tu conexión" else "No hay platos registrados", fontSize = 14.sp, color = Color.White.copy(0.4f))
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { cargarPlato() }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                            Text("Reintentar", fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                // ── Plate Hero Section ──
                Box(Modifier.fillMaxWidth().alpha(plateAlpha).scale(plateScale), contentAlignment = Alignment.Center) {
                    // Glow circle
                    Box(Modifier.size(160.dp).shadow(30.dp, CircleShape, ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)).background(Brush.radialGradient(listOf(BlueAccent.copy(glowAlpha), Color.Transparent)), CircleShape))
                    // Icon
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍽️", fontSize = 72.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("PLATO DEL DÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BlueAccent.copy(0.6f), letterSpacing = 4.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Dish Name Card ──
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(plateAlpha)
                        .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = BlueAccent.copy(0.15f), spotColor = BlueAccent.copy(0.15f)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(BlueAccent.copy(0.15f), BlueElectric.copy(0.08f)))).padding(24.dp)) {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(nombre.ifEmpty { "Sin nombre" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            if (autor.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(6.dp).background(BlueAccent, CircleShape))
                                    Spacer(Modifier.width(8.dp))
                                    Text("por $autor", color = BlueSoft.copy(0.5f), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Section Label ──
                Text("INFORMACIÓN NUTRICIONAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.3f), letterSpacing = 3.sp, modifier = Modifier.alpha(card1Alpha))
                Spacer(Modifier.height(14.dp))

                // ── Nutrient Cards Row 1 ──
                Row(Modifier.fillMaxWidth().alpha(card1Alpha), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumNutrientCard("🔥", "Calorías", calorias, "kcal", BlueAccent, Modifier.weight(1f))
                    PremiumNutrientCard("💪", "Proteínas", proteinas, "g", Color(0xFF00E676), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth().alpha(card2Alpha), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PremiumNutrientCard("⚡", "Carbos", carbohidratos, "g", Color(0xFFFFAB40), Modifier.weight(1f))
                    PremiumNutrientCard("🥑", "Grasas", grasas, "g", Color(0xFFE040FB), Modifier.weight(1f))
                }

                Spacer(Modifier.height(28.dp))

                // ── Refresh Button ──
                Button(
                    onClick = { cargarPlato() },
                    modifier = Modifier.fillMaxWidth().height(54.dp).alpha(btnAlpha)
                        .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.3f), spotColor = BlueAccent.copy(0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Descubrir otro plato", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PremiumNutrientCard(emoji: String, label: String, value: String, unit: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = accentColor.copy(0.1f), spotColor = accentColor.copy(0.1f)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF162347))
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 11.sp, color = Color.White.copy(0.4f), fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value.ifEmpty { "—" }, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(Modifier.width(4.dp))
                Text(unit, fontSize = 13.sp, color = Color.White.copy(0.3f), modifier = Modifier.offset(y = (-5).dp))
            }
        }
    }
}
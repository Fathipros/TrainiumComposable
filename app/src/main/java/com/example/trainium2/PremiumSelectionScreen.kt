package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PremiumSelectionScreen(idUsuario: Int, onBack: () -> Unit, onSuccess: () -> Unit) {
    var planSeleccionado by remember { mutableStateOf("") }
    var metodoSeleccionado by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var crownVisible by remember { mutableStateOf(false) }
    var plansVisible by remember { mutableStateOf(false) }
    var payVisible by remember { mutableStateOf(false) }
    var btnVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val crownAlpha by animateFloatAsState(if (crownVisible) 1f else 0f, tween(600), label = "c")
    val crownScale by animateFloatAsState(if (crownVisible) 1f else 0.6f, tween(700, easing = FastOutSlowInEasing), label = "cs")
    val plansAlpha by animateFloatAsState(if (plansVisible) 1f else 0f, tween(500), label = "p")
    val payAlpha by animateFloatAsState(if (payVisible) 1f else 0f, tween(500), label = "pa")
    val btnAlpha by animateFloatAsState(if (btnVisible) 1f else 0f, tween(500), label = "b")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.15f, 0.4f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "g")

    LaunchedEffect(Unit) { delay(80); headerVisible = true; delay(120); crownVisible = true; delay(150); plansVisible = true; delay(150); payVisible = true; delay(150); btnVisible = true }

    val planes = listOf(
        Triple("Mensual", "9.99€", 1),
        Triple("Semestral", "49.99€", 6),
        Triple("Anual", "89.99€", 12)
    )
    val savings = listOf("", "Ahorra 17%", "Ahorra 25%")
    val emojis = listOf("📅", "📆", "🏆")
    val metodos = listOf(Pair("💳", "Tarjeta de crédito"), Pair("🅿️", "PayPal"))

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Hazte Premium", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Desbloquea todo el potencial", fontSize = 12.sp, color = Color.White.copy(0.35f))
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Crown Icon ──
            Box(Modifier.fillMaxWidth().alpha(crownAlpha).scale(crownScale), contentAlignment = Alignment.Center) {
                Box(Modifier.size(100.dp).shadow(24.dp, CircleShape, ambientColor = Color(0xFFFFD700).copy(glowAlpha), spotColor = Color(0xFFFFD700).copy(glowAlpha)).background(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(glowAlpha), Color.Transparent)), CircleShape))
                Text("👑", fontSize = 56.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text("PREMIUM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700).copy(0.6f), letterSpacing = 6.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().alpha(crownAlpha))

            Spacer(Modifier.height(24.dp))

            // ── Plans ──
            Text("ELIGE TU PLAN", fontSize = 11.sp, color = Color.White.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.alpha(plansAlpha))
            Spacer(Modifier.height(12.dp))

            planes.forEachIndexed { i, (nombre, precio, meses) ->
                val selected = planSeleccionado == nombre
                val isPopular = i == 2
                Card(
                    onClick = { planSeleccionado = nombre },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).alpha(plansAlpha)
                        .shadow(if (selected) 10.dp else 2.dp, RoundedCornerShape(16.dp), ambientColor = if (selected) BlueAccent.copy(0.2f) else Color.Transparent, spotColor = if (selected) BlueAccent.copy(0.2f) else Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) BlueAccent else Color.White.copy(0.1f)),
                    colors = CardDefaults.cardColors(containerColor = if (selected) BlueAccent.copy(0.1f) else Color(0xFF162347))
                ) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(if (selected) BlueAccent.copy(0.15f) else Color.White.copy(0.05f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text(emojis[i], fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(nombre, fontWeight = FontWeight.Bold, color = if (selected) BlueAccent else Color.White, fontSize = 16.sp)
                                if (isPopular) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(Modifier.background(Color(0xFFFFD700).copy(0.15f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text("POPULAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                    }
                                }
                            }
                            Row {
                                Text("$meses ${if (meses == 1) "mes" else "meses"}", fontSize = 12.sp, color = Color.White.copy(0.35f))
                                if (savings[i].isNotEmpty()) { Spacer(Modifier.width(8.dp)); Text(savings[i], fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676)) }
                            }
                        }
                        Text(precio, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (selected) BlueAccent else BlueSoft)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Payment Methods ──
            Text("MÉTODO DE PAGO", fontSize = 11.sp, color = Color.White.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.alpha(payAlpha))
            Spacer(Modifier.height(12.dp))

            metodos.forEach { (emoji, nombre) ->
                val selected = metodoSeleccionado == nombre
                Card(
                    onClick = { metodoSeleccionado = nombre },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).alpha(payAlpha)
                        .shadow(if (selected) 6.dp else 0.dp, RoundedCornerShape(14.dp), ambientColor = BlueElectric.copy(0.1f), spotColor = BlueElectric.copy(0.1f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) BlueElectric else Color.White.copy(0.1f)),
                    colors = CardDefaults.cardColors(containerColor = if (selected) BlueElectric.copy(0.08f) else Color(0xFF162347))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(nombre, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) BlueElectric else Color.White, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── CTA Button ──
            Button(
                onClick = {
                    if (planSeleccionado.isEmpty() || metodoSeleccionado.isEmpty()) { Toast.makeText(context, "Selecciona un plan y método de pago", Toast.LENGTH_SHORT).show(); return@Button }
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection(); if (conn != null) {
                            try {
                                val meses = when (planSeleccionado) { "Mensual" -> 1; "Semestral" -> 6; else -> 12 }
                                val monto = when (planSeleccionado) { "Mensual" -> 9.99; "Semestral" -> 49.99; else -> 89.99 }
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); val hoy = sdf.format(Date()); val cal = Calendar.getInstance().apply { add(Calendar.MONTH, meses) }; val fin = sdf.format(cal.time)
                                conn.prepareStatement("UPDATE USUARIO SET PREMIUM = 1, FECHA_INI_PREM = ?, FECHA_FIN_PREM = ? WHERE ID = ?").apply { setString(1, hoy); setString(2, fin); setInt(3, idUsuario); executeUpdate() }
                                conn.prepareStatement("INSERT INTO PAGOS (id_usuario, tipo, monto, fecha_pago) VALUES (?, ?, ?, ?)").apply { setInt(1, idUsuario); setString(2, "Premium $planSeleccionado"); setDouble(3, monto); setString(4, hoy); executeUpdate() }
                                conn.close()
                                withContext(Dispatchers.Main) { Toast.makeText(context, "🎉 ¡Bienvenido a Premium!", Toast.LENGTH_LONG).show(); onSuccess() }
                            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { Toast.makeText(context, "Error al procesar el pago", Toast.LENGTH_SHORT).show() } }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(btnAlpha)
                    .shadow(16.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f)),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("👑 CONFIRMAR Y PAGAR", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White, letterSpacing = 1.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
package com.example.trainium2

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Pago(val id: Int, val concepto: String, val monto: Double, val fecha: String)

@Composable
fun HistorialScreen(idUsuario: Int, onBack: () -> Unit) {
    var pagos by remember { mutableStateOf(listOf<Pago>()) }
    var cargando by remember { mutableStateOf(true) }
    var errorConexion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var headerVisible by remember { mutableStateOf(false) }
    var summaryVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val summaryAlpha by animateFloatAsState(if (summaryVisible) 1f else 0f, tween(600), label = "s")
    val summaryScale by animateFloatAsState(if (summaryVisible) 1f else 0.9f, tween(600, easing = FastOutSlowInEasing), label = "ss")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.25f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "g")

    fun cargarDatos() {
        cargando = true; errorConexion = false; headerVisible = false; summaryVisible = false
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection(); val tmp = mutableListOf<Pago>()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT ID, tipo, monto, fecha_pago FROM PAGOS WHERE id_usuario = ? ORDER BY fecha_pago DESC").apply { setInt(1, idUsuario) }.executeQuery()
                    while (rs.next()) tmp.add(Pago(rs.getInt("ID"), rs.getString("tipo") ?: "", rs.getDouble("monto"), rs.getString("fecha_pago") ?: ""))
                    conn.close()
                } else { withContext(Dispatchers.Main) { errorConexion = true } }
                withContext(Dispatchers.Main) { pagos = tmp; cargando = false; delay(80); headerVisible = true; delay(150); summaryVisible = true }
            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { errorConexion = true; cargando = false; headerVisible = true } }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Historial de Pagos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Tus transacciones premium", fontSize = 12.sp, color = Color.White.copy(0.35f))
                }
                IconButton(onClick = { cargarDatos() }) { Icon(Icons.Default.Refresh, null, tint = BlueAccent) }
            }
            Spacer(Modifier.height(16.dp))

            if (cargando) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent, strokeWidth = 3.dp) }
            } else if (errorConexion) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Error de conexión", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { cargarDatos() }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text("Reintentar", fontWeight = FontWeight.Bold) }
                    }
                }
            } else if (pagos.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💳", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Sin pagos registrados", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Aquí verás tus transacciones", fontSize = 13.sp, color = Color.White.copy(0.4f))
                    }
                }
            } else {
                // ── Summary Card ──
                val totalGastado = pagos.sumOf { it.monto }
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(summaryAlpha).scale(summaryScale)
                        .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(BlueAccent.copy(0.15f), BlueElectric.copy(0.08f)))).padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("TOTAL INVERTIDO", fontSize = 11.sp, color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("${String.format("%.2f", totalGastado)}€", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            }
                            Box(Modifier.size(50.dp).background(BlueAccent.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("💎", fontSize = 24.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                Text("TRANSACCIONES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.3f), letterSpacing = 3.sp)
                Spacer(Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(pagos) { index, p ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(index * 60L); itemVisible = true }
                        val itemAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400), label = "ph$index")

                        Card(
                            modifier = Modifier.fillMaxWidth().alpha(itemAlpha)
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.05f), spotColor = BlueAccent.copy(0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF162347))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).background(BlueAccent.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("💳", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.concepto, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                                    Text(p.fecha, fontSize = 12.sp, color = BlueSoft.copy(0.5f))
                                }
                                Text("${p.monto}€", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}
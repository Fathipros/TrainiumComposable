package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Reserva(val id: Int, val maquina: String, val fecha: String, val horaInicio: String, val horaFin: String, val usuario: String)

@Composable
fun ReservasScreen(isAdmin: Boolean, idUsuario: Int, onBack: () -> Unit) {
    var reservas by remember { mutableStateOf(listOf<Reserva>()) }
    var cargando by remember { mutableStateOf(true) }
    var errorConexion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var countVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val countAlpha by animateFloatAsState(if (countVisible) 1f else 0f, tween(600), label = "c")
    val countScale by animateFloatAsState(if (countVisible) 1f else 0.9f, tween(600, easing = FastOutSlowInEasing), label = "cs")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.25f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "g")

    fun cargarDatos() {
        cargando = true; errorConexion = false; headerVisible = false; countVisible = false
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection(); val tmp = mutableListOf<Reserva>()
                if (conn != null) {
                    val sql = if (isAdmin) "SELECT r.ID, m.NOMBRE as MAQUINA, r.FECHA, r.HORA_INICIO, r.HORA_FIN, u.NOMBRE as USUARIO FROM RESERVAS r INNER JOIN MAQUINAS m ON r.ID_MAQUINA = m.ID INNER JOIN USUARIO u ON r.ID_USUARIO = u.ID ORDER BY r.FECHA DESC"
                    else "SELECT r.ID, m.NOMBRE as MAQUINA, r.FECHA, r.HORA_INICIO, r.HORA_FIN, '' as USUARIO FROM RESERVAS r INNER JOIN MAQUINAS m ON r.ID_MAQUINA = m.ID WHERE r.ID_USUARIO = ? ORDER BY r.FECHA DESC"
                    val pstmt = conn.prepareStatement(sql); if (!isAdmin) pstmt.setInt(1, idUsuario)
                    val rs = pstmt.executeQuery()
                    while (rs.next()) tmp.add(Reserva(rs.getInt("ID"), rs.getString("MAQUINA") ?: "", rs.getString("FECHA") ?: "", rs.getString("HORA_INICIO") ?: "", rs.getString("HORA_FIN") ?: "", rs.getString("USUARIO") ?: ""))
                    conn.close()
                } else { withContext(Dispatchers.Main) { errorConexion = true } }
                withContext(Dispatchers.Main) { reservas = tmp; cargando = false; delay(80); headerVisible = true; delay(150); countVisible = true }
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
                    Text("Mis Reservas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(if (isAdmin) "Gestión de reservas" else "Tus reservas activas", fontSize = 12.sp, color = Color.White.copy(0.35f))
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
            } else if (reservas.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Sin reservas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Reserva una máquina para empezar", fontSize = 13.sp, color = Color.White.copy(0.4f))
                    }
                }
            } else {
                // ── Count Badge ──
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(countAlpha).scale(countScale)
                        .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(BlueAccent.copy(0.12f), BlueElectric.copy(0.06f)))).padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(BlueAccent.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text("📅", fontSize = 22.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("RESERVAS ACTIVAS", fontSize = 11.sp, color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                Text("${reservas.size}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(reservas) { index, r ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(index * 60L); itemVisible = true }
                        val itemAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400), label = "rv$index")

                        Card(
                            modifier = Modifier.fillMaxWidth().alpha(itemAlpha)
                                .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.05f), spotColor = BlueAccent.copy(0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF162347))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).background(BlueAccent.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Text("🏋️", fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.maquina, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📆", fontSize = 11.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(r.fecha, fontSize = 12.sp, color = BlueSoft.copy(0.5f))
                                        Spacer(Modifier.width(8.dp))
                                        Text("⏰", fontSize = 11.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text("${r.horaInicio}-${r.horaFin}", fontSize = 12.sp, color = BlueSoft.copy(0.5f))
                                    }
                                    if (isAdmin && r.usuario.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("👤 ${r.usuario}", fontSize = 12.sp, color = Color.White.copy(0.3f))
                                    }
                                }
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) { val conn = DatabaseAdmin.connection(); if (conn != null) { try { conn.prepareStatement("DELETE FROM RESERVAS WHERE ID = ?").apply { setInt(1, r.id); executeUpdate() }; conn.close(); withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Reserva eliminada", Toast.LENGTH_SHORT).show() } } catch (e: Exception) { e.printStackTrace() } } }
                                }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.6f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
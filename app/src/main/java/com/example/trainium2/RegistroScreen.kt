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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

data class PesoRegistro(val id: Int, val peso: Double, val fecha: String)

@Composable
fun RegistroScreen(idUsuario: Int, onBack: () -> Unit) {
    var registros by remember { mutableStateOf(listOf<PesoRegistro>()) }
    var pesoCampo by remember { mutableStateOf("") }
    var editandoId by remember { mutableStateOf<Int?>(null) }
    var editandoPeso by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }
    var errorConexion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var inputVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val inputAlpha by animateFloatAsState(if (inputVisible) 1f else 0f, tween(500), label = "i")
    val inputScale by animateFloatAsState(if (inputVisible) 1f else 0.95f, tween(500, easing = FastOutSlowInEasing), label = "is")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.25f, infiniteRepeatable(tween(2500), RepeatMode.Reverse), label = "g")

    fun cargarDatos() {
        cargando = true; errorConexion = false
        headerVisible = false; inputVisible = false
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection()
                val tmp = mutableListOf<PesoRegistro>()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT ID, PESO, FECHA FROM REGISTRO_PESO WHERE ID_USUARIO = ? ORDER BY FECHA DESC").apply { setInt(1, idUsuario) }.executeQuery()
                    while (rs.next()) tmp.add(PesoRegistro(rs.getInt("ID"), rs.getDouble("PESO"), rs.getString("FECHA") ?: ""))
                    conn.close()
                } else { withContext(Dispatchers.Main) { errorConexion = true } }
                withContext(Dispatchers.Main) { registros = tmp; cargando = false; delay(80); headerVisible = true; delay(120); inputVisible = true }
            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { errorConexion = true; cargando = false; delay(80); headerVisible = true; delay(120); inputVisible = true } }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Mi Registro de Peso", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Controla tu progreso", fontSize = 12.sp, color = Color.White.copy(0.35f))
                }
                IconButton(onClick = { cargarDatos() }) { Icon(Icons.Default.Refresh, null, tint = BlueAccent) }
            }
            Spacer(Modifier.height(16.dp))

            // ── Input Card ──
            Card(
                modifier = Modifier.fillMaxWidth().alpha(inputAlpha).scale(inputScale)
                    .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF162347))))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(BlueAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Text("⚖️", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedTextField(
                            value = pesoCampo, onValueChange = { pesoCampo = it },
                            modifier = Modifier.weight(1f), label = { Text("Peso (kg)") }, singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueAccent, unfocusedBorderColor = Color.White.copy(0.15f),
                                focusedLabelColor = BlueAccent, unfocusedLabelColor = Color.White.copy(0.4f),
                                cursorColor = BlueAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f))
                        )
                        Spacer(Modifier.width(10.dp))
                        FloatingActionButton(
                            onClick = {
                                val pesoVal = pesoCampo.replace(",", ".").toDoubleOrNull()
                                if (pesoVal == null) { Toast.makeText(context, "Introduce un peso válido", Toast.LENGTH_SHORT).show(); return@FloatingActionButton }
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val conn = DatabaseAdmin.connection()
                                        if (conn != null) {
                                            val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                            conn.prepareStatement("INSERT INTO REGISTRO_PESO (ID_USUARIO, PESO, FECHA) VALUES (?, ?, ?)").apply { setInt(1, idUsuario); setDouble(2, pesoVal); setString(3, hoy); executeUpdate() }
                                            conn.close()
                                            withContext(Dispatchers.Main) { pesoCampo = ""; Toast.makeText(context, "✅ Peso registrado", Toast.LENGTH_SHORT).show(); cargarDatos() }
                                        } else { withContext(Dispatchers.Main) { Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show() } }
                                    } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Solo puedes registrar un peso por día", Toast.LENGTH_SHORT).show(); cargarDatos() } }
                                }
                            },
                            containerColor = BlueAccent, contentColor = Color.White,
                            modifier = Modifier.size(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Icon(Icons.Default.Add, "Añadir") }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (cargando) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent, strokeWidth = 3.dp) }
            } else if (errorConexion) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Error de conexión", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Verifica tu conexión", fontSize = 13.sp, color = Color.White.copy(0.4f))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { cargarDatos() }, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) { Text("Reintentar", fontWeight = FontWeight.Bold) }
                    }
                }
            } else if (registros.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                        Text("Sin registros", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Añade tu primer peso arriba ☝️", fontSize = 13.sp, color = Color.White.copy(0.4f))
                    }
                }
            } else {
                // ── Section Label ──
                Text("HISTORIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.3f), letterSpacing = 3.sp)
                Spacer(Modifier.height(10.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    itemsIndexed(registros) { index, reg ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { delay(index * 60L); itemVisible = true }
                        val itemAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400), label = "ri$index")
                        val itemScale by animateFloatAsState(if (itemVisible) 1f else 0.95f, tween(400, easing = FastOutSlowInEasing), label = "ris$index")

                        Card(
                            modifier = Modifier.fillMaxWidth().alpha(itemAlpha).scale(itemScale)
                                .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.05f), spotColor = BlueAccent.copy(0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF162347))
                        ) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                // Weight indicator dot
                                Box(Modifier.size(8.dp).background(BlueAccent, CircleShape))
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    if (editandoId == reg.id) {
                                        OutlinedTextField(
                                            value = editandoPeso, onValueChange = { editandoPeso = it },
                                            modifier = Modifier.width(120.dp), singleLine = true, shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueAccent, unfocusedBorderColor = Color.White.copy(0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f), cursorColor = BlueAccent)
                                        )
                                    } else {
                                        Text("${reg.peso} kg", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                                        Text(reg.fecha, fontSize = 12.sp, color = BlueSoft.copy(0.5f))
                                    }
                                }
                                // Trend badge
                                if (editandoId != reg.id && index < registros.size - 1) {
                                    val diff = reg.peso - registros[index + 1].peso
                                    val trendColor = if (diff > 0) Color(0xFFFF6B6B) else if (diff < 0) Color(0xFF00E676) else Color.White.copy(0.3f)
                                    val trendText = if (diff > 0) "↑${String.format("%.1f", diff)}" else if (diff < 0) "↓${String.format("%.1f", -diff)}" else "="
                                    Box(Modifier.background(trendColor.copy(0.12f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                        Text(trendText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = trendColor)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                                if (editandoId == reg.id) {
                                    TextButton(onClick = {
                                        val np = editandoPeso.replace(",", ".").toDoubleOrNull() ?: return@TextButton
                                        scope.launch(Dispatchers.IO) { try { val conn = DatabaseAdmin.connection(); if (conn != null) { conn.prepareStatement("UPDATE REGISTRO_PESO SET PESO = ? WHERE ID = ?").apply { setDouble(1, np); setInt(2, reg.id); executeUpdate() }; conn.close(); withContext(Dispatchers.Main) { editandoId = null; cargarDatos() } } } catch (e: Exception) { e.printStackTrace() } }
                                    }) { Text("✓", color = BlueAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                } else {
                                    IconButton(onClick = { editandoId = reg.id; editandoPeso = reg.peso.toString() }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, null, tint = Color.White.copy(0.25f), modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(onClick = {
                                    scope.launch(Dispatchers.IO) { try { val conn = DatabaseAdmin.connection(); if (conn != null) { conn.prepareStatement("DELETE FROM REGISTRO_PESO WHERE ID = ?").apply { setInt(1, reg.id); executeUpdate() }; conn.close(); withContext(Dispatchers.Main) { cargarDatos() } } } catch (e: Exception) { e.printStackTrace() } }
                                }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B).copy(0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.example.trainium2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class Maquina(val id: Int, val nombre: String, val tipo: String, val estado: Int, val descripcion: String, val operativa: Int)

@Composable
fun MaquinasScreen(isAdmin: Boolean, idUsuario: Int, onBack: () -> Unit) {
    var listaMaquinas by remember { mutableStateOf(listOf<Maquina>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var mostrarDialogoAdd by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoTipo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }
    var maquinaParaReservar by remember { mutableStateOf<Maquina?>(null) }
    val calendar = Calendar.getInstance()

    fun cargarDatos() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection()
                val tmp = mutableListOf<Maquina>()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT ID, NOMBRE, TIPO, ESTADO, DESCRIPCION FROM MAQUINAS").executeQuery()
                    while (rs.next()) tmp.add(Maquina(rs.getInt("ID"), rs.getString("NOMBRE") ?: "", rs.getString("TIPO") ?: "", rs.getInt("ESTADO"), rs.getString("DESCRIPCION") ?: "", rs.getInt("ESTADO")))
                    conn.close()
                }
                withContext(Dispatchers.Main) { listaMaquinas = tmp; cargando = false }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun alternarEstadoOperativo(maquina: Maquina) {
        scope.launch(Dispatchers.IO) {
            val nuevoEstado = if (maquina.operativa == 1) 0 else 1
            val conn = DatabaseAdmin.connection()
            if (conn != null) { try { val pstmt = conn.prepareStatement("UPDATE MAQUINAS SET ESTADO = ? WHERE ID = ?"); pstmt.setInt(1, nuevoEstado); pstmt.setInt(2, maquina.id); pstmt.executeUpdate(); conn.close(); withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show() } } catch (e: Exception) { e.printStackTrace() } }
        }
    }

    fun eliminarMaquina(id: Int) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) { try { val pstmt = conn.prepareStatement("DELETE FROM MAQUINAS WHERE ID = ?"); pstmt.setInt(1, id); pstmt.executeUpdate(); conn.close(); withContext(Dispatchers.Main) { cargarDatos(); Toast.makeText(context, "Máquina eliminada", Toast.LENGTH_SHORT).show() } } catch (e: Exception) { e.printStackTrace() } }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    fun ejecutarReserva(maquina: Maquina, fecha: String, horaInicio: String) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val calFin = Calendar.getInstance().apply { time = sdf.parse(horaInicio)!!; add(Calendar.HOUR, 1) }
                    val horaFin = sdf.format(calFin.time)
                    val pstmt = conn.prepareStatement("INSERT INTO RESERVAS (ID_USUARIO, ID_MAQUINA, FECHA, HORA_INICIO, HORA_FIN) VALUES (?, ?, ?, ?, ?)")
                    pstmt.setInt(1, idUsuario); pstmt.setInt(2, maquina.id); pstmt.setString(3, fecha); pstmt.setString(4, "$horaInicio:00"); pstmt.setString(5, "$horaFin:00")
                    pstmt.executeUpdate(); conn.close()
                    withContext(Dispatchers.Main) { Toast.makeText(context, "Reserva: $horaInicio a $horaFin", Toast.LENGTH_LONG).show() }
                } catch (e: Exception) { withContext(Dispatchers.Main) { Toast.makeText(context, "Horario no disponible", Toast.LENGTH_SHORT).show() } }
            }
        }
    }

    val datePickerDialog = DatePickerDialog(context, { _, y, m, d ->
        val fechaSel = String.format("%d-%02d-%02d", y, m + 1, d)
        TimePickerDialog(context, { _, h, min ->
            if (h in 7..20) { maquinaParaReservar?.let { ejecutarReserva(it, fechaSel, String.format("%02d:%02d", h, min)) } }
            else Toast.makeText(context, "Horario permitido: 07:00 a 21:00", Toast.LENGTH_LONG).show()
        }, 12, 0, true).show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
        datePicker.minDate = System.currentTimeMillis(); datePicker.maxDate = System.currentTimeMillis() + (21L * 24 * 60 * 60 * 1000)
    }

    Scaffold(containerColor = Color.Transparent, floatingActionButton = {
        if (isAdmin) FloatingActionButton(onClick = { mostrarDialogoAdd = true }, containerColor = BlueAccent, contentColor = Color.White) { Icon(Icons.Default.Add, "Añadir") }
    }) { padding ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                    Column(Modifier.weight(1f)) {
                        Text("Reservar Máquina", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Selecciona y reserva tu equipo", fontSize = 12.sp, color = Color.White.copy(0.35f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (!cargando && listaMaquinas.isNotEmpty()) {
                    Text("${listaMaquinas.size} EQUIPOS DISPONIBLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(0.3f), letterSpacing = 3.sp)
                    Spacer(Modifier.height(10.dp))
                }
                if (cargando) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent) } }
                else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(listaMaquinas) { index, maquina ->
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { delay(index * 70L); itemVisible = true }
                            val itemAlpha by animateFloatAsState(if (itemVisible) 1f else 0f, tween(400), label = "i$index")

                            Card(
                                Modifier.fillMaxWidth().alpha(itemAlpha)
                                    .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.08f), spotColor = BlueAccent.copy(0.08f)),
                                elevation = CardDefaults.cardElevation(0.dp), shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF162347).copy(0.9f))
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val resId = context.resources.getIdentifier("maquina${maquina.id}", "drawable", context.packageName)
                                        if (resId != 0) Image(painterResource(resId), null, Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(maquina.nombre, fontWeight = FontWeight.Bold, color = Color.White)
                                            if (maquina.operativa == 0) Text("FUERA DE SERVICIO", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            else Text(maquina.tipo, fontSize = 12.sp, color = BlueSoft.copy(0.6f))
                                        }
                                        if (isAdmin) {
                                            IconButton(onClick = { alternarEstadoOperativo(maquina) }) { Icon(if (maquina.operativa == 1) Icons.Default.Build else Icons.Default.CheckCircle, null, tint = if (maquina.operativa == 1) Color.White.copy(0.4f) else BlueAccent) }
                                            IconButton(onClick = { eliminarMaquina(maquina.id) }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF6B6B)) }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(maquina.descripcion, fontSize = 13.sp, color = Color.White.copy(0.45f))
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { maquinaParaReservar = maquina; datePickerDialog.show() },
                                        enabled = maquina.operativa == 1, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.White.copy(0.05f), disabledContentColor = Color.White.copy(0.3f)),
                                        contentPadding = PaddingValues()
                                    ) {
                                        Box(Modifier.fillMaxWidth().height(42.dp).background(
                                            if (maquina.operativa == 1) Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)) else Brush.horizontalGradient(listOf(Color.White.copy(0.05f), Color.White.copy(0.05f))),
                                            RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                            Text(if (maquina.operativa == 1) "Reservar" else "No disponible", fontWeight = FontWeight.SemiBold, color = if (maquina.operativa == 1) Color.White else Color.White.copy(0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAdd) {
        val dColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BlueAccent, unfocusedBorderColor = Color.White.copy(0.2f), focusedLabelColor = BlueAccent, cursorColor = BlueAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f))
        AlertDialog(onDismissRequest = { mostrarDialogoAdd = false }, containerColor = Color(0xFF162347),
            title = { Text("Añadir máquina", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, shape = RoundedCornerShape(12.dp), colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevoTipo, onValueChange = { nuevoTipo = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tipo") }, shape = RoundedCornerShape(12.dp), colors = dColors)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = nuevaDesc, onValueChange = { nuevaDesc = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Descripción") }, shape = RoundedCornerShape(12.dp), colors = dColors)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        if (conn != null) { try { val pstmt = conn.prepareStatement("INSERT INTO MAQUINAS (NOMBRE, TIPO, ESTADO, DESCRIPCION) VALUES (?, ?, 1, ?)"); pstmt.setString(1, nuevoNombre); pstmt.setString(2, nuevoTipo); pstmt.setString(3, nuevaDesc); pstmt.executeUpdate(); conn.close(); withContext(Dispatchers.Main) { mostrarDialogoAdd = false; nuevoNombre = ""; nuevoTipo = ""; nuevaDesc = ""; cargarDatos() } } catch (e: Exception) { e.printStackTrace() } }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = BlueAccent, contentColor = Color.White)) { Text("Guardar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoAdd = false }) { Text("Cancelar", color = Color.White.copy(0.5f)) } }
        )
    }
}
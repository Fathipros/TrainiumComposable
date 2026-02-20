package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var telf by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var iconVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var btnVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80); iconVisible = true
        delay(120); titleVisible = true
        delay(150); formVisible = true
        delay(150); btnVisible = true
    }

    val iconAlpha by animateFloatAsState(if (iconVisible) 1f else 0f, tween(700), label = "ia")
    val iconScale by animateFloatAsState(if (iconVisible) 1f else 0.6f, tween(700, easing = FastOutSlowInEasing), label = "is")
    val titleAlpha by animateFloatAsState(if (titleVisible) 1f else 0f, tween(500), label = "ta")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "fa")
    val formScale by animateFloatAsState(if (formVisible) 1f else 0.95f, tween(500, easing = FastOutSlowInEasing), label = "fs")
    val btnAlpha by animateFloatAsState(if (btnVisible) 1f else 0f, tween(500), label = "ba")

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(0.15f, 0.35f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "g")

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BlueAccent, unfocusedBorderColor = Color.White.copy(0.15f),
        focusedLabelColor = BlueAccent, unfocusedLabelColor = Color.White.copy(0.4f),
        cursorColor = BlueAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f)
    )

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            // ── Back ──
            TextButton(onClick = onBack, Modifier.align(Alignment.Start)) {
                Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))

            // ── Icon with glow ──
            Box(contentAlignment = Alignment.Center, modifier = Modifier.alpha(iconAlpha).scale(iconScale)) {
                Box(Modifier.size(90.dp).shadow(24.dp, CircleShape, ambientColor = Color(0xFF00E676).copy(glowAlpha), spotColor = Color(0xFF00E676).copy(glowAlpha)).background(Brush.radialGradient(listOf(Color(0xFF00E676).copy(glowAlpha * 0.5f), Color.Transparent)), CircleShape))
                Text("✨", fontSize = 48.sp)
            }
            Spacer(Modifier.height(10.dp))

            // ── Title ──
            Text("Crear Cuenta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp, modifier = Modifier.alpha(titleAlpha))
            Text("Únete a la comunidad Trainium", fontSize = 13.sp, color = BlueSoft.copy(0.5f), modifier = Modifier.alpha(titleAlpha))
            Spacer(Modifier.height(22.dp))

            // ── Form Card ──
            Card(
                modifier = Modifier.fillMaxWidth().alpha(formAlpha).scale(formScale)
                    .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = BlueAccent.copy(0.08f), spotColor = BlueAccent.copy(0.08f)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162347).copy(0.85f))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("DATOS PERSONALES", fontSize = 11.sp, color = Color.White.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre completo") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("👤", fontSize = 16.sp) })
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = dni, onValueChange = { dni = it.uppercase() }, modifier = Modifier.fillMaxWidth(), label = { Text("DNI (8 números + 1 letra)") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🪪", fontSize = 16.sp) })
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📧", fontSize = 16.sp) })
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🔒", fontSize = 16.sp) })
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = telf, onValueChange = { telf = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📱", fontSize = 16.sp) })
                }
            }

            Spacer(Modifier.height(22.dp))

            // ── Register Button ──
            Button(
                onClick = {
                    if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || pass.isEmpty() || telf.isEmpty()) {
                        Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show(); return@Button
                    }
                    val dniRegex = Regex("^[0-9]{8}[A-Z]$")
                    if (!dni.matches(dniRegex)) {
                        Toast.makeText(context, "Formato de DNI inválido (Ej: 12345678A)", Toast.LENGTH_SHORT).show(); return@Button
                    }
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection(); var errorMsg: String? = null; var exito = false
                        if (conn != null) {
                            try {
                                val checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM USUARIO WHERE DNI = ?")
                                checkStmt.setString(1, dni); val rs = checkStmt.executeQuery(); rs.next()
                                if (rs.getInt(1) > 0) { errorMsg = "El DNI ya está registrado" }
                                else {
                                    val sql = "INSERT INTO USUARIO (NOMBRE, DNI, EMAIL, CONTRASEÑA_HASH, TELEFONO, ADMIN, PREMIUM) VALUES (?, ?, ?, ?, ?, 0, 0)"
                                    val pstmt = conn.prepareStatement(sql)
                                    pstmt.setString(1, nombre); pstmt.setString(2, dni); pstmt.setString(3, email); pstmt.setString(4, pass); pstmt.setString(5, telf)
                                    if (pstmt.executeUpdate() > 0) exito = true
                                }
                                conn.close()
                            } catch (e: Exception) { errorMsg = "Error en la base de datos: ${e.message}"; e.printStackTrace() }
                        } else { errorMsg = "No se pudo conectar con el servidor" }
                        withContext(Dispatchers.Main) {
                            if (exito) { Toast.makeText(context, "✅ Registro completado con éxito", Toast.LENGTH_LONG).show(); onBack() }
                            else Toast.makeText(context, errorMsg ?: "Error desconocido", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).alpha(btnAlpha)
                    .shadow(14.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.4f), spotColor = BlueAccent.copy(0.4f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
            ) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("🎉 Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onBack, Modifier.fillMaxWidth().alpha(btnAlpha)) { Text("¿Ya tienes cuenta? Inicia sesión", color = Color.White.copy(0.5f), fontSize = 13.sp) }
            Spacer(Modifier.height(16.dp))
        }
    }
}
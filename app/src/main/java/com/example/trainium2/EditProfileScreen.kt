package com.example.trainium2

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainium2.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    idUsuario: Int,
    onBack: () -> Unit,
    onNavigateToHistorial: (Int) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPremium by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var headerVisible by remember { mutableStateOf(false) }
    var avatarVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var premiumVisible by remember { mutableStateOf(false) }
    val headerAlpha by animateFloatAsState(if (headerVisible) 1f else 0f, tween(500), label = "h")
    val avatarAlpha by animateFloatAsState(if (avatarVisible) 1f else 0f, tween(600), label = "av")
    val avatarScale by animateFloatAsState(if (avatarVisible) 1f else 0.8f, tween(600, easing = FastOutSlowInEasing), label = "as")
    val formAlpha by animateFloatAsState(if (formVisible) 1f else 0f, tween(500), label = "f")
    val premiumAlpha by animateFloatAsState(if (premiumVisible) 1f else 0f, tween(500), label = "p")

    val infiniteTransition = rememberInfiniteTransition(label = "g")
    val glowAlpha by infiniteTransition.animateFloat(0.1f, 0.3f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ga")

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BlueAccent, unfocusedBorderColor = Color.White.copy(0.15f),
        focusedLabelColor = BlueAccent, unfocusedLabelColor = Color.White.copy(0.4f),
        cursorColor = BlueAccent, focusedTextColor = Color.White, unfocusedTextColor = Color.White.copy(0.9f)
    )

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) { try { val stmt = conn.prepareStatement("SELECT NOMBRE, EMAIL, TELEFONO, PREMIUM FROM USUARIO WHERE ID = ?"); stmt.setInt(1, idUsuario); val rs = stmt.executeQuery(); if (rs.next()) { nombre = rs.getString("NOMBRE") ?: ""; email = rs.getString("EMAIL") ?: ""; telefono = rs.getString("TELEFONO") ?: ""; isPremium = rs.getInt("PREMIUM") == 1 } } catch (e: Exception) { e.printStackTrace() } finally { conn.close() } }
            withContext(Dispatchers.Main) { cargando = false; delay(80); headerVisible = true; delay(120); avatarVisible = true; delay(150); formVisible = true; delay(150); premiumVisible = true }
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BlueDark, BlueMid, BlueDeep)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // ── Header ──
            Row(Modifier.fillMaxWidth().alpha(headerAlpha), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver", color = BlueAccent, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) {
                    Text("Ajustes de Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Edita tu información personal", fontSize = 12.sp, color = Color.White.copy(0.35f))
                }
            }

            if (cargando) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BlueAccent, strokeWidth = 3.dp) }
            } else {
                Spacer(Modifier.height(20.dp))

                // ── Avatar Section ──
                Box(Modifier.fillMaxWidth().alpha(avatarAlpha).scale(avatarScale), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(90.dp).shadow(20.dp, CircleShape, ambientColor = BlueAccent.copy(glowAlpha), spotColor = BlueAccent.copy(glowAlpha)).background(Brush.linearGradient(listOf(BlueAccent.copy(0.2f), BlueElectric.copy(0.1f))), CircleShape), contentAlignment = Alignment.Center) {
                        Text(if (nombre.isNotEmpty()) nombre.first().uppercaseChar().toString() else "👤", fontSize = if (nombre.isNotEmpty()) 36.sp else 40.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                    }
                }
                Spacer(Modifier.height(24.dp))

                // ── Form Card ──
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(formAlpha)
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = BlueAccent.copy(0.05f), spotColor = BlueAccent.copy(0.05f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162347))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("INFORMACIÓN PERSONAL", fontSize = 11.sp, color = Color.White.copy(0.3f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre completo") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("👤", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📧", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = telefono, onValueChange = { telefono = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Teléfono") }, singleLine = true, shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("📱", fontSize = 16.sp) })
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nueva contraseña (opcional)") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), colors = inputColors, leadingIcon = { Text("🔒", fontSize = 16.sp) })
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ── Save Button ──
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val conn = DatabaseAdmin.connection()
                            if (conn != null) { try {
                                val sql = if (password.isEmpty()) "UPDATE USUARIO SET NOMBRE = ?, EMAIL = ?, TELEFONO = ? WHERE ID = ?" else "UPDATE USUARIO SET NOMBRE = ?, EMAIL = ?, TELEFONO = ?, CONTRASEÑA_HASH = ? WHERE ID = ?"
                                val pstmt = conn.prepareStatement(sql); pstmt.setString(1, nombre); pstmt.setString(2, email); pstmt.setString(3, telefono)
                                if (password.isEmpty()) pstmt.setInt(4, idUsuario) else { pstmt.setString(4, password); pstmt.setInt(5, idUsuario) }
                                pstmt.executeUpdate(); withContext(Dispatchers.Main) { Toast.makeText(context, "✅ Perfil actualizado", Toast.LENGTH_SHORT).show() }
                            } catch (e: Exception) { e.printStackTrace() } finally { conn.close() } }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp).alpha(formAlpha)
                        .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = BlueAccent.copy(0.3f), spotColor = BlueAccent.copy(0.3f)),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
                ) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(BlueAccent, BlueElectric)), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Text("💾 Guardar cambios", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Premium Status Card ──
                Card(
                    modifier = Modifier.fillMaxWidth().alpha(premiumAlpha)
                        .shadow(if (isPremium) 12.dp else 4.dp, RoundedCornerShape(18.dp), ambientColor = if (isPremium) Color(0xFFFFD700).copy(glowAlpha) else BlueAccent.copy(0.05f), spotColor = if (isPremium) Color(0xFFFFD700).copy(glowAlpha) else BlueAccent.copy(0.05f)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(Modifier.fillMaxWidth().background(if (isPremium) Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF2A1D54))) else Brush.linearGradient(listOf(Color(0xFF1A2D54), Color(0xFF162347)))).padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(if (isPremium) Color(0xFFFFD700).copy(0.15f) else BlueAccent.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Star, null, tint = if (isPremium) Color(0xFFFFD700) else Color.White.copy(0.3f), modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("ESTADO DE CUENTA", fontSize = 11.sp, color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                if (isPremium) Text("PREMIUM ⭐", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                else Text("Estándar", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 16.sp)
                            }
                            if (!isPremium) {
                                Button(onClick = onNavigateToPremium, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)) {
                                    Text("Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(Modifier.padding(vertical = 6.dp), 1.dp, Color.White.copy(0.06f))
                OutlinedButton(onClick = { onNavigateToHistorial(idUsuario) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent.copy(0.3f))) {
                    Text("💳 Ver historial de pagos", color = BlueAccent)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
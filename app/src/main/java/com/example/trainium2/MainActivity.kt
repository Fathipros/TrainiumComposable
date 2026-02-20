package com.example.trainium2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trainium2.ui.theme.Trainium2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            val toggleTheme = { isDarkTheme = !isDarkTheme }

            Trainium2Theme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") {
                            SplashVideoScreen(
                                onVideoFinished = {
                                    navController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main") {
                            MainScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = toggleTheme,
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToForgot = { navController.navigate("forgot") },
                                onLoginSuccess = { nombre, isAdmin, idUsuario, isPremium ->
                                    navController.navigate("profile/$nombre/$isAdmin/$idUsuario/$isPremium")
                                }
                            )
                        }

                        composable(
                            "profile/{nombre}/{isAdmin}/{idUsuario}/{isPremium}",
                            arguments = listOf(
                                navArgument("nombre") { type = NavType.StringType },
                                navArgument("isAdmin") { type = NavType.IntType },
                                navArgument("idUsuario") { type = NavType.IntType },
                                navArgument("isPremium") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                            val isAdmin = backStackEntry.arguments?.getInt("isAdmin") == 1
                            val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                            val isPremium = backStackEntry.arguments?.getInt("isPremium") == 1

                            ProfileScreen(
                                nombre = nombre,
                                isAdmin = isAdmin,
                                idUsuario = idUsuario,
                                isPremium = isPremium,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = toggleTheme,
                                onLogout = { navController.navigate("main") { popUpTo(0) } },
                                onNavigateToMaquinas = { admin, id ->
                                    navController.navigate("maquinas/${if(admin) 1 else 0}/$id")
                                },
                                onNavigateToPlatos = { navController.navigate("platos") },
                                onNavigateToRegistro = { id -> navController.navigate("registro/$id") },
                                onNavigateToReservas = { admin, id ->
                                    navController.navigate("reservas/${if(admin) 1 else 0}/$id")
                                },
                                onNavigateToEditProfile = { id -> navController.navigate("edit_profile/$id") }
                            )
                        }

                        composable(
                            "edit_profile/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            EditProfileScreen(
                                idUsuario = id,
                                onBack = { navController.popBackStack() },
                                onNavigateToHistorial = { i -> navController.navigate("historial/$i") },
                                onNavigateToPremium = { navController.navigate("premium_selection/$id") }
                            )
                        }

                        composable(
                            "historial/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            HistorialScreen(idUsuario = id, onBack = { navController.popBackStack() })
                        }

                        composable(
                            "premium_selection/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            PremiumSelectionScreen(idUsuario = id, onBack = { navController.popBackStack() }, onSuccess = { navController.popBackStack() })
                        }

                        composable(
                            "maquinas/{isAdmin}/{idUsuario}",
                            arguments = listOf(
                                navArgument("isAdmin") { type = NavType.IntType },
                                navArgument("idUsuario") { type = NavType.IntType }
                            )
                        ) { bse ->
                            val admin = bse.arguments?.getInt("isAdmin") == 1
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            MaquinasScreen(isAdmin = admin, idUsuario = id, onBack = { navController.popBackStack() })
                        }

                        composable(
                            "registro/{idUsuario}",
                            arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                        ) { bse ->
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            RegistroScreen(idUsuario = id, onBack = { navController.popBackStack() })
                        }

                        composable(
                            "reservas/{isAdmin}/{idUsuario}",
                            arguments = listOf(
                                navArgument("isAdmin") { type = NavType.IntType },
                                navArgument("idUsuario") { type = NavType.IntType }
                            )
                        ) { bse ->
                            val admin = bse.arguments?.getInt("isAdmin") == 1
                            val id = bse.arguments?.getInt("idUsuario") ?: 0
                            ReservasScreen(isAdmin = admin, idUsuario = id, onBack = { navController.popBackStack() })
                        }

                        composable("platos") { PlatosScreen { navController.popBackStack() } }
                        composable("register") { RegisterScreen { navController.popBackStack() } }
                        composable("forgot") { ForgotPasswordScreen { navController.popBackStack() } }
                    }
                }
            }
        }
    }
}
package com.lexia.legalassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lexia.legalassistant.navigation.LexiaNavHost
import com.lexia.legalassistant.navigation.Screen
import com.lexia.legalassistant.ui.components.LexiaBottomBar
import com.lexia.legalassistant.ui.components.LexiaTopBar
import com.lexia.legalassistant.ui.theme.LexiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LexiaTheme {
                LexiaMainApp()
            }
        }
    }
}

@Composable
fun LexiaMainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Chat.route

    val currentTitle = when (currentRoute) {
        Screen.Catalog.route -> "Catálogo de Leyes"
        Screen.Chat.route -> "Chat RAG"
        Screen.History.route -> "Historial & Memoria"
        Screen.Profile.route -> "Autenticación & Licencia"
        Screen.ArticleDetail.route -> "Inspector de Vectores"
        Screen.PdfViewer.route -> "Visor de Dictamen"
        else -> "Asistente Jurídico"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentRoute != Screen.Auth.route) {
                LexiaTopBar(
                    subtitle = currentTitle,
                    onProfileClick = {
                        if (currentRoute != Screen.Profile.route) {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentRoute != Screen.Auth.route) {
                LexiaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { targetScreen ->
                        if (currentRoute != targetScreen.route) {
                            if (targetScreen == Screen.Catalog) {
                                // Regreso garantizado a la raíz del catálogo limpiando pantallas superiores en la pila
                                navController.navigate(Screen.Catalog.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(targetScreen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LexiaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

package com.lexia.legalassistant.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lexia.legalassistant.navigation.Screen
import com.lexia.legalassistant.ui.theme.*

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun LexiaBottomBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Catalog, Icons.Default.MenuBook, "Catálogo"),
        BottomNavItem(Screen.Chat, Icons.Default.SmartToy, "Chat RAG"),
        BottomNavItem(Screen.History, Icons.Default.History, "Historial"),
        BottomNavItem(Screen.Profile, Icons.Default.VerifiedUser, "Perfil")
    )

    NavigationBar(
        containerColor = LexiaSurface.copy(alpha = 0.95f),
        tonalElevation = 4.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = LexiaPrimary,
                    selectedTextColor = LexiaPrimary,
                    unselectedIconColor = LexiaOnSurfaceVariant,
                    unselectedTextColor = LexiaOnSurfaceVariant,
                    indicatorColor = LexiaSurfaceContainerHighest
                )
            )
        }
    }
}

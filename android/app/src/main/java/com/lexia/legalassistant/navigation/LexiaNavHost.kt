package com.lexia.legalassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lexia.legalassistant.ui.screens.auth.AuthScreen
import com.lexia.legalassistant.ui.screens.catalog.CatalogScreen
import com.lexia.legalassistant.ui.screens.chat.ArticleDetailScreen
import com.lexia.legalassistant.ui.screens.chat.ChatRagScreen
import com.lexia.legalassistant.ui.screens.dictamen.DictamenPdfScreen
import com.lexia.legalassistant.ui.screens.history.HistoryScreen

@Composable
fun LexiaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    com.lexia.legalassistant.data.repository.AuthRepository.init(context)
    val isUserLoggedIn = com.lexia.legalassistant.data.repository.AuthRepository.currentUser.value != null

    var currentLawId by remember { mutableStateOf("cpeum") }
    var currentArticleId by remember { mutableStateOf<String?>(null) }
    var currentGroupName by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) Screen.Catalog.route else Screen.Auth.route,
        modifier = modifier
    ) {
        composable(Screen.Catalog.route) {
            CatalogScreen(
                onSelectLaw = { law ->
                    LegalNavigationSession.selectLaw(law.id)
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatRagScreen(
                onNavigateToDictamen = { articleId, groupName ->
                    LegalNavigationSession.selectSession(
                        LegalNavigationSession.currentState.value.lawId,
                        articleId,
                        groupName
                    )
                    navController.navigate(Screen.PdfViewer.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onSelectSession = { lawId, articleId, groupName ->
                    LegalNavigationSession.selectSession(lawId, articleId, groupName)
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            com.lexia.legalassistant.ui.screens.profile.ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route)
                },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ArticleDetail.route) {
            ChatRagScreen(
                initialLawId = currentLawId,
                initialArticleId = currentArticleId,
                onNavigateToDictamen = { articleId, groupName ->
                    currentArticleId = articleId
                    currentGroupName = groupName
                    navController.navigate(Screen.PdfViewer.route)
                }
            )
        }

        composable(Screen.PdfViewer.route) {
            DictamenPdfScreen(
                lawId = currentLawId,
                articleId = currentArticleId,
                groupName = currentGroupName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

package com.lexia.legalassistant.navigation

sealed class Screen(val route: String, val title: String) {
    data object Catalog : Screen("catalog", "Catálogo")
    data object Chat : Screen("chat", "Chat RAG")
    data object History : Screen("history", "Historial")
    data object Profile : Screen("profile", "Perfil")
    data object ArticleDetail : Screen("article_detail", "Detalle de Artículo")
    data object PdfViewer : Screen("pdf_viewer", "Visor PDF")
    data object ProcessMail : Screen("process_mail", "Redacción Procesal")
    data object Auth : Screen("auth", "Iniciar Sesión")
}

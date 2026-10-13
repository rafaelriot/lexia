package com.lexia.legalassistant.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton state holder to synchronize selected law, article, and group
 * across navigation tabs (Catalog, History, Chat) without corrupting Compose backstack.
 */
object LegalNavigationSession {
    data class SessionState(
        val lawId: String = "cpeum",
        val articleId: String? = null,
        val groupName: String? = null,
        val updateTimestamp: Long = 0L
    )

    private val _currentState = MutableStateFlow(SessionState())
    val currentState = _currentState.asStateFlow()

    fun selectLaw(lawId: String) {
        _currentState.value = SessionState(
            lawId = lawId,
            articleId = null,
            groupName = null,
            updateTimestamp = System.currentTimeMillis()
        )
    }

    fun selectArticle(lawId: String, articleId: String?) {
        _currentState.value = SessionState(
            lawId = lawId,
            articleId = articleId,
            groupName = null,
            updateTimestamp = System.currentTimeMillis()
        )
    }

    fun selectSession(lawId: String, articleId: String?, groupName: String?) {
        _currentState.value = SessionState(
            lawId = lawId,
            articleId = articleId,
            groupName = groupName,
            updateTimestamp = System.currentTimeMillis()
        )
    }
}

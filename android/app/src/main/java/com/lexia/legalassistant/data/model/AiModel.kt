package com.lexia.legalassistant.data.model

enum class AiEngineType {
    GEMINI_PRO,
    GEMINI_FLASH,
    CLAUDE_LITIGATION,
    GPT4O_JURIS,
    OFFLINE_FAISS
}

data class AiModel(
    val id: String,
    val name: String,
    val shortBadge: String,
    val specialty: String,
    val description: String,
    val speedIndicator: String,
    val engineType: AiEngineType,
    val isOfflineCapable: Boolean = false,
    val iconEmoji: String = "⚖️"
)

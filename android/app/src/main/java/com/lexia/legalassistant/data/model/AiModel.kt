package com.lexia.legalassistant.data.model

enum class AiEngineType {
    GEMINI_PRO,
    GEMINI_FLASH,
    CLAUDE_LITIGATION,
    GPT4O_JURIS,
    GROQ_LPU,
    OFFLINE_FAISS
}

/**
 * Remote provider that actually receives the user's question.
 * LOCAL means no network call is performed (on-device template / FAISS engine).
 */
enum class LlmProvider(val displayName: String, val apiKeyHint: String, val consoleUrl: String) {
    GEMINI("Google Gemini", "AIza...", "https://aistudio.google.com/apikey"),
    GROQ("Groq LPU", "gsk_...", "https://console.groq.com/keys"),
    ANTHROPIC("Anthropic Claude", "sk-ant-...", "https://console.anthropic.com/settings/keys"),
    OPENAI("OpenAI GPT", "sk-...", "https://platform.openai.com/api-keys"),
    LOCAL("Motor local", "", "")
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
    val iconEmoji: String = "⚖️",
    /** Provider whose API receives the question. */
    val provider: LlmProvider = LlmProvider.LOCAL,
    /** Default model id sent to the provider API (user can override it in settings). */
    val defaultRemoteModelId: String = ""
)

/**
 * Verifiable trace of how an assistant answer was produced.
 * When [isLive] is true, the text came from the provider API and the fields
 * (httpStatus, requestId, token usage, model echoed by the server) prove it.
 */
data class AiTrace(
    val provider: LlmProvider,
    val requestedModel: String,
    val isLive: Boolean,
    val responseModel: String? = null,
    val httpStatus: Int? = null,
    val latencyMs: Long? = null,
    val requestId: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val errorMessage: String? = null
) {
    fun toLogLine(): String = buildString {
        append(provider.name.lowercase())
        append(" · ").append(responseModel ?: requestedModel)
        httpStatus?.let { append(" · HTTP ").append(it) }
        latencyMs?.let { append(" · ").append(it).append(" ms") }
        if (inputTokens != null || outputTokens != null) {
            append(" · tokens ").append(inputTokens ?: "?").append("→").append(outputTokens ?: "?")
        }
        requestId?.let { append(" · req ").append(it) }
        errorMessage?.let { append(" · error: ").append(it) }
    }
}

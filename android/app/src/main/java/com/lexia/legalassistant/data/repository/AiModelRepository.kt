package com.lexia.legalassistant.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.lexia.legalassistant.data.model.AiEngineType
import com.lexia.legalassistant.data.model.AiModel
import com.lexia.legalassistant.data.model.LlmProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AiModelRepository {

    private const val PREFS_NAME = "lexia_ai_prefs"
    private const val KEY_SELECTED_MODEL = "key_selected_ai_model"
    // Legacy key name kept so previously saved Gemini keys keep working.
    private const val KEY_GEMINI_API_KEY = "key_gemini_api_key"
    private const val KEY_API_KEY_PREFIX = "key_api_key_"
    private const val KEY_REMOTE_MODEL_PREFIX = "key_remote_model_"
    private var prefs: SharedPreferences? = null

    val defaultGeminiApiKey: String
        get() = com.lexia.legalassistant.BuildConfig.DEFAULT_GEMINI_KEY

    val defaultGroqApiKey: String
        get() = com.lexia.legalassistant.BuildConfig.DEFAULT_GROQ_KEY

    val availableModels: List<AiModel> = listOf(
        AiModel(
            id = "gemini-pro-scjn",
            name = "Gemini Pro Legal (SCJN)",
            shortBadge = "PRO SCJN",
            specialty = "Constitucional & Derechos Humanos",
            description = "Razonamiento hermenéutico profundo, análisis de contradicción de tesis y ponderación de derechos fundamentales.",
            speedIndicator = "Profundo (Alta precisión)",
            engineType = AiEngineType.GEMINI_PRO,
            isOfflineCapable = false,
            iconEmoji = "🌟",
            provider = LlmProvider.GEMINI,
            defaultRemoteModelId = "gemini-3.8-flash"
        ),
        AiModel(
            id = "gemini-flash-speed",
            name = "Gemini Flash Jurídico",
            shortBadge = "FLASH",
            specialty = "Audiencias & Consultas en Trámite",
            description = "Respuestas rápidas y ejecutivas con citas directas del DOF. Ideal para uso dinámico durante audiencias.",
            speedIndicator = "⚡ Ultrarrápido",
            engineType = AiEngineType.GEMINI_FLASH,
            isOfflineCapable = false,
            iconEmoji = "⚡",
            provider = LlmProvider.GEMINI,
            defaultRemoteModelId = "gemini-3.8-flash"
        ),
        AiModel(
            id = "groq-lpu-ultra",
            name = "LexIA UltraFast (Groq LPU)",
            shortBadge = "GROQ LPU",
            specialty = "Inferencia Instantánea & Respuestas en Vivo",
            description = "Velocidad de procesamiento extrema en microsegundos utilizando hardware LPU de Groq con modelos abiertos de vanguardia.",
            speedIndicator = "🚀 Ultra Rápido (LPU)",
            engineType = AiEngineType.GROQ_LPU,
            isOfflineCapable = false,
            iconEmoji = "⚡",
            provider = LlmProvider.GROQ,
            defaultRemoteModelId = "openai/gpt-oss-120b"
        ),
        AiModel(
            id = "lexia-offline-faiss",
            name = "LexIA Soberano Offline (FAISS)",
            shortBadge = "OFFLINE",
            specialty = "Privacidad Total & Local",
            description = "Motor local ejecutado 100% en el dispositivo sin conexión a internet: no envía la consulta a ninguna IA externa.",
            speedIndicator = "🔒 100% Offline",
            engineType = AiEngineType.OFFLINE_FAISS,
            isOfflineCapable = true,
            iconEmoji = "🔒",
            provider = LlmProvider.LOCAL
        )
    )

    /** Remote providers that have configured API keys and are active in the catalog. */
    val remoteProviders: List<LlmProvider> = listOf(
        LlmProvider.GEMINI,
        LlmProvider.GROQ
    )

    private val _currentModel = MutableStateFlow(availableModels[0])
    val currentModel: StateFlow<AiModel> = _currentModel.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedId = prefs?.getString(KEY_SELECTED_MODEL, availableModels[0].id)
            val matched = availableModels.find { it.id == savedId } ?: availableModels[0]
            _currentModel.value = matched
        }
    }

    fun selectModel(modelId: String) {
        val target = availableModels.find { it.id == modelId } ?: return
        _currentModel.value = target
        prefs?.edit()?.putString(KEY_SELECTED_MODEL, target.id)?.apply()
    }

    // ---------------- API keys (one per provider) ----------------

    private fun apiKeyPrefName(provider: LlmProvider): String =
        if (provider == LlmProvider.GEMINI) KEY_GEMINI_API_KEY else KEY_API_KEY_PREFIX + provider.name.lowercase()

    fun getApiKey(provider: LlmProvider): String? {
        if (provider == LlmProvider.LOCAL) return null
        val name = apiKeyPrefName(provider)
        if (prefs?.contains(name) == true) {
            return prefs?.getString(name, null)?.takeIf { it.isNotBlank() }
        }
        if (provider == LlmProvider.GEMINI) return defaultGeminiApiKey.takeIf { it.isNotBlank() }
        if (provider == LlmProvider.GROQ) return defaultGroqApiKey.takeIf { it.isNotBlank() }
        return null
    }

    fun setApiKey(provider: LlmProvider, key: String?) {
        if (provider == LlmProvider.LOCAL) return
        val name = apiKeyPrefName(provider)
        if (key.isNullOrBlank()) {
            prefs?.edit()?.putString(name, "")?.apply()
        } else {
            prefs?.edit()?.putString(name, key.trim())?.apply()
        }
    }

    fun getGeminiApiKey(): String? = getApiKey(LlmProvider.GEMINI)

    fun setGeminiApiKey(key: String?) = setApiKey(LlmProvider.GEMINI, key)

    fun getGroqApiKey(): String? = getApiKey(LlmProvider.GROQ)

    fun setGroqApiKey(key: String?) = setApiKey(LlmProvider.GROQ, key)

    // ---------------- Remote model id (overridable per AiModel) ----------------

    fun getRemoteModelId(model: AiModel): String =
        prefs?.getString(KEY_REMOTE_MODEL_PREFIX + model.id, null)?.takeIf { it.isNotBlank() }
            ?: model.defaultRemoteModelId

    fun setRemoteModelId(model: AiModel, remoteId: String?) {
        val name = KEY_REMOTE_MODEL_PREFIX + model.id
        if (remoteId.isNullOrBlank() || remoteId.trim() == model.defaultRemoteModelId) {
            prefs?.edit()?.remove(name)?.apply()
        } else {
            prefs?.edit()?.putString(name, remoteId.trim())?.apply()
        }
    }

    /** Model used for the connectivity test of a provider: the selected one if it matches, else the first of that provider. */
    fun representativeModel(provider: LlmProvider): AiModel? =
        currentModel.value.takeIf { it.provider == provider }
            ?: availableModels.firstOrNull { it.provider == provider }
}

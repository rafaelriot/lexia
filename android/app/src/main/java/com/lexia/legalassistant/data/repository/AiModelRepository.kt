package com.lexia.legalassistant.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.lexia.legalassistant.data.model.AiEngineType
import com.lexia.legalassistant.data.model.AiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AiModelRepository {

    private const val PREFS_NAME = "lexia_ai_prefs"
    private const val KEY_SELECTED_MODEL = "key_selected_ai_model"
    private const val KEY_GEMINI_API_KEY = "key_gemini_api_key"

    private var prefs: SharedPreferences? = null

    val availableModels: List<AiModel> = listOf(
        AiModel(
            id = "gemini-pro-scjn",
            name = "Gemini 1.5 Pro Legal (SCJN)",
            shortBadge = "PRO SCJN",
            specialty = "Constitucional & Derechos Humanos",
            description = "Razonamiento hermenéutico profundo, análisis de contradicción de tesis y ponderación de derechos fundamentales.",
            speedIndicator = "Profundo (Alta precisión)",
            engineType = AiEngineType.GEMINI_PRO,
            isOfflineCapable = false,
            iconEmoji = "🌟"
        ),
        AiModel(
            id = "gemini-flash-speed",
            name = "Gemini Flash Jurídico",
            shortBadge = "FLASH",
            specialty = "Audiencias & Consultas en Trámite",
            description = "Respuestas ultrarrápidas y ejecutivas con citas directas del DOF. Ideal para uso dinámico durante audiencias.",
            speedIndicator = "⚡ < 1s (Ultrarrápido)",
            engineType = AiEngineType.GEMINI_FLASH,
            isOfflineCapable = false,
            iconEmoji = "⚡"
        ),
        AiModel(
            id = "claude-litigation",
            name = "LexIA Litigante (Claude 3.5 Sonnet)",
            shortBadge = "LITIGIO",
            specialty = "Técnica Procesal, Amparo & Penal",
            description = "Especialista en estructuración de agravios, excepciones procesales, conceptos de violación y petitorios formales.",
            speedIndicator = "Argumentación avanzada",
            engineType = AiEngineType.CLAUDE_LITIGATION,
            isOfflineCapable = false,
            iconEmoji = "⚖️"
        ),
        AiModel(
            id = "gpt4o-jurisconsult",
            name = "JurisConsulto (GPT-4o)",
            shortBadge = "JURIS",
            specialty = "Corporativo, Civil & Dogmática",
            description = "Análisis comparativo, dictamen doctrinal, contratos mercantiles y jurisprudencia dogmática.",
            speedIndicator = "Integral",
            engineType = AiEngineType.GPT4O_JURIS,
            isOfflineCapable = false,
            iconEmoji = "🏛️"
        ),
        AiModel(
            id = "lexia-offline-faiss",
            name = "LexIA Soberano Offline (FAISS)",
            shortBadge = "OFFLINE",
            specialty = "Privacidad Total & Local",
            description = "Motor de búsqueda vectorial FAISS ejecutado 100% en el dispositivo sin conexión a internet ni fuga de datos.",
            speedIndicator = "🔒 100% Offline",
            engineType = AiEngineType.OFFLINE_FAISS,
            isOfflineCapable = true,
            iconEmoji = "🔒"
        )
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

    fun getGeminiApiKey(): String? {
        return prefs?.getString(KEY_GEMINI_API_KEY, null)?.takeIf { it.isNotBlank() }
    }

    fun setGeminiApiKey(key: String?) {
        if (key.isNullOrBlank()) {
            prefs?.edit()?.remove(KEY_GEMINI_API_KEY)?.apply()
        } else {
            prefs?.edit()?.putString(KEY_GEMINI_API_KEY, key.trim())?.apply()
        }
    }
}

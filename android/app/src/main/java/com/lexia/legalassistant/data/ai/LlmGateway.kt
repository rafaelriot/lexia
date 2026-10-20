package com.lexia.legalassistant.data.ai

import android.util.Log
import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single entry point for every outgoing LLM request.
 *
 * Every call is logged under the Logcat tag [TAG] (`adb logcat -s LexIA-AI`) with
 * provider, endpoint model, HTTP status, latency, request id and token usage.
 * API keys are never logged.
 */
object LlmGateway {

    const val TAG = "LexIA-AI"

    fun clientFor(provider: LlmProvider): LlmClient? = when (provider) {
        LlmProvider.GEMINI -> GeminiClient
        LlmProvider.GROQ -> GroqClient
        LlmProvider.ANTHROPIC -> AnthropicClient
        LlmProvider.OPENAI -> OpenAiClient
        LlmProvider.LOCAL -> null
    }

    suspend fun complete(
        provider: LlmProvider,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): LlmResult = withContext(Dispatchers.IO) {
        val cleanModel = model.trim().removePrefix("models/")
        val client = clientFor(provider)
            ?: return@withContext LlmResult(
                null,
                AiTrace(provider, cleanModel, isLive = false, errorMessage = "Proveedor local: no se realiza llamada de red")
            )

        safeLog { Log.i(TAG, "→ ${provider.name} model=$cleanModel systemChars=${systemPrompt.length} userChars=${userPrompt.length}") }
        val start = System.nanoTime()

        val result = try {
            client.complete(apiKey.trim(), cleanModel, systemPrompt, userPrompt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LlmResult(
                null,
                AiTrace(
                    provider = provider,
                    requestedModel = cleanModel,
                    isLive = false,
                    latencyMs = (System.nanoTime() - start) / 1_000_000,
                    errorMessage = "${e.javaClass.simpleName}: ${e.message ?: "sin detalle"}"
                )
            )
        }

        safeLog {
            if (result.isSuccess) Log.i(TAG, "← OK ${result.trace.toLogLine()} chars=${result.text?.length}")
            else Log.w(TAG, "← FAIL ${result.trace.toLogLine()}")
        }
        result
    }

    /** Lightweight connectivity check used by the "Probar conexión" button. */
    suspend fun ping(provider: LlmProvider, apiKey: String, model: String): LlmResult = complete(
        provider = provider,
        apiKey = apiKey,
        model = model,
        systemPrompt = "Eres un verificador de conectividad. Responde únicamente con el texto solicitado.",
        userPrompt = "Responde exactamente: LEXIA-OK"
    )

    /** android.util.Log is not available in plain JVM unit tests. */
    private inline fun safeLog(block: () -> Unit) {
        try { block() } catch (_: Throwable) { }
    }
}

package com.lexia.legalassistant.data.ai

import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import org.json.JSONArray
import org.json.JSONObject

/**
 * Groq LPU REST client (ultra-fast inference).
 * OpenAI-compatible completions API.
 * Docs: https://console.groq.com/docs/api-reference
 */
object GroqClient : LlmClient {
    override val provider = LlmProvider.GROQ

    private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

    override fun complete(apiKey: String, model: String, systemPrompt: String, userPrompt: String): LlmResult {
        val payload = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt))
                .put(JSONObject().put("role", "user").put("content", userPrompt))
            )
            put("max_tokens", 2048)
            put("temperature", 0.3)
        }

        val resp = LlmHttp.postJson(
            url = ENDPOINT,
            headers = mapOf("Authorization" to "Bearer $apiKey"),
            payload = payload
        )
        val json = resp.json()
        val requestId = resp.header("x-request-id") ?: json?.optString("id")?.takeIf { it.isNotBlank() }

        val base = AiTrace(
            provider = provider,
            requestedModel = model,
            isLive = resp.status in 200..299,
            httpStatus = resp.status,
            latencyMs = resp.latencyMs,
            requestId = requestId
        )

        if (resp.status !in 200..299) {
            return LlmResult(null, base.copy(errorMessage = resp.errorMessage()))
        }

        val choice = json?.optJSONArray("choices")?.optJSONObject(0)
        val text = choice?.optJSONObject("message")?.optString("content")?.trim().orEmpty()
        val usage = json?.optJSONObject("usage")
        val trace = base.copy(
            responseModel = json?.optString("model")?.takeIf { it.isNotBlank() },
            inputTokens = usage?.optInt("prompt_tokens"),
            outputTokens = usage?.optInt("completion_tokens")
        )

        return if (text.isBlank() || text == "null") {
            LlmResult(null, trace.copy(errorMessage = "Groq no devolvió texto (${choice?.optString("finish_reason")})"))
        } else {
            LlmResult(text, trace)
        }
    }
}

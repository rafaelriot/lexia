package com.lexia.legalassistant.data.ai

import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import org.json.JSONArray
import org.json.JSONObject

/**
 * Anthropic Messages API client (Claude).
 * Docs: https://docs.anthropic.com/en/api/messages
 */
object AnthropicClient : LlmClient {
    override val provider = LlmProvider.ANTHROPIC

    private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
    private const val API_VERSION = "2023-06-01"

    override fun complete(apiKey: String, model: String, systemPrompt: String, userPrompt: String): LlmResult {
        val payload = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("temperature", 0.3)
            put("system", systemPrompt)
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", userPrompt)))
        }

        val resp = LlmHttp.postJson(
            url = ENDPOINT,
            headers = mapOf(
                "x-api-key" to apiKey,
                "anthropic-version" to API_VERSION
            ),
            payload = payload
        )
        val json = resp.json()
        val requestId = resp.header("request-id") ?: json?.optString("id")?.takeIf { it.isNotBlank() }

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

        val content = json?.optJSONArray("content")
        val text = buildString {
            if (content != null) for (i in 0 until content.length()) {
                val block = content.optJSONObject(i) ?: continue
                if (block.optString("type") == "text") append(block.optString("text"))
            }
        }.trim()

        val usage = json?.optJSONObject("usage")
        val trace = base.copy(
            responseModel = json?.optString("model")?.takeIf { it.isNotBlank() },
            inputTokens = usage?.optInt("input_tokens"),
            outputTokens = usage?.optInt("output_tokens")
        )

        return if (text.isBlank()) {
            LlmResult(null, trace.copy(errorMessage = "Claude no devolvió texto (${json?.optString("stop_reason")})"))
        } else {
            LlmResult(text, trace)
        }
    }
}

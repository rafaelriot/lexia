package com.lexia.legalassistant.data.ai

import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Google Gemini REST client.
 * Docs: https://ai.google.dev/api/generate-content
 */
object GeminiClient : LlmClient {
    override val provider = LlmProvider.GEMINI

    private const val BASE = "https://generativelanguage.googleapis.com/v1beta/models/"

    override fun complete(apiKey: String, model: String, systemPrompt: String, userPrompt: String): LlmResult {
        val payload = JSONObject().apply {
            put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            put("contents", JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            ))
            // No maxOutputTokens: Gemini 2.5+ "thinking" tokens count against it and can truncate the answer.
            put("generationConfig", JSONObject().put("temperature", 0.3))
        }

        val encodedModel = URLEncoder.encode(model, "UTF-8")
        val resp = LlmHttp.postJson(
            url = "$BASE$encodedModel:generateContent",
            headers = mapOf("x-goog-api-key" to apiKey), // key in header, never in the URL
            payload = payload
        )
        val json = resp.json()

        if (resp.status !in 200..299) {
            return LlmResult(null, trace(model, resp, error = resp.errorMessage()))
        }

        val candidate = json?.optJSONArray("candidates")?.optJSONObject(0)
        val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
        val text = buildString {
            if (parts != null) for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i) ?: continue
                if (p.optBoolean("thought", false)) continue
                append(p.optString("text"))
            }
        }.trim()

        val usage = json?.optJSONObject("usageMetadata")
        val baseTrace = trace(model, resp).copy(
            responseModel = json?.optString("modelVersion")?.takeIf { it.isNotBlank() },
            requestId = json?.optString("responseId")?.takeIf { it.isNotBlank() },
            inputTokens = usage?.optInt("promptTokenCount"),
            outputTokens = usage?.optInt("candidatesTokenCount")
        )

        if (text.isBlank()) {
            val reason = json?.optJSONObject("promptFeedback")?.optString("blockReason")?.takeIf { it.isNotBlank() }
                ?: candidate?.optString("finishReason")?.takeIf { it.isNotBlank() }
                ?: "sin texto"
            return LlmResult(null, baseTrace.copy(errorMessage = "Gemini no devolvió texto ($reason)"))
        }
        return LlmResult(text, baseTrace)
    }

    private fun trace(model: String, resp: HttpJsonResponse, error: String? = null) = AiTrace(
        provider = provider,
        requestedModel = model,
        isLive = error == null,
        httpStatus = resp.status,
        latencyMs = resp.latencyMs,
        errorMessage = error
    )
}

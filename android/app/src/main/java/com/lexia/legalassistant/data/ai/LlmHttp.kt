package com.lexia.legalassistant.data.ai

import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.TreeMap

/** Result of a single LLM completion request. */
data class LlmResult(
    val text: String?,
    val trace: AiTrace
) {
    val isSuccess: Boolean get() = !text.isNullOrBlank() && trace.errorMessage == null
}

/** Contract implemented by every real provider client. */
interface LlmClient {
    val provider: LlmProvider

    /** Performs a blocking HTTP call. Must be invoked from a background dispatcher. */
    fun complete(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String
    ): LlmResult
}

internal data class HttpJsonResponse(
    val status: Int,
    val body: String,
    val headers: Map<String, String>,
    val latencyMs: Long
) {
    fun header(name: String): String? = headers[name]
    fun json(): JSONObject? = runCatching { JSONObject(body) }.getOrNull()

    /** Extracts the provider's error message (`{"error":{"message":...}}`) or a raw body snippet. */
    fun errorMessage(): String {
        val msg = json()?.optJSONObject("error")?.optString("message")
        return if (!msg.isNullOrBlank()) msg else body.take(240).ifBlank { "Respuesta vacía" }
    }
}

/** Minimal JSON-over-HTTPS helper based on HttpURLConnection (no extra dependencies). */
internal object LlmHttp {

    fun postJson(
        url: String,
        headers: Map<String, String>,
        payload: JSONObject,
        connectTimeoutMs: Int = 15_000,
        readTimeoutMs: Int = 90_000
    ): HttpJsonResponse {
        val start = System.nanoTime()
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        try {
            conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val responseHeaders = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
            conn.headerFields.forEach { (k, v) -> if (k != null && v.isNotEmpty()) responseHeaders[k] = v.first() }
            val latency = (System.nanoTime() - start) / 1_000_000
            return HttpJsonResponse(status, body, responseHeaders, latency)
        } finally {
            conn.disconnect()
        }
    }
}

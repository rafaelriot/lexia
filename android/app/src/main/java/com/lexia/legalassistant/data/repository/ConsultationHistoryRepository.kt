package com.lexia.legalassistant.data.repository

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.lexia.legalassistant.data.model.ConsultationSession
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object ConsultationHistoryRepository {
    private const val PREFS_NAME = "lexia_consultation_history"
    private const val KEY_SESSIONS = "saved_sessions"

    val sessions = mutableStateListOf<ConsultationSession>()
    private var isInitialized = false

    fun getSessionsForUser(userId: String): List<ConsultationSession> {
        return sessions.filter { it.userId == userId }
    }

    fun getSessionsCountForUser(userId: String): Int {
        return sessions.count { it.userId == userId }
    }

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadFromPrefs(context)
    }

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_SESSIONS, null)
            if (!jsonStr.isNullOrBlank()) {
                val array = JSONArray(jsonStr)
                sessions.clear()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    sessions.add(
                        ConsultationSession(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            userId = obj.optString("userId", "guest"),
                            title = obj.optString("title", "Consulta Jurídica"),
                            date = obj.optString("date", "Hoy"),
                            queriesCount = obj.optInt("queriesCount", 1),
                            chunksCount = obj.optInt("chunksCount", 5),
                            lawTag = obj.optString("lawTag", "CPEUM"),
                            lawId = obj.optString("lawId", "cpeum"),
                            articleId = if (obj.has("articleId") && !obj.isNull("articleId") && obj.getString("articleId").isNotEmpty()) obj.getString("articleId") else null,
                            groupName = if (obj.has("groupName") && !obj.isNull("groupName") && obj.getString("groupName").isNotEmpty()) obj.getString("groupName") else null,
                            lastQueryText = obj.optString("lastQueryText", ""),
                            aiAnswerExcerpt = obj.optString("aiAnswerExcerpt", ""),
                            isVoice = obj.optBoolean("isVoice", false),
                            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                        )
                    )
                }
            } else {
                seedInitialSessions(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            seedInitialSessions(context)
        }
    }

    private fun seedInitialSessions(context: Context) {
        sessions.clear()
        sessions.addAll(
            listOf(
                ConsultationSession(
                    id = "ses_104",
                    title = "Control de Legalidad y Flagrancia Art. 16",
                    date = "Hoy · 09:42 hrs",
                    queriesCount = 4,
                    chunksCount = 6,
                    lawTag = "CPEUM",
                    lawId = "cpeum",
                    articleId = "cpeum_16",
                    lastQueryText = "¿Cuáles son las garantías frente a una detención arbitraria en el artículo 16?",
                    aiAnswerExcerpt = "El control de la privación de la libertad se cimienta en el mandamiento escrito fundado y motivado.",
                    isVoice = false
                ),
                ConsultationSession(
                    id = "ses_103",
                    title = "Nacionalidad por Nacimiento y Naturalización Art. 30",
                    date = "Ayer · 16:15 hrs",
                    queriesCount = 3,
                    chunksCount = 5,
                    lawTag = "CPEUM",
                    lawId = "cpeum",
                    articleId = "cpeum_30",
                    lastQueryText = "¿Qué es el artículo 30 de la constitución política?",
                    aiAnswerExcerpt = "La nacionalidad mexicana se adquiere por nacimiento (jus soli y jus sanguinis) o por naturalización.",
                    isVoice = true
                ),
                ConsultationSession(
                    id = "ses_102",
                    title = "Garantía de Audiencia Previa Art. 14 Constitucional",
                    date = "08 Oct · 11:30 hrs",
                    queriesCount = 2,
                    chunksCount = 4,
                    lawTag = "CPEUM",
                    lawId = "cpeum",
                    articleId = "cpeum_14",
                    lastQueryText = "¿Qué formalidades esenciales exige el artículo 14?",
                    aiAnswerExcerpt = "Nadie podrá ser privado de la libertad o derechos sin juicio previo ante tribunales previamente establecidos.",
                    isVoice = false
                ),
                ConsultationSession(
                    id = "ses_101",
                    title = "Derechos Humanos y Principio Pro Persona Arts. 1-29",
                    date = "05 Oct · 18:00 hrs",
                    queriesCount = 5,
                    chunksCount = 8,
                    lawTag = "CPEUM",
                    lawId = "cpeum",
                    groupName = "DDHH (Arts. 1-29)",
                    lastQueryText = "¿Cómo aplica el principio pro persona en derechos humanos?",
                    aiAnswerExcerpt = "Las normas relativas a derechos humanos se interpretarán favoreciendo la protección más amplia a las personas.",
                    isVoice = false
                )
            )
        )
        saveToPrefs(context)
    }

    fun recordConsultation(
        context: Context,
        query: String,
        aiAnswer: String,
        lawId: String,
        articleId: String?,
        groupName: String?,
        isVoice: Boolean,
        userId: String = "guest"
    ) {
        init(context)

        val lawTag = when (lawId.lowercase()) {
            "cnpp" -> "CNPP"
            "amparo" -> "Amparo"
            else -> "CPEUM"
        }

        val article = articleId?.let { LegalArticlesRepository.getArticleById(it) }
        val title = when {
            article != null -> "${article.articleNumber}: ${article.topicTitle.take(38)}"
            groupName != null -> "Capítulo: $groupName"
            query.length > 5 -> query.take(45).let { if (query.length > 45) "$it..." else it }
            else -> "Consulta Constitucional General (136 Arts)"
        }

        // Check if an existing session exists for the same topic/article in the last 2 hours FOR THIS USER
        val existingIndex = sessions.indexOfFirst {
            it.userId == userId && (
                (it.articleId != null && it.articleId == articleId) ||
                (it.groupName != null && it.groupName == groupName) ||
                (articleId == null && groupName == null && it.articleId == null && it.groupName == null && (System.currentTimeMillis() - it.timestampMillis < 7200000))
            )
        }

        if (existingIndex >= 0) {
            val old = sessions[existingIndex]
            val updated = old.copy(
                queriesCount = old.queriesCount + 1,
                date = "Hoy · ${SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault()).format(Date())}",
                lastQueryText = query,
                aiAnswerExcerpt = aiAnswer.take(120),
                isVoice = old.isVoice || isVoice,
                timestampMillis = System.currentTimeMillis()
            )
            sessions.removeAt(existingIndex)
            sessions.add(0, updated)
        } else {
            val newSession = ConsultationSession(
                id = "ses_${System.currentTimeMillis() % 100000}",
                userId = userId,
                title = title,
                date = "Hoy · ${SimpleDateFormat("HH:mm 'hrs'", Locale.getDefault()).format(Date())}",
                queriesCount = 1,
                chunksCount = (article?.chunks?.size ?: 6),
                lawTag = lawTag,
                lawId = lawId,
                articleId = articleId,
                groupName = groupName,
                lastQueryText = query,
                aiAnswerExcerpt = aiAnswer.take(120),
                isVoice = isVoice,
                timestampMillis = System.currentTimeMillis()
            )
            sessions.add(0, newSession)
        }

        saveToPrefs(context)
    }

    fun deleteSession(context: Context, sessionId: String) {
        sessions.removeAll { it.id == sessionId }
        saveToPrefs(context)
    }

    fun clearAll(context: Context) {
        sessions.clear()
        saveToPrefs(context)
    }

    fun clearForUser(context: Context, userId: String) {
        sessions.removeAll { it.userId == userId }
        saveToPrefs(context)
    }

    private fun saveToPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val array = JSONArray()
            for (s in sessions) {
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("userId", s.userId)
                    put("title", s.title)
                    put("date", s.date)
                    put("queriesCount", s.queriesCount)
                    put("chunksCount", s.chunksCount)
                    put("lawTag", s.lawTag)
                    put("lawId", s.lawId)
                    put("articleId", s.articleId ?: "")
                    put("groupName", s.groupName ?: "")
                    put("lastQueryText", s.lastQueryText)
                    put("aiAnswerExcerpt", s.aiAnswerExcerpt)
                    put("isVoice", s.isVoice)
                    put("timestampMillis", s.timestampMillis)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_SESSIONS, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

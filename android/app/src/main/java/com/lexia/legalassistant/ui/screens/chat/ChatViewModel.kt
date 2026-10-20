package com.lexia.legalassistant.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lexia.legalassistant.data.model.AiModel
import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.RagMessage
import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.repository.ConsultationHistoryRepository
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * ChatViewModel - Manages conversation state, asynchronous AI synthesis, and consultation history recording.
 * Extends [ViewModel] to survive configuration changes (e.g. screen rotations) and decouple business logic
 * and coroutines from the Compose UI lifecycle.
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<RagMessage>>(emptyList())
    val messages: StateFlow<List<RagMessage>> = _messages.asStateFlow()

    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()

    private val _lastTrace = MutableStateFlow<AiTrace?>(null)
    val lastTrace: StateFlow<AiTrace?> = _lastTrace.asStateFlow()

    /**
     * Appends a user message to the feed.
     */
    fun appendUserMessage(
        query: String,
        senderName: String,
        isVoice: Boolean = false,
        voiceFile: File? = null,
        voiceDuration: Int = 3
    ): RagMessage {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val userMsg = if (isVoice) {
            RagMessage(
                id = UUID.randomUUID().toString(),
                isUser = true,
                senderName = senderName,
                text = query,
                timestamp = time,
                isVoiceRecording = true,
                voiceDurationSeconds = voiceDuration,
                voiceAudioWaveform = listOf(0.3f, 0.7f, 0.4f, 0.9f, 0.6f, 1.0f, 0.5f, 0.8f, 0.4f, 0.6f),
                audioFilePath = voiceFile?.absolutePath,
                transcribedText = query
            )
        } else {
            RagMessage(
                id = UUID.randomUUID().toString(),
                isUser = true,
                senderName = senderName,
                text = query,
                timestamp = time
            )
        }
        _messages.value = _messages.value + userMsg
        return userMsg
    }

    /**
     * Executes RAG search + LLM generation (Groq, Gemini, Claude, etc.) within [viewModelScope].
     */
    fun generateAiResponse(
        context: Context,
        userQuery: String,
        scope: ScopeChoice,
        lawId: String,
        activeArticle: LawArticleDetail?,
        activeGroupName: String?,
        aiModel: AiModel,
        isVoice: Boolean = false,
        activeUserId: String = "guest",
        onCompleted: (RagMessage) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isGeneratingResponse.value = true
            try {
                val response = LegalArticlesRepository.queryConstitutionalRag(
                    userQuery = userQuery,
                    scope = scope,
                    lawId = lawId,
                    activeArticle = activeArticle,
                    activeGroupName = activeGroupName,
                    aiModel = aiModel
                )
                _lastTrace.value = response.aiTrace
                _messages.value = _messages.value + response

                ConsultationHistoryRepository.recordConsultation(
                    context = context,
                    query = userQuery,
                    aiAnswer = response.text,
                    lawId = lawId,
                    articleId = activeArticle?.id,
                    groupName = activeGroupName,
                    isVoice = isVoice,
                    userId = activeUserId
                )

                onCompleted(response)
            } finally {
                _isGeneratingResponse.value = false
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}

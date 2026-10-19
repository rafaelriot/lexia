package com.lexia.legalassistant.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.model.LegalCitation
import com.lexia.legalassistant.data.model.RagMessage
import com.lexia.legalassistant.data.model.ScopeChoice
import com.lexia.legalassistant.data.repository.ConsultationHistoryRepository
import com.lexia.legalassistant.data.repository.DynamicStatutoryDirectory
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import com.lexia.legalassistant.ui.components.RagCitationCard
import com.lexia.legalassistant.ui.theme.*
import com.lexia.legalassistant.ui.screens.dictamen.PdfGeneratorHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun findReferencedArticlesInText(text: String, defaultLawId: String): List<LawArticleDetail> {
    val results = mutableListOf<LawArticleDetail>()
    val seenIds = mutableSetOf<String>()

    fun mapLawHint(hint: String): String {
        val h = hint.lowercase().trim()
        return when {
            h.contains("amparo") -> "amparo"
            h.contains("cnpp") || h.contains("procedimientos penales") || h.contains("procedimiento penal") -> "cnpp"
            h.contains("cpf") || h.contains("penal federal") -> "cpf"
            h.contains("cpet") || h.contains("tabasco") -> "cpet"
            h.contains("constituc") || h.contains("cpeum") || h.contains("carta magna") -> "cpeum"
            h.contains("civil federal") || h.contains("ccf") -> "ccf"
            h.contains("trabajo") || h.contains("lft") -> "lft"
            else -> defaultLawId
        }
    }

    // Pattern 1: "Artículo 262 del Código Penal Federal / del CPF"
    val regexPost = Regex("""(?i)(?:art[íi]culo|art\.?)\s+(\d{1,4})[°º]?(?:\s+(?:de\s+la\s+|del\s+)?([a-záéíóúñ\s]+))?""")
    for (match in regexPost.findAll(text)) {
        val num = match.groupValues[1].toIntOrNull() ?: continue
        val lawHint = match.groupValues.getOrNull(2) ?: ""
        val lawId = mapLawHint(lawHint)
        val articleId = "${lawId}_$num"
        if (!seenIds.contains(articleId)) {
            val article = DynamicStatutoryDirectory.getArticle(lawId, num)
            if (article != null) {
                seenIds.add(articleId)
                results.add(article)
            }
        }
    }

    // Pattern 2: "Código Penal Federal (CPF, Artículo 262)" or "CPF · Artículo 262"
    val regexPre = Regex("""(?i)\b(cpeum|cpf|cnpp|amparo|cpet|ccf|lft|código penal federal|código penal|código nacional|ley de amparo|constituci[óo]n)\b[^\n\.\;]{0,35}?(?:art[íi]culo|art\.?)\s*(\d{1,4})""")
    for (match in regexPre.findAll(text)) {
        val lawHint = match.groupValues[1]
        val num = match.groupValues[2].toIntOrNull() ?: continue
        val lawId = mapLawHint(lawHint)
        val articleId = "${lawId}_$num"
        if (!seenIds.contains(articleId)) {
            val article = DynamicStatutoryDirectory.getArticle(lawId, num)
            if (article != null) {
                seenIds.add(articleId)
                results.add(article)
            }
        }
    }

    return results
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRagScreen(
    initialLawId: String = "cpeum",
    initialArticleId: String? = null,
    onNavigateToDictamen: (articleId: String?, groupName: String?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val audioHelper = remember { AudioVoiceHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            audioHelper.release()
        }
    }

    LaunchedEffect(Unit) {
        com.lexia.legalassistant.data.repository.AiModelRepository.init(context)
    }

    val currentAiModel by com.lexia.legalassistant.data.repository.AiModelRepository.currentModel.collectAsState()
    var showAiModelSelector by remember { mutableStateOf(false) }
    var geminiApiKeyInput by remember { mutableStateOf(com.lexia.legalassistant.data.repository.AiModelRepository.getGeminiApiKey() ?: "") }
    var isApiKeyCardExpanded by remember { mutableStateOf(false) }

    val navSessionState by com.lexia.legalassistant.navigation.LegalNavigationSession.currentState.collectAsState()

    var selectedLawId by remember { mutableStateOf(navSessionState.lawId.ifBlank { initialLawId }) }
    var selectedArticleId by remember { mutableStateOf<String?>(navSessionState.articleId ?: initialArticleId) }
    var selectedGroupName by remember { mutableStateOf<String?>(navSessionState.groupName) } // e.g. "DDHH (Arts. 1-29)"
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Chat IA, 1: Texto DOF & Chunks FAISS
    var showArticlePicker by remember { mutableStateOf(false) }
    var showScopeDialog by remember { mutableStateOf(false) }
    var inspectedArticle by remember { mutableStateOf<LawArticleDetail?>(null) }

    var queryText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isGeneratingResponse by remember { mutableStateOf(false) }
    var playingAudioId by remember { mutableStateOf<String?>(null) }
    var currentRecognizedVoiceText by remember { mutableStateOf("") }

    var pendingQueryText by remember { mutableStateOf("") }
    var pendingIsVoice by remember { mutableStateOf(false) }
    var pendingVoiceFile by remember { mutableStateOf<java.io.File?>(null) }
    var pendingVoiceDuration by remember { mutableIntStateOf(3) }

    val currentLaw = remember(selectedLawId) {
        LegalArticlesRepository.getLawById(selectedLawId)
    }

    val currentArticle = remember(selectedArticleId) {
        selectedArticleId?.let { LegalArticlesRepository.getArticleById(it) }
    }

    val currentLawArticles = remember(selectedLawId) {
        LegalArticlesRepository.getArticlesForLaw(selectedLawId)
    }

    val currentLawGroups = remember(selectedLawId) {
        LegalArticlesRepository.getGroupsForLaw(selectedLawId)
    }

    val currentGroupArticles = remember(selectedGroupName, selectedLawId, currentLawArticles) {
        LegalArticlesRepository.filterArticlesByGroup(currentLawArticles, selectedGroupName)
    }

    // Si cambia la ley activa, descartar capítulo/artículo que pertenezcan a otra ley
    var previousLawId by remember { mutableStateOf(selectedLawId) }
    LaunchedEffect(selectedLawId) {
        if (previousLawId == selectedLawId) return@LaunchedEffect
        previousLawId = selectedLawId
        if (selectedGroupName != null && selectedGroupName !in currentLawGroups) selectedGroupName = null
        if (selectedArticleId != null && !selectedArticleId!!.startsWith("${selectedLawId}_")) selectedArticleId = null
    }

    // Permission launcher for microphone recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
                currentRecognizedVoiceText = ""
                val started = audioHelper.startRecording(
                    onPartialResult = { transcribed ->
                        currentRecognizedVoiceText = transcribed
                    },
                    onFinalResult = { finalText, _ ->
                        if (finalText.isNotBlank()) {
                            currentRecognizedVoiceText = finalText
                        }
                    }
                )
                if (!started) {
                    isRecording = false
                    Toast.makeText(context, "No se pudo iniciar el reconocedor de voz", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Se requiere permiso de micrófono para notas de voz", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Recording timer effect
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Initial Messages in chat: Inicia completamente limpio
    val messages = remember(initialLawId, initialArticleId) {
        mutableStateListOf<RagMessage>()
    }

    // Reactive sync effect with LegalNavigationSession
    LaunchedEffect(navSessionState.updateTimestamp) {
        if (navSessionState.updateTimestamp > 0) {
            selectedLawId = navSessionState.lawId
            selectedArticleId = navSessionState.articleId
            selectedGroupName = navSessionState.groupName
        }
    }

    // Function to switch active article
    fun selectArticle(article: LawArticleDetail) {
        selectedArticleId = article.id
        selectedLawId = article.lawId
        selectedGroupName = null
    }

    // Function to switch active group (e.g. DDHH)
    fun selectGroup(groupName: String) {
        selectedGroupName = groupName
        selectedArticleId = null
    }

    // Function to switch back to General Constitution
    fun selectGeneralConstitution() {
        selectedGroupName = null
        selectedArticleId = null
    }

    // Function to generate automated AI RAG response based on legal query and selected scope
    fun generateAiResponse(userQuery: String, scope: ScopeChoice, isVoice: Boolean = false) {
        coroutineScope.launch {
            isGeneratingResponse = true
            delay(750) // FAISS vector retrieval & synthesis simulation

            val response = LegalArticlesRepository.queryConstitutionalRag(
                userQuery = userQuery,
                scope = scope,
                lawId = selectedLawId,
                activeArticle = currentArticle,
                activeGroupName = selectedGroupName,
                aiModel = currentAiModel
            )

            messages.add(response)
            isGeneratingResponse = false

            // Automatically record consultation into persistent history
            val activeUserId = com.lexia.legalassistant.data.repository.AuthRepository.currentUser.value?.id ?: "guest"
            ConsultationHistoryRepository.recordConsultation(
                context = context,
                query = userQuery,
                aiAnswer = response.text,
                lawId = selectedLawId,
                articleId = selectedArticleId,
                groupName = selectedGroupName,
                isVoice = isVoice,
                userId = activeUserId
            )

            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Function to execute query after choosing scope in dialog
    fun executeScopedQuery(
        query: String,
        scope: ScopeChoice,
        isVoice: Boolean,
        voiceFile: java.io.File?,
        voiceDuration: Int
    ) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val userMsg = if (isVoice) {
            RagMessage(
                id = UUID.randomUUID().toString(),
                isUser = true,
                senderName = "Lic. M. Herrera",
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
                senderName = "Lic. M. Herrera",
                text = query,
                timestamp = time
            )
        }

        messages.add(userMsg)
        queryText = ""
        pendingQueryText = ""
        pendingIsVoice = false
        pendingVoiceFile = null

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }

        generateAiResponse(query, scope, isVoice)
    }

    // Function to initiate text message sending (triggers Scope Dialog)
    fun onAttemptSendTextMessage() {
        val textToSend = queryText.trim()
        if (textToSend.isBlank()) return

        pendingQueryText = textToSend
        pendingIsVoice = false
        pendingVoiceFile = null
        pendingVoiceDuration = 3
        showScopeDialog = true
    }

    // Function to initiate voice message sending (triggers Scope Dialog)
    fun onAttemptSendVoiceMessage() {
        val duration = if (recordingSeconds == 0) 3 else recordingSeconds
        isRecording = false
        recordingSeconds = 0

        audioHelper.stopRecording { recordedFile, recognizedText ->
            val textFound = when {
                recognizedText.isNotBlank() -> recognizedText.trim()
                currentRecognizedVoiceText.isNotBlank() -> currentRecognizedVoiceText.trim()
                audioHelper.lastTranscribedText.isNotBlank() -> audioHelper.lastTranscribedText.trim()
                else -> ""
            }

            val queryToAnalyze = if (textFound.isNotBlank()) {
                textFound
            } else {
                // Si el micrófono no captó palabras con claridad (por silencio o ruido ambiental),
                // dejamos el texto honesto y editable para que el usuario redacte o confirme su pregunta.
                "Consulta por voz (${duration}s)"
            }

            pendingQueryText = queryToAnalyze
            pendingIsVoice = true
            pendingVoiceFile = recordedFile
            pendingVoiceDuration = duration
            currentRecognizedVoiceText = ""
            showScopeDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
    ) {
        // TOP CONTEXT HEADER BAR: Limpia, Moderna y Minimalista
        Surface(
            color = LexiaSurfaceContainerLowest,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LexiaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = LexiaOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        val activeTitle = when {
                            currentArticle != null -> "${currentArticle.lawShortName} · ${currentArticle.articleNumber}"
                            selectedGroupName != null -> "${currentLaw.shortName} · $selectedGroupName"
                            else -> currentLaw.title
                        }
                        Text(
                            text = activeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LexiaSuccess600)
                            )
                            val activeSubtitle = when {
                                currentArticle != null -> currentArticle.topicTitle
                                selectedGroupName != null -> "Capítulo especializado"
                                else -> "${currentLaw.articlesCount} Artículos indexados en FAISS"
                            }
                            Text(
                                text = activeSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = LexiaSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LexiaSuccess100
                    ) {
                        Text(
                            text = currentLaw.dofStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSuccess800,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dual Tab Switcher Elegante: Chat IA con LexIA vs Artículos
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = LexiaSurfaceContainerLowest,
                    contentColor = LexiaPrimary,
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Chat IA con LexIA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                                val tabLabel = when {
                                    currentArticle != null -> "Artículos (${currentArticle.chunks.size} Chunks)"
                                    else -> "Artículos (${currentLaw.articlesCount})"
                                }
                                Text(tabLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }
            }
        }

        // TAB CONTENT
        if (selectedTab == 0) {
            // MODE 0: AI CHAT RAG FEED (Maximizado para conversación)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Empty State Limpio: Solo invitación sutil a preguntar
                if (messages.isEmpty() && !isGeneratingResponse) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = LexiaPrimaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.ChatBubble,
                                            contentDescription = null,
                                            tint = LexiaPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "¿En qué te puedo asesorar hoy?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaPrimary
                                )
                                Text(
                                    text = "Escribe tu consulta o graba una nota de voz sobre ${currentLaw.shortName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaSecondary
                                )
                            }
                        }
                    }
                }

                // Chat Messages Feed
                items(messages) { message ->
                    if (message.isUser) {
                        // User Bubble
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${message.senderName} · ${message.timestamp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary,
                                modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                            )

                            if (message.isVoiceRecording) {
                                val isPlaying = playingAudioId == message.id
                                Surface(
                                    color = LexiaPrimary,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.widthIn(min = 240.dp, max = 320.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // 1. Reproductor de Audio (Play/Pause, ondas y duración)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (isPlaying) {
                                                        audioHelper.stopPlayback()
                                                        playingAudioId = null
                                                    } else {
                                                        playingAudioId = message.id
                                                        audioHelper.playAudio(message.audioFilePath) {
                                                            playingAudioId = null
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(LexiaPrimaryContainer)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                                    tint = LexiaOnPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val bars = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.5f, 1.0f, 0.6f, 0.8f, 0.3f, 0.5f, 0.9f, 0.4f)
                                                bars.forEachIndexed { i, heightFactor ->
                                                    val barHeight = (heightFactor * 22).dp
                                                    Box(
                                                        modifier = Modifier
                                                            .width(3.dp)
                                                            .height(barHeight)
                                                            .clip(RoundedCornerShape(2.dp))
                                                            .background(
                                                                if (isPlaying && (i % 2 == 0)) LexiaTertiaryFixed else LexiaOnPrimary.copy(alpha = 0.8f)
                                                            )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "0:0${message.voiceDurationSeconds}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaOnPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = if (isPlaying) "▶️ Reproduciendo nota de voz..." else "🎙️ Audio grabado de consulta",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaPrimaryFixedDim,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                                        )

                                        // 2. Línea divisoria
                                        HorizontalDivider(
                                            color = LexiaOnPrimary.copy(alpha = 0.2f),
                                            thickness = 1.dp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // 3. Bloque con letras de la transcripción
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.22f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.FormatQuote,
                                                        contentDescription = null,
                                                        tint = LexiaSecondaryFixed,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "Transcripción de voz:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = LexiaSecondaryFixed,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                val displayText = (message.transcribedText ?: message.text)
                                                    .replace("🎙️", "")
                                                    .replace("\"", "")
                                                    .trim()
                                                Text(
                                                    text = "«$displayText»",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = LexiaOnPrimary,
                                                    fontWeight = FontWeight.Medium,
                                                    lineHeight = 20.sp,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = LexiaPrimary,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.widthIn(max = 320.dp)
                                ) {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LexiaOnPrimary,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Assistant Bubble
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(LexiaPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Balance,
                                        contentDescription = null,
                                        tint = LexiaOnPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = message.senderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LexiaPrimary
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = LexiaSurfaceContainerHighest
                                ) {
                                    Text(
                                        text = "RAG Pipeline FAISS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LexiaPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = message.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaOutline
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = message.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = LexiaOnSurface,
                                        lineHeight = 22.sp
                                    )

                                    message.citation?.let { cit ->
                                        RagCitationCard(
                                            citation = cit,
                                            onViewFullDofText = {
                                                val art = cit.articleId?.let { LegalArticlesRepository.getArticleById(it) }
                                                    ?: currentLawArticles.firstOrNull { it.articleNumber.equals(cit.articleTitle, ignoreCase = true) || cit.articleTitle.contains(it.articleNumber, ignoreCase = true) }
                                                    ?: currentArticle
                                                if (art != null) {
                                                    inspectedArticle = art
                                                } else {
                                                    selectedTab = 1
                                                }
                                            },
                                            onPrintPdf = {
                                                val art = cit.articleId?.let { LegalArticlesRepository.getArticleById(it) }
                                                    ?: currentArticle
                                                val dictamen = LegalArticlesRepository.buildDictamen(
                                                    articleId = art?.id,
                                                    groupName = null,
                                                    lawId = art?.lawId ?: selectedLawId
                                                )
                                                val pdfFile = PdfGeneratorHelper.generateDictamenPdf(context, dictamen)
                                                PdfGeneratorHelper.printPdf(context, pdfFile, dictamen.title)
                                            }
                                        )
                                    }

                                    val additionalArticles = remember(message.text, selectedLawId) {
                                        findReferencedArticlesInText(message.text, message.citation?.lawId ?: selectedLawId)
                                            .filter { it.id != message.citation?.articleId }
                                    }

                                    if (additionalArticles.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Artículos citados en la respuesta:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                additionalArticles.forEach { refArt ->
                                                    OutlinedButton(
                                                        onClick = { inspectedArticle = refArt },
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(30.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            containerColor = LexiaSurfaceContainerLow,
                                                            contentColor = LexiaPrimary
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MenuBook,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp),
                                                            tint = LexiaPrimary
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${refArt.lawShortName} ${refArt.articleNumber}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "· Ver y PDF",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = LexiaSecondary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (message.exceptionalGrounds.isNotEmpty()) {
                                        Text(
                                            text = "Elementos Identificados:",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = LexiaOnSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        message.exceptionalGrounds.forEach { point ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 7.dp)
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(LexiaPrimary)
                                                )
                                                Text(
                                                    text = point,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = LexiaOnSurfaceVariant,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }

                                    // Grounding pill
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = LexiaSuccess100,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = LexiaSuccess600,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Cotejo normativo oficial SCJN / DOF",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = LexiaSuccess800,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Quick actions: Copiar respuesta y Dictamen PDF
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(message.text))
                                                Toast.makeText(context, "Respuesta copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = LexiaSecondary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copiar", style = MaterialTheme.typography.labelSmall, color = LexiaSecondary)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        FilledTonalButton(
                                            onClick = { onNavigateToDictamen(selectedArticleId, selectedGroupName) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(13.dp), tint = LexiaPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Dictamen", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Generating indicator
                if (isGeneratingResponse) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = LexiaSurfaceContainerLow,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = LexiaPrimary
                                )
                                Text(
                                    text = "Buscando en FAISS y sintetizando con IA...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LexiaPrimary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // MODE 1: STATUTORY TEXT & VECTOR CHUNKS INSPECTOR
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (currentArticle != null) {
                    // 1. SPECIFIC ARTICLE VIEW
                    item {
                        TextButton(
                            onClick = { selectGeneralConstitution() },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = LexiaPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Ver los ${currentLawArticles.size} artículos de ${currentLaw.shortName}",
                                color = LexiaPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentArticle.topicTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaOnSurface
                                        )
                                        Text(
                                            text = "Materia: ${currentArticle.category} · DOF Reforma: ${currentArticle.lastReformDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSecondary
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = LexiaSuccess100) {
                                        Text(
                                            text = currentArticle.dofStatus,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSuccess800,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LexiaSurfaceContainerLowest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentArticle.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LexiaOnSurfaceVariant,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { onNavigateToDictamen(currentArticle.id, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LexiaPrimary,
                                        contentColor = LexiaOnPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generar Dictamen PDF para ${currentArticle.articleNumber}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Fragmentos Vectoriales Indexados (k=${currentArticle.chunks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                    }

                    items(currentArticle.chunks) { chunk ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(if (chunk.isTopMatch) LexiaAi600 else LexiaSecondary)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${chunk.path} · ID: ${chunk.chunkId}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaOnSurface
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (chunk.isTopMatch) LexiaAi100 else LexiaSurfaceContainer
                                        ) {
                                            Text(
                                                text = "Sim: ${chunk.similarity}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (chunk.isTopMatch) LexiaAi800 else LexiaSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = chunk.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LexiaOnSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedGroupName != null) {
                    // 2. CHAPTER GROUP VIEW
                    val groupArticles = currentGroupArticles

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Capítulo: $selectedGroupName",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary
                                        )
                                        Text(
                                            text = "${groupArticles.size} Artículos agrupados · Bloque Temático",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSecondary
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = LexiaSuccess100) {
                                        Text(
                                            text = "DOF Vigente",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSuccess800,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Este grupo comprende las disposiciones constitucionales de la materia. Puedes tocar cualquier artículo para seleccionarlo individualmente o realizar preguntas globales sobre este capítulo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onNavigateToDictamen(null, selectedGroupName) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LexiaPrimary,
                                        contentColor = LexiaOnPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generar Dictamen PDF del Capítulo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Artículos de este Capítulo (${groupArticles.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                    }

                    items(groupArticles) { art ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectArticle(art) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${art.articleNumber}: ${art.topicTitle}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                    Text(
                                        text = art.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LexiaOnSurfaceVariant,
                                        maxLines = 2,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = LexiaPrimary)
                            }
                        }
                    }
                } else {
                    // 3. GENERAL LAW OVERVIEW & ARTICLES DIRECTORY (DYNAMIC FOR ANY SELECTED LAW)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentLaw.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary
                                        )
                                        Text(
                                            text = "${currentLaw.articlesCount} Artículos · Materia: ${currentLaw.category} · ${currentLaw.shortName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSecondary
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = LexiaSuccess100) {
                                        Text(
                                            text = currentLaw.dofStatus,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSuccess800,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Cuerpo normativo oficial en materia ${currentLaw.category.lowercase()}. Todas las consultas formuladas se procesan mediante búsqueda transversal FAISS en los ${currentLaw.articlesCount} artículos vigentes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showArticlePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LexiaPrimary,
                                        contentColor = LexiaOnPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explorar los ${currentLaw.articlesCount} Artículos de ${currentLaw.shortName}", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { onNavigateToDictamen(null, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = LexiaPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generar Dictamen Oficial de ${currentLaw.shortName}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Capítulos y Ejes de ${currentLaw.shortName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                    }

                    // Chapter horizontal selector chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentLawGroups) { gName ->
                                val isSelected = (selectedGroupName == null && gName.startsWith("Todos")) || selectedGroupName == gName
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (gName.startsWith("Todos")) {
                                            selectedGroupName = null
                                        } else {
                                            selectGroup(gName)
                                        }
                                    },
                                    label = { Text(gName, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LexiaPrimaryContainer,
                                        selectedLabelColor = LexiaOnPrimaryContainer,
                                        containerColor = LexiaSurfaceContainerLowest,
                                        labelColor = LexiaOnSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Artículos de ${currentLaw.shortName} (${currentGroupArticles.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LexiaPrimary
                            )
                            Text(
                                text = "Toca para inspeccionar",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary
                            )
                        }
                    }

                    items(currentGroupArticles) { art ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectArticle(art) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = art.articleNumber,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = LexiaSurfaceContainerHighest
                                        ) {
                                            Text(
                                                text = art.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = art.topicTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LexiaOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = art.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LexiaOnSurfaceVariant,
                                        maxLines = 2,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = LexiaPrimary)
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM INPUT BAR (Active for both Chat and Quick Questions)
        Surface(
            color = LexiaSurfaceContainerLowest,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LexiaErrorContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(LexiaError.copy(alpha = pulseAlpha))
                            )

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (currentRecognizedVoiceText.isNotBlank()) "«$currentRecognizedVoiceText»" else "Grabando consulta por voz...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LexiaOnErrorContainer,
                                    maxLines = 1
                                )
                                Text(
                                    text = "00:0${recordingSeconds} · Escuchando micrófono...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaError
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    audioHelper.stopRecording()
                                    isRecording = false
                                    recordingSeconds = 0
                                    currentRecognizedVoiceText = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cancelar",
                                    tint = LexiaError
                                )
                            }

                            IconButton(
                                onClick = { onAttemptSendVoiceMessage() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LexiaSuccess600)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Enviar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Text Field & Mic Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val inputPlaceholder = when {
                        isRecording -> if (currentRecognizedVoiceText.isNotBlank()) "🎙️ \"$currentRecognizedVoiceText\"" else "🎙️ Escuchando (${recordingSeconds}s)..."
                        currentArticle != null -> "Pregunta sobre ${currentArticle.articleNumber}..."
                        selectedGroupName != null -> "Pregunta sobre $selectedGroupName..."
                        else -> "Pregunta sobre la Constitución en general..."
                    }

                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = {
                            Text(
                                text = inputPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LexiaOutline
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isRecording,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LexiaSurfaceContainerLow,
                            unfocusedContainerColor = LexiaSurfaceContainerLow,
                            focusedBorderColor = LexiaPrimary,
                            unfocusedBorderColor = LexiaOutlineVariant
                        ),
                        maxLines = 3
                    )

                    if (queryText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                selectedTab = 0 // Switch to chat view
                                onAttemptSendTextMessage()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LexiaPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = LexiaOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (isRecording) {
                                    onAttemptSendVoiceMessage()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        isRecording = true
                                        selectedTab = 0 // Switch to chat view
                                        currentRecognizedVoiceText = ""
                                        val started = audioHelper.startRecording(
                                            onPartialResult = { transcribed ->
                                                currentRecognizedVoiceText = transcribed
                                            },
                                            onFinalResult = { finalText, _ ->
                                                if (finalText.isNotBlank()) {
                                                    currentRecognizedVoiceText = finalText
                                                }
                                            }
                                        )
                                        if (!started) {
                                            isRecording = false
                                            Toast.makeText(context, "No se pudo iniciar el reconocedor de voz", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) LexiaError else LexiaPrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Grabar voz",
                                tint = LexiaOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // SCOPE CLARIFICATION DIALOG: ASKS USER IF SEARCH IS GENERAL OR FOR SELECTED SCOPE (WITH COMBO)
    if (showScopeDialog) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        // Scope type: "GENERAL", "GROUP", "ARTICLE"
        var scopeType by remember(showScopeDialog) {
            mutableStateOf(
                when {
                    currentArticle != null -> "ARTICLE"
                    selectedGroupName != null -> "GROUP"
                    else -> "GENERAL"
                }
            )
        }
        var scopeGroupName by remember(showScopeDialog) {
            mutableStateOf(selectedGroupName ?: currentLawGroups.drop(1).firstOrNull() ?: "DDHH (Arts. 1-29)")
        }
        var scopeArticle by remember(showScopeDialog) {
            mutableStateOf(currentArticle ?: currentLawArticles.firstOrNull())
        }

        AlertDialog(
            onDismissRequest = { showScopeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = LexiaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Alcance de la Consulta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LexiaPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (pendingIsVoice) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LexiaPrimaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp), tint = LexiaPrimary)
                                    Text("🎙️ Nota de voz (${pendingVoiceDuration}s) · Pregunta detectada:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                                }
                                OutlinedTextField(
                                    value = pendingQueryText,
                                    onValueChange = { pendingQueryText = it },
                                    label = { Text("Pregunta a consultar con la IA", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    maxLines = 3
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LexiaSurfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "«$pendingQueryText»",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = LexiaOnSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Contexto de ley seleccionada
                    Text(
                        text = "Ley activa: ${currentLaw.title}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LexiaSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "¿Dónde deseas enfocar la búsqueda de la IA?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaOnSurface
                    )

                    // COMBO / SELECTOR DESPLEGABLE INTERACTIVO
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { dropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, LexiaPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (scopeType) {
                                            "GENERAL" -> Icons.Default.Balance
                                            "GROUP" -> Icons.Default.Shield
                                            else -> Icons.Default.PinDrop
                                        },
                                        contentDescription = null,
                                        tint = LexiaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    val comboText = when (scopeType) {
                                        "GENERAL" -> "Toda la ${currentLaw.shortName} General (${currentLaw.articlesCount} Arts.)"
                                        "GROUP" -> "Paquete: $scopeGroupName"
                                        "ARTICLE" -> "${scopeArticle?.articleNumber ?: "Art."} · ${scopeArticle?.topicTitle?.take(22) ?: ""}..."
                                        else -> "Toda la ${currentLaw.shortName} General"
                                    }
                                    Text(
                                        text = comboText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar",
                                    tint = LexiaPrimary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 380.dp)
                        ) {
                            // 1. Toda la Ley en General
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "🏛️ Toda la ${currentLaw.shortName} en General",
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary
                                        )
                                        Text(
                                            text = "Analizar todo el cuerpo normativo (${currentLaw.articlesCount} Artículos)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    scopeType = "GENERAL"
                                    dropdownExpanded = false
                                },
                                leadingIcon = {
                                    if (scopeType == "GENERAL") {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 2. Sección: Paquetes y Capítulos
                            Text(
                                text = "PAQUETES Y CAPÍTULOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaOutline,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )

                            currentLawGroups.drop(1).forEach { grp ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = "📁 $grp", style = MaterialTheme.typography.bodyMedium)
                                    },
                                    onClick = {
                                        scopeType = "GROUP"
                                        scopeGroupName = grp
                                        dropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        if (scopeType == "GROUP" && scopeGroupName == grp) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Sección: Artículos Individuales
                            Text(
                                text = "ARTÍCULOS ESPECÍFICOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaOutline,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )

                            currentLawArticles.take(10).forEach { art ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "📌 ${art.articleNumber} · ${art.topicTitle.take(28)}...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = art.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        scopeType = "ARTICLE"
                                        scopeArticle = art
                                        dropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        if (scopeType == "ARTICLE" && scopeArticle?.id == art.id) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 4. Ver los demás artículos del catálogo
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "🔍 Ver los ${currentLaw.articlesCount} artículos completos...",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    showScopeDialog = false
                                    showArticlePicker = true
                                }
                            )
                        }
                    }

                    // Explicación dinámica del alcance elegido
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LexiaSurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (scopeType) {
                                    "GENERAL" -> Icons.Default.Info
                                    "GROUP" -> Icons.Default.Shield
                                    else -> Icons.Default.PinDrop
                                },
                                contentDescription = null,
                                tint = LexiaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            val explanation = when (scopeType) {
                                "GENERAL" -> "La IA analizará todos los ${currentLaw.articlesCount} artículos de la ${currentLaw.shortName} sin acotación."
                                "GROUP" -> "La búsqueda se limitará a los artículos del paquete $scopeGroupName."
                                "ARTICLE" -> "La respuesta se enfocará exclusivamente en el ${scopeArticle?.articleNumber} (${scopeArticle?.topicTitle})."
                                else -> ""
                            }
                            Text(
                                text = explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = LexiaOnSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // BOTÓN PRINCIPAL DE BÚSQUEDA
                    Button(
                        onClick = {
                            val q = pendingQueryText
                            val isV = pendingIsVoice
                            val vFile = pendingVoiceFile
                            val vDur = pendingVoiceDuration
                            showScopeDialog = false

                            when (scopeType) {
                                "GENERAL" -> {
                                    selectedArticleId = null
                                    selectedGroupName = null
                                    executeScopedQuery(q, ScopeChoice.GENERAL_CONSTITUTION, isV, vFile, vDur)
                                }
                                "GROUP" -> {
                                    selectedArticleId = null
                                    selectedGroupName = scopeGroupName
                                    executeScopedQuery(q, ScopeChoice.GROUP_DDHH, isV, vFile, vDur)
                                }
                                "ARTICLE" -> {
                                    val art = scopeArticle
                                    if (art != null) {
                                        selectedArticleId = art.id
                                        selectedGroupName = null
                                        executeScopedQuery(q, ScopeChoice.SPECIFIC_ARTICLE, isV, vFile, vDur)
                                    } else {
                                        executeScopedQuery(q, ScopeChoice.GENERAL_CONSTITUTION, isV, vFile, vDur)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LexiaPrimary,
                            contentColor = LexiaOnPrimary
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val btnLabel = when (scopeType) {
                            "GENERAL" -> "Buscar en toda la ${currentLaw.shortName}"
                            "GROUP" -> "Buscar en $scopeGroupName"
                            "ARTICLE" -> "Buscar en ${scopeArticle?.articleNumber ?: "Artículo"}"
                            else -> "Consultar con este Alcance"
                        }
                        Text(btnLabel, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showScopeDialog = false }) {
                    Text("Cancelar", color = LexiaSecondary)
                }
            }
        )
    }

    // AI MODEL SELECTOR DIALOG: PERMITE AL ABOGADO ELEGIR EL MOTOR DE IA JURÍDICO
    if (showAiModelSelector) {
        AlertDialog(
            onDismissRequest = { showAiModelSelector = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = LexiaPrimaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(currentAiModel.iconEmoji, fontSize = 24.sp)
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccionar Inteligencia Artificial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Elige el motor especializado para tus consultas",
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.lexia.legalassistant.data.repository.AiModelRepository.availableModels.forEach { model ->
                        val isSelected = model.id == currentAiModel.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LexiaPrimaryContainer.copy(alpha = 0.25f) else LexiaSurfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) LexiaPrimary else LexiaOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.lexia.legalassistant.data.repository.AiModelRepository.selectModel(model.id)
                                    showAiModelSelector = false
                                    Toast.makeText(context, "IA activa: ${model.name}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(model.iconEmoji, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = model.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) LexiaPrimary else LexiaOnSurface
                                            )
                                            Text(
                                                text = model.specialty,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaSecondary
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Activo",
                                            tint = LexiaPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (model.isOfflineCapable) LexiaSuccess100 else LexiaSecondaryContainer
                                    ) {
                                        Text(
                                            text = model.speedIndicator,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (model.isOfflineCapable) LexiaSuccess800 else LexiaOnSecondaryContainer,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = model.shortBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaOutline
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LexiaSurfaceContainerLow,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LexiaOutlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isApiKeyCardExpanded = !isApiKeyCardExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Google Gemini API Key (Opcional)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                }
                                Icon(
                                    imageVector = if (isApiKeyCardExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = LexiaSecondary
                                )
                            }

                            if (isApiKeyCardExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Si deseas que Gemini responda en vivo desde los servidores de Google, ingresa tu API Key gratuita de Google AI Studio. Si lo dejas vacío, LexIA utiliza el motor dogmático inteligente 100% offline y privado.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = geminiApiKeyInput,
                                    onValueChange = { geminiApiKeyInput = it },
                                    label = { Text("API Key (AIzaSy...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            com.lexia.legalassistant.data.repository.AiModelRepository.setGeminiApiKey(geminiApiKeyInput)
                                            Toast.makeText(context, if (geminiApiKeyInput.isBlank()) "Modo Offline activado" else "API Key de Gemini guardada", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Guardar")
                                    }
                                    if (geminiApiKeyInput.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                geminiApiKeyInput = ""
                                                com.lexia.legalassistant.data.repository.AiModelRepository.setGeminiApiKey(null)
                                                Toast.makeText(context, "API Key eliminada · Modo Offline", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Borrar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAiModelSelector = false }) {
                    Text("Cerrar", color = LexiaSecondary)
                }
            }
        )
    }

    // BOTTOM SHEET: PICK ANY OF THE 136 CONSTITUTIONAL ARTICLES OR OTHER LAWS
    if (showArticlePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var pickerSearch by remember { mutableStateOf("") }
        var pickerChapterFilter by remember { mutableStateOf("Todos") }

        val chapterOptions = listOf("Todos") + currentLawGroups.drop(1)

        val allLawArticles = remember(selectedLawId) {
            LegalArticlesRepository.getArticlesForLaw(selectedLawId)
        }

        val filteredArticles = remember(pickerSearch, pickerChapterFilter, allLawArticles) {
            var list = LegalArticlesRepository.filterArticlesByGroup(allLawArticles, pickerChapterFilter)
            if (pickerSearch.isNotBlank()) {
                val q = pickerSearch.trim().lowercase()
                val qNum = q.filter { it.isDigit() }.toIntOrNull()
                list = list.filter {
                    (qNum != null && LegalArticlesRepository.articleNumberOf(it) == qNum) ||
                    it.articleNumber.lowercase().contains(q) ||
                    it.topicTitle.lowercase().contains(q) ||
                    it.summary.lowercase().contains(q)
                }
            }
            list
        }

        ModalBottomSheet(
            onDismissRequest = { showArticlePicker = false },
            sheetState = sheetState,
            containerColor = LexiaSurfaceContainerLowest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Directorio de ${currentLaw.shortName} (${currentLaw.articlesCount} Artículos)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = "Selecciona un artículo para cargarlo en el Chat y en el Inspector",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }

                    IconButton(onClick = { showArticlePicker = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = pickerSearch,
                    onValueChange = { pickerSearch = it },
                    placeholder = { Text("Buscar en ${currentLaw.shortName} por número o palabra clave...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LexiaSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chapter filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chapterOptions) { chap ->
                        val isSelected = pickerChapterFilter == chap
                        FilterChip(
                            selected = isSelected,
                            onClick = { pickerChapterFilter = chap },
                            label = { Text(chap, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LexiaPrimaryContainer,
                                selectedLabelColor = LexiaOnPrimaryContainer,
                                containerColor = LexiaSurfaceContainer,
                                labelColor = LexiaOnSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Articles List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredArticles) { art ->
                        val isCurrent = art.id == selectedArticleId
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) LexiaPrimaryContainer.copy(alpha = 0.5f) else LexiaSurfaceContainerLow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectArticle(art)
                                    showArticlePicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = art.articleNumber,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = LexiaSurfaceContainerHighest
                                        ) {
                                            Text(
                                                text = art.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LexiaSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = art.topicTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LexiaOnSurface
                                    )
                                    Text(
                                        text = art.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LexiaOnSurfaceVariant,
                                        maxLines = 2,
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = LexiaPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // VENTANA / MODAL DEDICADO PARA VISUALIZACIÓN ÍNTEGRA DEL ARTÍCULO REFERENCIADO
    inspectedArticle?.let { article ->
        ModalBottomSheet(
            onDismissRequest = { inspectedArticle = null },
            containerColor = LexiaSurfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Ley y Estatus DOF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = article.articleNumber,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = "${article.lawShortName} · Materia: ${article.category}",
                            style = MaterialTheme.typography.labelMedium,
                            color = LexiaSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LexiaSuccess100
                    ) {
                        Text(
                            text = article.dofStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSuccess800,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Topic title
                Text(
                    text = article.topicTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LexiaOnSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Última reforma publicada en DOF: ${article.lastReformDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LexiaOutline
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Texto Íntegro / Contenido Normativo Completo
                Text(
                    text = "TEXTO ÍNTEGRO DEL PRECEPTO LEGAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LexiaPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LexiaSurfaceContainerLow,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LexiaOutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LexiaOnSurface,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (article.chunks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "FRANGMENTOS Y CRITERIOS INDEXADOS (k=${article.chunks.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LexiaSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    article.chunks.forEach { chunk ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = chunk.path,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                    Text(
                                        text = "Similitud: ${chunk.similarity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LexiaSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chunk.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Barra de Acciones: Imprimir PDF y Abrir Dictamen Oficial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val dictamen = LegalArticlesRepository.buildDictamen(
                                articleId = article.id,
                                groupName = null,
                                lawId = article.lawId
                            )
                            val pdfFile = PdfGeneratorHelper.generateDictamenPdf(context, dictamen)
                            PdfGeneratorHelper.printPdf(context, pdfFile, "${article.lawShortName} ${article.articleNumber}")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LexiaPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Imprimir",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imprimir PDF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            inspectedArticle = null
                            onNavigateToDictamen(article.id, null)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LexiaPrimary,
                            contentColor = LexiaOnPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Dictamen",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver Dictamen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

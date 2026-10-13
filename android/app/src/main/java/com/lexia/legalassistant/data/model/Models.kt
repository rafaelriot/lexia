package com.lexia.legalassistant.data.model

enum class ScopeChoice {
    SPECIFIC_ARTICLE,
    GROUP_DDHH,
    GENERAL_CONSTITUTION
}

data class LawItem(
    val id: String,
    val title: String,
    val shortName: String,
    val dofStatus: String = "DOF Vigente",
    val jurisdiction: String = "Federal",
    val category: String = "Constitucional",
    val articlesCount: Int = 136,
    val lastReformDate: String = "Mayo 2021"
)

data class LegalCitation(
    val articleTitle: String,
    val excerpt: String,
    val similarityScore: Float = 0.942f,
    val reformDate: String = "DOF 28-05-2021",
    val citationPath: String = "CPEUM > Art. 16 > Párrafo 1°"
)

data class RagMessage(
    val id: String,
    val isUser: Boolean,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val citation: LegalCitation? = null,
    val exceptionalGrounds: List<String> = emptyList(),
    val isVoiceRecording: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val voiceAudioWaveform: List<Float> = emptyList(),
    val audioFilePath: String? = null,
    val transcribedText: String? = null
)

data class VectorChunk(
    val chunkId: String,
    val chunkIndex: Int,
    val similarity: Float,
    val path: String,
    val content: String,
    val isTopMatch: Boolean = false
)

data class LawArticleDetail(
    val id: String,
    val lawId: String,
    val lawShortName: String,
    val articleNumber: String,
    val topicTitle: String,
    val category: String,
    val summary: String,
    val dofStatus: String = "VIGENTE DOF",
    val lastReformDate: String = "28 de mayo de 2021",
    val totalChunks: Int = 18,
    val chunks: List<VectorChunk>
)

data class DictamenData(
    val fileName: String,
    val title: String,
    val subtitle: String,
    val expediente: String,
    val materiaConsultadaTitle: String = "I. PRECISIÓN DE LA MATERIA CONSULTADA",
    val materiaConsultadaDesc: String,
    val fundamentacionNormativaTitle: String = "II. FUNDAMENTACIÓN NORMATIVA DOF",
    val fundamentacionNormativaText: String,
    val supuestosTitle: String = "III. ELEMENTOS JURISPRUDENCIALES Y DOGMÁTICOS",
    val supuestosList: List<String>,
    val reformDate: String = "DOF Vigente",
    val dofStatus: String = "VIGENTE DOF",
    val hashSha256: String = "4f98a2e1d71b839c09bf45ef203847291aeb7c4581938471928374a5e1289c09"
)

data class ConsultationSession(
    val id: String,
    val userId: String = "guest",
    val title: String,
    val date: String,
    val queriesCount: Int,
    val chunksCount: Int,
    val lawTag: String,
    val lawId: String = "cpeum",
    val articleId: String? = null,
    val groupName: String? = null,
    val lastQueryText: String = "",
    val aiAnswerExcerpt: String = "",
    val isVoice: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class LawSyncStatus(
    val lawId: String,
    val shortName: String,
    val fullName: String,
    val articlesCount: Int,
    val lastSyncDate: String,
    val versionDof: String,
    val isUpdating: Boolean = false,
    val statusMessage: String = "Sincronizado y vigente DOF",
    val officialSourceUrl: String = "https://www.diputados.gob.mx/LeyesBiblio/"
)


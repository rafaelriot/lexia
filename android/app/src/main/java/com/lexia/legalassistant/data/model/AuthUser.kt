package com.lexia.legalassistant.data.model

enum class AuthProvider {
    GOOGLE,
    CREDENTIALS,
    GUEST
}

enum class LicenseTier(val displayName: String, val badge: String, val dailyQueries: Int) {
    ESTUDIANTE_GRATUITO("Estudiante / Básico", "10 consultas/día", 10),
    PROFESIONAL_FAISS("Abogado / FAISS RAG", "Ilimitado", 9999),
    PODER_JUDICIAL("Poder Judicial / Institucional", "Ilimitado + Privado", 9999)
}

data class UserAiContract(
    val email: String,
    val fullName: String,
    val planName: String,
    val contractedEngines: List<AiEngineType>,
    val licenseBadge: String,
    val institution: String,
    val isValidContract: Boolean,
    val licenseToken: String
)

data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val cedulaProfesional: String? = null,
    val authProvider: AuthProvider = AuthProvider.GOOGLE,
    val licenseTier: LicenseTier = LicenseTier.PROFESIONAL_FAISS,
    val isLoggedIn: Boolean = true,
    val token: String? = null,
    val contractedAiEngines: List<AiEngineType> = listOf(AiEngineType.OFFLINE_FAISS, AiEngineType.GEMINI_FLASH)
)

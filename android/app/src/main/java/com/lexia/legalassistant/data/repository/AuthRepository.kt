package com.lexia.legalassistant.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.lexia.legalassistant.data.model.AuthProvider
import com.lexia.legalassistant.data.model.AuthUser
import com.lexia.legalassistant.data.model.LicenseTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthRepository {

    private const val PREFS_NAME = "lexia_auth_prefs"
    private const val KEY_IS_LOGGED = "key_is_logged"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_EMAIL = "key_email"
    private const val KEY_NAME = "key_name"
    private const val KEY_PROVIDER = "key_provider"
    private const val KEY_TIER = "key_tier"
    private const val KEY_CEDULA = "key_cedula"
    private const val KEY_PHOTO_URL = "key_photo_url"

    private var prefs: SharedPreferences? = null

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }

    private fun loadSession() {
        val p = prefs ?: return
        val isLogged = p.getBoolean(KEY_IS_LOGGED, false)
        if (isLogged) {
            val id = p.getString(KEY_USER_ID, "usr_default") ?: "usr_default"
            val email = p.getString(KEY_EMAIL, "abogado@lexia.mx") ?: "abogado@lexia.mx"
            val name = p.getString(KEY_NAME, "Lic. Usuario") ?: "Lic. Usuario"
            val providerStr = p.getString(KEY_PROVIDER, AuthProvider.GOOGLE.name)
            val provider = try { AuthProvider.valueOf(providerStr!!) } catch (e: Exception) { AuthProvider.GOOGLE }
            val tierStr = p.getString(KEY_TIER, LicenseTier.PROFESIONAL_FAISS.name)
            val tier = try { LicenseTier.valueOf(tierStr!!) } catch (e: Exception) { LicenseTier.PROFESIONAL_FAISS }
            val cedula = p.getString(KEY_CEDULA, null)
            val photoUrl = p.getString(KEY_PHOTO_URL, null)

            _currentUser.value = AuthUser(
                id = id,
                email = email,
                displayName = name,
                photoUrl = photoUrl,
                authProvider = provider,
                licenseTier = tier,
                cedulaProfesional = cedula,
                isLoggedIn = true
            )
        } else {
            // Default: no session active yet
            _currentUser.value = null
        }
    }

    fun checkEmailAiContract(email: String): com.lexia.legalassistant.data.model.UserAiContract {
        val cleanEmail = email.trim().lowercase()
        return when {
            cleanEmail.endsWith("@scjn.gob.mx") || cleanEmail.endsWith("@pjf.gob.mx") || cleanEmail.endsWith("@tribunal.mx") || cleanEmail.contains("pj") -> {
                com.lexia.legalassistant.data.model.UserAiContract(
                    email = email,
                    fullName = "Funcionario Judicial Colegiado",
                    planName = "Convenio Institucional SCJN / Poder Judicial",
                    contractedEngines = listOf(
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO,
                        com.lexia.legalassistant.data.model.AiEngineType.GROQ_LPU,
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                        com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS
                    ),
                    licenseBadge = "🌟 SUITE COMPLETA ILIMITADA",
                    institution = "Suprema Corte & Consejo de la Judicatura Federal",
                    isValidContract = true,
                    licenseToken = "SCJN-ENTERPRISE-2026"
                )
            }
            cleanEmail.endsWith("@unam.mx") || cleanEmail.endsWith("@derecho.unam.mx") || cleanEmail.endsWith("@buap.mx") || cleanEmail.endsWith("@itesm.mx") || cleanEmail.endsWith("@anahuac.mx") -> {
                com.lexia.legalassistant.data.model.UserAiContract(
                    email = email,
                    fullName = "Investigador / Catedrático de Derecho",
                    planName = "Convenio Académico Universitario",
                    contractedEngines = listOf(
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                        com.lexia.legalassistant.data.model.AiEngineType.GROQ_LPU,
                        com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS,
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO
                    ),
                    licenseBadge = "🎓 ACADÉMICO / INVESTIGACIÓN",
                    institution = "Red Nacional de Facultades de Derecho",
                    isValidContract = true,
                    licenseToken = "UNAM-ACAD-IA-2026"
                )
            }
            cleanEmail.contains("amparo") || cleanEmail.contains("litigio") || cleanEmail.contains("abogad") || cleanEmail.contains("legal") -> {
                com.lexia.legalassistant.data.model.UserAiContract(
                    email = email,
                    fullName = "Licenciado Postulante Certificado",
                    planName = "Contrato Despacho Litigante Groq & Gemini",
                    contractedEngines = listOf(
                        com.lexia.legalassistant.data.model.AiEngineType.GROQ_LPU,
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO,
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                        com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS
                    ),
                    licenseBadge = "⚡ LITIGIO ULTRA (GROQ & GEMINI)",
                    institution = "Barra y Colegio de Abogados",
                    isValidContract = true,
                    licenseToken = "BARRA-LITIGIO-GROQ"
                )
            }
            cleanEmail.isNotEmpty() && cleanEmail.contains("@") -> {
                // Contrato Estándar Profesional para cualquier otro correo verificado
                com.lexia.legalassistant.data.model.UserAiContract(
                    email = email,
                    fullName = cleanEmail.substringBefore("@").replace(".", " ").capitalize(),
                    planName = "Membresía Profesional Colegiada",
                    contractedEngines = listOf(
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                        com.lexia.legalassistant.data.model.AiEngineType.GROQ_LPU,
                        com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS,
                        com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO
                    ),
                    licenseBadge = "⚡ ACCESO PROFESIONAL ACTIVO",
                    institution = "Membresía Individual LexIA",
                    isValidContract = true,
                    licenseToken = "LEXIA-PRO-${cleanEmail.hashCode().toString().takeLast(6)}"
                )
            }
            else -> {
                com.lexia.legalassistant.data.model.UserAiContract(
                    email = email,
                    fullName = "Usuario No Registrado",
                    planName = "Sin contrato activo",
                    contractedEngines = emptyList(),
                    licenseBadge = "SIN CONTRATO",
                    institution = "N/A",
                    isValidContract = false,
                    licenseToken = ""
                )
            }
        }
    }

    fun registerAndLoginWithContract(
        name: String,
        email: String,
        cedula: String?,
        contract: com.lexia.legalassistant.data.model.UserAiContract
    ): AuthUser {
        val user = AuthUser(
            id = "user_${email.hashCode()}",
            email = email,
            displayName = name.ifBlank { contract.fullName },
            cedulaProfesional = cedula,
            authProvider = AuthProvider.CREDENTIALS,
            licenseTier = if (contract.contractedEngines.contains(com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO)) {
                LicenseTier.PODER_JUDICIAL
            } else {
                LicenseTier.PROFESIONAL_FAISS
            },
            isLoggedIn = true,
            token = contract.licenseToken,
            contractedAiEngines = contract.contractedEngines
        )
        saveUser(user)
        return user
    }

    fun loginWithGoogle(email: String, displayName: String, idToken: String? = null, photoUrl: String? = null): AuthUser {
        val contract = checkEmailAiContract(email)
        val user = AuthUser(
            id = "google_${email.hashCode()}",
            email = email,
            displayName = displayName.ifBlank { email.substringBefore("@") },
            photoUrl = photoUrl,
            authProvider = AuthProvider.GOOGLE,
            licenseTier = LicenseTier.PROFESIONAL_FAISS,
            isLoggedIn = true,
            token = idToken ?: contract.licenseToken,
            contractedAiEngines = contract.contractedEngines.ifEmpty { listOf(com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH, com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS) }
        )
        saveUser(user)
        return user
    }

    fun loginWithCedula(cedula: String, licenseToken: String): AuthUser {
        val user = AuthUser(
            id = "cedula_$cedula",
            email = "cedula.$cedula@tribunal.mx",
            displayName = "Licenciado Colegiado ($cedula)",
            cedulaProfesional = cedula,
            authProvider = AuthProvider.CREDENTIALS,
            licenseTier = if (licenseToken.contains("PREM", ignoreCase = true)) LicenseTier.PODER_JUDICIAL else LicenseTier.PROFESIONAL_FAISS,
            isLoggedIn = true,
            token = licenseToken,
            contractedAiEngines = listOf(
                com.lexia.legalassistant.data.model.AiEngineType.GEMINI_PRO,
                com.lexia.legalassistant.data.model.AiEngineType.GROQ_LPU,
                com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS
            )
        )
        saveUser(user)
        return user
    }

    fun generateUnamStudentCredentials(fullName: String): Pair<String, String> {
        val sanitized = fullName.trim()
            .lowercase()
            .replace(Regex("[áàäâ]"), "a")
            .replace(Regex("[éèëê]"), "e")
            .replace(Regex("[íìïî]"), "i")
            .replace(Regex("[óòöô]"), "o")
            .replace(Regex("[úùüû]"), "u")
            .replace(Regex("[ñ]"), "n")
            .replace(Regex("[^a-z0-9\\s]"), "")
        val parts = sanitized.split(Regex("\\s+")).filter { it.isNotBlank() }
        val alias = when {
            parts.size >= 2 -> "${parts[0]}.${parts[1]}"
            parts.size == 1 -> parts[0]
            else -> "alumno.unam"
        }
        val randomSuffix = (100..999).random()
        val generatedEmail = "$alias$randomSuffix@derecho.unam.mx"
        val generatedMatricula = "UNAM-DER-${(3000000..3999999).random()}"
        return Pair(generatedEmail, generatedMatricula)
    }

    fun registerStudent(
        fullName: String,
        email: String,
        matricula: String
    ): AuthUser {
        val cleanEmail = email.trim().lowercase()
        val studentId = "student_${cleanEmail.hashCode().toString().replace("-", "s")}_${System.currentTimeMillis() % 10000}"
        val user = AuthUser(
            id = studentId,
            email = cleanEmail,
            displayName = fullName.trim().ifBlank { "Estudiante UNAM ($matricula)" },
            cedulaProfesional = matricula,
            authProvider = AuthProvider.CREDENTIALS,
            licenseTier = LicenseTier.ESTUDIANTE_GRATUITO,
            isLoggedIn = true,
            token = "UNAM-LIC-$matricula",
            contractedAiEngines = listOf(
                com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS
            )
        )
        saveUser(user)
        return user
    }

    fun loginAsGuest(): AuthUser {
        val uniqueTimestamp = System.currentTimeMillis()
        val randomSuffix = (1000..9999).random()
        val studentEmail = "estudiante.$randomSuffix@derecho.unam.mx"
        val user = AuthUser(
            id = "guest_student_$uniqueTimestamp",
            email = studentEmail,
            displayName = "Estudiante UNAM #$randomSuffix",
            authProvider = AuthProvider.GUEST,
            licenseTier = LicenseTier.ESTUDIANTE_GRATUITO,
            isLoggedIn = true,
            cedulaProfesional = "MATR-TEMP-$randomSuffix",
            contractedAiEngines = listOf(
                com.lexia.legalassistant.data.model.AiEngineType.GEMINI_FLASH,
                com.lexia.legalassistant.data.model.AiEngineType.OFFLINE_FAISS
            )
        )
        saveUser(user)
        return user
    }

    fun logout() {
        _currentUser.value = null
        prefs?.edit()?.clear()?.apply()
    }

    private fun saveUser(user: AuthUser) {
        _currentUser.value = user
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED, true)
            putString(KEY_USER_ID, user.id)
            putString(KEY_EMAIL, user.email)
            putString(KEY_NAME, user.displayName)
            putString(KEY_PHOTO_URL, user.photoUrl)
            putString(KEY_PROVIDER, user.authProvider.name)
            putString(KEY_TIER, user.licenseTier.name)
            putString(KEY_CEDULA, user.cedulaProfesional)
            apply()
        }
    }
}

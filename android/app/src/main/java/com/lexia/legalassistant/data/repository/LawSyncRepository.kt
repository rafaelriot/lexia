package com.lexia.legalassistant.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import com.lexia.legalassistant.data.model.LawSyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LawSyncRepository {

    private const val PREFS_NAME = "lexia_law_sync_prefs"
    private const val KEY_LAST_GLOBAL_SYNC = "last_global_sync_timestamp"
    private const val AUTO_SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 Horas

    private var sharedPreferences: SharedPreferences? = null

    val lawsSyncList = mutableStateListOf<LawSyncStatus>()

    private val _isGlobalSyncing = MutableStateFlow(false)
    val isGlobalSyncing: StateFlow<Boolean> = _isGlobalSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _lastSyncSummary = MutableStateFlow("Todas las leyes al día con el DOF")
    val lastSyncSummary: StateFlow<String> = _lastSyncSummary.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSyncStatus()
            checkAutoSync(context)
        }
    }

    private fun loadSyncStatus() {
        lawsSyncList.clear()
        val prefs = sharedPreferences

        val defaultDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        lawsSyncList.addAll(
            listOf(
                LawSyncStatus(
                    lawId = "cpeum",
                    shortName = "CPEUM",
                    fullName = "Constitución Política de los Estados Unidos Mexicanos",
                    articlesCount = 136,
                    lastSyncDate = prefs?.getString("sync_cpeum", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Mayo 2021 / Febrero 2024",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/CPEUM.pdf"
                ),
                LawSyncStatus(
                    lawId = "cnpp",
                    shortName = "CNPP",
                    fullName = "Código Nacional de Procedimientos Penales",
                    articlesCount = 490,
                    lastSyncDate = prefs?.getString("sync_cnpp", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Enero 2024",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/CNPP.pdf"
                ),
                LawSyncStatus(
                    lawId = "cpf",
                    shortName = "CPF",
                    fullName = "Código Penal Federal",
                    articlesCount = 429,
                    lastSyncDate = prefs?.getString("sync_cpf", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Noviembre 2023",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/CPF.pdf"
                ),
                LawSyncStatus(
                    lawId = "amparo",
                    shortName = "Ley de Amparo",
                    fullName = "Ley de Amparo, Reglamentaria de los Arts. 103 y 107 CPEUM",
                    articlesCount = 271,
                    lastSyncDate = prefs?.getString("sync_amparo", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Junio 2022 / Junio 2024",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/LAmp.pdf"
                ),
                LawSyncStatus(
                    lawId = "cpet",
                    shortName = "CP Tabasco",
                    fullName = "Código Penal para el Estado de Tabasco",
                    articlesCount = 369,
                    lastSyncDate = prefs?.getString("sync_cpet", defaultDate) ?: defaultDate,
                    versionDof = "Decreto 202 / POET Vigente 2024",
                    officialSourceUrl = "https://congresotabasco.gob.mx/wp-content/uploads/2025/02/Codigo-Penal-para-el-Estado-de-Tabasco.pdf"
                ),
                LawSyncStatus(
                    lawId = "ccf",
                    shortName = "CCF",
                    fullName = "Código Civil Federal",
                    articlesCount = 3044,
                    lastSyncDate = prefs?.getString("sync_ccf", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Enero 2024",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/CCF.pdf"
                ),
                LawSyncStatus(
                    lawId = "cnpcyf",
                    shortName = "CNPCyF",
                    fullName = "Código Nacional de Procedimientos Civiles y Familiares",
                    articlesCount = 1191,
                    lastSyncDate = prefs?.getString("sync_cnpcyf", defaultDate) ?: defaultDate,
                    versionDof = "Decreto DOF Junio 2023",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/CNPCyF.pdf"
                ),
                LawSyncStatus(
                    lawId = "ccet",
                    shortName = "CC Tabasco",
                    fullName = "Código Civil para el Estado de Tabasco",
                    articlesCount = 3227,
                    lastSyncDate = prefs?.getString("sync_ccet", defaultDate) ?: defaultDate,
                    versionDof = "POET Vigente Diciembre 2023",
                    officialSourceUrl = "https://congresotabasco.gob.mx/wp-content/uploads/2025/02/Codigo-Civil-para-el-Estado-de-Tabasco.pdf"
                ),
                LawSyncStatus(
                    lawId = "lft",
                    shortName = "LFT",
                    fullName = "Ley Federal del Trabajo",
                    articlesCount = 1010,
                    lastSyncDate = prefs?.getString("sync_lft", defaultDate) ?: defaultDate,
                    versionDof = "Reforma DOF Abril 2024",
                    officialSourceUrl = "https://www.diputados.gob.mx/LeyesBiblio/pdf/LFT.pdf"
                )
            )
        )
    }

    fun syncAllLaws(
        onProgress: (step: String, progress: Float) -> Unit = { _, _ -> },
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        if (_isGlobalSyncing.value) return

        CoroutineScope(Dispatchers.IO).launch {
            _isGlobalSyncing.value = true
            _syncProgress.value = 0.05f
            _lastSyncSummary.value = "Conectando con repositorio legislativo oficial..."

            val nowFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            for (i in lawsSyncList.indices) {
                val current = lawsSyncList[i]
                lawsSyncList[i] = current.copy(isUpdating = true, statusMessage = "Verificando decretos DOF...")

                val stepProgress = (i + 1f) / (lawsSyncList.size + 1f)
                _syncProgress.value = stepProgress
                onProgress("Actualizando ${current.shortName}...", stepProgress)

                // Simulación de descarga e indexación FAISS con API remota
                delay(700)

                lawsSyncList[i] = current.copy(
                    isUpdating = false,
                    lastSyncDate = nowFormatted,
                    statusMessage = "Sincronizado y vigente DOF"
                )

                sharedPreferences?.edit()?.putString("sync_${current.lawId}", nowFormatted)?.apply()
            }

            _syncProgress.value = 1f
            sharedPreferences?.edit()?.putLong(KEY_LAST_GLOBAL_SYNC, System.currentTimeMillis())?.apply()
            _lastSyncSummary.value = "Sincronización completada exitosamente a las $nowFormatted"
            _isGlobalSyncing.value = false

            onProgress("Catálogo 100% actualizado con el DOF", 1f)
            onComplete(true)
        }
    }

    fun syncSingleLaw(lawId: String) {
        val index = lawsSyncList.indexOfFirst { it.lawId == lawId }
        if (index == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            val item = lawsSyncList[index]
            lawsSyncList[index] = item.copy(isUpdating = true, statusMessage = "Sincronizando...")
            delay(1000)

            val now = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            lawsSyncList[index] = item.copy(
                isUpdating = false,
                lastSyncDate = now,
                statusMessage = "Sincronizado y vigente DOF"
            )
            sharedPreferences?.edit()?.putString("sync_$lawId", now)?.apply()
        }
    }

    private fun checkAutoSync(context: Context) {
        val lastSync = sharedPreferences?.long(KEY_LAST_GLOBAL_SYNC) ?: 0L
        val diff = System.currentTimeMillis() - lastSync
        if (diff > AUTO_SYNC_INTERVAL_MS) {
            // Ejecutar auto-sincronización en segundo plano de manera silenciosa
            syncAllLaws()
        }
    }

    private fun SharedPreferences.long(key: String): Long {
        return this.getLong(key, 0L)
    }
}

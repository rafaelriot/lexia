package com.lexia.legalassistant.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import com.lexia.legalassistant.data.repository.ConsultationHistoryRepository
import com.lexia.legalassistant.data.repository.LawSyncRepository
import com.lexia.legalassistant.data.repository.AiModelRepository
import com.lexia.legalassistant.ui.components.AiProviderSettingsCard
import com.lexia.legalassistant.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        LawSyncRepository.init(context)
        com.lexia.legalassistant.data.repository.AuthRepository.init(context)
        AiModelRepository.init(context)
        ConsultationHistoryRepository.init(context)
    }

    val currentAiModel by AiModelRepository.currentModel.collectAsState()

    val currentUser by com.lexia.legalassistant.data.repository.AuthRepository.currentUser.collectAsState()
    val activeUserId = currentUser?.id ?: "guest"
    val userConsultationsCount = remember(ConsultationHistoryRepository.sessions.toList(), activeUserId) {
        ConsultationHistoryRepository.getSessionsCountForUser(activeUserId)
    }
    val isSyncing by LawSyncRepository.isGlobalSyncing.collectAsState()
    val syncProgress by LawSyncRepository.syncProgress.collectAsState()
    val syncSummary by LawSyncRepository.lastSyncSummary.collectAsState()

    var autoSyncEnabled by remember { mutableStateOf(true) }

    var userAvatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val photoUrl = currentUser?.photoUrl

    LaunchedEffect(photoUrl) {
        if (!photoUrl.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = URL(photoUrl).openStream()
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        userAvatarBitmap = bmp.asImageBitmap()
                    }
                } catch (e: Exception) {
                    userAvatarBitmap = null
                }
            }
        } else {
            userAvatarBitmap = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
    ) {
        // TOP APP BAR
        Surface(
            color = LexiaSurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LexiaPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L",
                            color = LexiaOnPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "LexIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = "Perfil & Sincronización Normativa",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LexiaSuccess100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LexiaSuccess800, modifier = Modifier.size(12.dp))
                        Text("PREMIUM ACTIVO", style = MaterialTheme.typography.labelSmall, color = LexiaSuccess800, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // LAWYER PROFILE HEADER CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(LexiaPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (userAvatarBitmap != null) {
                                    Image(
                                        bitmap = userAvatarBitmap!!,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else if (currentUser != null && currentUser!!.displayName.isNotBlank()) {
                                    Text(
                                        text = currentUser!!.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = LexiaPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: "Lic. Usuario (Sin iniciar sesión)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaPrimary
                                )
                                Text(
                                    text = currentUser?.email ?: "Acceso como invitado · Modo consulta libre",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentUser != null) Icons.Default.Verified else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (currentUser != null) LexiaSuccess600 else LexiaSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = currentUser?.let { "${it.licenseTier.displayName} (${it.licenseTier.badge})" } ?: "Inicia sesión con tu cuenta de Gmail",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUser != null) LexiaSuccess800 else LexiaOutline
                                    )
                                }
                            }
                        }

                        // Botón de autenticación / Cerrar sesión
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (currentUser != null) {
                                OutlinedButton(
                                    onClick = {
                                        com.lexia.legalassistant.data.repository.AuthRepository.logout()
                                        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                                        onLogout()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cerrar Sesión", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = onNavigateToAuth,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LexiaPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Iniciar con Google (Gmail)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        HorizontalDivider(color = LexiaOutlineVariant.copy(alpha = 0.5f))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("10,167", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                                Text("Artículos DOF/POET", style = MaterialTheme.typography.labelSmall, color = LexiaSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$userConsultationsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                                Text("Consultas RAG", style = MaterialTheme.typography.labelSmall, color = LexiaSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("9 Códigos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                                Text("Leyes Activas", style = MaterialTheme.typography.labelSmall, color = LexiaSecondary)
                            }
                        }
                    }
                }
            }

            // AI PROVIDERS & API KEYS SETTINGS
            item {
                AiProviderSettingsCard(currentModel = currentAiModel)
            }

            // SECTION HEADER: LEGAL REPOSITORY SYNC
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Biblioteca Normativa y Actualización",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                    }

                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LexiaPrimary
                        )
                    }
                }
            }

            // MASTER SYNC CARD WITH "ACTUALIZAR LEYES AHORA" BUTTON
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LexiaPrimaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LexiaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = LexiaOnPrimary, modifier = Modifier.size(22.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sincronizador con el DOF",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaPrimary
                                )
                                Text(
                                    text = syncSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSecondary
                                )
                            }
                        }

                        if (isSyncing) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LinearProgressIndicator(
                                    progress = { syncProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = LexiaPrimary,
                                    trackColor = LexiaOutlineVariant
                                )
                                Text(
                                    text = "Descargando reformas y recalculando vectores FAISS...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // MASTER ACTION BUTTON
                        Button(
                            onClick = {
                                LawSyncRepository.syncAllLaws(
                                    onProgress = { _, _ -> },
                                    onComplete = {
                                        Toast.makeText(context, "Todas las leyes fueron actualizadas con el DOF", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LexiaPrimary,
                                contentColor = LexiaOnPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Default.HourglassTop else Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSyncing) "Actualizando biblioteca..." else "Actualizar Todas las Leyes Ahora",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // INDIVIDUAL LAW BOOKS LIST
            items(LawSyncRepository.lawsSyncList) { lawSync ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(LexiaPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (lawSync.lawId) {
                                            "cnpp" -> Icons.Default.Gavel
                                            "cpf" -> Icons.Default.LocalPolice
                                            "amparo" -> Icons.Default.Shield
                                            "cpet" -> Icons.Default.AccountBalance
                                            else -> Icons.Default.Balance
                                        },
                                        contentDescription = null,
                                        tint = LexiaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = lawSync.shortName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaPrimary
                                    )
                                    Text(
                                        text = "${lawSync.articlesCount} Artículos indexados",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LexiaSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (lawSync.isUpdating) LexiaPrimaryContainer else LexiaSuccess100
                            ) {
                                Text(
                                    text = if (lawSync.isUpdating) "Actualizando..." else "DOF Vigente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (lawSync.isUpdating) LexiaPrimary else LexiaSuccess800,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = lawSync.fullName,
                            style = MaterialTheme.typography.bodySmall,
                            color = LexiaOnSurfaceVariant
                        )

                        Divider(color = LexiaOutlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Última verificación:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaOutline,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = lawSync.lastSyncDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaSecondary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    LawSyncRepository.syncSingleLaw(lawSync.lawId)
                                    Toast.makeText(context, "Sincronizando ${lawSync.shortName}...", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !lawSync.isUpdating && !isSyncing,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verificar", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // AUTO SYNC SETTINGS CARD
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
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
                            text = "Configuración de Actualización",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sincronización automática periódica", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = LexiaPrimary)
                                Text("Verifica reformas en el DOF cada 24 horas en segundo plano.", style = MaterialTheme.typography.bodySmall, color = LexiaSecondary)
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { autoSyncEnabled = it }
                            )
                        }

                        Divider(color = LexiaOutlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Disponibilidad sin conexión", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = LexiaPrimary)
                                Text("Los 1,326 artículos están disponibles localmente sin internet.", style = MaterialTheme.typography.bodySmall, color = LexiaSecondary)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LexiaSuccess600, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            // LOGOUT BUTTON
            item {
                OutlinedButton(
                    onClick = {
                        com.lexia.legalassistant.data.repository.AuthRepository.logout()
                        Toast.makeText(context, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LexiaError),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cerrar Sesión")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

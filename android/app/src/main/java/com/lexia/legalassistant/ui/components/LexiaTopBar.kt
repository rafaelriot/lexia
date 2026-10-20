package com.lexia.legalassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
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
import com.lexia.legalassistant.ui.theme.*
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap

@Composable
fun LexiaTopBar(
    subtitle: String,
    onProfileClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.lexia.legalassistant.data.repository.AiModelRepository.init(context)
        com.lexia.legalassistant.data.repository.AuthRepository.init(context)
    }

    val currentAiModel by com.lexia.legalassistant.data.repository.AiModelRepository.currentModel.collectAsState()
    val currentUser by com.lexia.legalassistant.data.repository.AuthRepository.currentUser.collectAsState()
    var showAiSelectorModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Cargar foto de perfil de Google en segundo plano de manera reactiva
    var userAvatarBitmap by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ImageBitmap?>(null) }
    val photoUrl = currentUser?.photoUrl

    androidx.compose.runtime.LaunchedEffect(photoUrl) {
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

    // DIÁLOGO MODAL GLOBAL DE SELECCIÓN DE IA
    if (showAiSelectorModal) {
        AlertDialog(
            onDismissRequest = { showAiSelectorModal = false },
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
                        text = "Motor de Inteligencia Artificial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Selecciona la IA especializada para tus consultas",
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
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) LexiaPrimary else LexiaOutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.lexia.legalassistant.data.repository.AiModelRepository.selectModel(model.id)
                                    showAiSelectorModal = false
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
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAiSelectorModal = false }) {
                    Text("Cerrar", color = LexiaSecondary)
                }
            }
        )
    }

    Surface(
        color = LexiaSurface.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        // LexIA Application Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Emblem Box
                    Box(
                        modifier = Modifier
                            .size(32.dp)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LexIA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = LexiaPrimary
                            )
                            // Premium Interactive AI Pill / Selector
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LexiaSuccess100,
                                border = BorderStroke(1.dp, LexiaSuccess600.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .clickable { showAiSelectorModal = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = currentAiModel.iconEmoji,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = currentAiModel.shortBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LexiaSuccess800,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Cambiar IA",
                                        tint = LexiaSuccess800,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = LexiaOnSurfaceVariant
                        )
                    }
                }

                // Profile Avatar Button (Foto de Google o Inicial/Ícono Institucional)
                val isProfileActive = subtitle == "Autenticación & Licencia"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isProfileActive) LexiaPrimary else LexiaPrimaryContainer.copy(alpha = 0.15f))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (userAvatarBitmap != null) {
                        Image(
                            bitmap = userAvatarBitmap!!,
                            contentDescription = "Foto de perfil de Google",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else if (currentUser != null && currentUser!!.displayName.isNotBlank()) {
                        Text(
                            text = currentUser!!.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfileActive) LexiaOnPrimary else LexiaPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil de Usuario",
                            tint = if (isProfileActive) LexiaOnPrimary else LexiaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

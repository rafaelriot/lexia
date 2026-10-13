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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.ui.theme.*
import android.widget.Toast

@Composable
fun LexiaTopBar(
    subtitle: String,
    onProfileClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.lexia.legalassistant.data.repository.AiModelRepository.init(context)
    }

    val currentAiModel by com.lexia.legalassistant.data.repository.AiModelRepository.currentModel.collectAsState()
    var showAiSelectorModal by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Simulated Android System Status Bar (09:41, 5G, Wi-Fi, battery)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "09:41",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LexiaOnSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal",
                        modifier = Modifier.size(14.dp),
                        tint = LexiaOnSurfaceVariant
                    )
                    Text(
                        text = "5G",
                        style = MaterialTheme.typography.labelSmall,
                        color = LexiaOnSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "WiFi",
                        modifier = Modifier.size(14.dp),
                        tint = LexiaOnSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        modifier = Modifier.size(16.dp),
                        tint = LexiaOnSurfaceVariant
                    )
                }
            }

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

                // Profile Avatar Button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(LexiaSecondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = LexiaPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

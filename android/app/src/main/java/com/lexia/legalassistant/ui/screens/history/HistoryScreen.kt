package com.lexia.legalassistant.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.data.repository.ConsultationHistoryRepository
import com.lexia.legalassistant.ui.theme.*

@Composable
fun HistoryScreen(
    onSelectSession: (lawId: String, articleId: String?, groupName: String?) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ConsultationHistoryRepository.init(context)
        com.lexia.legalassistant.data.repository.AuthRepository.init(context)
    }

    val currentUser by com.lexia.legalassistant.data.repository.AuthRepository.currentUser.collectAsState()
    val activeUserId = currentUser?.id ?: "guest"
    val allSessions = ConsultationHistoryRepository.sessions
    val userSessions = remember(allSessions.toList(), activeUserId) {
        allSessions.filter { it.userId == activeUserId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
            .padding(16.dp)
    ) {
        // AI Memory Header Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LexiaPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = LexiaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Memoria de Consultas (${currentUser?.displayName ?: "Invitado"})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaOnSurface
                    )
                    Text(
                        text = "Tus preguntas por texto y notas de voz quedan registradas dinámicamente para tu cuenta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaOnSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial de Consultas (${userSessions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LexiaPrimary
            )

            if (userSessions.isNotEmpty()) {
                TextButton(
                    onClick = { ConsultationHistoryRepository.clearForUser(context, activeUserId) }
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = LexiaError)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar", style = MaterialTheme.typography.labelSmall, color = LexiaError)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (userSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = LexiaSecondary
                    )
                    Text(
                        text = "Aún no tienes consultas guardadas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Text(
                        text = "Realiza preguntas por texto o notas de voz en el chat de LexIA para verlas aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaOnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                items(userSessions, key = { it.id }) { session ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSession(session.lawId, session.articleId, session.groupName)
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = LexiaSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = session.id.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaSecondary
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (session.isVoice) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = LexiaPrimaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(11.dp), tint = LexiaPrimary)
                                                Text(
                                                    text = "Nota de voz",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = LexiaPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = LexiaSurfaceContainerHighest
                                    ) {
                                        Text(
                                            text = session.lawTag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LexiaPrimary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { ConsultationHistoryRepository.deleteSession(context, session.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = LexiaOutline, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaPrimary
                            )

                            if (session.lastQueryText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "«${session.lastQueryText}»",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = LexiaOnSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            if (session.aiAnswerExcerpt.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = LexiaSurfaceContainerLow,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = session.aiAnswerExcerpt,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = LexiaOnSurface,
                                        maxLines = 2,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = session.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSecondary
                                )
                                Text(
                                    text = "${session.queriesCount} consultas · ${session.chunksCount} chunks FAISS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

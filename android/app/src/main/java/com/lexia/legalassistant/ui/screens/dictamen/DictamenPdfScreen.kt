package com.lexia.legalassistant.ui.screens.dictamen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import com.lexia.legalassistant.ui.theme.*

@Composable
fun DictamenPdfScreen(
    lawId: String = "cpeum",
    articleId: String? = null,
    groupName: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val dictamenData = remember(lawId, articleId, groupName) {
        LegalArticlesRepository.buildDictamen(articleId, groupName, lawId)
    }

    val handleShare: () -> Unit = {
        try {
            val pdfFile = PdfGeneratorHelper.generateDictamenPdf(context, dictamenData)
            PdfGeneratorHelper.sharePdf(context, pdfFile, dictamenData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val handleDownload: () -> Unit = {
        try {
            val pdfFile = PdfGeneratorHelper.generateDictamenPdf(context, dictamenData)
            PdfGeneratorHelper.downloadPdfToPublicFolder(context, pdfFile, dictamenData.fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurfaceContainer)
    ) {
        // App bar for Document Viewer
        Surface(
            color = LexiaSurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = LexiaPrimary)
                    }
                    Column {
                        Text(
                            text = dictamenData.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = dictamenData.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }
                }

                Row {
                    IconButton(onClick = handleShare) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = LexiaPrimary)
                    }
                    IconButton(onClick = handleDownload) {
                        Icon(Icons.Default.Download, contentDescription = "Descargar", tint = LexiaPrimary)
                    }
                }
            }
        }

        // PDF Canvas Viewer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            // Paper simulation sheet
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LexiaOutlineVariant, RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Formal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dictamenData.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = LexiaPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = dictamenData.expediente,
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = LexiaSuccess100
                        ) {
                            Text(
                                text = dictamenData.dofStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaSuccess800,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = LexiaPrimaryContainer,
                        thickness = 2.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Case summary
                    Text(
                        text = dictamenData.materiaConsultadaTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dictamenData.materiaConsultadaDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaOnSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = dictamenData.fundamentacionNormativaTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = LexiaSurfaceContainerLow,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dictamenData.fundamentacionNormativaText,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Serif,
                            color = LexiaOnSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section III: Supuestos
                    Text(
                        text = dictamenData.supuestosTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        dictamenData.supuestosList.forEach { point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(LexiaPrimary)
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LexiaOnSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Forensic Verification QR Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LexiaOutlineVariant, RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VERIFICACIÓN FORENSE DIGITAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaPrimary
                            )
                            Text(
                                text = "${dictamenData.hashSha256}\nModelo: LexIA-RAG v3.4 DistilBERT Legal-ES\nCotejo normativo SCJN / DOF: ${dictamenData.reformDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary,
                                fontSize = 9.sp
                            )
                        }

                        // Simulated QR graphic
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(1.dp, LexiaPrimary, RoundedCornerShape(4.dp))
                                .background(LexiaSurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Code",
                                tint = LexiaPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Bar
        Surface(
            color = LexiaSurfaceContainerLowest,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = handleShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LexiaPrimary
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compartir", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = handleDownload,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LexiaPrimary,
                        contentColor = LexiaOnPrimary
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Descargar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

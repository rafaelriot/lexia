package com.lexia.legalassistant.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.data.model.LawArticleDetail
import com.lexia.legalassistant.data.repository.LegalArticlesRepository
import com.lexia.legalassistant.ui.theme.*

@Composable
fun ArticleDetailScreen(
    initialLawId: String = "cpeum",
    initialArticleId: String = "cpeum_16",
    onNavigateBack: () -> Unit = {},
    onGenerateDictamen: (LawArticleDetail) -> Unit = {}
) {
    var selectedLawId by remember { mutableStateOf(initialLawId) }
    var selectedArticleId by remember { mutableStateOf(initialArticleId) }
    var articleSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }

    val lawOptions = listOf(
        Pair("cpeum", "CPEUM (136 Artículos)"),
        Pair("cnpp", "CNPP (Proc. Penales)"),
        Pair("amparo", "Ley de Amparo")
    )

    val cpeumCategories = listOf(
        "Todos",
        "DDHH (1-29)",
        "Nacionalidad (30-38)",
        "Soberanía (39-48)",
        "Poderes (49-107)",
        "Responsabilidades (108-114)",
        "Estados (115-122)",
        "Trabajo (123)",
        "General (124-136)"
    )

    val currentLawArticles = remember(selectedLawId) {
        LegalArticlesRepository.getArticlesForLaw(selectedLawId)
    }

    // Auto-select first article if current selection isn't in this law
    LaunchedEffect(selectedLawId) {
        if (currentLawArticles.none { it.id == selectedArticleId }) {
            selectedArticleId = currentLawArticles.firstOrNull()?.id ?: "cpeum_16"
        }
    }

    val filteredArticles = remember(currentLawArticles, articleSearchQuery, selectedCategoryFilter) {
        var list = currentLawArticles
        if (selectedLawId == "cpeum" && selectedCategoryFilter != "Todos") {
            list = when (selectedCategoryFilter) {
                "DDHH (1-29)" -> list.filter { it.category == "Garantías y Derechos Humanos" }
                "Nacionalidad (30-38)" -> list.filter { it.category == "Nacionalidad y Ciudadanía" }
                "Soberanía (39-48)" -> list.filter { it.category == "Soberanía y Territorio" }
                "Poderes (49-107)" -> list.filter { it.category in listOf("División de Poderes", "Poder Legislativo", "Poder Ejecutivo", "Poder Judicial y Amparo") }
                "Responsabilidades (108-114)" -> list.filter { it.category == "Responsabilidades Públicas" }
                "Estados (115-122)" -> list.filter { it.category == "Estados y Municipios" }
                "Trabajo (123)" -> list.filter { it.category == "Laboral y Previsión Social" }
                "General (124-136)" -> list.filter { it.category in listOf("Prevenciones Generales", "Reformas Constitucionales", "Inviolabilidad Constitucional") }
                else -> list
            }
        }
        if (articleSearchQuery.isNotBlank()) {
            val q = articleSearchQuery.trim().lowercase()
            list = list.filter {
                it.articleNumber.lowercase().contains(q) ||
                it.topicTitle.lowercase().contains(q) ||
                it.summary.lowercase().contains(q)
            }
        }
        list
    }

    val selectedArticle = remember(selectedArticleId) {
        LegalArticlesRepository.getArticleById(selectedArticleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
    ) {
        // Top App Bar
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = LexiaPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "${selectedArticle.lawShortName} · ${selectedArticle.articleNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = "Inspector FAISS & Embeddings DistilBERT Legal",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }
                }

                Button(
                    onClick = { onGenerateDictamen(selectedArticle) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LexiaPrimary,
                        contentColor = LexiaOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dictamen", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Law Switcher & Filter Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "CUERPO NORMATIVO SELECCIONADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LexiaSecondary
                    )

                    // Law Selector Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(lawOptions) { (id, name) ->
                            val isLawSelected = selectedLawId == id
                            FilterChip(
                                selected = isLawSelected,
                                onClick = { selectedLawId = id },
                                label = { Text(name, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LexiaPrimary,
                                    selectedLabelColor = LexiaOnPrimary,
                                    containerColor = LexiaSurfaceContainerLowest,
                                    labelColor = LexiaOnSurface
                                )
                            )
                        }
                    }

                    // Search input to filter articles
                    OutlinedTextField(
                        value = articleSearchQuery,
                        onValueChange = { articleSearchQuery = it },
                        placeholder = {
                            Text(
                                "Filtrar artículos (ej: Art. 1, 14, 16, 19, 20...)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = LexiaSecondary, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LexiaSurfaceContainerLowest,
                            unfocusedContainerColor = LexiaSurfaceContainerLowest,
                            focusedBorderColor = LexiaPrimary,
                            unfocusedBorderColor = LexiaOutlineVariant
                        )
                    )

                    // Constitutional Chapter Category Filter (for CPEUM)
                    if (selectedLawId == "cpeum") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(cpeumCategories) { cat ->
                                val isCatSelected = selectedCategoryFilter == cat
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { selectedCategoryFilter = cat },
                                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
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
                }
            }

            // Article Carousel / Navigation Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Índice Constitucional (${filteredArticles.size} Artículos)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Text(
                            text = "Toca un artículo para explorarlo",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredArticles) { art ->
                            val isSelected = art.id == selectedArticle.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) LexiaPrimaryContainer else LexiaSurfaceContainerLowest,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) LexiaPrimary else LexiaOutlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { selectedArticleId = art.id }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = art.articleNumber,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) LexiaOnPrimaryContainer else LexiaPrimary
                                    )
                                    Text(
                                        text = art.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) LexiaPrimary else LexiaSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Grounding Info Card for Current Article
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
                                    text = selectedArticle.topicTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaOnSurface
                                )
                                Text(
                                    text = "Materia: ${selectedArticle.category} · DOF Reforma: ${selectedArticle.lastReformDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSecondary
                                )
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = LexiaSuccess100) {
                                Text(
                                    text = selectedArticle.dofStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSuccess800,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Article Summary Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LexiaSurfaceContainerLowest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedArticle.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = LexiaOnSurfaceVariant,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Indexado con ${selectedArticle.totalChunks} chunks semánticos (768 dim)",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary
                            )
                            Text(
                                text = "Base Vectorial FAISS",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Chunks Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chunks Recuperados por FAISS (${selectedArticle.chunks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Surface(shape = RoundedCornerShape(12.dp), color = LexiaAi100) {
                        Text(
                            text = "Top Match RAG",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaAi800,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // List of Chunks for Selected Article
            items(selectedArticle.chunks) { chunk ->
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
                                .padding(12.dp)
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
        }
    }
}

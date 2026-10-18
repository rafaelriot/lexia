package com.lexia.legalassistant.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.data.model.LawItem
import com.lexia.legalassistant.ui.theme.*

@Composable
fun CatalogScreen(
    onSelectLaw: (LawItem) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val lawsList = com.lexia.legalassistant.data.repository.LegalArticlesRepository.catalogLaws

    // Obtener dinámicamente las categorías reales de los libros del catálogo más opciones complementarias
    val categories = remember(lawsList) {
        val existingCategories = lawsList.map { it.category }.distinct()
        listOf("Todos") + existingCategories + listOf("Civil", "Familiar", "Laboral").filter { it !in existingCategories }
    }

    val filteredLaws = remember(searchQuery, selectedCategory, lawsList) {
        lawsList.filter { law ->
            val matchesCategory = when (selectedCategory) {
                "Todos" -> true
                "Civil" -> law.category.equals("Civil", ignoreCase = true) || law.title.contains("Civil", ignoreCase = true)
                "Familiar" -> law.category.equals("Familiar", ignoreCase = true) || law.title.contains("Familia", ignoreCase = true) || law.title.contains("Familiar", ignoreCase = true)
                else -> law.category.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    law.title.contains(searchQuery, ignoreCase = true) ||
                    law.shortName.contains(searchQuery, ignoreCase = true) ||
                    law.category.contains(searchQuery, ignoreCase = true) ||
                    law.jurisdiction.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search OutlinedTextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar códigos, leyes o materias...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = LexiaOutline) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = LexiaSecondary)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LexiaSurfaceContainerLowest,
                unfocusedContainerColor = LexiaSurfaceContainerLowest,
                focusedBorderColor = LexiaPrimary,
                unfocusedBorderColor = LexiaOutlineVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Header de Tipos de Leyes con badge de total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(16.dp))
                Text(
                    text = "Materia Jurídica",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LexiaPrimary
                )
            }

            Text(
                text = "${filteredLaws.size} de ${lawsList.size} libros",
                style = MaterialTheme.typography.labelSmall,
                color = LexiaSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row con íconos por especialidad
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                val count = if (cat == "Todos") {
                    lawsList.size
                } else {
                    lawsList.count { it.category.equals(cat, ignoreCase = true) }
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    leadingIcon = {
                        val icon = when (cat) {
                            "Penal" -> Icons.Default.Gavel
                            "Constitucional" -> Icons.Default.Balance
                            "Amparo" -> Icons.Default.Shield
                            "Civil" -> Icons.Default.MenuBook
                            "Familiar" -> Icons.Default.FamilyRestroom
                            "Laboral" -> Icons.Default.Work
                            else -> Icons.Default.Category
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = {
                        Text(
                            text = if (count > 0) "$cat ($count)" else cat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LexiaPrimaryContainer,
                        selectedLabelColor = LexiaOnPrimaryContainer,
                        selectedLeadingIconColor = LexiaOnPrimaryContainer,
                        containerColor = LexiaSurfaceContainerLowest,
                        labelColor = LexiaOnSurface,
                        iconColor = LexiaSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredLaws.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = LexiaOutline,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No hay libros en la materia «$selectedCategory»",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LexiaPrimary
                    )
                    Text(
                        text = "Prueba seleccionando «Todos» o «Penal» para ver los códigos vigentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaSecondary
                    )
                    Button(
                        onClick = {
                            selectedCategory = "Todos"
                            searchQuery = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LexiaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver Todos los Libros")
                    }
                }
            }
        } else {
            // Statutory Laws List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            items(filteredLaws) { law ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LexiaSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectLaw(law) }
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
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LexiaPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = LexiaOnPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = law.shortName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LexiaPrimary
                                )
                            }

                            // Vigente Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LexiaSuccess100
                            ) {
                                Text(
                                    text = law.dofStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaSuccess800,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = law.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LexiaOnSurface,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${law.articlesCount} Artículos · Jurisdicción ${law.jurisdiction}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LexiaSecondary
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = LexiaSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

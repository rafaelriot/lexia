package com.lexia.legalassistant.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.data.ai.LlmGateway
import com.lexia.legalassistant.data.model.AiModel
import com.lexia.legalassistant.data.model.LlmProvider
import com.lexia.legalassistant.data.repository.AiModelRepository
import com.lexia.legalassistant.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Settings card to configure one API key per provider, override the remote model id of the
 * selected model and run a live "Probar conexión" check against each provider.
 */
@Composable
fun AiProviderSettingsCard(currentModel: AiModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    val keyInputs = remember {
        mutableStateMapOf<LlmProvider, String>().apply {
            AiModelRepository.remoteProviders.forEach { put(it, AiModelRepository.getApiKey(it).orEmpty()) }
        }
    }
    val testResults = remember { mutableStateMapOf<LlmProvider, Pair<Boolean, String>>() }
    val testing = remember { mutableStateListOf<LlmProvider>() }
    var remoteModelInput by remember(currentModel.id) { mutableStateOf(AiModelRepository.getRemoteModelId(currentModel)) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = LexiaSurfaceContainerLow,
        border = BorderStroke(1.dp, LexiaOutlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = LexiaPrimary, modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            text = "Conexión con APIs de IA",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        val configured = AiModelRepository.remoteProviders.count { keyInputs[it].orEmpty().isNotBlank() }
                        Text(
                            text = "$configured de ${AiModelRepository.remoteProviders.size} proveedores con API Key",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = LexiaSecondary
                )
            }

            if (!expanded) return@Column

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cada modelo envía la pregunta a la API de su proveedor. Sin API Key se muestra una respuesta local marcada como \"sin IA\". Las claves se guardan sólo en este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = LexiaOnSurfaceVariant,
                lineHeight = 15.sp
            )

            // Remote model id for the selected model
            if (currentModel.provider != LlmProvider.LOCAL) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Modelo remoto para ${currentModel.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LexiaOnSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = remoteModelInput,
                        onValueChange = { remoteModelInput = it },
                        label = { Text("ID del modelo (${currentModel.defaultRemoteModelId})") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        AiModelRepository.setRemoteModelId(currentModel, remoteModelInput)
                        remoteModelInput = AiModelRepository.getRemoteModelId(currentModel)
                        Toast.makeText(context, "Modelo remoto: $remoteModelInput", Toast.LENGTH_SHORT).show()
                    }) { Text("Guardar") }
                }
            }

            AiModelRepository.remoteProviders.forEach { provider ->
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LexiaOutlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                val input = keyInputs[provider].orEmpty()
                val isActive = currentModel.provider == provider
                Text(
                    text = provider.displayName + if (isActive) "  · usado por el modelo activo" else "",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) LexiaPrimary else LexiaOnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { keyInputs[provider] = it },
                    label = { Text("API Key (${provider.apiKeyHint})") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            AiModelRepository.setApiKey(provider, input)
                            Toast.makeText(
                                context,
                                if (input.isBlank()) "API Key de ${provider.displayName} eliminada" else "API Key de ${provider.displayName} guardada",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar") }

                    OutlinedButton(
                        enabled = input.isNotBlank() && provider !in testing,
                        onClick = {
                            val model = AiModelRepository.representativeModel(provider) ?: return@OutlinedButton
                            val remoteId = if (model.id == currentModel.id) remoteModelInput.ifBlank { model.defaultRemoteModelId }
                                else AiModelRepository.getRemoteModelId(model)
                            testing.add(provider)
                            testResults.remove(provider)
                            scope.launch {
                                val result = LlmGateway.ping(provider, input, remoteId)
                                val t = result.trace
                                testResults[provider] = if (result.isSuccess) {
                                    true to "✅ HTTP ${t.httpStatus} · ${t.responseModel ?: t.requestedModel} · ${t.latencyMs} ms · respuesta: \"${result.text?.take(40)}\""
                                } else {
                                    false to "❌ ${t.httpStatus?.let { "HTTP $it · " } ?: ""}${t.requestedModel} · ${t.errorMessage}"
                                }
                                testing.remove(provider)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (provider in testing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = LexiaPrimary)
                        } else {
                            Text("Probar conexión")
                        }
                    }
                }
                testResults[provider]?.let { (ok, msg) ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (ok) LexiaSuccess700 else LexiaError,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

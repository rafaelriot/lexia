package com.lexia.legalassistant.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lexia.legalassistant.data.model.AiTrace
import com.lexia.legalassistant.data.model.LlmProvider
import com.lexia.legalassistant.ui.theme.*

/**
 * Verifiable origin badge shown under every assistant answer.
 * Tap to copy the full trace (provider, model, HTTP status, request id, tokens).
 */
@Composable
fun AiTraceChip(trace: AiTrace, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val isLocalEngine = trace.provider == LlmProvider.LOCAL
    val container = when {
        trace.isLive -> LexiaSuccess50
        isLocalEngine -> LexiaSurfaceContainerHigh
        else -> LexiaErrorContainer
    }
    val content = when {
        trace.isLive -> LexiaSuccess800
        isLocalEngine -> LexiaOnSurfaceVariant
        else -> LexiaOnErrorContainer
    }
    val icon = when {
        trace.isLive -> Icons.Default.CloudDone
        isLocalEngine -> Icons.Default.Lock
        else -> Icons.Default.CloudOff
    }
    val title = when {
        trace.isLive -> "IA en vivo · ${trace.provider.displayName} · ${trace.responseModel ?: trace.requestedModel}"
        isLocalEngine -> "Motor local · la consulta no se envió a ninguna IA externa"
        else -> "Respuesta local (sin IA) · ${trace.provider.displayName} · ${trace.requestedModel}"
    }
    val details = buildList {
        trace.httpStatus?.let { add("HTTP $it") }
        trace.latencyMs?.let { add(String.format(java.util.Locale.US, "%.2f s", it / 1000.0)) }
        if (trace.inputTokens != null || trace.outputTokens != null) {
            add("tokens ${trace.inputTokens ?: "?"}→${trace.outputTokens ?: "?"}")
        }
        trace.requestId?.let { add("req ${it.take(28)}") }
    }.joinToString(" · ")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = container,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                clipboard.setText(AnnotatedString(trace.toLogLine()))
                Toast.makeText(context, "Traza de IA copiada", Toast.LENGTH_SHORT).show()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
                if (details.isNotBlank()) {
                    Text(text = details, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = content)
                }
                trace.errorMessage?.let {
                    Text(text = "Motivo: $it", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = content)
                }
            }
        }
    }
}

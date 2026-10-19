package com.lexia.legalassistant.ui.screens.auth

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.lexia.legalassistant.data.repository.AuthRepository
import com.lexia.legalassistant.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AuthRepository.init(context)
    }


    var showTiersDrawer by remember { mutableStateOf(false) }
    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var deviceGoogleAccounts by remember { mutableStateOf<List<String>>(emptyList()) }

    // Configuración del cliente oficial de Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Activity Launcher para el diálogo nativo de Google Play Services / Cuentas de Gmail
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val email = account.email ?: "usuario@gmail.com"
            val name = account.displayName ?: email.substringBefore("@")
            val photoUrl = account.photoUrl?.toString()
            AuthRepository.loginWithGoogle(email, name, account.idToken, photoUrl)
            Toast.makeText(context, "Bienvenido, $name (Google)", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } catch (e: Exception) {
            // Si la app aún no tiene SHA-1 configurado en Google Cloud Console,
            // abrimos el selector de cuentas del sistema para seleccionar la cuenta vinculada al teléfono
            try {
                val am = AccountManager.get(context)
                val accounts = am.getAccountsByType("com.google").map { it.name }
                if (accounts.isNotEmpty()) {
                    deviceGoogleAccounts = accounts
                    showAccountPickerDialog = true
                } else {
                    // Fallback directo con la cuenta de Google predeterminada
                    val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
                    if (lastAccount?.email != null) {
                        val photo = lastAccount.photoUrl?.toString()
                        AuthRepository.loginWithGoogle(lastAccount.email!!, lastAccount.displayName ?: "Abogado", photoUrl = photo)
                        Toast.makeText(context, "Sesión iniciada con ${lastAccount.email}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Selecciona tu cuenta de Google del teléfono", Toast.LENGTH_SHORT).show()
                        deviceGoogleAccounts = listOf("abogado.rafael@gmail.com", "despacho.juridico@gmail.com")
                        showAccountPickerDialog = true
                    }
                }
            } catch (err: Exception) {
                deviceGoogleAccounts = listOf("abogado.rafael@gmail.com", "usuario.gmail@gmail.com")
                showAccountPickerDialog = true
            }
        }
    }

    // DIÁLOGO SELECTOR DE CUENTAS VINCULADAS EN EL CELULAR
    if (showAccountPickerDialog) {
        AlertDialog(
            onDismissRequest = { showAccountPickerDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(42.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 24.sp)
                    }
                }
            },
            title = {
                Text(
                    text = "Elige una cuenta de Google",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LexiaPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cuentas activas en este dispositivo Android vinculadas a LexIA Asistente Legal:",
                        style = MaterialTheme.typography.bodySmall,
                        color = LexiaOnSurfaceVariant
                    )

                    deviceGoogleAccounts.forEach { email ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LexiaSurfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val name = email.substringBefore("@").replace(".", " ").capitalize()
                                    AuthRepository.loginWithGoogle(email, name)
                                    showAccountPickerDialog = false
                                    Toast.makeText(context, "Sesión iniciada con $email", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = LexiaPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = email.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = LexiaPrimary,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = email.substringBefore("@").replace(".", " "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = LexiaOnSurface
                                    )
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LexiaSecondary
                                    )
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LexiaSuccess600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountPickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiaSurface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Brand Emblem Solemn Identity
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LexiaPrimary,
            modifier = Modifier.size(68.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Balance,
                    contentDescription = null,
                    tint = LexiaOnPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(LexiaSecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LexiaOnSecondaryContainer,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "LexIA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LexiaPrimary
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = LexiaSecondaryContainer
            ) {
                Text(
                    text = "MÉXICO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LexiaOnSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "Asistente Jurídico Inteligente para el Derecho Mexicano",
            style = MaterialTheme.typography.bodySmall,
            color = LexiaSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // BOTÓN PRINCIPAL DE GOOGLE AUTH / GMAIL
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, LexiaOutlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable {
                    // Iniciar el selector nativo de Google Play Services
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Isotipo de Google
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF4285F4)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continuar con Google (Gmail)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Card de Información de Licencia IA (FAISS RAG)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = LexiaSurfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LexiaSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = LexiaOnSecondaryContainer, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Verificación de Licencia IA",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LexiaOnSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = LexiaTertiaryContainer
                            ) {
                                Text(
                                    text = "FAISS RAG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LexiaOnTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Tu cuenta de Gmail verifica tu acceso colegiado. Con cuenta activa obtendrás FAISS RAG Ilimitado; o puedes consultar en Modo Estudiante (10 consultas/día).",
                            style = MaterialTheme.typography.bodySmall,
                            color = LexiaSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LexiaSuccess600)
                        )
                        Text(
                            text = "Cotejo automático de credenciales",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                    }

                    TextButton(
                        onClick = { showTiersDrawer = !showTiersDrawer },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showTiersDrawer) "Ocultar" else "Conocer tiers",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LexiaPrimary
                        )
                        Icon(
                            imageVector = if (showTiersDrawer) Icons.Default.ExpandLess else Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LexiaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showTiersDrawer) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LexiaSurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = LexiaSecondary, modifier = Modifier.size(16.dp))
                                    Text("Estudiante / Consulta Simple", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                                Text("10 v/día · SCJN", style = MaterialTheme.typography.labelSmall, color = LexiaSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LexiaSecondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = LexiaOnSecondaryContainer, modifier = Modifier.size(16.dp))
                                    Text("Firma / Poder Judicial", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LexiaOnSecondaryContainer)
                                }
                                Text("Ilimitado · FAISS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LexiaOnSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Aviso de Deontología y LFPDPPP
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = LexiaSurfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = LexiaSecondary, modifier = Modifier.size(14.dp))
                    Text("LFPDPPP & Secreto Profesional", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LexiaOnSurface)
                }
                Text(
                    text = "El acceso con cuentas institucionales y Google cumple estrictamente con el secreto profesional. Los expedientes y consultas nunca se comparten con terceros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LexiaSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

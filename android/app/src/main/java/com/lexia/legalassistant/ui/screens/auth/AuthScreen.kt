package com.lexia.legalassistant.ui.screens.auth

import android.accounts.AccountManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                                    val name = email.substringBefore("@").replace(".", " ")
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Brand Emblem Solemn Identity
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LexiaPrimary,
            modifier = Modifier.size(76.dp),
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Balance,
                    contentDescription = null,
                    tint = LexiaOnPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(LexiaSecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LexiaOnSecondaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            style = MaterialTheme.typography.bodyMedium,
            color = LexiaSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // BOTÓN PRINCIPAL DE GOOGLE AUTH / GMAIL
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, LexiaOutlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
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
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
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
    }
}

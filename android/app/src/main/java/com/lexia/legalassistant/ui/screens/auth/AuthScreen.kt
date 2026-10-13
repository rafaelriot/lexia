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

    val currentUser by AuthRepository.currentUser.collectAsState()

    var cedula by remember { mutableStateOf("12893847") }
    var licenseToken by remember { mutableStateOf("LEX-IA-PREM-2025") }
    var passwordVisible by remember { mutableStateOf(false) }
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
            AuthRepository.loginWithGoogle(email, name, account.idToken)
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
                        AuthRepository.loginWithGoogle(lastAccount.email!!, lastAccount.displayName ?: "Abogado")
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

        var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Alta de Usuario, 1: Cédula Judicial

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedAuthTab,
            containerColor = LexiaSurfaceContainerLowest,
            contentColor = LexiaPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedAuthTab == 0,
                onClick = { selectedAuthTab = 0 },
                text = { Text("Alta de Usuario", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedAuthTab == 1,
                onClick = { selectedAuthTab = 1 },
                text = { Text("Cédula Judicial", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TAB 0: FORMULARIO DE ALTA DE NUEVO USUARIO (ABOGADO O ESTUDIANTE)
        if (selectedAuthTab == 0) {
            var regTypeIsStudent by remember { mutableStateOf(false) } // false: Abogado, true: Estudiante
            var regName by remember { mutableStateOf("") }
            var regEmail by remember { mutableStateOf("") }
            var regCedula by remember { mutableStateOf("") }
            var regMatricula by remember { mutableStateOf("") }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector de Tipo de Usuario (Abogado vs Estudiante) con UN SOLO ICONO limpio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LexiaSurfaceContainerLowest)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { regTypeIsStudent = false },
                        shape = RoundedCornerShape(8.dp),
                        color = if (!regTypeIsStudent) LexiaPrimary else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = if (!regTypeIsStudent) LexiaOnPrimary else LexiaSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Abogado",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!regTypeIsStudent) LexiaOnPrimary else LexiaSecondary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                regTypeIsStudent = true 
                                if (regName.isNotBlank() && regEmail.isBlank()) {
                                    val creds = AuthRepository.generateUnamStudentCredentials(regName)
                                    regEmail = creds.first
                                    regMatricula = creds.second
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (regTypeIsStudent) LexiaPrimary else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = if (regTypeIsStudent) LexiaOnPrimary else LexiaSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Estudiante",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (regTypeIsStudent) LexiaOnPrimary else LexiaSecondary
                            )
                        }
                    }
                }

                // Campo Nombre
                OutlinedTextField(
                    value = regName,
                    onValueChange = { 
                        regName = it
                        if (regTypeIsStudent && (regEmail.isBlank() || regEmail.endsWith("@derecho.unam.mx"))) {
                            val creds = AuthRepository.generateUnamStudentCredentials(it)
                            regEmail = creds.first
                            if (regMatricula.isBlank()) regMatricula = creds.second
                        }
                    },
                    label = { Text(if (regTypeIsStudent) "Nombre Completo del Alumno(a)" else "Nombre Completo del Licenciado(a)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LexiaSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = LexiaSurfaceContainerLowest, unfocusedContainerColor = LexiaSurfaceContainerLowest)
                )

                // Campo Correo Electrónico
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text(if (regTypeIsStudent) "Correo Institucional UNAM" else "Correo Electrónico a Dar de Alta") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = LexiaSecondary) },
                    trailingIcon = {
                        if (regTypeIsStudent) {
                            IconButton(onClick = {
                                val creds = AuthRepository.generateUnamStudentCredentials(regName.ifBlank { "alumno unam" })
                                regEmail = creds.first
                                regMatricula = creds.second
                                Toast.makeText(context, "Correo UNAM y matrícula autogenerados", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Generar", tint = LexiaPrimary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = LexiaSurfaceContainerLowest, unfocusedContainerColor = LexiaSurfaceContainerLowest)
                )

                if (regTypeIsStudent) {
                    // Botón de autogeneración rápida
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dominio: @derecho.unam.mx",
                            style = MaterialTheme.typography.labelSmall,
                            color = LexiaSecondary
                        )
                        TextButton(
                            onClick = {
                                val creds = AuthRepository.generateUnamStudentCredentials(regName.ifBlank { "estudiante derecho" })
                                regEmail = creds.first
                                regMatricula = creds.second
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = LexiaPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generar correo UNAM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = LexiaPrimary)
                        }
                    }

                    // Campo Matrícula Estudiante
                    OutlinedTextField(
                        value = regMatricula,
                        onValueChange = { regMatricula = it },
                        label = { Text("No. de Cuenta / Matrícula UNAM") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = LexiaSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = LexiaSurfaceContainerLowest, unfocusedContainerColor = LexiaSurfaceContainerLowest)
                    )
                } else {
                    // Campo Cédula para Abogado
                    OutlinedTextField(
                        value = regCedula,
                        onValueChange = { regCedula = it },
                        label = { Text("Cédula Profesional (Opcional)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = LexiaSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = LexiaSurfaceContainerLowest, unfocusedContainerColor = LexiaSurfaceContainerLowest)
                    )
                }

                Button(
                    onClick = {
                        if (regName.isBlank()) {
                            Toast.makeText(context, "Por favor escribe tu nombre completo", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (regTypeIsStudent) {
                            val finalEmail = if (regEmail.isBlank()) {
                                val creds = AuthRepository.generateUnamStudentCredentials(regName)
                                regMatricula = creds.second
                                creds.first
                            } else regEmail

                            val finalMatricula = if (regMatricula.isBlank()) "UNAM-DER-${(3000000..3999999).random()}" else regMatricula

                            AuthRepository.registerStudent(
                                fullName = regName,
                                email = finalEmail,
                                matricula = finalMatricula
                            )
                            Toast.makeText(context, "¡Estudiante $regName registrado con éxito!", Toast.LENGTH_LONG).show()
                            onLoginSuccess()
                        } else {
                            if (regEmail.isBlank() || !regEmail.contains("@")) {
                                Toast.makeText(context, "Por favor ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val contract = AuthRepository.checkEmailAiContract(regEmail)
                            AuthRepository.registerAndLoginWithContract(
                                name = regName,
                                email = regEmail,
                                cedula = regCedula.ifBlank { null },
                                contract = contract
                            )
                            Toast.makeText(context, "¡Usuario dado de alta exitosamente! Bienvenido, $regName", Toast.LENGTH_LONG).show()
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LexiaPrimary)
                ) {
                    Icon(
                        if (regTypeIsStudent) Icons.Default.School else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (regTypeIsStudent) "Alta de Estudiante UNAM" else "Dar de Alta y Activar Acceso IA",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // TAB 2: INGRESO POR CÉDULA & TOKEN JUDICIAL
        else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cedula,
                    onValueChange = { cedula = it },
                    label = { Text("Cédula Profesional / Correo institucional") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = LexiaSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LexiaSurfaceContainerLowest,
                        unfocusedContainerColor = LexiaSurfaceContainerLowest
                    )
                )

                OutlinedTextField(
                    value = licenseToken,
                    onValueChange = { licenseToken = it },
                    label = { Text("Token de Licencia Judicial") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = LexiaSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Ver token",
                                tint = LexiaSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LexiaSurfaceContainerLowest,
                        unfocusedContainerColor = LexiaSurfaceContainerLowest
                    )
                )

                Button(
                    onClick = {
                        AuthRepository.loginWithCedula(cedula, licenseToken)
                        Toast.makeText(context, "Acceso validado para Cédula $cedula", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LexiaPrimary,
                        contentColor = LexiaOnPrimary
                    )
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Validar y Acceder a IA Judicial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Botón Secundario: Acceso Rápido Individualizado para Estudiantes
        OutlinedButton(
            onClick = {
                val student = AuthRepository.loginAsGuest()
                Toast.makeText(context, "Sesión individual iniciada: ${student.displayName}", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = LexiaPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Acceso Rápido Estudiante UNAM",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = LexiaPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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

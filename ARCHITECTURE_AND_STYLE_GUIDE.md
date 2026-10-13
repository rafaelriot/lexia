# Guía de Arquitectura, Estilo, Tokens y Reglas de Desarrollo — LexIA Legal Assistant

Este documento define la **norma técnica y visual oficial** para la modificación, mantenimiento y creación de nuevas ventanas y componentes en **LexIA Legal Assistant**. Cualquier desarrollo futuro debe apegarse estrictamente a estos lineamientos para mantener la coherencia institucional, estética y arquitectónica del sistema.

---

## 1. Filosofía de Diseño: *Sovereign Legal Tech*
LexIA está diseñado como una herramienta de grado judicial y pericial para abogados, ministerios públicos, defensores y magistrados. Su estética transmite:
- **Autoridad Institucional**: Inspirado en los códigos federales, sellos de autenticidad y tipografías solemnes.
- **Rigor Forense**: Trazabilidad documental con hashes SHA-256, estatus DOF en tiempo real y citación estricta de párrafos.
- **Micro-interacciones Fluidas**: Retroalimentación háptica, animaciones de pulso en grabación de voz, transiciones suaves y tarjetas con franjas de acento.

---

## 2. Sistema de Colores y Tokens Oficiales (`Color.kt`)

Ubicación del archivo fuente: [`Color.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/ui/theme/Color.kt)

| Token | Valor Hex | Uso Oficial |
| :--- | :--- | :--- |
| **`LexiaPrimary`** | `#00236F` | Azul Soberano Institucional. Encabezados, botones primarios, barras de navegación activa y franjas de acento. |
| **`LexiaOnPrimary`** | `#FFFFFF` | Texto y elementos sobre fondos primarios. |
| **`LexiaPrimaryContainer`** | `#1E3A8A` | Contenedores profundos, iconos con fondo circular, banners de acento. |
| **`LexiaPrimaryFixed`** | `#DCE1FF` | Fondos de pills y badges informativos activos. |
| **`LexiaSecondary`** | `#4F5E83` | Azul Acero. Metadatos, subtítulos, horas, fechas e iconos secundarios. |
| **`LexiaTertiaryContainer`** | `#653400` | Oro Procesal / Ámbar Inteligente. Franja de citación dogmática SCJN. |
| **`LexiaOnTertiaryContainer`**| `#FC922B` | Texto y acentos sobre contenedores terciarios. |
| **`LexiaSuccess600`** | `#059669` | Verde DOF Vigente. Puntos de estado en línea, validación legal y cotejo aprobado. |
| **`LexiaSuccess100`** | `#D1FAE5` | Fondo suave para chips de "VIGENTE DOF" y "Oficial SCJN". |
| **`LexiaAi100`** | `#FEF3C7` | Fondo sutil para coincidencias vectoriales FAISS de alta similitud. |
| **`LexiaAi800`** | `#92400E` | Texto destacado en fragmentos con similitud > 0.95. |
| **`LexiaSurface`** | `#F8F9FF` | Fondo general de todas las pantallas. |
| **`LexiaSurfaceContainerLowest`** | `#FFFFFF` | Fondo de tarjetas principales elevadas, globos de mensajes y menús flotantes. |
| **`LexiaSurfaceContainerLow`** | `#EFF4FF` | Fondos de banners informativos, citas dogmáticas y campos de formulario inactivos. |
| **`LexiaOnSurface`** | `#0B1C30` | Color primario para texto de lectura, títulos de artículos y nombres. |
| **`LexiaOnSurfaceVariant`**| `#444651` | Texto descriptivo de resúmenes legales y párrafos secundarios. |
| **`LexiaOutline`** | `#757682` | Bordes sutiles de inputs y separadores visuales. |
| **`LexiaError`** | `#BA1A1A` | Grabación de micrófono en progreso, acciones de eliminación de historial. |

> [!CAUTION]
> **Prohibición de Colores Genéricos**: Nunca emplear `Color.Black`, `Color.Blue`, `Color.Red` ni colores hexadecimales quemados directamente en el código. Usar siempre los tokens del paquete `com.lexia.legalassistant.ui.theme.*`.

---

## 3. Tipografía y Jerarquía Textual

La tipografía debe respetar las escalas semánticas de Material 3:
- **Títulos de Pantallas**: `MaterialTheme.typography.titleLarge` o `titleMedium`, peso `FontWeight.Bold`, color `LexiaPrimary`.
- **Nombres de Artículos / Subtítulos**: `MaterialTheme.typography.titleSmall`, peso `FontWeight.SemiBold`, color `LexiaOnSurface`.
- **Citas Legales Textuales**: `MaterialTheme.typography.bodyMedium` o `bodySmall`, `fontStyle = FontStyle.Italic`, `lineHeight = 20.sp` o `22.sp`, entre comillas latinas («...»).
- **Metadatos (Fechas, Folios, Chunks)**: `MaterialTheme.typography.labelSmall`, color `LexiaSecondary`.

---

## 4. Estándar de Componentes de Interfaz

### A. Barra de Memoria Activa y Contexto
Toda pantalla de chat o análisis normativo debe exhibir un contenedor estructurado:
- Contenedor: `Card` con `shape = RoundedCornerShape(12.dp)`, `containerColor = LexiaSurfaceContainerLowest`, elevación `1.dp` y borde `1.dp` en `LexiaSurfaceContainerHigh`.
- Franja lateral de acento: `Box` de `width = 4.dp`, `fillMaxHeight()`, color `LexiaPrimary`.
- Icono de Inteligencia: Icono `Psychology` dentro de un círculo con `LexiaPrimaryContainer`.
- Pill de Alcance: `Surface` con `RoundedCornerShape(4.dp)` indicando si es `⚖️ CPEUM General (136)`, `🛡️ Capítulo DDHH` o `🏛️ Artículo Específico`.
- Botón de acción: `TextButton` o `AssistChip` para cambiar o configurar el alcance.

### B. Tarjetas de Cita Dogmática RAG (`RagCitationCard.kt`)
- Franja lateral izquierda en `LexiaTertiaryContainer` (5dp).
- Encabezado con icono `MenuBook`, título del artículo y píldora con puntaje de similitud (`Similitud: 0.985`).
- Texto legal íntegro entre comillas latinas («...»).
- Pie de tarjeta con fecha de reforma DOF y botón `Ver texto íntegro DOF`.

### C. Globos de Conversación (Chat Bubbles)
- **Mensajes del Usuario**: Alineados a la derecha, forma `RoundedCornerShape(16.dp)` con esquina inferior derecha en `4.dp`, fondo `LexiaPrimary`, texto `LexiaOnPrimary`. Si es nota de voz, incluir icono `Mic` y forma de onda simulada.
- **Mensajes de LexIA Assistant**: Alineados a la izquierda, forma `RoundedCornerShape(16.dp)` con esquina superior izquierda en `4.dp`, fondo `LexiaSurfaceContainerLowest`, elevación `1.5.dp`, nombre de emisor `LexIA Assistant` en negrita con icono de validación verde.

### D. Botones Institucionales
- Botones Primarios: `shape = RoundedCornerShape(8.dp)`, color `LexiaPrimary`, texto blanco en `FontWeight.Bold`.
- Botones Secundarios/Esquema: `OutlinedButton` con borde `1.5.dp` en `LexiaPrimary`.
- Botones de Barra de Dictamen/Cambiar: Altura `32.dp`, `PaddingValues(horizontal = 10.dp, vertical = 4.dp)`.

---

## 5. Arquitectura de Estado y Navegación

### A. Grafo de Navegación (`LexiaNavHost.kt`)
Ubicación: [`LexiaNavHost.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/navigation/LexiaNavHost.kt)

El grafo centraliza tres estados globales de alcance:
1. `currentLawId`: Cuerpo legal seleccionado (`"cpeum"`, `"cnpp"`, `"amparo"`).
2. `currentArticleId`: Identificador del artículo activo (ej. `"cpeum_30"`), o `null` si está en modo general.
3. `currentGroupName`: Capítulo temático activo (ej. `"DDHH (Arts. 1-29)"`), o `null`.

### B. Rutas Registradas (`Screen.kt`)
- `Screen.Catalog` (`"catalog"`): Directorio general de leyes y códigos.
- `Screen.Chat` (`"chat"`): Interfaz conversacional RAG, selector de chunks y visor de texto legal.
- `Screen.History` (`"history"`): Registro persistente de consultas y sesiones previas.
- `Screen.PdfViewer` (`"pdf_viewer"`): Visor y generador de dictamen técnico oficial con exportación a PDF.
- `Screen.Profile` (`"profile"`): Credenciales procesales y licencia IA.

---

## 6. Arquitectura de Datos y Repositorios

### A. Repositorio de Artículos Constitucionales (`LegalArticlesRepository.kt`)
Ubicación: [`LegalArticlesRepository.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/data/repository/LegalArticlesRepository.kt)
- `cpeumArticles`: Colección completa de los **136 artículos** de la Constitución Política de los Estados Unidos Mexicanos con título temático, categoría dogmática/orgánica, resumen oficial DOF y chunks vectoriales indexados.
- `findBestMatchingArticle(query)`: Motor de búsqueda semántica y léxica que identifica números de artículos (ej. Art. 30, Art. 16, Art. 123) o conceptos clave (ej. nacionalidad, amparo, salario, flagrancia).
- `queryConstitutionalRag(userQuery, scope, activeArticle, activeGroupName)`: Generador dinámico de dictamen y respuesta jurídica que:
  - Responde de forma adaptativa y específica a lo preguntado.
  - Genera respuestas **no fijas** con puntuaciones de similitud y citas legales exactas.
- `buildDictamen(articleId, groupName, lawId)`: Construye el objeto `DictamenData` para la generación del PDF dinámico sin quemar artículos fijos.

### B. Repositorio de Historial Persistente (`ConsultationHistoryRepository.kt`)
Ubicación: [`ConsultationHistoryRepository.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/data/repository/ConsultationHistoryRepository.kt)
- Almacena en `SharedPreferences` cada consulta realizada en el chat (por texto o por voz).
- Mantiene una lista reactiva `sessions` (`mutableStateListOf<ConsultationSession>`) que se sincroniza automáticamente con `HistoryScreen`.
- Permite abrir consultas previas, reanudar el análisis en el chat o eliminar entradas.

### C. Generador de Documentos PDF Oficiales (`PdfGeneratorHelper.kt`)
Ubicación: [`PdfGeneratorHelper.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/ui/screens/dictamen/PdfGeneratorHelper.kt)
- Renderiza el dictamen en Canvas vectorial A4 (`595 x 842 pt`) con auto-wrapping de párrafos.
- Dibuja el marco de seguridad institucional, caja de citación literal en azul marino y oro, viñetas de supuestos dogmáticos y bloque criptográfico SHA-256.
- Guarda el archivo con nomenclatura dinámica (ej. `Dictamen_Articulo_30_CPEUM.pdf`, `Dictamen_Constitucion_General_CPEUM.pdf`).
- Implementa `sharePdf` y `downloadPdfToPublicFolder` para descarga real en el dispositivo.

### D. Asistente de Audio y Voz (`AudioVoiceHelper.kt`)
Ubicación: [`AudioVoiceHelper.kt`](file:///c:/xampp/htdocs/stitch_lexia_legal_assistant/android/app/src/main/java/com/lexia/legalassistant/ui/screens/chat/AudioVoiceHelper.kt)
- Graba audio AAC con `MediaRecorder.AudioSource.VOICE_RECOGNITION` para evitar conflictos con el micrófono.
- Ejecuta `SpeechRecognizer` en el hilo principal (`Looper.getMainLooper()`) con soporte para idioma español (`es-MX`).
- Declarado en `AndroidManifest.xml` dentro del bloque `<queries>` para compatibilidad con Android 11+.

---

## 7. Reglas Inquebrantables para Nuevas Ventanas o Cambios

1. **Cero Textos Fijos en Respuestas de IA**: Toda consulta debe pasar por `LegalArticlesRepository.queryConstitutionalRag` para recuperar el artículo o tema relevante y redactar una respuesta particular.
2. **Respeto a la Selección de Alcance**: Si el usuario está en "Constitución en General", la respuesta debe abarcar toda la Carta Magna; si seleccionó un artículo (ej. Art. 30), el análisis debe enfocarse en dicho precepto.
3. **Validación de Notas de Voz**: Todo audio grabado debe mostrar la transcripción detectada y permitir confirmación o edición en la ventana emergente de alcance antes de disparar la consulta.
4. **Registro Automático en el Historial**: Cada consulta procesada debe registrarse en `ConsultationHistoryRepository.recordConsultation` para reflejarse en la pestaña de Historial.
5. **Generación Dinámica de Dictamen**: El botón de "Dictamen" o "Generar PDF" debe invocar `LegalArticlesRepository.buildDictamen(articleId, groupName, lawId)` usando el contexto activo, garantizando que el PDF refleje el artículo seleccionado y no el Artículo 16 por defecto.

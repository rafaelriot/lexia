---
description: Reglas de arquitectura, diseño soberano, estilo, tokens y repositorios de LexIA Legal Assistant
globs: **/*.kt, **/*.xml, **/*.md
---

# Reglas Oficiales de Arquitectura y Estilo LexIA

Al modificar o crear nuevas ventanas, componentes o lógica en LexIA Legal Assistant:

1. **Tokens de Color Obligatorios**:
   - Usar exclusivamente tokens de `com.lexia.legalassistant.ui.theme.*` (`LexiaPrimary`, `LexiaSecondary`, `LexiaTertiaryContainer`, `LexiaSuccess600`, `LexiaSurface`, etc.).
   - Nunca usar colores estándar de Compose (`Color.Black`, `Color.Blue`, etc.) ni códigos hexadecimales quemados.

2. **Respuestas de IA Dinámicas (Cero Respuestas Fijas)**:
   - Todas las preguntas al asistente deben procesarse mediante `LegalArticlesRepository.queryConstitutionalRag(...)`.
   - Si la consulta es sobre un artículo específico (ej. Art. 30, Art. 123, Art. 16), debe responder sobre ese precepto sin importar el modo de entrada.

3. **Dictamen Técnico Dinámico**:
   - La pantalla y generador de dictamen `DictamenPdfScreen` y `PdfGeneratorHelper` deben recibir el `DictamenData` generado por `LegalArticlesRepository.buildDictamen(articleId, groupName, lawId)`.
   - Queda prohibido hardcodear el Artículo 16 en el visor o descarga de PDF.

4. **Historial de Consultas Persistente**:
   - Cada consulta exitosa debe registrarse mediante `ConsultationHistoryRepository.recordConsultation(...)`.
   - `HistoryScreen` debe observar `ConsultationHistoryRepository.sessions` y permitir reanudar o eliminar sesiones.

5. **Notas de Voz**:
   - `AudioVoiceHelper` utiliza `MediaRecorder.AudioSource.VOICE_RECOGNITION` y ejecuta `SpeechRecognizer` en el hilo principal (`Looper.getMainLooper()`).
   - El diálogo de alcance debe permitir revisar y ajustar el texto de la nota de voz antes de enviarlo.

Para especificaciones completas de componentes y dimensiones, consultar `ARCHITECTURE_AND_STYLE_GUIDE.md`.

package com.lexia.legalassistant.ui.screens.chat

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.io.File
import java.util.Locale

class AudioVoiceHelper(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentOutputFile: File? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onFinalResultCallback: ((String, File?) -> Unit)? = null

    var lastTranscribedText: String = ""
        private set

    fun startRecording(
        onPartialResult: ((String) -> Unit)? = null,
        onFinalResult: ((String, File?) -> Unit)? = null
    ): Boolean {
        return try {
            lastTranscribedText = ""
            onFinalResultCallback = onFinalResult

            val cacheDir = context.cacheDir
            val audioFile = File(cacheDir, "lexia_voice_${System.currentTimeMillis()}.m4a")
            currentOutputFile = audioFile

            // Ejecutamos SpeechRecognizer en el hilo principal con acceso exclusivo al micrófono
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                startSpeechRecognition(onPartialResult)
            }

            true
        } catch (e: Exception) {
            Log.e("AudioVoiceHelper", "Error starting recording: ${e.message}", e)
            false
        }
    }

    private fun startSpeechRecognition(onPartialResult: ((String) -> Unit)?) {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                // Si ya existe un recognizer previo, destruirlo limpiamente
                try {
                    speechRecognizer?.destroy()
                } catch (ignored: Exception) {}

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            Log.d("AudioVoiceHelper", "SpeechRecognizer: Listo para capturar voz")
                        }
                        override fun onBeginningOfSpeech() {
                            Log.d("AudioVoiceHelper", "SpeechRecognizer: Inicio de voz detectado")
                        }
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            Log.d("AudioVoiceHelper", "SpeechRecognizer: Fin de habla detectado")
                        }
                        override fun onError(error: Int) {
                            val errorDesc = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio error (3)"
                                SpeechRecognizer.ERROR_CLIENT -> "Client error (5)"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions error (9)"
                                SpeechRecognizer.ERROR_NETWORK -> "Network error (2)"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout (1)"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No match (7)"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Busy (8)"
                                SpeechRecognizer.ERROR_SERVER -> "Server error (4)"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout (6)"
                                else -> "Code ($error)"
                            }
                            Log.w("AudioVoiceHelper", "SpeechRecognizer aviso: $errorDesc")
                            // Si termina por error o timeout, entregamos lo que tengamos acumulado
                            val recognized = lastTranscribedText.trim()
                            onFinalResultCallback?.invoke(recognized, currentOutputFile)
                            onFinalResultCallback = null
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                lastTranscribedText = matches[0].trim()
                                Log.d("AudioVoiceHelper", "SpeechRecognizer final: $lastTranscribedText")
                                onPartialResult?.invoke(lastTranscribedText)
                            }
                            val recognized = lastTranscribedText.trim()
                            onFinalResultCallback?.invoke(recognized, currentOutputFile)
                            onFinalResultCallback = null
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                lastTranscribedText = matches[0].trim()
                                Log.d("AudioVoiceHelper", "SpeechRecognizer parcial: $lastTranscribedText")
                                onPartialResult?.invoke(lastTranscribedText)
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-MX")
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra("android.speech.extra.GET_AUDIO", true)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
                }
                speechRecognizer?.startListening(intent)
            } else {
                Log.w("AudioVoiceHelper", "Speech recognition no disponible en este dispositivo")
            }
        } catch (e: Exception) {
            Log.w("AudioVoiceHelper", "Speech recognition exception: ${e.message}")
        }
    }

    fun stopRecording(onComplete: ((File?, String) -> Unit)? = null): Pair<File?, String> {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w("AudioVoiceHelper", "Error stopListening recognizer: ${e.message}")
        }

        // Si se provee onComplete, se invoca con el resultado final
        val text = lastTranscribedText.trim()
        val file = currentOutputFile
        if (onComplete != null) {
            // Breve espera en background o llamada directa si ya se tiene el texto
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val finalText = lastTranscribedText.trim().ifBlank { text }
                onComplete(file, finalText)
            }, 300)
        }
        return Pair(file, text)
    }

    fun playAudio(filePath: String?, onCompletion: () -> Unit) {
        stopPlayback()

        val file = filePath?.let { File(it) }
        if (file != null && file.exists() && file.length() > 0) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        stopPlayback()
                        onCompletion()
                    }
                    setOnErrorListener { _, _, _ ->
                        stopPlayback()
                        onCompletion()
                        true
                    }
                }
                return
            } catch (e: Exception) {
                Log.e("AudioVoiceHelper", "Error playing audio file: ${e.message}", e)
            }
        }

        // Fallback tone when no audio file exists (e.g. simulated chat message)
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
        } catch (e: Exception) {
            Log.e("AudioVoiceHelper", "Tone error: ${e.message}")
        }
        onCompletion()
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.w("AudioVoiceHelper", "Error stopping player: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    fun release() {
        stopRecording()
        stopPlayback()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w("AudioVoiceHelper", "Error destruyendo SpeechRecognizer: ${e.message}")
        } finally {
            speechRecognizer = null
        }
    }
}

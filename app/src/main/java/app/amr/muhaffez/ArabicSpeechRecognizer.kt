package app.amr.muhaffez

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ArabicSpeechRecognizer(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null
  private var isListening = false
  private var shouldContinueListening = false

  private val _voiceText = MutableLiveData<String>()
  val voiceText: LiveData<String> = _voiceText

  private val handler = Handler(Looper.getMainLooper())

  // Create the listener once
  private val recognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {
      println("recognizedText, Ready for speech")
    }

    override fun onBeginningOfSpeech() {
      println("recognizedText, Beginning of speech detected")
    }

    override fun onRmsChanged(rmsdB: Float) {
      // Audio level changed - can be used for visual feedback
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
      println("recognizedText, End of speech")
      isListening = false
      // Don't set shouldContinueListening to false - we want to continue
    }

    override fun onError(error: Int) {
      isListening = false
      val errorMessage = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown error: $error"
      }

      println("recognizedText, Recognition error: $errorMessage")

      // For certain errors, restart recognition automatically
      when (error) {
        SpeechRecognizer.ERROR_CLIENT -> {
          // Client error - this happens when we call stop while already stopped
          // Don't try to restart - just ignore it
          println("recognizedText, Client error - ignoring (already stopped)")
        }
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
          // These are normal - just restart
          println("recognizedText, Restarting recognition after normal timeout/no-match")
          handler.postDelayed({
            startRecognition()
          }, 300)
        }
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
        SpeechRecognizer.ERROR_SERVER,
        11 -> {
          // Recoverable errors - just restart (error 11 is often transient)
          println("recognizedText, Restarting recognition after recoverable error: $error")
          handler.postDelayed({
            startRecognition()
          }, 500)
        }
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
          // Network errors - stop and notify user
          shouldContinueListening = false
          println("recognizedText, $errorMessage - Please check your internet connection")
        }
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
          // Permission errors - stop and notify user
          shouldContinueListening = false
          println("recognizedText, $errorMessage")
        }
        else -> {
          // Unknown errors - try to recover once with longer delay
          println("recognizedText, Attempting to recover from error: $error")
          handler.postDelayed({
            if (shouldContinueListening) {
              startRecognition()
            }
          }, 500)
        }
      }
    }

    override fun onResults(results: Bundle?) {
      isListening = false
      results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
        if (it.isNotEmpty()) {
          _voiceText.postValue(it[0])
          println("recognizedText, onResults: ${it[0]}")
        }
      }

      // Restart recognition for continuous listening
      if (shouldContinueListening) {
        println("recognizedText, Restarting recognition for continuous listening")
        handler.postDelayed({
          startRecognition()
        }, 300)
      }
    }

    override fun onPartialResults(partialResults: Bundle?) {
      partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
        if (it.isNotEmpty()) {
          _voiceText.postValue(it[0])
          println("recognizedText, onPartialResults: ${it[0]}")
        }
      }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
  }

  fun startRecording() {
    if (isListening) {
      println("recognizedText, Already listening")
      return
    }

    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      println("recognizedText, Speech recognition not available")
      return
    }

    shouldContinueListening = true
    startRecognition()
  }

  private fun startRecognition() {
    if (!shouldContinueListening) {
      return
    }

    try {
      // Clean up existing recognizer before creating new one
      speechRecognizer?.destroy()
      speechRecognizer = null

      // Initialize speech recognizer
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      speechRecognizer?.setRecognitionListener(recognitionListener)

      // Create recognition intent
      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar")
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "ar")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        // Shorter silence timeout for continuous recognition
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
      }

      isListening = true
      speechRecognizer?.startListening(intent)
      println("recognizedText, Started listening for Arabic speech")

    } catch (e: Exception) {
      println("recognizedText, Failed to start speech recognition: ${e.message}")
      isListening = false
      shouldContinueListening = false
    }
  }

  fun stopRecording() {
    if (!isListening) {
      println("recognizedText, Not listening")
      return
    }

    shouldContinueListening = false
    isListening = false
    speechRecognizer?.stopListening()
    println("recognizedText, Stopped listening")
  }

  fun destroy() {
    shouldContinueListening = false
    isListening = false
    speechRecognizer?.destroy()
    speechRecognizer = null
    println("recognizedText, Speech recognizer destroyed")
  }
}

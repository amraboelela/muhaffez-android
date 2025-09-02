package app.amr.muhaffez

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ArabicSpeechRecognizer(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null
  private var recognizerIntent: Intent? = null

  private val _voiceText = MutableLiveData<String>()
  val voiceText: LiveData<String> = _voiceText
  //private var isListening = false
  private var isManuallyStopping = false

  // Create the listener once
  private val recognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle?) {
      //isListening = true
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
      println("recognizedText, onEndOfSpeech")
      //isListening = false
      if (!isManuallyStopping) {
        // Restart listening automatically after a pause
        startRecording()
      }
    }

    override fun onError(error: Int) {
      //isListening = false
      stopRecording()
      println("recognizedText, speech error: $error") // Debugging
      if (!isManuallyStopping && error != SpeechRecognizer.ERROR_CLIENT) {
        startRecording()
      } else {
        // It's a critical error or you manually stopped, so destroy the recognizer.
        stopRecording()
      }
    }

    override fun onResults(results: Bundle?) {
      //isListening = false
      results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
        _voiceText.postValue(it.joinToString(" "))
        println("recognizedText, onResults: $it")
      }
    }

    override fun onPartialResults(partialResults: Bundle?) {
      partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
        _voiceText.postValue(it.joinToString(" "))
        println("recognizedText, onPartialResults: $it")
      }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
  }

  fun startRecording() {
    if (speechRecognizer == null) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
      //putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
    }

    speechRecognizer?.startListening(recognizerIntent)
    //isListening = true
  }

  fun stopRecording() {
    //if (isListening) {
    speechRecognizer?.stopListening()
      //isListening = false
    //}
  }

  fun destroy() {
    speechRecognizer?.destroy()
    speechRecognizer = null
  }
}

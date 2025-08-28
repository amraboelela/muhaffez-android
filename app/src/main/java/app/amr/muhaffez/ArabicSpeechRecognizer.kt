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

  fun startRecording() {
    if (speechRecognizer == null) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
          stopRecording()
        }

        override fun onResults(results: Bundle?) {
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          matches?.let {
            _voiceText.postValue(it.joinToString(" "))
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          partial?.let {
            _voiceText.postValue(it.joinToString(" "))
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })
    }

    // Intent for Arabic recognition
    recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    speechRecognizer?.startListening(recognizerIntent)
  }

  fun stopRecording() {
    speechRecognizer?.stopListening()
    speechRecognizer?.cancel()
  }

  fun destroy() {
    speechRecognizer?.destroy()
    speechRecognizer = null
  }
}

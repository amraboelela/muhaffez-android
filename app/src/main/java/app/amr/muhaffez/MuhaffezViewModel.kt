package app.amr.muhaffez

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MuhaffezViewModel : ViewModel() {
  var isRecording = mutableStateOf(false)
  var voiceText = mutableStateOf("")
  var matchedWords = mutableStateOf(listOf<Pair<String, Boolean>>())

  fun resetData() {
    voiceText.value = ""
    matchedWords.value = emptyList()
  }
}

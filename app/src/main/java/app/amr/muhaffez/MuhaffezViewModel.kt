package app.amr.muhaffez

import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.math.min

class MuhaffezViewModel : ViewModel() {

  var voiceText by mutableStateOf("")
  fun setVoiceText(value: String) {
    voiceText = value
    voiceWords = value.normalizedArabic().split(" ")
    if (value.isNotEmpty()) {
      updateFoundAyat()
      updateMatchedWords()
    }
  }

  var isRecording by mutableStateOf(false)
  var matchedWords by mutableStateOf(listOf<Pair<String, Boolean>>())
  fun setMatchedWords(value: List<Pair<String, Boolean>>) {
    matchedWords = value
    updatePages()
  }

  var foundAyat = mutableListOf<Int>()

  var quranText = ""
    set(value) {
      quranText = value
      quranWords = value.split(" ")
    }

  private var quranWords = listOf<String>()
  private var voiceWords = listOf<String>()

  var tempRightPage = PageModel()
  var tempLeftPage = PageModel()
  var rightPage = PageModel()
  var leftPage = PageModel()
  var currentPageIsRight = true
    set(value) {
      if (value) {
        tempLeftPage.reset()
      }
      if (!currentPageIsRight && value) {
        rightPage.reset()
      }
      currentPageIsRight = value
    }

  val quranModel: QuranModel by lazy { QuranModel.shared }
  val quranLines: List<String> by lazy { quranModel.quranLines }

  // Timers (debounce using coroutines)
  private var debounceJob: Job? = null
  private var peekJob: Job? = null
  private val matchThreshold = 0.6
  private val seekMatchThreshold = 0.7

  // --- Actions ---
  fun resetData() {
    foundAyat.clear()
    quranText = ""
    setMatchedWords(emptyList())
    setVoiceText("")
    currentPageIsRight = true
    tempRightPage.reset()
    tempLeftPage.reset()
    rightPage.reset()
    leftPage.reset()
  }

  // --- Ayah Matching ---
  private fun updateFoundAyat() {
    if (foundAyat.size == 1) return

    foundAyat.clear()
    val normVoice = voiceText.normalizedArabic()
    if (normVoice.isEmpty()) return

    quranLines.forEachIndexed { index, line ->
      if (line.normalizedArabic().startsWith(normVoice)) {
        foundAyat.add(index)
      }
    }

    if (foundAyat.isEmpty()) {
      debounceJob?.cancel()
      debounceJob = viewModelScope.launch {
        delay(1000)
        performFallbackMatch(normVoice)
      }
    }

    updateQuranText()
  }

  private fun performFallbackMatch(normVoice: String) {
    var bestIndex: Int? = null
    var bestScore = 0.0

    quranLines.forEachIndexed { index, line ->
      val lineNorm = line.normalizedArabic()
      if (lineNorm.length >= normVoice.length) {
        val prefix = lineNorm.take(normVoice.length + 2)
        val score = normVoice.similarity(prefix)
        if (score > bestScore) {
          bestScore = score
          bestIndex = index
        }
        if (score > 0.9) return@forEachIndexed
      }
    }

    bestIndex?.let {
      foundAyat.clear()
      foundAyat.add(it)
      updateQuranText()
      updateMatchedWords()
    }
  }

  private fun updateQuranText() {
    foundAyat.firstOrNull()?.let { firstIndex ->
      quranText = quranLines[firstIndex]
      if (foundAyat.size == 1) {
        val endIndex = min(firstIndex + 200, quranLines.size)
        val extraLines = quranLines.subList(firstIndex + 1, endIndex)
        quranText = (listOf(quranText) + extraLines).joinToString(" ")
      }
    }
  }

  // --- Word Matching ---
  fun updateMatchedWords() {
    if (foundAyat.size != 1) return

    peekJob?.cancel()
    peekJob = viewModelScope.launch {
      delay(3000)
      peekHelper()
    }

    val results = mutableListOf<Pair<String, Boolean>>()
    var quranWordsIndex = -1

    for (voiceWord in voiceWords) {
      quranWordsIndex++
      if (quranWordsIndex >= quranWords.size) break

      val qWord = quranWords[quranWordsIndex]
      val normQWord = qWord.normalizedArabic()
      val score = voiceWord.similarity(normQWord)

      if (score >= matchThreshold) {
        results.add(qWord to true)
        continue
      }

      if (tryBackwardMatch(quranWordsIndex, voiceWord, results)) continue
      if (tryForwardMatch(quranWordsIndex, voiceWord, results)) continue

      results.add(qWord to true)
    }
    setMatchedWords(results)
  }

  private fun tryBackwardMatch(index: Int, voiceWord: String, results: MutableList<Pair<String, Boolean>>): Boolean {
    for (step in 1..3) {
      if (index - step < 0) break
      val qWord = quranWords[index - step]
      if (voiceWord.similarity(qWord.normalizedArabic()) > seekMatchThreshold) {
        repeat(step) { results.removeLastOrNull() }
        results.add(qWord to true)
        return true
      }
    }
    return false
  }

  private fun tryForwardMatch(index: Int, voiceWord: String, results: MutableList<Pair<String, Boolean>>): Boolean {
    for (step in 1..3) {
      if (index + step >= quranWords.size) break
      val qWord = quranWords[index + step]
      if (voiceWord.similarity(qWord.normalizedArabic()) > seekMatchThreshold) {
        results.add(quranWords[index] to true)
        for (s in 1 until step) {
          results.add(quranWords[index + s] to true)
        }
        results.add(qWord to true)
        return true
      }
    }
    return false
  }

  // --- Peek Helper ---
  fun peekHelper() {
    if (!isRecording) return
    val results = matchedWords.toMutableList()
    val quranWordsIndex = matchedWords.size

    if (quranWordsIndex + 2 < quranWords.size) {
      results.add(quranWords[quranWordsIndex] to false)
      results.add(quranWords[quranWordsIndex + 1] to false)
      setMatchedWords(results)
    }
  }
}

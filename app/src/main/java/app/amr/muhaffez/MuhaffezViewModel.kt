package app.amr.muhaffez

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.listOf
import kotlin.math.min

class MuhaffezViewModel(context: Context? = null) : ViewModel() {

  var voiceText by mutableStateOf("")
    private set

  var textToPredict by mutableStateOf("")
    private set

  var voiceTextHasBesmillah by mutableStateOf(false)
    private set

  var voiceTextHasA3ozoBellah by mutableStateOf(false)
    private set

  var updatingFoundAyat by mutableStateOf(false)
    private set

  // ML Model for ayah prediction
  private var mlModel: AyaFinderMLModel? = null

  init {
    // Initialize ML model if context is provided
    context?.let {
      try {
        mlModel = AyaFinderMLModel(it)
        println("ML Model initialized successfully")
      } catch (e: Exception) {
        println("Failed to initialize ML model: ${e.message}")
        e.printStackTrace()
      }
    }
  }

  fun updateVoiceText(value: String) {
    voiceText = value
    textToPredict = value.normalizedArabic()
    updateTextToPredict()

    // Check for A3ozoBellah
    if (!voiceTextHasA3ozoBellah && voiceText.hasA3ozoBellah()) {
      println("voiceText didSet, voiceTextHasA3ozoBellah = true")
      voiceTextHasA3ozoBellah = true
    }

    if (value.isNotEmpty()) {
      if (foundAyat.size == 1) {
        if (!updatingFoundAyat) {
          updateMatchedWords()
        }
      } else {
        updateFoundAyat()
      }
    }
  }

  private fun updateTextToPredict() {
    var text = voiceText.normalizedArabic()
    if (voiceTextHasA3ozoBellah) {
      text = text.removeA3ozoBellah()
    }
    if (voiceTextHasBesmillah) {
      text = text.removeBasmallah()
    }
    textToPredict = text
    voiceWords = textToPredict.split(" ")
    println("updateTextToPredict textToPredict: $textToPredict")
  }

  var isRecording by mutableStateOf(false)
  var matchedWords by mutableStateOf(listOf<Pair<String, Boolean>>())
    private set
  fun updateMatchedWords(value: List<Pair<String, Boolean>>) {
    matchedWords = value
    updatePages()
  }

  var previousVoiceWordsCount = 0

  private var _foundAyat = mutableStateOf(listOf<Int>())
  var foundAyat: List<Int>
    get() = _foundAyat.value
    set(value) {
      _foundAyat.value = value
      pageCurrentLineIndex = value.firstOrNull() ?: 0
    }

  var pageCurrentLineIndex by mutableStateOf(0)

  var pageMatchedWordsIndex = 0

  var quranText = ""
    set(value) {
      field = value
      quranWords = value.split(" ")
    }

  var quranWords = listOf<String>()
  var voiceWords = listOf<String>()

  var tempPage = PageModel()
  var rightPage by mutableStateOf(PageModel())
  var leftPage by mutableStateOf(PageModel())
  var currentPageIsRight by mutableStateOf(true)
    private set
  fun updateCurrentPageIsRight(value: Boolean) {
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
  private val matchThreshold = 0.7
  private val simiMatchThreshold = 0.6
  private val seekMatchThreshold = 0.95

  // --- Actions ---
  fun resetData() {
    debounceJob?.cancel()
    foundAyat = emptyList()
    quranText = ""
    updateMatchedWords(emptyList())
    voiceText = ""
    currentPageIsRight = true
    tempPage.reset()
    tempPage.isFirstPage = true
    rightPage.reset()
    leftPage.reset()
    pageCurrentLineIndex = 0
    pageMatchedWordsIndex = 0
    previousVoiceWordsCount = 0
    voiceTextHasBesmillah = false
    voiceTextHasA3ozoBellah = false
    updatingFoundAyat = false
  }

  // --- Ayah Matching ---
  private fun updateFoundAyat() {
    println("updateFoundAyat")
    updatingFoundAyat = true
    debounceJob?.cancel()
    if (foundAyat.size == 1) return

    foundAyat = emptyList()
    println("updateFoundAyat textToPredict: $textToPredict")
    if (textToPredict.length <= 10) {
      println("updateFoundAyat textToPredict.length <= 10")
      return
    }

    // Fast prefix check
    quranLines.forEachIndexed { index, line ->
      val normLine = line.normalizedArabic()
      if (normLine.startsWith(textToPredict) || textToPredict.startsWith(normLine)) {
        if (index == 0) {
          println("updateFoundAyat, voiceTextHasBesmillah = true")
          voiceTextHasBesmillah = true
          if (textToPredict.isEmpty()) {
            return
          }
        } else {
          foundAyat = foundAyat + index
        }
      }
    }

    println("updateFoundAyat foundAyat: $foundAyat")
    if (foundAyat.isNotEmpty()) {
      for (ayahIndex in foundAyat) {
        println("  Found ayah [$ayahIndex]: ${quranLines[ayahIndex]}")
      }
    }

    // Fallback with debounce if no matches
    if (foundAyat.isEmpty() || textToPredict.length < 17) {
      println("foundAyat.isEmpty() || textToPredict.length < 17")
      debounceJob = viewModelScope.launch {
        delay(1000)
        performFallbackMatch()
      }
      return
    }

    println("updateFoundAyat foundAyat 2: $foundAyat")
    updateQuranText()
    updateMatchedWords()
    updatingFoundAyat = false
  }

  private fun performFallbackMatch() {
    println("performFallbackMatch textToPredict: $textToPredict")

    // Try ML model prediction first
    tryMLModelMatch()?.let { ayahIndex ->
      // Strip bismillah if flag is set
      if (ayahIndex == 0) {
        println("performFallbackMatch, voiceTextHasBesmillah = true")
        voiceTextHasBesmillah = true
        performFallbackMatch()
        return
      }
      println("#coreml ML prediction accepted")
      foundAyat = emptyList()
      foundAyat = foundAyat + ayahIndex
      updateQuranText()
      updateMatchedWords()
      updatingFoundAyat = false
      return
    }

    println("#coreml ML model failed or had low similarity score")

    // Fallback to similarity matching
    var bestIndex: Int? = null
    var bestScore = 0.0

    quranLines.forEachIndexed { index, line ->
      val ayahNorm = line.normalizedArabic()
      val ayahPrefix = ayahNorm.take(textToPredict.length)
      val textPrefix = textToPredict.take(ayahPrefix.length)
      val similarity = textPrefix.similarity(ayahPrefix)

      if (similarity > bestScore) {
        bestScore = similarity
        bestIndex = index
      }
      if (similarity > 0.9) {
        println("Early break at index $index: $line")
        println("  similarity: ${String.format("%.2f", similarity)}")
        return@forEachIndexed
      }
    }

    bestIndex?.let {
      if (it > 0) {
        println("performFallbackMatch bestIndex: $it")
        println("performFallbackMatch bestIndex ayah: ${quranLines[it]}")
        foundAyat = emptyList()
        foundAyat = foundAyat + it
        updateQuranText()
        updateMatchedWords()
      }
    }
    updatingFoundAyat = false
  }

  /**
   * Try ML model prediction and validate with similarity check
   * Returns ayah index if best match from top 5 ML predictions has similarity > 70%
   */
  private fun tryMLModelMatch(): Int? {
    val mlModel = this.mlModel ?: return null

    val prediction = mlModel.predict(textToPredict) ?: return null

    println("ML Model prediction - Index: ${prediction.ayahIndex}, Probability: ${prediction.probability}")
    println("Top 5 predictions:")
    for ((index, prob) in prediction.top5) {
      println("  [$index] ${String.format("%.2f%%", prob * 100)}: ${quranLines.getOrNull(index) ?: ""}")
    }

    // Check top 5 predictions and return the one with highest similarity to normalized voice
    if (textToPredict.isEmpty()) {
      println("textToPredict is empty, returning null")
      return null
    }

    var bestMatch: Pair<Int, Double>? = null
    var bestSimilarity = 0.0

    for ((index, _) in prediction.top5) {
      if (index < 0 || index >= quranLines.size) continue

      val ayahNorm = quranLines[index].normalizedArabic()
      val ayahPrefix = ayahNorm.take(textToPredict.length)
      val textPrefix = textToPredict.take(ayahPrefix.length)
      val similarity = textPrefix.similarity(ayahPrefix)

      if (similarity > bestSimilarity) {
        bestSimilarity = similarity
        bestMatch = Pair(index, similarity)
      }
    }

    bestMatch?.let { (index, similarity) ->
      println("#coreml Best match: [$index] with ${String.format("%.2f", similarity)} similarity - ${quranLines[index]}")
      if (similarity >= 0.7) {
        return index
      } else {
        println("#coreml Best match rejected - similarity too low: ${String.format("%.2f", similarity)}")
        return null
      }
    }

    return null
  }

  private fun updateQuranText() {
    foundAyat.firstOrNull()?.let { firstIndex ->
      quranText = quranLines[firstIndex]
      if (foundAyat.size == 1) {
        println("updateQuranText firstIndex: $firstIndex")
        println("updateQuranText quranLines[firstIndex]: ${quranLines[firstIndex]}")
        val endIndex = min(firstIndex + 500, quranLines.size)
        val extraLines = quranLines.subList(firstIndex + 1, endIndex)
        quranText = (listOf(quranText) + extraLines).joinToString(" ")
      }
    }
  }

  // --- Word Matching ---
  fun updateMatchedWords() {
    if (foundAyat.size != 1) return

    var results = matchedWords.toMutableList()  // start with previous results
    var quranWordsIndex = results.size - 1  // continue from last matched index
    var voiceIndex = if (previousVoiceWordsCount > 1) previousVoiceWordsCount - 2 else previousVoiceWordsCount

    println("voiceWords: $voiceWords")
    var canAdvance = true
    if (voiceIndex >= voiceWords.size) {
      println("voiceIndex >= voiceWords.size")
    }

    while (voiceIndex < voiceWords.size) {
      val voiceWord = voiceWords[voiceIndex]
      if (canAdvance) {
        quranWordsIndex++
        peekJob?.cancel()
        peekJob = viewModelScope.launch {
          delay(5000)
          peekHelper()
        }
      }
      canAdvance = true
      if (quranWordsIndex >= quranWords.size) {
        println("quranWordsIndex >= quranWords.size, voiceWord: $voiceWord")
        break
      }

      val qWord = quranWords[quranWordsIndex]
      val normQWord = qWord.normalizedArabic()
      val score = voiceWord.similarity(normQWord)

      if (score >= matchThreshold) {
        println("Matched word, voiceWord: $voiceWord, qWord: $qWord")
        results.add(qWord to true)
      } else {
        if (tryBackwardMatch(quranWordsIndex, voiceWord, results)) {
          canAdvance = false
        } else if (voiceWord.length > 3 && tryForwardMatch(quranWordsIndex, voiceWord, results)) {
          // matched in forward search
          quranWordsIndex = results.size - 1
        } else {
          if (score >= simiMatchThreshold) {
            println("Simimatched, voiceWord: $voiceWord, qWord: $qWord")
            results.add(qWord to true)
          } else {
            println("Unmatched, voiceWord: $voiceWord, qWord: $qWord")
            canAdvance = false
          }
        }
      }
      voiceIndex++
    }
    updateMatchedWords(results)
    previousVoiceWordsCount = voiceWords.size
  }

  private fun tryBackwardMatch(
    index: Int,
    voiceWord: String,
    results: MutableList<Pair<String, Boolean>>
  ): Boolean {
    for (step in 1..10) {
      if (index - step < 0) break
      val qWord = quranWords[index - step]
      if (voiceWord.similarity(qWord.normalizedArabic()) >= seekMatchThreshold) {
        println("tryBackwardMatch, voiceWord: $voiceWord, qWord: $qWord")
        return true
      }
    }
    return false
  }

  private fun tryForwardMatch(
    index: Int,
    voiceWord: String,
    results: MutableList<Pair<String, Boolean>>
  ): Boolean {
    for (step in 1..17) {
      if (index + step >= quranWords.size) break
      val qWord = quranWords[index + step]
      if (voiceWord.similarity(qWord.normalizedArabic()) >= seekMatchThreshold) {
        results.add(quranWords[index] to true)
        for (s in 1 until step) {
          results.add(quranWords[index + s] to true)
        }
        results.add(qWord to true)
        println("tryForwardMatch, voiceWord: $voiceWord, qWord: $qWord")
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
      updateMatchedWords(results)
    }
  }

  override fun onCleared() {
    super.onCleared()
    mlModel?.close()
  }
}

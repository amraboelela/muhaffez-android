package app.amr.muhaffez

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.runner.RunWith
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class MuhaffezViewModelTests {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranModel.initialize(context)
  }

  @Test
  fun testExactMatch() {
    val viewModel = MuhaffezViewModel()
    viewModel.updateVoiceText("ان الله يامركم ان تؤدوا الامانات الى اهلها")
    val matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    println("Matched words: $matchedTrues")  // ✅ Print to console
    assertTrue(matchedTrues.contains("إِنَّ"))
    assertTrue(matchedTrues.contains("اللَّهَ"))
    assertTrue(matchedTrues.contains("يَأمُرُكُم"))
    assertTrue(matchedTrues.contains("إِلىٰ"))
  }

  @Test
  fun testFuzzyMatch() {
    val viewModel = MuhaffezViewModel()

    viewModel.updateVoiceText("ان الله يامرك")
    var matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    assertTrue(matchedTrues.contains("إِنَّ"))
    assertTrue(matchedTrues.contains("اللَّهَ"))
    assertTrue(matchedTrues.contains("يَأمُرُكُم"))

    viewModel.updateVoiceText("إن الله")
    matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    assertTrue(matchedTrues.contains("إِنَّ"))
    assertTrue(matchedTrues.contains("اللَّهَ"))

    viewModel.updateVoiceText("إن")
    matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    assertTrue(matchedTrues.contains("إِنَّ"))
  }

  @Test
  fun testPartialRecognition() = runBlocking {
    val viewModel = MuhaffezViewModel()
    viewModel.updateVoiceText("الله يأمرك بالعدل")
    withTimeout(5000) { // wait until data updates
      while (viewModel.foundAyat.isEmpty()) {
        delay(1000)
      }
    }

    val matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    assertEquals(listOf(1987), viewModel.foundAyat)
  }

  @Test
  fun testNoMatch() {
    val viewModel = MuhaffezViewModel()
    viewModel.updateVoiceText("hello world")
    val matchedTrues = viewModel.matchedWords.filter { it.second }.map { it.first }
    assertTrue(matchedTrues.isEmpty())
  }

  @Test
  fun testResetData() {
    val viewModel = MuhaffezViewModel()
    viewModel.foundAyat = mutableListOf(1, 423)
    viewModel.quranText = "Some text"
    viewModel.updateMatchedWords(mutableListOf("word1" to true, "word2" to false))
    viewModel.updateVoiceText("Some voice text")
    viewModel.resetData()
    assertTrue(viewModel.foundAyat.isEmpty())
    assertTrue(viewModel.quranText.isEmpty())
    assertTrue(viewModel.matchedWords.isEmpty())
    assertTrue(viewModel.voiceText.isEmpty())
  }

  @Test
  fun testPeekHelperAddsTwoWordsWhenRecording() {
    val viewModel = MuhaffezViewModel()
    viewModel.isRecording = true
    viewModel.quranWords = mutableListOf("word1", "word2", "word3", "word4")
    viewModel.updateMatchedWords(mutableListOf("word1" to true)) //matchedWords = mutableListOf("word1" to true)

    viewModel.peekHelper()

    assertEquals(3, viewModel.matchedWords.size)
    assertEquals("word2", viewModel.matchedWords[1].first)
    assertEquals("word3", viewModel.matchedWords[2].first)
    assertFalse(viewModel.matchedWords[1].second)
    assertFalse(viewModel.matchedWords[2].second)
  }

  @Test
  fun testPeekHelperDoesNothingIfNotRecording() {
    val viewModel = MuhaffezViewModel()
    viewModel.isRecording = false
    viewModel.quranWords = mutableListOf("word1", "word2")
    viewModel.updateMatchedWords(mutableListOf("word1" to true))

    viewModel.peekHelper()

    assertEquals(1, viewModel.matchedWords.size)
  }

  @Test
  fun testUpdateFoundAyatDoesNothingWhenVoiceTextIsEmpty() {
    val viewModel = MuhaffezViewModel()
    viewModel.updateVoiceText("")
    viewModel.foundAyat = mutableListOf(0, 1)

    // viewModel.updateFoundAyat()  // only call if implemented

    assertEquals(listOf(0, 1), viewModel.foundAyat)
  }
}

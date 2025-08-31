package app.amr.muhaffez

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class MuhaffezViewModelTwoPagesTests {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranModel.initialize(context)
  }

  @Test
  fun testDisplayText() {
    val viewModel = MuhaffezViewModel()
    viewModel.foundAyat = mutableListOf(0)
    viewModel.updateMatchedWords(listOf("Bismillah" to true, "Rahman" to false))

    val textString = viewModel.rightPage.textString

    assertTrue(textString.contains("Bismillah"))
    assertTrue(textString.contains("Rahman"))
  }

  @Test
  fun testColoredFromMatched() {
    val viewModel = MuhaffezViewModel()
    viewModel.updateVoiceText("الم ذٰلِكَ الكِتابُ لا رَيبَ فيهِ هُدًى لِلمُتَّقينَ")

    val result = viewModel.leftPage.textString

    println("result: $result")
    assertTrue(result.contains("الكِتابُ"))
    assertTrue(result.contains("لِلمُتَّقينَ"))
  }

  @Test
  fun testColoredFromMatched_addsRub3Separator() = runBlocking {
    val viewModel = MuhaffezViewModel()

    viewModel.updateVoiceText("إِنَّ رَبَّهُم بِهِم يَومَئِذٍ لَخَبيرٌ")
    while (viewModel.foundAyat.isEmpty()) {
      kotlinx.coroutines.delay(1000)
    }
    var textString = viewModel.leftPage.textString

    assertTrue(!textString.contains("─"))
    assertTrue(!textString.contains("القارعة"))
    assertTrue(!textString.contains("⭐"))

    viewModel.updateVoiceText("إِنَّ رَبَّهُم بِهِم يَومَئِذٍ لَخَبيرٌ القارِعَةُ")
    assertEquals(6, viewModel.voiceWords.size)
    textString = viewModel.leftPage.textString
    assertTrue(textString.contains("⭐"))
    viewModel.isRecording = true
    while (viewModel.leftPage.textString.length == textString.length) {
      kotlinx.coroutines.delay(1000)
    }
    assertTrue(viewModel.leftPage.textString.length > textString.length)
    textString = viewModel.leftPage.textString
    println("textString: $textString")
    assertTrue(textString.contains("إِنَّ"))

    viewModel.resetData()
    viewModel.updateVoiceText("عَينًا فيها تُسَمّىٰ سَلسَبيلًا")
    textString = viewModel.rightPage.textString
    assertTrue(textString.contains("⭐"))

    viewModel.resetData()
    viewModel.updateVoiceText("نحن جعلناها تذكرة")
    viewModel.updateVoiceText("نحن جعلناها تذكرة فسبح باسم ربك العظيم")
    textString = viewModel.leftPage.textString
    assertTrue(textString.contains("نَحنُ"))
  }
}

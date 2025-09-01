package app.amr.muhaffez

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QuranModelTests {
  private lateinit var model: QuranModel

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranModel.initialize(context)
    model = QuranModel.shared
  }

  @Test
  fun testSingletonSharedInstance() {
    val instance1 = QuranModel.shared
    val instance2 = QuranModel.shared
    assertSame(instance1, instance2) // Singleton check
  }

  @Test
  fun testQuranLinesNotEmpty() {
    assertTrue("Quran lines should not be empty", model.quranLines.isNotEmpty())
  }

  @Test
  fun testPageMarkersNotEmpty() {
    assertTrue("Page markers should not be empty", model.pageMarkers.isNotEmpty())
  }

  @Test
  fun testFirstLineContent() {
    val firstLine = model.quranLines.firstOrNull() ?: ""
    assertTrue(firstLine.isNotEmpty())
    assertTrue("First line should contain Arabic characters", firstLine.contains(Regex("\\p{Arabic}")))
  }

  @Test
  fun testPageMarkersInRange() {
    model.pageMarkers.forEach {
      assertTrue(it >= 0 && it < model.quranLines.size)
    }
    assertEquals(603, model.pageMarkers.size)
    assertEquals("صِراطَ الَّذينَ أَنعَمتَ عَلَيهِم غَيرِ المَغضوبِ عَلَيهِم وَلَا الضّالّينَ", model.quranLines[model.pageMarkers[0]])
    assertEquals("رَبَّنا إِنَّكَ جامِعُ النّاسِ لِيَومٍ لا رَيبَ فيهِ إِنَّ اللَّهَ لا يُخلِفُ الميعادَ", model.quranLines[model.pageMarkers[49]])
  }

  @Test
  fun testPageMarkersSorted() {
    val sortedMarkers = model.pageMarkers.sorted()
    assertEquals("Page markers should be sorted ascending", sortedMarkers, model.pageMarkers)
  }

  @Test
  fun testPageNumberForAyahIndex() {
    assertEquals(1, model.pageNumber(index = 0))
    assertEquals(1, model.pageNumber(index = 6))
    assertEquals(2, model.pageNumber(index = 7))
    assertEquals(603, model.pageNumber(index = model.pageMarkers[602]))
  }

  @Test
  fun testSurahNameValidPages() {
    assertEquals("البقرة", model.surahNameFor(page = 49))
    assertEquals("الفاتحة", model.surahNameFor(page = 1))
    assertEquals("الناس", model.surahNameFor(page = 604))
  }

  @Test
  fun testSurahNameInvalidPage() {
    assertEquals("", model.surahNameFor(page = 0))
    assertEquals("", model.surahNameFor(page = -5))
  }

  @Test
  fun testSurahNameForAyahIndex() {
    assertEquals("الفاتحة", model.surahNameAt(index = 0))
    val lastIndex = model.quranLines.size - 1
    assertEquals("الناس", model.surahNameAt(index = lastIndex))
    assertTrue(model.surahNameAt(index = -1).isEmpty())
    assertTrue(model.surahNameAt(index = model.quranLines.size).isEmpty())
  }

  @Test
  fun testIsEndOfSurah() {
    assertTrue(model.isEndOfSurah(6))
    assertFalse(model.isEndOfSurah(7))
  }

  @Test
  fun testIsEndOfRub3() {
    assertFalse(model.isEndOfRub3(6))
    assertFalse(model.isEndOfRub3(21))
  }

  @Test
  fun testFillRightPage() {
    val viewModel = MuhaffezViewModel()
    val ayahIndex = 1
    model.updatePages(viewModel, ayahIndex)
    assertEquals(1, viewModel.tempRightPage.juzNumber)
    assertEquals("الفاتحة", viewModel.tempRightPage.surahName)
    assertEquals(1, viewModel.tempRightPage.pageNumber)
  }

  @Test
  fun testFillLeftPage() {
    val viewModel = MuhaffezViewModel()
    val ayahIndex = 10
    model.updatePageModelsIfNeeded(viewModel, ayahIndex)
    assertEquals(1, viewModel.tempLeftPage.juzNumber)
    assertEquals("البقرة", viewModel.tempLeftPage.surahName)
    assertEquals(2, viewModel.tempLeftPage.pageNumber)
  }

  @Test
  fun testIsRightPage() {
    fun pageNumber(index: Int) = index
    fun isRightPage(index: Int) = pageNumber(index) % 2 == 1
    assertTrue(isRightPage(1))
    assertFalse(isRightPage(2))
  }
}

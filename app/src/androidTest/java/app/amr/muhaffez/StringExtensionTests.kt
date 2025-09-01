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
class StringExtensionTests {

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    QuranModel.initialize(context)
    //model = QuranModel.shared
  }

  @Test
  fun testRemovingTashkeel() {
    val text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    val expected = "بسم الله الرحمن الرحيم"
    println("text.removingTashkeel: ${text.removingTashkeel()}")
    assertEquals(expected, text.removingTashkeel())
  }

  @Test
  fun testNormalizedArabic_keepsTextIfNoPrefix() {
    val original = "الحمد لله رب العالمين"
    val expected = original
    val normalized = original.normalizedArabic()
    assertEquals(expected, normalized)
  }

  @Test
  fun testNormalizedArabic_normalizesHamzaVariants() {
    val original = "أدخل إلى المدرسة"
    val expected = "ادخل الى المدرسة"
    val normalized = original.normalizedArabic()
    assertEquals(expected, normalized)
  }

  @Test
  fun testNormalizedArabicHamzaVariants() {
    val text = "إسلام أمان آخرة مؤمن رئيس"
    val normalized = text.normalizedArabic()
    println("normalized: $normalized")
    assertTrue(normalized.contains("اسلام"))
    assertTrue(normalized.contains("امان"))
    assertTrue(normalized.contains("اخرة"))
    assertTrue(normalized.contains("مومن"))
    assertTrue(normalized.contains("رييس"))
  }

  @Test
  fun testFindIn() {
    val lines = listOf(
      "قال إنه من سليمان",
      "وإنه بسم الله الرحمن الرحيم",
      "ألا تعلوا علي وأتوني مسلمين"
    )

    val search1 = "انه من سليمان"
    val found1 = search1.findIn(lines)
    assertEquals("قال إنه من سليمان", found1)

    val search2 = "بسم الله"
    val found2 = search2.findIn(lines)
    assertEquals("وإنه بسم الله الرحمن الرحيم", found2)

    val search3 = "غير موجود"
    val found3 = search3.findIn(lines)
    assertNull(found3)
  }

  @Test
  fun testFindLineStartingIn() {
    val lines = listOf(
      "ان الله يأمركم أن تؤدوا الأمانات",
      "وإذا حكمتم بين الناس أن تحكموا بالعدل"
    )

    val search = "ان الله يأمركم"
    val result = search.findLineStartingIn(lines)
    if (result != null) {
      assertEquals(0, result.second)
      assertEquals("ان الله يأمركم أن تؤدوا الأمانات", result.first)
    } else {
      fail("Expected line not found")
    }
  }

  @Test
  fun testLevenshteinDistance() {
    assertEquals(3, "kitten".levenshteinDistance("sitting"))
    assertEquals(2, "flaw".levenshteinDistance("lawn"))
    assertEquals(0, "test".levenshteinDistance("test"))
  }

  @Test
  fun testSimilarity() {
    val sim1 = "kitten".similarity("sitting")
    val sim2 = "flaw".similarity("lawn")
    val sim3 = "identical".similarity("identical")

    println("sim1: $sim1")
    println("sim2: $sim2")
    println("sim3: $sim3")
    assertTrue(sim1 > 0.5 && sim1 < 1.0)
    assertTrue(sim2 >= 0.5 && sim2 < 1.0)
    assertEquals(1.0, sim3, 0.0)
  }
}

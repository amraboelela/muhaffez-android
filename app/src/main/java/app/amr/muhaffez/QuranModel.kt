package app.amr.muhaffez

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.compose.ui.text.AnnotatedString

class QuranModel private constructor(context: Context) {
  val quranLines: List<String>
  val pageMarkers: List<Int>
  val rub3Markers: List<Int>
  val surahMarkers: List<Int>

  // Maps start page number to surah name
  val surahs: List<Pair<Int, String>> = listOf(
    1 to "الفاتحة",
    2 to "البقرة",
    50 to "آل عمران",
    77 to "النساء",
    106 to "المائدة",
    128 to "الأنعام",
    151 to "الأعراف",
    177 to "الأنفال",
    187 to "التوبة",
    208 to "يونس",
    221 to "هود",
    235 to "يوسف",
    249 to "الرعد",
    255 to "إبراهيم",
    262 to "الحجر",
    267 to "النحل",
    282 to "الإسراء",
    293 to "الكهف",
    305 to "مريم",
    312 to "طه",
    322 to "الأنبياء",
    332 to "الحج",
    342 to "المؤمنون",
    350 to "النور",
    359 to "الفرقان",
    367 to "الشعراء",
    377 to "النمل",
    385 to "القصص",
    396 to "العنكبوت",
    404 to "الروم",
    411 to "لقمان",
    415 to "السجدة",
    418 to "الأحزاب",
    428 to "سبأ",
    434 to "فاطر",
    440 to "يس",
    446 to "الصافات",
    453 to "ص",
    458 to "الزمر",
    467 to "غافر",
    477 to "فصلت",
    483 to "الشورى",
    489 to "الزخرف",
    496 to "الدخان",
    499 to "الجاثية",
    502 to "الأحقاف",
    507 to "محمد",
    511 to "الفتح",
    515 to "الحجرات",
    518 to "ق",
    520 to "الذاريات",
    523 to "الطور",
    526 to "النجم",
    528 to "القمر",
    531 to "الرحمن",
    534 to "الواقعة",
    537 to "الحديد",
    542 to "المجادلة",
    545 to "الحشر",
    549 to "الممتحنة",
    551 to "الصف",
    553 to "الجمعة",
    554 to "المنافقون",
    556 to "التغابن",
    558 to "الطلاق",
    560 to "التحريم",
    562 to "الملك",
    564 to "القلم",
    566 to "الحاقة",
    568 to "المعارج",
    570 to "نوح",
    572 to "الجن",
    574 to "المزمل",
    575 to "المدثر",
    577 to "القيامة",
    578 to "الإنسان",
    580 to "المرسلات",
    582 to "النبأ",
    583 to "النازعات",
    585 to "عبس",
    586 to "التكوير",
    587 to "الانفطار",
    587 to "المطففين",
    589 to "الانشقاق",
    590 to "البروج",
    591 to "الطارق",
    591 to "الأعلى",
    592 to "الغاشية",
    593 to "الفجر",
    594 to "البلد",
    595 to "الشمس",
    595 to "الليل",
    596 to "الضحى",
    596 to "الشرح",
    597 to "التين",
    597 to "العلق",
    598 to "القدر",
    598 to "البينة",
    599 to "الزلزلة",
    599 to "العاديات",
    600 to "القارعة",
    600 to "التكاثر",
    601 to "العصر",
    601 to "الهمزة",
    601 to "الفيل",
    602 to "قريش",
    602 to "الماعون",
    602 to "الكوثر",
    603 to "الكافرون",
    603 to "النصر",
    603 to "المسد",
    604 to "الإخلاص",
    604 to "الفلق",
    604 to "الناس"
  )

  init {
    val lines = mutableListOf<String>()
    val pageMarkersList = mutableListOf<Int>()
    val rub3MarkersList = mutableListOf<Int>()
    val surahMarkersList = mutableListOf<Int>()

    try {
      val inputStream = context.assets.open("quran-simple-min.txt")
      val content = inputStream.bufferedReader(Charsets.UTF_8).readText()
      val fileLines = content.split("\n")

      for (line in fileLines) {
        when {
          line.isEmpty() -> pageMarkersList.add(lines.size - 1)
          line == "*" -> rub3MarkersList.add(lines.size - 1)
          line == "-" -> surahMarkersList.add(lines.size - 1)
          else -> lines.add(line)
        }
      }
    } catch (e: Exception) {
      println("❌ Error reading file: ${e.message}")
    }

    quranLines = lines
    pageMarkers = pageMarkersList
    rub3Markers = rub3MarkersList
    surahMarkers = surahMarkersList
  }

  fun pageNumber(index: Int): Int {
    if (pageMarkers.isEmpty() || index < 0 || index >= quranLines.size) return 0
    for ((pageIndex, marker) in pageMarkers.withIndex()) {
      if (index <= marker) return pageIndex + 1
    }
    return pageMarkers.size + 1
  }

  fun rub3Number(index: Int): Int {
    if (rub3Markers.isEmpty() || index < 0 || index >= quranLines.size) return 0
    for ((rub3Index, marker) in rub3Markers.withIndex()) {
      if (index < marker) return rub3Index + 1
    }
    return rub3Markers.size + 1
  }

  fun juzNumber(index: Int): Int {
    val rub3Num = rub3Number(index)
    return Math.ceil(rub3Num / 8.0).toInt()
  }

  fun surahNameFor(page: Int): String {
    if (page < 1) return ""
    for (i in surahs.indices.reversed()) {
      if (page >= surahs[i].first) return surahs[i].second
    }
    return ""
  }

  fun surahNameForAyahIndex(index: Int): String {
    if (surahMarkers.isEmpty() || index < 0 || index >= quranLines.size) return ""
    for (i in surahMarkers.indices.reversed()) {
      if (index >= surahMarkers[i]) return surahs[i + 1].second
    }
    return surahs[0].second
  }

  fun isRightPage(index: Int): Boolean {
    return pageNumber(index) % 2 == 1
  }

  fun isEndOfRub3(ayahIndex: Int): Boolean {
    return rub3Markers.contains(ayahIndex)
  }

  fun isEndOfSurah(ayahIndex: Int): Boolean {
    return surahMarkers.contains(ayahIndex)
  }

  fun updatePages(viewModel: MuhaffezViewModel, index: Int) {
    if (isRightPage(index)) {
      viewModel.tempRightPage.juzNumber = juzNumber(index)
      viewModel.tempRightPage.surahName = surahNameForAyahIndex(index)
      viewModel.tempRightPage.pageNumber = pageNumber(index)
    } else {
      viewModel.tempLeftPage.juzNumber = juzNumber(index)
      viewModel.tempLeftPage.surahName = surahNameForAyahIndex(index)
      viewModel.tempLeftPage.pageNumber = pageNumber(index)
    }
    viewModel.currentPageIsRight = isRightPage(index)
  }

  fun updatePageModelsIfNeeded(viewModel: MuhaffezViewModel, index: Int) {
    if (viewModel.currentPageIsRight != isRightPage(index)) {
      updatePages(viewModel, index)
      if (viewModel.currentPageIsRight) {
        viewModel.tempRightPage.text = AnnotatedString.Builder()
        viewModel.tempLeftPage.text = AnnotatedString.Builder()
      }
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: QuranModel? = null

    /** Initialize the singleton once, usually in Application or MainActivity */
    fun initialize(context: Context) {
      if (INSTANCE == null) {
        synchronized(this) {
          if (INSTANCE == null) {
            INSTANCE = QuranModel(context.applicationContext)
          }
        }
      }
    }

    /** Access the singleton anywhere later without passing context */
    val shared: QuranModel
      get() = INSTANCE ?: throw IllegalStateException(
        "QuranModel not initialized. Call QuranModel.shared.initialize(context) first."
      )
  }
}
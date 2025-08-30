package app.amr.muhaffez

import android.text.SpannableStringBuilder

data class PageModel(
  var juzNumber: Int = 0,
  var surahName: String = "",
  var pageNumber: Int = 0,
  var text: SpannableStringBuilder = SpannableStringBuilder("")
) {
  val textString: String
    get() = text.toString()

  fun reset() {
    juzNumber = 0
    surahName = ""
    pageNumber = 0
    text = SpannableStringBuilder("")
  }
}

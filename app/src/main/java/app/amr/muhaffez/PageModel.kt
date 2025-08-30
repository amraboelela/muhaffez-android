package app.amr.muhaffez

import androidx.compose.ui.text.AnnotatedString

data class PageModel(
  var juzNumber: Int = 0,
  var surahName: String = "",
  var pageNumber: Int = 0,
  var text: AnnotatedString.Builder = AnnotatedString.Builder()
) {
  val annotatedString: AnnotatedString
    get() = text.toAnnotatedString()
  val string: String
    get() = text.toString()

  fun reset() {
    juzNumber = 0
    surahName = ""
    pageNumber = 0
    text = AnnotatedString.Builder()
  }
}

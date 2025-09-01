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

  val textString: String
    get() = annotatedString.text

  fun reset() {
    juzNumber = 0
    surahName = ""
    pageNumber = 0
    text = AnnotatedString.Builder()
  }

  // Use a different name like `deepCopy()` to avoid the conflict.
  fun deepCopy(): PageModel {
    val newTextBuilder = AnnotatedString.Builder(text.toAnnotatedString())

    return PageModel(
      juzNumber = juzNumber,
      surahName = surahName,
      pageNumber = pageNumber,
      text = newTextBuilder
    )
  }
}

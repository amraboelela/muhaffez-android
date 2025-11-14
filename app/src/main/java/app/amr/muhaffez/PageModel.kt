package app.amr.muhaffez

import androidx.compose.ui.text.AnnotatedString

data class PageModel(
  var juzNumber: Int = 0,
  var surahName: String = "",
  var pageNumber: Int = 0,
  var text: AnnotatedString.Builder = AnnotatedString.Builder(),
  var isFirstPage: Boolean = false
) {
  val annotatedString: AnnotatedString
    get() = text.toAnnotatedString()

  val textString: String
    get() = annotatedString.text

  val isEmpty: Boolean
    get() = textString.isEmpty()

  fun reset() {
    juzNumber = 0
    surahName = ""
    pageNumber = 0
    text = AnnotatedString.Builder()
    isFirstPage = false
  }

  // Use a different name like `deepCopy()` to avoid the conflict.
  fun deepCopy(): PageModel {
    val newTextBuilder = AnnotatedString.Builder(text.toAnnotatedString())

    return PageModel(
      juzNumber = juzNumber,
      surahName = surahName,
      pageNumber = pageNumber,
      text = newTextBuilder,
      isFirstPage = isFirstPage
    )
  }
}

package app.amr.muhaffez

import androidx.compose.ui.text.AnnotatedString

enum class PageType {
  LEFT,
  RIGHT,
  PRE_LEFT
}

data class PageModel(
  var juzNumber: Int = 0,
  var surahName: String = "",
  var pageNumber: Int = 0,
  var text: AnnotatedString.Builder = AnnotatedString.Builder(),
  var isFirstPage: Boolean = false,
  var pageType: PageType = PageType.RIGHT
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
    if (pageType == PageType.PRE_LEFT) {
      pageType = PageType.LEFT
    }
  }

  // Use a different name like `deepCopy()` to avoid the conflict.
  fun deepCopy(): PageModel {
    val newTextBuilder = AnnotatedString.Builder(text.toAnnotatedString())

    return PageModel(
      juzNumber = juzNumber,
      surahName = surahName,
      pageNumber = pageNumber,
      text = newTextBuilder,
      isFirstPage = isFirstPage,
      pageType = pageType
    )
  }
}

package app.amr.muhaffez

data class PageModel(
  var juzNumber: Int = 0,
  var surahName: String = "",
  var pageNumber: Int = 0,
  var text: CharSequence = "" // Use SpannableString if you need rich text
) {
  val textString: String
    get() = text.toString()

  fun reset() {
    juzNumber = 0
    surahName = ""
    pageNumber = 0
    text = ""
  }
}

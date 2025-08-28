package app.amr.muhaffez

object ArabicDiacritics {
  // Arabic diacritics Unicode range U+064B to U+065F plus U+0670 (dagger alif)
  val diacritics: Set<Char> by lazy {
    val set = mutableSetOf<Char>()
    // Add harakat: fatha, damma, kasra, sukun, shadda, etc.
    for (codePoint in 0x064B..0x065F) {
      set.add(codePoint.toChar())
    }
    // Add dagger alif
    set.add(0x0670.toChar())
    set
  }

  // Function to remove diacritics from a string
  fun removeDiacritics(text: String): String {
    return text.filter { it !in diacritics }
  }
}

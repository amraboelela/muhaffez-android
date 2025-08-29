package app.amr.muhaffez

// In Kotlin, you'd typically define these as extension functions on the String class.
// There isn't a direct equivalent for `CharacterSet.arabicDiacritics`, so we define it manually.

private val ARABIC_DIACRITICS = setOf('\u064B', '\u064C', '\u064D', '\u064E', '\u064F', '\u0650', '\u0651', '\u0652', '\u0653', '\u0654', '\u0655', '\u0656', '\u0657', '\u0658', '\u0659', '\u065A', '\u065B', '\u065C', '\u065D', '\u065E', '\u0670')

fun String.removingTashkeel(): String {
  return this.filter {
    it !in ARABIC_DIACRITICS
  }
}

fun String.removingControlCharacters(): String {
  // Kotlin's `replace` function supports regular expressions by default
  return this.replace("\\p{Cf}".toRegex(), "")
}

fun String.normalizedArabic(): String {
  // 1. Remove diacritics and control characters
  var text = this.removingTashkeel().removingControlCharacters()

  // 2. Normalize hamza variants
  val hamzaMap = mapOf(
    'إ' to 'ا', 'أ' to 'ا', 'آ' to 'ا',
    'ؤ' to 'و', 'ئ' to 'ي'
  )
  text = text.map { hamzaMap.getOrDefault(it, it) }.joinToString("")

  return text
}

fun String.findIn(lines: List<String>): String? {
  val normalizedSearch = this.normalizedArabic()
  return lines.firstOrNull { line ->
    val normalizedLine = line.normalizedArabic()
    normalizedLine.contains(normalizedSearch)
  }
}

fun String.findLineStartingIn(lines: List<String>): Pair<String, Int>? {
  val normalizedSearch = this.normalizedArabic()

  for ((index, line) in lines.withIndex()) {
    if (line.normalizedArabic().startsWith(normalizedSearch)) {
      return Pair(line, index)
    }
  }
  return null
}

// Levenshtein distance
fun String.levenshteinDistance(target: String): Int {
  val sourceArray = this.toCharArray()
  val targetArray = target.toCharArray()
  val (n, m) = sourceArray.size to targetArray.size
  val dist = Array(n + 1) { IntArray(m + 1) }

  for (i in 0..n) dist[i][0] = i
  for (j in 0..m) dist[0][j] = j

  for (i in 1..n) {
    for (j in 1..m) {
      if (sourceArray[i - 1] == targetArray[j - 1]) {
        dist[i][j] = dist[i - 1][j - 1]
      } else {
        dist[i][j] = minOf(
          dist[i - 1][j] + 1,
          dist[i][j - 1] + 1,
          dist[i - 1][j - 1] + 1
        )
      }
    }
  }
  return dist[n][m]
}

// Similarity ratio (0...1)
fun String.similarity(to: String): Double {
  val maxLen = maxOf(this.length, to.length)
  if (maxLen == 0) return 1.0
  val dist = this.levenshteinDistance(to)
  return 1.0 - dist.toDouble() / maxLen.toDouble()
}
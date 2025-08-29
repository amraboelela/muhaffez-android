package app.amr.muhaffez

import androidx.compose.runtime.*

fun MuhaffezViewModel.updatePages() {
  val tempRightPage = SpannableStringBuilder()
  val tempLeftPage = SpannableStringBuilder()

  val firstIndex = foundAyat.firstOrNull() ?: return

  val quranModel = QuranModel.getInstance()
  var currentLineIndex = firstIndex
  var wordsInCurrentLine = wordsForLine(quranLines, currentLineIndex)
  var wordIndexInLine = 0

  fun advanceLine() {
    currentLineIndex += 1
    wordsInCurrentLine = wordsForLine(quranLines, currentLineIndex)
    wordIndexInLine = 0
  }

  fun add(separator: CharSequence) {
    if (quranModel.isRightPage(currentLineIndex)) {
      tempRightPage.append(separator)
    } else {
      tempLeftPage.append(separator)
    }
  }

  quranModel.updatePages(this, currentLineIndex)

  for ((_, pair) in matchedWords.withIndex()) {
    val (word, isMatched) = pair
    quranModel.updatePageModelsIfNeeded(this, currentLineIndex)

    if (isBeginningOfAya(wordIndexInLine)) {
      if (quranModel.isEndOfSurah(currentLineIndex - 1)) {
        add(surahSeparator(currentLineIndex))
        if (quranModel.isEndOfRub3(currentLineIndex - 1)) {
          add("⭐ ")
        }
      }
    }

    val attributedWord = attributedWord(word, isMatched)
    if (quranModel.isRightPage(currentLineIndex)) {
      tempRightPage.append(attributedWord)
    } else {
      tempLeftPage.append(attributedWord)
    }

    wordIndexInLine++
    add(" ")

    if (isEndOfAya(wordIndexInLine, wordsInCurrentLine.size)) {
      add("🌼 ")
      if (quranModel.isEndOfSurah(currentLineIndex)) {
        add("\n")
      }
      if (quranModel.isEndOfRub3(currentLineIndex) && !quranModel.isEndOfSurah(currentLineIndex)) {
        add("⭐ ")
      }
      advanceLine()
    }
  }

  rightPage = tempRightPage
  leftPage = tempLeftPage
}

// --- Helpers ---

private fun attributedWord(word: String, matched: Boolean): CharSequence {
  val spannable = SpannableStringBuilder(word)
  val color = if (matched) Color.BLACK else Color.RED
  spannable.setSpan(ForegroundColorSpan(color), 0, word.length, 0)
  spannable.setSpan(AbsoluteSizeSpan(35, true), 0, word.length, 0)
  return spannable
}

private fun wordsForLine(lines: List<String>, index: Int): List<String> {
  return if (index < lines.size) lines[index].split(" ") else emptyList()
}

private fun isBeginningOfAya(wordIndex: Int): Boolean {
  return wordIndex == 0
}

private fun isEndOfAya(wordIndex: Int, wordCount: Int): Boolean {
  return wordIndex >= wordCount
}

private fun surahSeparator(ayaIndex: Int): CharSequence {
  val quranModel = QuranModel.getInstance()
  val surahName = quranModel.surahName(ayaIndex)
  val spannable = SpannableStringBuilder("\n\t\t\t\t\tسورة $surahName\n\n")
  spannable.setSpan(AbsoluteSizeSpan(28, true), 0, spannable.length, 0)
  spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
  return spannable
}

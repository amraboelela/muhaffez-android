package app.amr.muhaffez

import android.text.SpannableStringBuilder
import androidx.compose.runtime.*
import android.text.style.ForegroundColorSpan
import android.text.style.AbsoluteSizeSpan
import android.graphics.Color
import android.text.style.UnderlineSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.sp

fun MuhaffezViewModel.updatePages() {
  if (pageMatchedWordsIndex >= matchedWords.size) {
    return
  }
  tempPage.text = AnnotatedString.Builder()
  var currentLineIndex = pageCurrentLineIndex
  var wordsInCurrentLine = wordsForLine(quranLines, currentLineIndex)
  var wordIndexInLine = 0
  val matchedWordsIndex = pageMatchedWordsIndex

  fun advanceLine() {
    if (quranModel.isRightPage(currentLineIndex)) {
      tempPage.pageType = PageType.RIGHT
      rightPage = tempPage.deepCopy()
    } else {
      tempPage.pageType = PageType.LEFT
      leftPage = tempPage.deepCopy()
    }
    currentLineIndex += 1
    wordsInCurrentLine = wordsForLine(quranLines, currentLineIndex)
    wordIndexInLine = 0
  }

  fun add(separator: CharSequence) {
    tempPage.text.append(separator)
  }

  quranModel.updatePages(this, currentLineIndex)

  for (i in matchedWordsIndex until matchedWords.size) {
    if (currentPageIsRight != quranModel.isRightPage(currentLineIndex)) {
      pageCurrentLineIndex = currentLineIndex
      pageMatchedWordsIndex = i
      tempPage.reset()
    }
    quranModel.updatePageModelsIfNeeded(this, currentLineIndex)

    if (isBeginningOfAya(wordIndexInLine)) {
      if (quranModel.isEndOfSurah(currentLineIndex - 1)) {
        add(surahSeparator(currentLineIndex))
      }
      if (quranModel.isEndOfRub3(currentLineIndex - 1)) {
        add("⭐ ")
      }
    }

    val attributedWord = attributedWord(matchedWords[i].first, matchedWords[i].second)
    tempPage.text.append(attributedWord)
    wordIndexInLine++
    add(" ")

    if (isEndOfAya(wordIndexInLine, wordsInCurrentLine.size)) {
      add("🌼 ")
      if (quranModel.isEndOfSurah(currentLineIndex)) {
        add("\n")
      }
      advanceLine()
    }
  }
  if (quranModel.isRightPage(currentLineIndex)) {
    if (leftPage.isEmpty) {
      tempPage.pageType = PageType.RIGHT
      rightPage = tempPage.deepCopy()
    }
  } else {
    tempPage.pageType = PageType.LEFT
    leftPage = tempPage.deepCopy()
  }
}

// --- Helpers ---

private fun attributedWord(word: String, matched: Boolean): AnnotatedString {
  return AnnotatedString(
    text = word,
    spanStyle = SpanStyle(
      color = if (matched) ComposeColor.Black else ComposeColor.Red
    )
  )
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

fun MuhaffezViewModel.surahSeparator(ayaIndex: Int): CharSequence {
  val surahName = quranModel.surahNameAt(ayaIndex)
  val spannable = SpannableStringBuilder("\n\t\t\t\t\tسورة $surahName\n\n")
  spannable.setSpan(AbsoluteSizeSpan(28, true), 0, spannable.length, 0)
  spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
  return spannable
}

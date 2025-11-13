package app.amr.muhaffez

import android.text.SpannableStringBuilder
import androidx.compose.runtime.*
import android.text.style.ForegroundColorSpan
import android.text.style.AbsoluteSizeSpan
import android.graphics.Color
import android.text.style.UnderlineSpan
import androidx.compose.ui.text.AnnotatedString

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
      rightPage = tempPage.deepCopy()
    } else {
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

  // Update tempPage info one final time before copying
  quranModel.updatePages(this, currentLineIndex)
  println("updatePages: before final copy tempPage - page=${tempPage.pageNumber}, surah=${tempPage.surahName}, juz=${tempPage.juzNumber}")

  // Update currentPageIsRight BEFORE updating the page objects
  updateCurrentPageIsRight(quranModel.isRightPage(currentLineIndex))

  if (quranModel.isRightPage(currentLineIndex)) {
    rightPage = tempPage.deepCopy()
    println("updatePages: final rightPage - page=${rightPage.pageNumber}, surah=${rightPage.surahName}, juz=${rightPage.juzNumber}")
  } else {
    leftPage = tempPage.deepCopy()
    println("updatePages: final leftPage - page=${leftPage.pageNumber}, surah=${leftPage.surahName}, juz=${leftPage.juzNumber}")
  }
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

fun MuhaffezViewModel.surahSeparator(ayaIndex: Int): CharSequence {
  val surahName = quranModel.surahNameFor(ayaIndex) //surahName(ayaIndex)
  val spannable = SpannableStringBuilder("\n\t\t\t\t\tسورة $surahName\n\n")
  spannable.setSpan(AbsoluteSizeSpan(28, true), 0, spannable.length, 0)
  spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
  return spannable
}

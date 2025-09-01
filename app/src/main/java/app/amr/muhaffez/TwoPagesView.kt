package app.amr.muhaffez

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.launch

@Composable
fun TwoPagesView(viewModel: MuhaffezViewModel) {
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()
  var scrollToPage by remember { mutableStateOf("RIGHT") }

  LaunchedEffect(viewModel.rightPage, viewModel.leftPage) {
    // Scroll to current page when text changes
    coroutineScope.launch {
      val screenWidth = 1080f // You may want to calculate screen width dynamically
      val offset = if (viewModel.currentPageIsRight) screenWidth else 0f
      scrollState.scrollTo(offset.toInt())
    }
  }

  LaunchedEffect(Unit) {
    // On appear scroll to RIGHT page initially
    val screenWidth = 1080f
    scrollState.scrollTo(screenWidth.toInt())

      // Scroll to the "RIGHT" page initially
    //  proxy.scrollTo("RIGHT", anchor = 0.5f) // Compose uses Float anchor 0f..1f

      // --- Testing moving page animation ---
      // viewModel.currentPageIsRight = true
      // delay(2000)
      // viewModel.currentPageIsRight = false
      // delay(2000)
      // viewModel.currentPageIsRight = true

      // --- Testing displaying Surah Al-Fateha ---
      viewModel.updateVoiceText("الحَمدُ لِلَّهِ رَبِّ العالَمينَ")
//
//      delay(200) // 0.2 seconds
//
//      viewModel.updateVoiceText("""
//        الحَمدُ لِلَّهِ رَبِّ العالَمينَ
//        الرَّحمٰنِ الرَّحيمِ
//        مالِكِ يَومِ الدّينِ
//        إِيّاكَ نَعبُدُ وَإِيّاكَ نَستَعينُ
//        اهدِنَا الصِّراطَ المُستَقيمَ
//        صِراطَ الَّذينَ أَنعَمتَ عَلَيهِم غَيرِ المَغضوبِ عَلَيهِم وَلَا الضّالّينَ
//
//        -
//        الم ذٰلِكَ الكِتابُ لا رَيبَ فيهِ هُدًى لِلمُتَّقينَ
//    """.trimIndent())
  }

  Row(
    modifier = Modifier
      .horizontalScroll(scrollState, enabled = true)
      .fillMaxSize()
  ) {
    PageView(pageModel = viewModel.leftPage, isRight = false, modifier = Modifier.width(360.dp))
    PageView(pageModel = viewModel.rightPage, isRight = true, modifier = Modifier.width(360.dp))
  }
}

@Composable
fun PageView(pageModel: PageModel, isRight: Boolean, modifier: Modifier = Modifier) {
  val opacity = 0.5f
  val shadowWidth = 4.dp

  Row(
    modifier = modifier.fillMaxHeight(),
    verticalAlignment = Alignment.Top
  ) {
    if (!isRight) {
      Box(
        modifier = Modifier
          .width(shadowWidth)
          .fillMaxHeight()
          .background(Color.Gray.copy(alpha = opacity))
          .padding(top = 10.dp)
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (true) { //(pageModel.pageNumber > 0) {
          Text("${pageModel.pageNumber}", fontSize = 20.sp)
          Spacer(modifier = Modifier.weight(1f))
          Text(pageModel.surahName, fontSize = 20.sp)
          Spacer(modifier = Modifier.weight(1f))
          Text("جزء ${pageModel.juzNumber}", fontSize = 20.sp)
        } else {
          Text("Nothing")
        }
      }

      Column(
        modifier = Modifier
          .fillMaxHeight()
          .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Spacer(modifier = Modifier.weight(1f))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Text(
            pageModel.annotatedString,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
          )
        }
        Spacer(modifier = Modifier.weight(1f))
      }
    }

    if (isRight) {
      Box(
        modifier = Modifier
          .width(shadowWidth)
          .fillMaxHeight()
          .background(Color.Gray.copy(alpha = opacity))
          .padding(top = 10.dp)
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun TwoPagesPreview() {
  MaterialTheme {
    TwoPagesView(viewModel = MuhaffezViewModel())
  }
}
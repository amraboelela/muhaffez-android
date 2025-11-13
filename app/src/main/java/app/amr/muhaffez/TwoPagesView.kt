package app.amr.muhaffez

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints

@Composable
fun TwoPagesView(viewModel: MuhaffezViewModel) {
  val scrollState = rememberScrollState()
  //val coroutineScope = rememberCoroutineScope()

  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp.dp
  val density = LocalDensity.current
  val screenWidthPx = with(density) { screenWidthDp.toPx() }

  LaunchedEffect(viewModel.currentPageIsRight, viewModel.rightPage, viewModel.leftPage) {
    // Wait for layout to complete by checking if scroll state is ready
    kotlinx.coroutines.delay(50)
    val offset = if (viewModel.currentPageIsRight) screenWidthPx else 0f
    scrollState.animateScrollTo(offset.toInt())
  }

  LaunchedEffect(Unit) {
    scrollState.scrollTo(screenWidthPx.toInt())
    //viewModel.updateVoiceText("الحَمدُ لِلَّهِ رَبِّ العالَمينَ")

    // --- Testing moving page animation ---
//     delay(2000)
//     viewModel.updateCurrentPageIsRight(false)
//     delay(2000)
//     viewModel.updateCurrentPageIsRight(true)

    // --- Testing displaying Surah Al-Fateha ---
    //viewModel.updateVoiceText("الحَمدُ لِلَّهِ رَبِّ العالَمينَ")
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
      .padding(bottom = 120.dp)  // Add padding to avoid mic button overlap
  ) {
    PageView(viewModel.leftPage, isRight = false, modifier = Modifier.width(screenWidthDp))
    PageView(viewModel.rightPage, isRight = true, modifier = Modifier.width(screenWidthDp))
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
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 30.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (pageModel.pageNumber > 0) {
          Text("${pageModel.pageNumber}", fontSize = 20.sp)
          Spacer(modifier = Modifier.weight(1f))
          Text(pageModel.surahName, fontSize = 20.sp)
          Spacer(modifier = Modifier.weight(1f))
          Text("جزء ${pageModel.juzNumber}", fontSize = 20.sp)
        } else {
          Text(" ")
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
          .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        if (pageModel.isFirstPage) {
          Spacer(modifier = Modifier.weight(1f))
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          AutoSizeText(
            text = pageModel.annotatedString,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp),
            textAlign = TextAlign.Start,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            minimumScaleFactor = 0.5f
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

@Composable
fun AutoSizeText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  textAlign: TextAlign = TextAlign.Start,
  fontSize: androidx.compose.ui.unit.TextUnit = 24.sp,
  lineHeight: androidx.compose.ui.unit.TextUnit = 32.sp,
  minimumScaleFactor: Float = 0.5f,
) {
  var adjustedFontSize by remember { mutableStateOf(fontSize) }
  val textMeasurer = rememberTextMeasurer()

  BoxWithConstraints(modifier = modifier) {
    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidth.toPx() }.toInt()
    val maxHeightPx = with(density) { maxHeight.toPx() }.toInt().takeIf { it > 0 } ?: Int.MAX_VALUE

    LaunchedEffect(text.text, maxWidthPx, maxHeightPx) {
      if (maxWidthPx <= 0) return@LaunchedEffect

      val minFont = fontSize.value * minimumScaleFactor
      var low = minFont
      var high = fontSize.value
      var bestFit = low

      while (high - low > 0.5f) {
        val test = (low + high) / 2f
        val result = textMeasurer.measure(
          text = text,
          style = TextStyle(
            fontSize = test.sp,
            lineHeight = (lineHeight.value * test / fontSize.value).sp,
            textAlign = textAlign
          ),
          constraints = Constraints(
            maxWidth = maxWidthPx,
            maxHeight = maxHeightPx
          )
        )

        // Check if text actually fits in the constraints
        // hasVisualOverflow checks if text was clipped due to maxLines or size constraints
        val fits = !result.hasVisualOverflow
        if (fits) {
          low = test
          bestFit = test
        } else {
          high = test
        }
      }

      adjustedFontSize = bestFit.sp
    }

    Text(
      text = text,
      fontSize = adjustedFontSize,
      lineHeight = (lineHeight.value * adjustedFontSize.value / fontSize.value).sp,
      textAlign = textAlign,
      modifier = Modifier.fillMaxWidth(),
      softWrap = true,
      maxLines = Int.MAX_VALUE
    )
  }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 600)
@Composable
fun TwoPagesPreview() {
  MaterialTheme {
    TwoPagesView(viewModel = MuhaffezViewModel())
  }
}
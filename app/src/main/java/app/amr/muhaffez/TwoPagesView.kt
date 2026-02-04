package app.amr.muhaffez

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit

@Composable
fun TwoPagesView(viewModel: MuhaffezViewModel) {
  val scrollState = rememberScrollState()

  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp.dp
  val density = LocalDensity.current
  val screenWidthPx = with(density) { screenWidthDp.toPx() }

  LaunchedEffect(viewModel.rightPage.textString, viewModel.leftPage.textString, viewModel.currentPageIsRight) {
    // Wait for layout to complete by checking if scroll state is ready
    kotlinx.coroutines.delay(50)
    val offset = when (viewModel.leftPage.pageType) {
      PageType.LEFT -> if (viewModel.currentPageIsRight) screenWidthPx else 0f
      PageType.PRE_LEFT -> if (viewModel.currentPageIsRight) 0f else screenWidthPx
      else -> 0f
    }
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

//    viewModel.updateVoiceText("""
//    انما يستجيب الذين يسمعون والموتى يبعثهم الله ثم اليه يرجعون
//        وقالوا لولا نزل عليه اية من ربه قل ان الله قادر على ان ينزل اية ولكن اكثرهم لا يعلمون
//    وما من دابة في الارض ولا طائر يطير بجناحيه الا امم امثالكم ما فرطنا في الكتاب من شيء ثم الى ربهم يحشرون
//        والذين كذبوا باياتنا صم وبكم في الظلمات من يشا الله يضلله ومن يشا يجعله على صراط مستقيم
//    قل ارايتكم ان اتاكم عذاب الله او اتتكم الساعة اغير الله تدعون ان كنتم صادقين
//    بل اياه تدعون فيكشف ما تدعون اليه ان شاء وتنسون ما تشركون
//        ولقد ارسلنا الى امم من قبلك فاخذناهم بالباساء والضراء لعلهم يتضرعون
//    فلولا اذ جاءهم باسنا تضرعوا ولكن قست قلوبهم وزين لهم الشيطان ما كانوا يعملون
//        فلما نسوا ما ذكروا به فتحنا عليهم ابواب كل شيء حتى اذا فرحوا
//        بما اوتوا اخذناهم بغتة فاذا هم مبلسون
//  """.trimIndent())

    // فقطع دابر القوم الذين ظلموا والحمد لله رب العالمين
    // بما اوتوا اخذناهم بغتة فاذا هم مبلسون
  }
  Row(
    modifier = Modifier
      .horizontalScroll(scrollState, enabled = true)
      .fillMaxSize()
      .padding(bottom = 90.dp)  // Add padding to avoid mic button overlap
  ) {
    if (viewModel.leftPage.pageType == PageType.LEFT) {
      PageView(viewModel.leftPage, isRight = false, modifier = Modifier.width(screenWidthDp))
    }
    PageView(viewModel.rightPage, isRight = true, modifier = Modifier.width(screenWidthDp))
    if (viewModel.leftPage.pageType == PageType.PRE_LEFT) {
      PageView(viewModel.leftPage, isRight = false, modifier = Modifier.width(screenWidthDp))
    }
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
          .padding(top = 30.dp, start = 8.dp, end = 8.dp),
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
  fontSize: TextUnit = 24.sp,
  lineHeight: TextUnit = 32.sp,
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

        // Add a small buffer to account for font descent and rounding issues
        val fits = result.size.width <= maxWidthPx && result.size.height < maxHeightPx - 45
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

package app.amr.muhaffez

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.MutableLiveData
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@Composable
fun MuhaffezView(viewModel: MuhaffezViewModel, recognizer: ArabicSpeechRecognizer) {
  val context = LocalContext.current
  val sharedPrefs = remember {
    context.getSharedPreferences("MuhaffezPrefs", Context.MODE_PRIVATE)
  }

  val recognizedText by recognizer.voiceText.observeAsState("")
  var hasSeenTip by remember {
    mutableStateOf(sharedPrefs.getBoolean("hasSeenTip", false))
  }

  LaunchedEffect(Unit) {
    // Use this for testing rub3 mark before
    //viewModel.updateVoiceText("ذٰلِكَ بِأَنَّ اللَّهَ نَزَّلَ الكِتابَ بِالحَقِّ وَإِنَّ الَّذينَ اختَلَفوا فِي الكِتابِ لَفي شِقاقٍ بَعيدٍ")
  }

  LaunchedEffect(recognizedText) {
    println("recognizedText: $recognizedText")
    viewModel.updateVoiceText(recognizedText)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    if (viewModel.matchedWords.isEmpty() && viewModel.voiceText.isEmpty() && !hasSeenTip) {
      // Empty state - show instruction message like iOS
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
      ) {
        // Instruction card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color(0xFF007AFF) // iOS blue
          ),
          shape = MaterialTheme.shapes.large
        ) {
          Text(
            text = "Tap here and start reciting from the Quran",
            modifier = Modifier
              .padding(horizontal = 20.dp, vertical = 16.dp)
              .fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
          )
        }

        // Arrow pointing down to mic
        Icon(
          imageVector = Icons.Default.ArrowDownward,
          contentDescription = null,
          tint = Color(0xFF007AFF),
          modifier = Modifier
            .size(32.dp)
            //.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))
      }
    } else if (viewModel.matchedWords.isEmpty() && viewModel.voiceText.isNotEmpty()) {
      // Loading state
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF007AFF), strokeWidth = 4.dp)
      }
    } else {
      // Content state - show Quran pages
      TwoPagesView(viewModel)
    }

    // Mic button - positioned at bottom center like iOS
    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 50.dp)
        .size(56.dp),
      shape = CircleShape,
      color = Color.White,
      shadowElevation = 2.dp,
      tonalElevation = 0.dp,
      onClick = {
        // Save that user has seen the tip
        if (!hasSeenTip) {
          sharedPrefs.edit().putBoolean("hasSeenTip", true).apply()
          hasSeenTip = true
        }

        if (viewModel.isRecording) {
          recognizer.stopRecording()
        } else {
          viewModel.resetData()
          recognizer.startRecording()
        }
        viewModel.isRecording = !viewModel.isRecording
      },
      interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (viewModel.isRecording) Icons.Default.Mic else Icons.Default.MicOff,
          contentDescription = "Mic",
          tint = if (viewModel.isRecording) Color.Red else Color(0xFF007AFF),
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

// Note: Preview is not available for this view because ArabicSpeechRecognizer
// requires Android runtime classes that aren't available in the preview environment.
// Please run the app on a device or emulator to see the UI.

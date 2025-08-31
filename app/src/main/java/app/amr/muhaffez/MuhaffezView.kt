package app.amr.muhaffez

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import app.amr.muhaffez.ui.theme.MuhaffezTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun MuhaffezView(viewModel: MuhaffezViewModel, recognizer: ArabicSpeechRecognizer) {
  val recognizedText by recognizer.voiceText.observeAsState("")
  LaunchedEffect(Unit) {
    // Use this for testing rub3 mark before
    viewModel.updateVoiceText("ذٰلِكَ بِأَنَّ اللَّهَ نَزَّلَ الكِتابَ بِالحَقِّ وَإِنَّ الَّذينَ اختَلَفوا فِي الكِتابِ لَفي شِقاقٍ بَعيدٍ")
  }

  LaunchedEffect(recognizedText) {
    viewModel.updateVoiceText(recognizedText)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (viewModel.matchedWords.isEmpty() && viewModel.voiceText.isNotEmpty()) {
      // Show progress indicator if text received but no matches yet
      Spacer(modifier = Modifier.height(16.dp))
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.Blue, strokeWidth = 4.dp)
      }
      Spacer(modifier = Modifier.height(16.dp))
    } else {
      TwoPagesView(viewModel)
    }
    IconButton(
      onClick = {
        if (viewModel.isRecording) {
          recognizer.stopRecording()
        } else {
          viewModel.resetData()
          recognizer.startRecording()
        }
        viewModel.isRecording = !viewModel.isRecording
      },
      modifier = Modifier
        .size(60.dp)
        .background(Color(0xFFEFEFEF), CircleShape)
        .shadow(4.dp, CircleShape)
    ) {
      Icon(
        imageVector = if (viewModel.isRecording) Icons.Default.Mic else Icons.Default.MicOff,
        contentDescription = "Mic",
        tint = if (viewModel.isRecording) Color.Red else Color.Blue
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MuhaffezViewPreview() {
  val dummyRecognizer = ArabicSpeechRecognizer(context = LocalContext.current)
  MuhaffezTheme {
    MuhaffezView(
      viewModel = MuhaffezViewModel(),
      recognizer = dummyRecognizer
    )
  }
}
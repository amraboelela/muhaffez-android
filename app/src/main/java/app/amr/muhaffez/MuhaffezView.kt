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

@Composable
fun MuhaffezView(viewModel: MuhaffezViewModel, recognizer: ArabicSpeechRecognizer) {
  // Observe speech recognition result
  LaunchedEffect(recognizer.voiceText) {
    recognizer.voiceText.observeForever {
      viewModel.voiceText.value = it
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (viewModel.matchedWords.value.isEmpty() && viewModel.voiceText.value.isNotEmpty()) {
        // Show progress indicator if text received but no matches yet
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = Color.Blue, strokeWidth = 4.dp)
        }
      } else {
        TwoPagesView(viewModel) // Placeholder for your two-page UI
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Mic button
    IconButton(
      onClick = {
        if (viewModel.isRecording.value) {
          recognizer.stopRecording()
        } else {
          viewModel.resetData()
          recognizer.startRecording()
        }
        viewModel.isRecording.value = !viewModel.isRecording.value
      },
      modifier = Modifier
        .size(60.dp)
        .background(Color(0xFFEFEFEF), CircleShape)
        .shadow(4.dp, CircleShape)
    ) {
      Icon(
        imageVector = if (viewModel.isRecording.value) Icons.Default.Mic else Icons.Default.MicOff,
        contentDescription = "Mic",
        tint = if (viewModel.isRecording.value) Color.Red else Color.Blue
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
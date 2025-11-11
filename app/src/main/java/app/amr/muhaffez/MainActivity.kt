package app.amr.muhaffez

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import app.amr.muhaffez.ui.theme.MuhaffezTheme

class MainActivity : ComponentActivity() {

  private lateinit var recognizer: ArabicSpeechRecognizer
  private lateinit var viewModel: MuhaffezViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    QuranModel.initialize(this)
    recognizer = ArabicSpeechRecognizer(this)
    viewModel = MuhaffezViewModel(this)  // Pass context for ML model initialization

    // Permission request for mic
    val requestPermissionLauncher = registerForActivityResult(
      ActivityResultContracts.RequestPermission()
    ) { isGranted ->
      setContent {
        MuhaffezTheme {
          if (isGranted) {
            MuhaffezView(viewModel, recognizer)
          } else {
            PermissionDeniedMessage()
          }
        }
      }
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
      == PackageManager.PERMISSION_GRANTED
    ) {
      setContent {
        MuhaffezTheme {
          MuhaffezView(viewModel, recognizer)
        }
      }
    } else {
      requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }
}

@Composable
fun PermissionDeniedMessage() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Text("Microphone permission is required for speech recognition.")
  }
}

@Preview(showBackground = true)
@Composable
fun PermissionDeniedMessagePreview() {
    MuhaffezTheme {
      PermissionDeniedMessage()
    }
}
package app.amr.muhaffez

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import app.amr.muhaffez.ui.theme.MuhaffezTheme

class MainActivity : ComponentActivity() {

  private lateinit var recognizer: ArabicSpeechRecognizer
  private lateinit var viewModel: MuhaffezViewModel
  private var wasRecordingBeforeBackground = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Enable edge-to-edge and set status bar with dark icons
    enableEdgeToEdge()
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = true  // Dark icons on light background
    }

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
            MuhaffezView(viewModel, recognizer, ::setKeepScreenOn)
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
          MuhaffezView(viewModel, recognizer, ::setKeepScreenOn)
        }
      }
    } else {
      requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  // Keep screen on while recording (equivalent to UIApplication.shared.isIdleTimerDisabled)
  fun setKeepScreenOn(keepOn: Boolean) {
    if (keepOn) {
      window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  override fun onPause() {
    super.onPause()
    // Save recording state and stop recording when app goes to background
    wasRecordingBeforeBackground = viewModel.isRecording
    if (wasRecordingBeforeBackground) {
      recognizer.stopRecording()
      viewModel.isRecording = false
      setKeepScreenOn(false)
      println("MainActivity: Stopped recording due to app going to background")
    }
  }

  override fun onResume() {
    super.onResume()
    // Optionally resume recording when app comes back to foreground
    // Commented out for now - user can manually restart if needed
    // if (wasRecordingBeforeBackground) {
    //   recognizer.startRecording()
    //   viewModel.isRecording = true
    //   println("MainActivity: Resumed recording as app came to foreground")
    // }
  }

  override fun onDestroy() {
    super.onDestroy()
    recognizer.destroy()
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
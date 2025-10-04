package com.example.visiongpt

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.visiongpt.ui.VisionGptApp
import com.example.visiongpt.ui.VisionGptViewModel
import com.example.visiongpt.ui.theme.VisionGptTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VisionGptViewModel = hiltViewModel()
            VisionGptTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text(text = "VisionGPT") }) }) { innerPadding ->
                    VisionGptApp(
                        modifier = Modifier.padding(innerPadding),
                        onStartListening = { viewModel.onStartListening(this@MainActivity) },
                        onCameraClicked = {}
                    )
                }
            }
        }
    }
}
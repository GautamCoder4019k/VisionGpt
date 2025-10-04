package com.example.visiongpt.ui

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.camera.core.CameraXThreads.TAG
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legalease.ui.message.audioToText.SpeechToTextHelper
import com.example.visiongpt.data.ImageRepository
import com.example.visiongpt.ui.cameraX.CameraXHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.io.File
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class VisionGptViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ImageRepository,
) :
    ViewModel() {
    val inputText = mutableStateOf("")

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val speechToTextHelper = SpeechToTextHelper(context)
    var isListening = mutableStateOf(false)
    private val cameraXHelper = CameraXHelper(context)

    private val filePath = mutableStateOf<File?>(null)

    fun onStartListening(lifecycleOwner: LifecycleOwner) {
        onInputChange("")
        _uiState.value = UiState.Listening
        isListening.value = true
        // Capture photo first
        cameraXHelper.startCamera(lifecycleOwner) { photoPath ->
            filePath.value = photoPath
            // Handle photo path, e.g., store it in a variable or process it
            println("Photo captured at: $photoPath")
        }

        // Then start listening for speech
        speechToTextHelper.startListening(
            onResult = {
                inputText.value = it
                _uiState.value = UiState.Loading
//                uploadImage(it, filePath = filePath.value)
                isListening.value = false
            },
            onError = {
                _uiState.value = UiState.Loading
                inputText.value = it
                isListening.value = false
            }
        )
    }

    val uploadResult = mutableStateOf<String?>(null)

    @SuppressLint("RestrictedApi")
    fun uploadImage(text: String, filePath: File?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (filePath != null) {
                    val response =
                        repository.uploadImageAndGetText(
                            filePath,
                            text
                        )
                    onInputChange(response)
                    speak(response)
                    Log.d(TAG, "uploadImage:${response} ")
                }
            } catch (e: Exception) {
                Log.d("TAG", "uploadImage: ${e.message}")
            }
        }
    }

    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }
    }


    private fun speak(text: String) {
        _uiState.value = UiState.Speaking
        viewModelScope.launch(Dispatchers.IO) {
            if (text.isNotEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts.stop()
        tts.shutdown()
    }

    //reinitialize viewmodel
    fun clear() {
        onInputChange("")
        onCleared()
    }


    fun onInputChange(text: String) {
        inputText.value = text
    }
}

sealed class UiState {
    data object Speaking : UiState()
    data object Listening : UiState()
    data object Loading : UiState()
}

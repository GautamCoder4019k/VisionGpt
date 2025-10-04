package com.example.visiongpt.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.legalease.ui.message.audioToText.RequestMicrophonePermission
import com.example.visiongpt.R
import com.example.visiongpt.ui.theme.LightGray

@Composable
fun VisionGptApp(
    modifier: Modifier = Modifier,
    onStartListening: () -> Unit,
    onCameraClicked: () -> Unit
) {
    val viewModel: VisionGptViewModel = hiltViewModel()
    val items by remember {
        mutableStateOf(emptyList<String>())
    }
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState()

    RequestMicrophonePermission { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                "Microphone permission is required to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (items.isNotEmpty()) {
            //TODO
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (uiState.value) {
                    is UiState.Loading -> {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                        )
                    }

                    is UiState.Listening -> {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize(0.4f)
                                    .alpha(0.8f),
                                tint = Color.LightGray
                            )
                            Text(text = "Listening", fontSize = 24.sp)
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.speaking),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize(0.4f)
                                    .alpha(0.8f),
                                tint = Color.LightGray
                            )
                            Text(text = "Response", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            value = viewModel.inputText.value,
            onValueChange = { viewModel.onInputChange(it) },
            leadingIcon = {
                IconButton(onClick = onStartListening) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "microphone",
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.clear() }) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(36.dp)
                    )
                }

            },
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = LightGray,
                focusedContainerColor = LightGray,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
}


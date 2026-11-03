package com.example.kickboardconnectiontest.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kickboardconnectiontest.screens.ConnectedScreen
import com.example.kickboardconnectiontest.screens.ReadyScreen
import com.example.kickboardconnectiontest.screens.StartScreen
import com.example.kickboardconnectiontest.viewmodel.KickboardConnectionViewModel
import kotlinx.coroutines.delay

val PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION
)

fun haveAllPermissions(context: Context) =
    PERMISSIONS
        .all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

@SuppressLint("MissingPermission")
@Composable
fun MainNavigation(viewModel: KickboardConnectionViewModel = viewModel()) {

    val context = LocalContext.current
    var allPermissionsGranted by remember {
        mutableStateOf(haveAllPermissions(context))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.all { it }) {
            allPermissionsGranted = true
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showStartScreen by remember { mutableStateOf(true) }
    var showReadyScreen by remember { mutableStateOf(false) } // 처음에는 false로 설정

    LaunchedEffect(key1 = showStartScreen) {
        if (showStartScreen) {
            delay(2000L) // Delay for 2 seconds
            showStartScreen = false
            showReadyScreen = true // Show ReadyScreen after StartScreen
        }
    }

    LaunchedEffect(key1 = showReadyScreen) {
        if (showReadyScreen) {
            delay(2000L) // Delay for 2 seconds
        }
    }

    if (!uiState.isConnectionSuccessful) {
        AnimatedVisibility(
            visible = showStartScreen,
            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
            // Apply fade-out animation with duration
        ) {
            if (showStartScreen) {
                StartScreen()
            }
        }

        AnimatedVisibility(
            visible = showReadyScreen,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000))
            // Apply fade-in animation with duration
        ) {
            if (showReadyScreen) {
                ReadyScreen(
                    startConnection = {
                        if(!allPermissionsGranted) {
                            launcher.launch(PERMISSIONS)
                        }
                        else {
                            if(!uiState.isInitialized) viewModel.initializeConnectionObjects()
                            viewModel.startDeviceConnect()
                        }
                    },
                    sequenceRunning = uiState.isSequenceRunning,
                    nick = viewModel.nickname.value,
                    setNick = viewModel::setNickName,
                    device = viewModel.deviceNumber.value,
                    setDevice = viewModel::setDeviceName
                )
            }
        }
    } else {
        ConnectedScreen(
            disconnectFunc = viewModel::stopDeviceConnect,
            penPoint = uiState.debugingString!!
        )
    }
}

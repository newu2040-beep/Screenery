package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AudioSourceOption
import com.example.data.model.RecordingItem
import com.example.service.ScreenRecordingService
import com.example.ui.components.CountdownOverlay
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.QuickSettingsSheet
import com.example.ui.components.RenameDialog
import com.example.ui.components.TrimmingDialog
import com.example.ui.components.VideoDetailsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ScreeneryBg
import com.example.ui.theme.ScreeneryPillActiveBg
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreenerySurface
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ValidationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

enum class ScreenTab {
    Home, Recordings, Settings
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val autoStartPrompt = intent.getBooleanExtra("auto_start_prompt", false)

        setContent {
            MyApplicationTheme {
                ScreeneryApp(
                    initialAutoStartPrompt = autoStartPrompt
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreeneryApp(
    initialAutoStartPrompt: Boolean = false,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(ScreenTab.Home) }
    var selectedPlayerItem by remember { mutableStateOf<RecordingItem?>(null) }
    var selectedTrimmingItem by remember { mutableStateOf<RecordingItem?>(null) }
    var selectedDetailsItem by remember { mutableStateOf<RecordingItem?>(null) }
    var selectedRenameItem by remember { mutableStateOf<RecordingItem?>(null) }
    var selectedDeleteItem by remember { mutableStateOf<RecordingItem?>(null) }

    var showQuickSettingsSheet by remember { mutableStateOf(false) }
    var countdownRemaining by remember { mutableIntStateOf(0) }
    var pendingProjectionData by remember { mutableStateOf<Pair<Int, Intent>?>(null) }

    val recordingStatus by viewModel.recordingStatus.collectAsState()
    val deviceCapability by viewModel.deviceCapability.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val allRecordings by viewModel.allRecordings.collectAsState()
    val recentRecordings by viewModel.recentRecordings.collectAsState()
    val isTrimming by viewModel.isTrimming.collectAsState()
    val trimProgress by viewModel.trimProgress.collectAsState()

    // MediaProjection screen capture launcher
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val resultCode = result.resultCode
            val data = result.data!!
            if (currentConfig.countdownSeconds > 0) {
                pendingProjectionData = Pair(resultCode, data)
                countdownRemaining = currentConfig.countdownSeconds
            } else {
                startServiceWithProjection(context, resultCode, data)
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Screen capture permission was cancelled")
            }
        }
    }

    // Permission launcher for Mic and Notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val audioGranted = grants[Manifest.permission.RECORD_AUDIO] ?: true
        if (audioGranted) {
            // Proceed to MediaProjection request
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission is needed to record audio")
            }
        }
    }

    fun checkPermissionsAndRecord() {
        // Validation check (storage + encoder)
        when (val validation = viewModel.validateBeforeRecording()) {
            is ValidationResult.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(validation.message)
                }
                return
            }
            ValidationResult.Success -> {}
        }

        val neededPermissions = mutableListOf<String>()
        if (currentConfig.audioSource != AudioSourceOption.NONE) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.RECORD_AUDIO)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (neededPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
        }
    }

    // Handle auto start request if triggered from Quick Settings Tile
    LaunchedEffect(initialAutoStartPrompt) {
        if (initialAutoStartPrompt && !recordingStatus.isRecording) {
            checkPermissionsAndRecord()
        }
    }

    // Handle countdown timer before recording starts
    LaunchedEffect(countdownRemaining) {
        if (countdownRemaining > 0) {
            delay(1000)
            countdownRemaining -= 1
            if (countdownRemaining == 0 && pendingProjectionData != null) {
                val (resultCode, data) = pendingProjectionData!!
                startServiceWithProjection(context, resultCode, data)
                pendingProjectionData = null
            }
        }
    }

    // Function to share recording using FileProvider
    fun shareRecording(item: RecordingItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                scope.launch { snackbarHostState.showSnackbar("Video file not found on disk") }
                return
            }
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Recording"))
        } catch (e: Exception) {
            scope.launch { snackbarHostState.showSnackbar("Unable to share video: ${e.localizedMessage}") }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (selectedPlayerItem == null) {
                    NavigationBar(
                        containerColor = ScreenerySurface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.Home,
                            onClick = { currentTab = ScreenTab.Home },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ScreeneryPrimary,
                                selectedTextColor = ScreeneryPrimary,
                                indicatorColor = ScreeneryPillActiveBg,
                                unselectedIconColor = ScreeneryTextSecondary,
                                unselectedTextColor = ScreeneryTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_home")
                        )
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.Recordings,
                            onClick = { currentTab = ScreenTab.Recordings },
                            icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Recordings") },
                            label = { Text("Recordings", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ScreeneryPrimary,
                                selectedTextColor = ScreeneryPrimary,
                                indicatorColor = ScreeneryPillActiveBg,
                                unselectedIconColor = ScreeneryTextSecondary,
                                unselectedTextColor = ScreeneryTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_recordings")
                        )
                        NavigationBarItem(
                            selected = currentTab == ScreenTab.Settings,
                            onClick = { currentTab = ScreenTab.Settings },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                            label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ScreeneryPrimary,
                                selectedTextColor = ScreeneryPrimary,
                                indicatorColor = ScreeneryPillActiveBg,
                                unselectedIconColor = ScreeneryTextSecondary,
                                unselectedTextColor = ScreeneryTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_settings")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // If player is open, show PlayerScreen
                if (selectedPlayerItem != null) {
                    PlayerScreen(
                        item = selectedPlayerItem!!,
                        onBack = { selectedPlayerItem = null },
                        onTrim = {
                            selectedTrimmingItem = selectedPlayerItem
                        },
                        onShare = {
                            shareRecording(selectedPlayerItem!!)
                        },
                        onDelete = {
                            selectedDeleteItem = selectedPlayerItem
                        },
                        onDetails = {
                            selectedDetailsItem = selectedPlayerItem
                        }
                    )
                } else {
                    when (currentTab) {
                        ScreenTab.Home -> {
                            HomeScreen(
                                config = currentConfig,
                                deviceCapability = deviceCapability,
                                recordingStatus = recordingStatus,
                                recentRecordings = recentRecordings,
                                onStartRecordingClick = { checkPermissionsAndRecord() },
                                onPauseResumeClick = {
                                    if (recordingStatus.isPaused) viewModel.resumeRecording() else viewModel.pauseRecording()
                                },
                                onStopRecordingClick = {
                                    viewModel.stopRecording()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Recording saved to gallery")
                                    }
                                },
                                onSeeAllQuickSettings = { showQuickSettingsSheet = true },
                                onSeeAllRecordings = { currentTab = ScreenTab.Recordings },
                                onPlayRecording = { item -> selectedPlayerItem = item },
                                onTrimRecording = { item -> selectedTrimmingItem = item },
                                onShareRecording = { item -> shareRecording(item) },
                                onDeleteRecording = { item -> selectedDeleteItem = item },
                                onDetailsRecording = { item -> selectedDetailsItem = item }
                            )
                        }
                        ScreenTab.Recordings -> {
                            RecordingsScreen(
                                recordings = allRecordings,
                                onPlay = { item -> selectedPlayerItem = item },
                                onTrim = { item -> selectedTrimmingItem = item },
                                onRename = { item -> selectedRenameItem = item },
                                onShare = { item -> shareRecording(item) },
                                onDelete = { item -> selectedDeleteItem = item },
                                onDetails = { item -> selectedDetailsItem = item }
                            )
                        }
                        ScreenTab.Settings -> {
                            SettingsScreen(
                                currentConfig = currentConfig,
                                deviceCapability = deviceCapability,
                                onConfigChanged = { updated ->
                                    viewModel.updateConfig(updated)
                                },
                                onBack = { currentTab = ScreenTab.Home }
                            )
                        }
                    }
                }
            }
        }

        // Quick Settings Bottom Sheet
        if (showQuickSettingsSheet) {
            QuickSettingsSheet(
                config = currentConfig,
                deviceCapability = deviceCapability,
                onConfigChanged = { updated -> viewModel.updateConfig(updated) },
                onOpenFullSettings = { currentTab = ScreenTab.Settings },
                onDismiss = { showQuickSettingsSheet = false }
            )
        }

        // Details Bottom Sheet
        selectedDetailsItem?.let { item ->
            VideoDetailsDialog(
                item = item,
                onDismiss = { selectedDetailsItem = null }
            )
        }

        // Trimming Dialog
        selectedTrimmingItem?.let { item ->
            TrimmingDialog(
                item = item,
                isTrimming = isTrimming,
                trimProgress = trimProgress,
                onTrim = { startMs, endMs ->
                    viewModel.trimRecording(item, startMs, endMs) { success, newItem ->
                        if (success && newItem != null) {
                            selectedTrimmingItem = null
                            selectedPlayerItem = newItem
                            scope.launch {
                                snackbarHostState.showSnackbar("Trimmed video saved: ${newItem.title}")
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to trim video")
                            }
                        }
                    }
                },
                onDismiss = { selectedTrimmingItem = null }
            )
        }

        // Rename Dialog
        selectedRenameItem?.let { item ->
            RenameDialog(
                currentTitle = item.title,
                onConfirm = { newTitle ->
                    viewModel.renameRecording(item.id, newTitle)
                    selectedRenameItem = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Renamed to $newTitle")
                    }
                },
                onDismiss = { selectedRenameItem = null }
            )
        }

        // Delete Dialog
        selectedDeleteItem?.let { item ->
            DeleteConfirmationDialog(
                itemTitle = item.title,
                onConfirm = {
                    viewModel.deleteRecording(item)
                    if (selectedPlayerItem?.id == item.id) {
                        selectedPlayerItem = null
                    }
                    selectedDeleteItem = null
                    scope.launch {
                        snackbarHostState.showSnackbar("Recording deleted")
                    }
                },
                onDismiss = { selectedDeleteItem = null }
            )
        }

        // Countdown Overlay
        CountdownOverlay(
            count = countdownRemaining,
            onFinished = { /* handled in LaunchedEffect */ }
        )
    }
}

private fun startServiceWithProjection(context: Context, resultCode: Int, data: Intent) {
    val serviceIntent = Intent(context, ScreenRecordingService::class.java).apply {
        action = ScreenRecordingService.ACTION_START
        putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, resultCode)
        putExtra(ScreenRecordingService.EXTRA_DATA_INTENT, data)
    }
    ContextCompat.startForegroundService(context, serviceIntent)
}

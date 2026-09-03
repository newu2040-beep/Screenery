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
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.AppThemeMode
import com.example.data.model.AudioSourceOption
import com.example.data.model.RecordingItem
import com.example.service.ScreenRecordingService
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.ProfileEditDialog
import com.example.ui.components.RenameDialog
import com.example.ui.components.TrimmingDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ValidationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val mediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var pendingRecordingIntent: Intent? = null
    private var isIncomingShareIntent: Boolean = false

    // Media Projection Permission Launcher
    private val projectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startRecordingServiceWithProjection(result.resultCode, result.data!!)
        } else {
            Toast.makeText(this, "Screen capture permission was cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio & Notification Permissions Launcher
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val cfg = viewModel.currentConfig.value
        val needsAudio = cfg.audioSource != AudioSourceOption.NONE

        if (needsAudio && !audioGranted) {
            Toast.makeText(this, "Audio permission is required for microphone recording", Toast.LENGTH_SHORT).show()
        }
        // Proceed to media projection prompt
        launchMediaProjectionRequest()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val currentConfig by viewModel.currentConfig.collectAsStateWithLifecycle()
            val deviceCapability by viewModel.deviceCapability.collectAsStateWithLifecycle()
            val recordingStatus by viewModel.recordingStatus.collectAsStateWithLifecycle()
            val recentRecordings by viewModel.recentRecordings.collectAsStateWithLifecycle()
            val allRecordings by viewModel.allRecordings.collectAsStateWithLifecycle()
            val isTrimming by viewModel.isTrimming.collectAsStateWithLifecycle()
            val trimProgress by viewModel.trimProgress.collectAsStateWithLifecycle()

            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            var itemToRename by remember { mutableStateOf<RecordingItem?>(null) }
            var itemToDelete by remember { mutableStateOf<RecordingItem?>(null) }
            var itemToTrim by remember { mutableStateOf<RecordingItem?>(null) }
            var activePlayingItem by remember { mutableStateOf<RecordingItem?>(null) }
            var showProfileDialog by remember { mutableStateOf(false) }

            // Countdown state
            var countdownNumber by remember { mutableIntStateOf(0) }
            var isCountingDown by remember { mutableStateOf(false) }

            // Handle incoming share sheet trigger
            LaunchedEffect(isIncomingShareIntent) {
                if (isIncomingShareIntent) {
                    isIncomingShareIntent = false
                    snackbarHostState.showSnackbar(
                        message = "Shared to Screenery — Starting recording with saved settings...",
                        duration = SnackbarDuration.Short
                    )
                    delay(500)
                    checkPermissionsAndRecord()
                }
            }

            MyApplicationTheme(
                themeMode = currentConfig.themeMode,
                pastelTheme = currentConfig.pastelTheme,
                isCompactMode = currentConfig.isCompactMode
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_scaffold")
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("home") {
                                HomeScreen(
                                    config = currentConfig,
                                    deviceCapability = deviceCapability,
                                    recordingStatus = recordingStatus,
                                    recentRecordings = recentRecordings,
                                    onStartRecordingClick = {
                                        if (currentConfig.countdownSeconds > 0) {
                                            isCountingDown = true
                                            countdownNumber = currentConfig.countdownSeconds
                                        } else {
                                            checkPermissionsAndRecord()
                                        }
                                    },
                                    onPauseResumeClick = {
                                        if (recordingStatus.isPaused) {
                                            viewModel.resumeRecording()
                                        } else {
                                            viewModel.pauseRecording()
                                        }
                                    },
                                    onStopRecordingClick = {
                                        viewModel.stopRecording()
                                        scope.launch {
                                            val msg = if (currentConfig.autoSaveToGallery) {
                                                "Recording stopped & saved to Phone Gallery!"
                                            } else {
                                                "Recording saved successfully!"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    },
                                    onEditProfileClick = { showProfileDialog = true },
                                    onToggleThemeModeClick = {
                                        val nextMode = when (currentConfig.themeMode) {
                                            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                                            AppThemeMode.LIGHT -> AppThemeMode.DARK
                                            AppThemeMode.DARK -> AppThemeMode.SYSTEM
                                        }
                                        viewModel.updateThemeMode(nextMode)
                                    },
                                    onToggleCompactModeClick = {
                                        val newCompact = !currentConfig.isCompactMode
                                        viewModel.toggleCompactMode(newCompact)
                                        scope.launch {
                                            val modeText = if (newCompact) "Compact Mode: Tight screen fit enabled" else "Spacious Mode: Expressive layout enabled"
                                            snackbarHostState.showSnackbar(modeText)
                                        }
                                    },
                                    onSeeAllQuickSettings = { navController.navigate("settings") },
                                    onSeeAllRecordings = { navController.navigate("recordings") },
                                    onPlayRecording = { item ->
                                        activePlayingItem = item
                                        navController.navigate("player")
                                    },
                                    onTrimRecording = { item -> itemToTrim = item },
                                    onShareRecording = { item -> shareRecordingItem(item) },
                                    onDeleteRecording = { item -> itemToDelete = item },
                                    onDetailsRecording = { item ->
                                        activePlayingItem = item
                                        navController.navigate("player")
                                    }
                                )
                            }

                            composable("recordings") {
                                RecordingsScreen(
                                    recordings = allRecordings,
                                    onPlay = { item ->
                                        activePlayingItem = item
                                        navController.navigate("player")
                                    },
                                    onTrim = { item -> itemToTrim = item },
                                    onRename = { item -> itemToRename = item },
                                    onShare = { item -> shareRecordingItem(item) },
                                    onDelete = { item -> itemToDelete = item },
                                    onDetails = { item ->
                                        activePlayingItem = item
                                        navController.navigate("player")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    currentConfig = currentConfig,
                                    deviceCapability = deviceCapability,
                                    onConfigChanged = { updated -> viewModel.updateConfig(updated) },
                                    onEditProfileClick = { showProfileDialog = true },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("player") {
                                activePlayingItem?.let { item ->
                                    PlayerScreen(
                                        item = item,
                                        onBack = { navController.popBackStack() },
                                        onTrim = { itemToTrim = item },
                                        onShare = { shareRecordingItem(item) },
                                        onDelete = { itemToDelete = item },
                                        onDetails = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("${item.title} • ${item.resolutionString} • ${item.formattedSize}")
                                            }
                                        }
                                    )
                                } ?: run {
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }

                        // Fullscreen Countdown Overlay
                        if (isCountingDown) {
                            CountdownOverlay(
                                number = countdownNumber,
                                onTick = {
                                    if (countdownNumber > 1) {
                                        countdownNumber--
                                    } else {
                                        isCountingDown = false
                                        checkPermissionsAndRecord()
                                    }
                                },
                                onCancel = {
                                    isCountingDown = false
                                }
                            )
                        }

                        // Profile Edit Dialog
                        if (showProfileDialog) {
                            ProfileEditDialog(
                                currentName = currentConfig.userName,
                                currentAvatar = currentConfig.userAvatarEmoji,
                                onSave = { name, avatar ->
                                    viewModel.updateUserProfile(name, avatar)
                                    showProfileDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Profile updated: $name $avatar")
                                    }
                                },
                                onDismiss = { showProfileDialog = false }
                            )
                        }

                        // Trimming Dialog
                        itemToTrim?.let { item ->
                            TrimmingDialog(
                                item = item,
                                isTrimming = isTrimming,
                                trimProgress = trimProgress,
                                onTrim = { startMs, endMs ->
                                    viewModel.trimRecording(item, startMs, endMs) { success, newItem ->
                                        itemToTrim = null
                                        scope.launch {
                                            if (success) {
                                                snackbarHostState.showSnackbar("Trimmed video exported and saved to Gallery!")
                                            } else {
                                                snackbarHostState.showSnackbar("Trimming failed. Please try again.")
                                            }
                                        }
                                    }
                                },
                                onDismiss = { itemToTrim = null }
                            )
                        }

                        // Rename Dialog
                        itemToRename?.let { item ->
                            RenameDialog(
                                currentTitle = item.title,
                                onConfirm = { newTitle ->
                                    viewModel.renameRecording(item.id, newTitle)
                                    itemToRename = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Recording renamed to $newTitle")
                                    }
                                },
                                onDismiss = { itemToRename = null }
                            )
                        }

                        // Delete Confirmation Dialog
                        itemToDelete?.let { item ->
                            DeleteConfirmationDialog(
                                itemTitle = item.title,
                                onConfirm = {
                                    viewModel.deleteRecording(item)
                                    itemToDelete = null
                                    if (activePlayingItem?.id == item.id) {
                                        activePlayingItem = null
                                        navController.popBackStack()
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Recording deleted")
                                    }
                                },
                                onDismiss = { itemToDelete = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) {
            isIncomingShareIntent = true
        }
    }

    private fun checkPermissionsAndRecord() {
        val validation = viewModel.validateBeforeRecording()
        if (validation is ValidationResult.Error) {
            Toast.makeText(this, validation.message, Toast.LENGTH_LONG).show()
            return
        }

        val requiredPermissions = mutableListOf<String>()

        val cfg = viewModel.currentConfig.value
        if (cfg.audioSource != AudioSourceOption.NONE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.RECORD_AUDIO)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (requiredPermissions.isNotEmpty()) {
            permissionsLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            launchMediaProjectionRequest()
        }
    }

    private fun launchMediaProjectionRequest() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        projectionLauncher.launch(captureIntent)
    }

    private fun startRecordingServiceWithProjection(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenRecordingService::class.java).apply {
            action = ScreenRecordingService.ACTION_START
            putExtra(ScreenRecordingService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenRecordingService.EXTRA_DATA_INTENT, data)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun shareRecordingItem(item: RecordingItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                Toast.makeText(this, "Recording file not found on disk", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, item.title)
                putExtra(Intent.EXTRA_TEXT, "Shared via Screenery: ${item.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Screen Recording"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing video: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CountdownOverlay(
    number: Int,
    onTick: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(number) {
        delay(1000)
        onTick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .testTag("countdown_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

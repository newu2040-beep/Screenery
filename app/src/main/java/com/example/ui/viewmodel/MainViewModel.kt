package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppThemeMode
import com.example.data.model.AudioSourceOption
import com.example.data.model.DeviceCapability
import com.example.data.model.PastelTheme
import com.example.data.model.RecordingConfig
import com.example.data.model.RecordingItem
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoFps
import com.example.data.model.VideoOrientation
import com.example.data.model.VideoResolution
import com.example.data.repository.RecordingRepository
import com.example.engine.GalleryHelper
import com.example.engine.VideoTrimmer
import com.example.service.ScreenRecordingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface ValidationResult {
    data object Success : ValidationResult
    data class Error(val message: String) : ValidationResult
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingRepository(application)

    val recordingStatus = ScreenRecordingService.recordingStatus
    val deviceCapability: StateFlow<DeviceCapability> = repository.deviceCapability
    val currentConfig: StateFlow<RecordingConfig> = repository.currentConfig

    val allRecordings: StateFlow<List<RecordingItem>> = repository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRecordings: StateFlow<List<RecordingItem>> = repository.recentRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isTrimming = MutableStateFlow(false)
    val isTrimming: StateFlow<Boolean> = _isTrimming.asStateFlow()

    private val _trimProgress = MutableStateFlow(0f)
    val trimProgress: StateFlow<Float> = _trimProgress.asStateFlow()

    fun updateConfig(config: RecordingConfig) {
        repository.updateConfig(config)
    }

    fun updateResolution(resolution: VideoResolution) {
        repository.updateConfig(currentConfig.value.copy(resolution = resolution))
    }

    fun updateAspectRatio(aspectRatio: VideoAspectRatio) {
        repository.updateConfig(currentConfig.value.copy(aspectRatio = aspectRatio))
    }

    fun updateFps(fps: VideoFps) {
        repository.updateConfig(currentConfig.value.copy(fps = fps))
    }

    fun updateBitrate(bitrateMbps: Int) {
        repository.updateConfig(currentConfig.value.copy(bitrateMbps = bitrateMbps.coerceIn(1, 100)))
    }

    fun updateAudioSource(audioSource: AudioSourceOption) {
        repository.updateConfig(currentConfig.value.copy(audioSource = audioSource))
    }

    fun updateAudioQuality(sampleRate: Int, bitrateKbps: Int) {
        repository.updateConfig(
            currentConfig.value.copy(
                audioSampleRate = sampleRate,
                audioBitrateKbps = bitrateKbps
            )
        )
    }

    fun updateOrientation(orientation: VideoOrientation) {
        repository.updateConfig(currentConfig.value.copy(orientation = orientation))
    }

    fun updateCountdown(seconds: Int) {
        repository.updateConfig(currentConfig.value.copy(countdownSeconds = seconds))
    }

    fun toggleShowTouches(enabled: Boolean) {
        repository.updateConfig(currentConfig.value.copy(showTouches = enabled))
    }

    fun toggleFloatingControls(enabled: Boolean) {
        repository.updateConfig(currentConfig.value.copy(floatingControls = enabled))
    }

    fun toggleAutoHideFloatingBar(enabled: Boolean) {
        repository.updateConfig(currentConfig.value.copy(autoHideFloatingBar = enabled))
    }

    fun updateAutoStop(minutes: Int) {
        repository.updateConfig(currentConfig.value.copy(autoStopMinutes = minutes))
    }

    fun toggleHideStatusBar(enabled: Boolean) {
        repository.updateConfig(currentConfig.value.copy(hideStatusBar = enabled))
    }

    fun toggleAutoSaveToGallery(enabled: Boolean) {
        repository.updateConfig(currentConfig.value.copy(autoSaveToGallery = enabled))
    }

    fun updateThemeMode(mode: AppThemeMode) {
        repository.updateConfig(currentConfig.value.copy(themeMode = mode))
    }

    fun updatePastelTheme(pastelTheme: PastelTheme) {
        repository.updateConfig(currentConfig.value.copy(pastelTheme = pastelTheme))
    }

    fun updateUserProfile(name: String, avatar: String) {
        repository.updateConfig(currentConfig.value.copy(userName = name, userAvatarEmoji = avatar))
    }

    fun pauseRecording() {
        val intent = Intent(getApplication(), ScreenRecordingService::class.java).apply {
            action = ScreenRecordingService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun resumeRecording() {
        val intent = Intent(getApplication(), ScreenRecordingService::class.java).apply {
            action = ScreenRecordingService.ACTION_RESUME
        }
        getApplication<Application>().startService(intent)
    }

    fun stopRecording() {
        val intent = Intent(getApplication(), ScreenRecordingService::class.java).apply {
            action = ScreenRecordingService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    fun validateBeforeRecording(): ValidationResult {
        try {
            val context = getApplication<Application>()
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            val stat = StatFs(moviesDir.absolutePath)
            val availableMb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
            if (availableMb < 150) {
                return ValidationResult.Error("Insufficient storage space: only ${availableMb}MB available. Need at least 150MB.")
            }
        } catch (_: Exception) {}

        val cfg = currentConfig.value
        val caps = deviceCapability.value
        if (cfg.resolution == VideoResolution.RES_4K && !caps.is4kSupported) {
            return ValidationResult.Error("4K recording is not supported by your device hardware encoder.")
        }
        if (cfg.fps == VideoFps.FPS_120 && !caps.is120FpsSupported) {
            return ValidationResult.Error("120 FPS is not supported by your display/encoder.")
        }

        return ValidationResult.Success
    }

    fun renameRecording(id: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.renameRecording(id, newTitle.trim())
        }
    }

    fun deleteRecording(item: RecordingItem) {
        viewModelScope.launch {
            repository.deleteRecording(item)
        }
    }

    fun trimRecording(
        item: RecordingItem,
        startMs: Long,
        endMs: Long,
        onFinished: (Boolean, RecordingItem?) -> Unit
    ) {
        viewModelScope.launch {
            _isTrimming.value = true
            _trimProgress.value = 0f

            val context = getApplication<Application>()
            val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val dstFile = File(moviesDir, "${item.title}_trimmed_$timeStamp.mp4")

            val success = VideoTrimmer.trimVideo(
                srcPath = item.filePath,
                dstPath = dstFile.absolutePath,
                startMs = startMs,
                endMs = endMs,
                onProgress = { progress ->
                    _trimProgress.value = progress
                }
            )

            _isTrimming.value = false

            if (success && dstFile.exists() && dstFile.length() > 0) {
                val newDurationMs = (endMs - startMs).coerceAtLeast(1000L)
                val newTitle = "${item.title}_Trimmed"

                // Save to gallery if auto-save is enabled
                if (currentConfig.value.autoSaveToGallery) {
                    try {
                        GalleryHelper.saveVideoToGallery(context, dstFile, newTitle)
                    } catch (_: Exception) {}
                }

                val newItem = repository.saveCompletedRecording(
                    filePath = dstFile.absolutePath,
                    durationMs = newDurationMs,
                    width = item.width,
                    height = item.height,
                    fps = item.fps,
                    bitrateMbps = item.bitrateMbps,
                    codec = item.codec,
                    audioSource = item.audioSource,
                    customTitle = newTitle
                )
                onFinished(true, newItem)
            } else {
                onFinished(false, null)
            }
        }
    }
}

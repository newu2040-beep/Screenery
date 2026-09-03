package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import com.example.data.db.RecordingDao
import com.example.data.db.RecordingDatabase
import com.example.data.model.AudioSourceOption
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.RecordingItem
import com.example.data.model.VideoFps
import com.example.data.model.VideoOrientation
import com.example.data.model.VideoResolution
import com.example.engine.DeviceDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingRepository(private val context: Context) {

    private val db = RecordingDatabase.getDatabase(context)
    private val dao: RecordingDao = db.recordingDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("screenery_prefs", Context.MODE_PRIVATE)

    private val _deviceCapability = MutableStateFlow(DeviceDetector.detectCapabilities(context))
    val deviceCapability: StateFlow<DeviceCapability> = _deviceCapability.asStateFlow()

    private val _currentConfig = MutableStateFlow(loadConfig())
    val currentConfig: StateFlow<RecordingConfig> = _currentConfig.asStateFlow()

    val allRecordings: Flow<List<RecordingItem>> = dao.getAllRecordings()
    val recentRecordings: Flow<List<RecordingItem>> = dao.getRecentRecordings(limit = 5)

    init {
        // Only real-time actual recordings saved by user
    }

    fun updateConfig(config: RecordingConfig) {
        _currentConfig.value = config
        saveConfig(config)
    }

    fun refreshDeviceCapabilities() {
        _deviceCapability.value = DeviceDetector.detectCapabilities(context)
    }

    suspend fun getRecordingById(id: Long): RecordingItem? {
        return withContext(Dispatchers.IO) {
            dao.getRecordingById(id)
        }
    }

    suspend fun saveCompletedRecording(
        filePath: String,
        durationMs: Long,
        width: Int,
        height: Int,
        fps: Int,
        bitrateMbps: Int,
        codec: String = "H.264 (AVC)",
        audioSource: String = "Microphone",
        customTitle: String? = null
    ): RecordingItem {
        return withContext(Dispatchers.IO) {
            val file = File(filePath)
            val sizeBytes = if (file.exists()) file.length() else 0L
            val defaultTitle = customTitle ?: run {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                "Recording_${dateFormat.format(Date())}"
            }

            val item = RecordingItem(
                title = defaultTitle,
                filePath = filePath,
                durationMs = durationMs,
                width = width,
                height = height,
                fps = fps,
                bitrateMbps = bitrateMbps,
                sizeBytes = sizeBytes,
                codec = codec,
                audioSource = audioSource,
                dateAdded = System.currentTimeMillis()
            )
            val id = dao.insertRecording(item)
            item.copy(id = id)
        }
    }

    suspend fun renameRecording(id: Long, newTitle: String) {
        withContext(Dispatchers.IO) {
            dao.renameRecording(id, newTitle)
        }
    }

    suspend fun deleteRecording(item: RecordingItem) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(item.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {}
            dao.deleteRecording(item)
        }
    }

    suspend fun insertRecording(item: RecordingItem): Long {
        return withContext(Dispatchers.IO) {
            dao.insertRecording(item)
        }
    }

    private fun loadConfig(): RecordingConfig {
        val resOrdinal = prefs.getInt("cfg_res", VideoResolution.RES_1080P.ordinal)
        val fpsOrdinal = prefs.getInt("cfg_fps", VideoFps.FPS_60.ordinal)
        val bitrate = prefs.getInt("cfg_bitrate", 20)
        val audioOrdinal = prefs.getInt("cfg_audio", AudioSourceOption.MICROPHONE.ordinal)
        val orientOrdinal = prefs.getInt("cfg_orient", VideoOrientation.AUTO.ordinal)
        val countdown = prefs.getInt("cfg_countdown", 3)
        val showTouches = prefs.getBoolean("cfg_touches", false)
        val floating = prefs.getBoolean("cfg_floating", true)
        val autoStop = prefs.getInt("cfg_autostop", 0)

        val resolution = VideoResolution.entries.getOrElse(resOrdinal) { VideoResolution.RES_1080P }
        val fps = VideoFps.entries.getOrElse(fpsOrdinal) { VideoFps.FPS_60 }
        val audio = AudioSourceOption.entries.getOrElse(audioOrdinal) { AudioSourceOption.MICROPHONE }
        val orient = VideoOrientation.entries.getOrElse(orientOrdinal) { VideoOrientation.AUTO }

        return RecordingConfig(
            resolution = resolution,
            fps = fps,
            bitrateMbps = bitrate,
            audioSource = audio,
            orientation = orient,
            countdownSeconds = countdown,
            showTouches = showTouches,
            floatingControls = floating,
            autoStopMinutes = autoStop
        )
    }

    private fun saveConfig(config: RecordingConfig) {
        prefs.edit()
            .putInt("cfg_res", config.resolution.ordinal)
            .putInt("cfg_fps", config.fps.ordinal)
            .putInt("cfg_bitrate", config.bitrateMbps)
            .putInt("cfg_audio", config.audioSource.ordinal)
            .putInt("cfg_orient", config.orientation.ordinal)
            .putInt("cfg_countdown", config.countdownSeconds)
            .putBoolean("cfg_touches", config.showTouches)
            .putBoolean("cfg_floating", config.floatingControls)
            .putInt("cfg_autostop", config.autoStopMinutes)
            .apply()
    }
}

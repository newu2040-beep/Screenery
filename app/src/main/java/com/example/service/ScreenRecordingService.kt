package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AudioSourceOption
import com.example.data.model.RecordingConfig
import com.example.data.model.VideoResolution
import com.example.data.repository.RecordingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingStatus(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val currentFilePath: String? = null,
    val lastSavedRecordingId: Long? = null,
    val error: String? = null
) {
    val formattedTime: String
        get() {
            val min = elapsedSeconds / 60
            val sec = elapsedSeconds % 60
            return "%02d:%02d".format(min, sec)
        }
}

class ScreenRecordingService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private var autoStopJob: Job? = null
    private var storageMonitorJob: Job? = null

    private var currentConfig: RecordingConfig = RecordingConfig()
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L
    private var actualDurationMs: Long = 0L
    private var recordingWidth = 1080
    private var recordingHeight = 1920

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var repository: RecordingRepository

    companion object {
        private const val TAG = "ScreenRecordingService"
        const val CHANNEL_ID = "screenery_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.screenery.recorder.ACTION_START"
        const val ACTION_PAUSE = "com.screenery.recorder.ACTION_PAUSE"
        const val ACTION_RESUME = "com.screenery.recorder.ACTION_RESUME"
        const val ACTION_STOP = "com.screenery.recorder.ACTION_STOP"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA_INTENT = "extra_data_intent"

        private val _recordingStatus = MutableStateFlow(RecordingStatus())
        val recordingStatus: StateFlow<RecordingStatus> = _recordingStatus.asStateFlow()

        var activeServiceInstance: ScreenRecordingService? = null
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activeServiceInstance = this
        repository = RecordingRepository(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val dataIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_DATA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_DATA_INTENT)
                }

                currentConfig = repository.currentConfig.value
                // Promote to foreground immediately on Android 14+ before calling getMediaProjection
                promoteToForeground("Preparing recording...")

                if (resultCode != 0 && dataIntent != null) {
                    startRecordingEngine(resultCode, dataIntent)
                } else {
                    Log.e(TAG, "Missing MediaProjection permission intent")
                    stopServiceSafely()
                }
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun promoteToForeground(statusText: String) {
        val notification = buildNotification(statusText, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            val hasMicPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (currentConfig.audioSource != AudioSourceOption.NONE && hasMicPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            try {
                startForeground(NOTIFICATION_ID, notification, type)
            } catch (e: Exception) {
                Log.w(TAG, "startForeground with types failed, falling back to base type", e)
                try {
                    startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
                } catch (e2: Exception) {
                    Log.e(TAG, "startForeground fallback failed", e2)
                }
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startRecordingEngine(resultCode: Int, data: Intent) {
        try {
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)

            if (mediaProjection == null) {
                Log.e(TAG, "Failed to get MediaProjection")
                _recordingStatus.value = _recordingStatus.value.copy(error = "Screen capture permission denied")
                stopServiceSafely()
                return
            }

            // Register callback to safely handle termination
            mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped by system")
                    stopRecording()
                }
            }, Handler(Looper.getMainLooper()))

            // Calculate dimensions and orientation
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            val screenDensity = displayMetrics.densityDpi

            val dims = currentConfig.aspectRatio.calculateDimensions(
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                resolution = currentConfig.resolution,
                orientation = currentConfig.orientation
            )
            // Dimensions must be even integers for video encoders
            recordingWidth = (dims.first / 2) * 2
            recordingHeight = (dims.second / 2) * 2

            // Prepare output file
            val moviesDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
            if (!moviesDir.exists()) moviesDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            currentOutputFile = File(moviesDir, "Screenery_$timeStamp.mp4")

            // Initialize MediaRecorder
            @Suppress("DEPRECATION")
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(applicationContext)
            } else {
                MediaRecorder()
            }

            val hasMicPermission = ContextCompat.checkSelfPermission(
                this@ScreenRecordingService,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            val shouldRecordAudio = currentConfig.audioSource != AudioSourceOption.NONE && hasMicPermission

            var audioConfigured = false

            mediaRecorder?.apply {
                if (shouldRecordAudio) {
                    try {
                        val audioSource = when (currentConfig.audioSource) {
                            AudioSourceOption.SYSTEM -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    MediaRecorder.AudioSource.VOICE_RECOGNITION
                                } else {
                                    MediaRecorder.AudioSource.MIC
                                }
                            }
                            AudioSourceOption.BOTH -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    MediaRecorder.AudioSource.VOICE_COMMUNICATION
                                } else {
                                    MediaRecorder.AudioSource.MIC
                                }
                            }
                            AudioSourceOption.MICROPHONE -> MediaRecorder.AudioSource.MIC
                            AudioSourceOption.NONE -> MediaRecorder.AudioSource.MIC
                        }
                        setAudioSource(audioSource)
                        audioConfigured = true
                    } catch (e: Exception) {
                        Log.w(TAG, "Preferred audio source failed, trying MIC fallback", e)
                        try {
                            setAudioSource(MediaRecorder.AudioSource.MIC)
                            audioConfigured = true
                        } catch (e2: Exception) {
                            Log.e(TAG, "All audio sources failed. Continuing with video only.", e2)
                            audioConfigured = false
                        }
                    }
                }

                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(currentOutputFile!!.absolutePath)

                // Dimensions & Bitrate
                setVideoSize(recordingWidth, recordingHeight)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)

                if (audioConfigured) {
                    try {
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setAudioEncodingBitRate(currentConfig.audioBitrateKbps * 1000)
                        setAudioSamplingRate(currentConfig.audioSampleRate)
                        setAudioChannels(2)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed setting advanced audio encoder settings, falling back to standard AAC", e)
                        try {
                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        } catch (_: Exception) {}
                    }
                }

                setVideoEncodingBitRate(currentConfig.bitrateMbps * 1000 * 1000)
                setVideoFrameRate(currentConfig.fps.fps)

                prepare()
            }

            // Setup virtual display
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreeneryRecording",
                recordingWidth,
                recordingHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder!!.surface,
                null,
                null
            )

            mediaRecorder?.start()
            startTimeMs = System.currentTimeMillis()

            _recordingStatus.value = RecordingStatus(
                isRecording = true,
                isPaused = false,
                elapsedSeconds = 0,
                currentFilePath = currentOutputFile?.absolutePath
            )

            updateNotification(0)
            startTimer()
            startSafetyMonitors()

            // Optional Floating overlay
            if (currentConfig.floatingControls && android.provider.Settings.canDrawOverlays(this)) {
                try {
                    startService(Intent(this, FloatingControlService::class.java))
                } catch (_: Exception) {}
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initiating recording", e)
            _recordingStatus.value = _recordingStatus.value.copy(error = e.localizedMessage ?: "Failed to record screen")
            cleanup()
            stopServiceSafely()
        }
    }

    private fun stopServiceSafely() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (_: Exception) {}
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var elapsed = _recordingStatus.value.elapsedSeconds
            while (isActive && _recordingStatus.value.isRecording) {
                if (!_recordingStatus.value.isPaused) {
                    delay(1000)
                    elapsed++
                    _recordingStatus.value = _recordingStatus.value.copy(elapsedSeconds = elapsed)
                    updateNotification(elapsed)
                } else {
                    delay(500)
                }
            }
        }
    }

    private fun startSafetyMonitors() {
        // Auto-stop monitor
        if (currentConfig.autoStopMinutes > 0) {
            autoStopJob?.cancel()
            autoStopJob = serviceScope.launch {
                delay(currentConfig.autoStopMinutes * 60 * 1000L)
                if (_recordingStatus.value.isRecording) {
                    stopRecording()
                }
            }
        }

        // Storage space monitor
        storageMonitorJob?.cancel()
        storageMonitorJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive && _recordingStatus.value.isRecording) {
                try {
                    val stat = StatFs(currentOutputFile?.parent ?: filesDir.absolutePath)
                    val availableMb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
                    if (availableMb < 100) { // under 100MB remaining
                        Log.w(TAG, "Low storage: ${availableMb}MB. Stopping recording safely.")
                        withContext(Dispatchers.Main) {
                            stopRecording()
                        }
                        break
                    }
                } catch (_: Exception) {}
                delay(5000)
            }
        }
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                _recordingStatus.value = _recordingStatus.value.copy(isPaused = true)
                updateNotification(_recordingStatus.value.elapsedSeconds)
            } catch (e: Exception) {
                Log.e(TAG, "Pause failed", e)
            }
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                _recordingStatus.value = _recordingStatus.value.copy(isPaused = false)
                updateNotification(_recordingStatus.value.elapsedSeconds)
            } catch (e: Exception) {
                Log.e(TAG, "Resume failed", e)
            }
        }
    }

    fun stopRecording() {
        if (!_recordingStatus.value.isRecording) return

        actualDurationMs = System.currentTimeMillis() - startTimeMs
        val recordedFile = currentOutputFile
        val recordedSeconds = _recordingStatus.value.elapsedSeconds

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "MediaRecorder stop failed", e)
        }

        cleanup()

        // Save recorded file to DB
        serviceScope.launch {
            if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
                val savedItem = repository.saveCompletedRecording(
                    filePath = recordedFile.absolutePath,
                    durationMs = (recordedSeconds * 1000L).coerceAtLeast(actualDurationMs),
                    width = recordingWidth,
                    height = recordingHeight,
                    fps = currentConfig.fps.fps,
                    bitrateMbps = currentConfig.bitrateMbps,
                    codec = "H.264 (AVC)",
                    audioSource = currentConfig.audioSource.label
                )
                _recordingStatus.value = RecordingStatus(
                    isRecording = false,
                    isPaused = false,
                    elapsedSeconds = 0,
                    lastSavedRecordingId = savedItem.id
                )
            } else {
                _recordingStatus.value = RecordingStatus(isRecording = false)
            }

            // Stop floating overlay if running
            try {
                stopService(Intent(applicationContext, FloatingControlService::class.java))
            } catch (_: Exception) {}

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun cleanup() {
        timerJob?.cancel()
        autoStopJob?.cancel()
        storageMonitorJob?.cancel()

        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}
        virtualDisplay = null

        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null
    }

    override fun onDestroy() {
        cleanup()
        activeServiceInstance = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screenery Recording Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Screen recording controls: Pause, Resume, Stop, and elapsed time counter"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String, elapsedSeconds: Long): Notification {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timeString = "%02d:%02d".format(minutes, seconds)
        val isPaused = _recordingStatus.value.isPaused

        val openAppIntent = PendingIntent.getActivity(
            this,
            100,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = if (isPaused) {
            val resumeIntent = PendingIntent.getService(
                this,
                101,
                Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_RESUME },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "▶ Resume",
                resumeIntent
            ).build()
        } else {
            val pauseIntent = PendingIntent.getService(
                this,
                102,
                Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_PAUSE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "⏸ Pause",
                pauseIntent
            ).build()
        }

        val stopIntent = PendingIntent.getService(
            this,
            103,
            Intent(this, ScreenRecordingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "⏹ Stop & Save",
            stopIntent
        ).build()

        val title = if (isPaused) "⏸ Paused ($timeString)" else "🔴 Recording ($timeString)"
        val content = if (isPaused) {
            "Paused at $timeString • Tap Resume to continue"
        } else {
            "${currentConfig.resolution.label} • ${currentConfig.fps.fps} FPS • ${currentConfig.audioSource.label}"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText("Screenery")
            .setSmallIcon(R.drawable.ic_notification_record)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(elapsedSeconds: Long) {
        val notification = buildNotification("Recording...", elapsedSeconds)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}

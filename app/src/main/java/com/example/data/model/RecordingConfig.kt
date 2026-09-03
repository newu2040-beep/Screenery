package com.example.data.model

enum class VideoResolution(val label: String, val subLabel: String, val width: Int, val height: Int) {
    RES_DEVICE("Device", "Native", 0, 0),
    RES_720P("720p", "HD", 1280, 720),
    RES_1080P("1080p", "FHD", 1920, 1080),
    RES_1440P("1440p", "2K", 2560, 1440),
    RES_4K("4K", "UHD", 3840, 2160);

    fun getActualDimensions(screenWidth: Int, screenHeight: Int): Pair<Int, Int> {
        return if (this == RES_DEVICE || width == 0 || height == 0) {
            screenWidth to screenHeight
        } else {
            // Respect portrait / landscape aspect ratio based on device orientation
            if (screenWidth > screenHeight) {
                // Landscape
                maxOf(width, height) to minOf(width, height)
            } else {
                // Portrait
                minOf(width, height) to maxOf(width, height)
            }
        }
    }
}

enum class VideoFps(val fps: Int, val subLabel: String) {
    FPS_24(24, "Cinema"),
    FPS_30(30, "Standard"),
    FPS_60(60, "Smooth"),
    FPS_90(90, "High"),
    FPS_120(120, "Ultra")
}

enum class AudioSourceOption(val label: String) {
    NONE("None"),
    SYSTEM("System Audio"),
    MICROPHONE("Microphone"),
    BOTH("Both")
}

enum class VideoOrientation(val label: String) {
    AUTO("Auto"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

data class RecordingConfig(
    val resolution: VideoResolution = VideoResolution.RES_1080P,
    val fps: VideoFps = VideoFps.FPS_60,
    val bitrateMbps: Int = 20, // 1 to 100 Mbps
    val audioSource: AudioSourceOption = AudioSourceOption.MICROPHONE,
    val orientation: VideoOrientation = VideoOrientation.AUTO,
    val countdownSeconds: Int = 3, // 0 (Off), 3, 5, 10
    val showTouches: Boolean = false,
    val floatingControls: Boolean = true,
    val autoStopMinutes: Int = 0, // 0 is disabled
    val hideStatusBar: Boolean = false
) {
    val summaryString: String
        get() = "${resolution.label} • ${fps.fps} FPS • ${bitrateMbps} Mbps"
}

data class DeviceCapability(
    val deviceWidth: Int = 1080,
    val deviceHeight: Int = 2400,
    val refreshRate: Float = 60f,
    val maxSupportedFps: Int = 60,
    val supportedFpsList: List<VideoFps> = listOf(VideoFps.FPS_24, VideoFps.FPS_30, VideoFps.FPS_60),
    val supportedResolutions: List<VideoResolution> = listOf(VideoResolution.RES_DEVICE, VideoResolution.RES_720P, VideoResolution.RES_1080P),
    val is4kSupported: Boolean = false,
    val is120FpsSupported: Boolean = false,
    val isHevcSupported: Boolean = true,
    val isHdrSupported: Boolean = false,
    val availableStorageBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val hardwareEncoders: List<String> = emptyList(),
    val recommendedConfig: RecordingConfig = RecordingConfig()
)

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
            if (screenWidth > screenHeight) {
                maxOf(width, height) to minOf(width, height)
            } else {
                minOf(width, height) to maxOf(width, height)
            }
        }
    }
}

enum class VideoAspectRatio(
    val label: String,
    val subLabel: String,
    val ratioWidth: Int,
    val ratioHeight: Int,
    val description: String
) {
    MATCH_DEVICE("Full Screen", "Match Device", 0, 0, "Native screen aspect ratio"),
    RATIO_16_9("16:9", "Landscape", 16, 9, "YouTube, TV & Desktop monitors"),
    RATIO_9_16("9:16", "Vertical", 9, 16, "Shorts, Reels, TikTok & Stories"),
    RATIO_1_1("1:1", "Square", 1, 1, "Instagram Feed & Square Posts"),
    RATIO_4_3("4:3", "Standard", 4, 3, "Tablets, Presentations & Classic TV"),
    RATIO_3_4("3:4", "Portrait", 3, 4, "Social Portrait & Posts"),
    RATIO_21_9("21:9", "Ultra-Wide", 21, 9, "Cinematic & Ultra-Wide displays");

    fun calculateDimensions(
        screenWidth: Int,
        screenHeight: Int,
        resolution: VideoResolution,
        orientation: VideoOrientation = VideoOrientation.AUTO
    ): Pair<Int, Int> {
        val isLandscape = when (orientation) {
            VideoOrientation.LANDSCAPE -> true
            VideoOrientation.PORTRAIT -> false
            VideoOrientation.AUTO -> screenWidth > screenHeight
        }

        if (this == MATCH_DEVICE || ratioWidth == 0 || ratioHeight == 0) {
            val base = resolution.getActualDimensions(screenWidth, screenHeight)
            var w = base.first
            var h = base.second
            if (isLandscape && w < h) {
                val temp = w; w = h; h = temp
            } else if (!isLandscape && w > h) {
                val temp = w; w = h; h = temp
            }
            return ((w / 2) * 2).coerceAtLeast(320) to ((h / 2) * 2).coerceAtLeast(320)
        }

        val baseShort = when (resolution) {
            VideoResolution.RES_720P -> 720
            VideoResolution.RES_1080P -> 1080
            VideoResolution.RES_1440P -> 1440
            VideoResolution.RES_4K -> 2160
            VideoResolution.RES_DEVICE -> minOf(screenWidth, screenHeight).coerceAtLeast(720)
        }

        val rw = ratioWidth.toFloat()
        val rh = ratioHeight.toFloat()

        val w: Int
        val h: Int

        if (this == RATIO_1_1) {
            w = baseShort
            h = baseShort
        } else if (this == RATIO_9_16 || this == RATIO_3_4) {
            if (isLandscape) {
                h = baseShort
                w = (baseShort * (rh / rw)).toInt()
            } else {
                w = baseShort
                h = (baseShort * (rh / rw)).toInt()
            }
        } else {
            if (isLandscape) {
                h = baseShort
                w = (baseShort * (rw / rh)).toInt()
            } else {
                w = baseShort
                h = (baseShort * (rw / rh)).toInt()
            }
        }

        val finalW = ((w / 2) * 2).coerceIn(320, 4096)
        val finalH = ((h / 2) * 2).coerceIn(320, 4096)
        return finalW to finalH
    }
}

enum class VideoFps(val fps: Int, val subLabel: String) {
    FPS_24(24, "Cinema"),
    FPS_30(30, "Standard"),
    FPS_60(60, "Smooth"),
    FPS_90(90, "High"),
    FPS_120(120, "Ultra")
}

enum class AudioSourceOption(val label: String, val description: String) {
    NONE("Mute", "Silent recording without audio track"),
    MICROPHONE("Microphone", "Voice commentary & room ambient audio"),
    SYSTEM("System Audio", "Internal app sounds, music & game audio"),
    BOTH("Mic + System", "Simultaneous voice commentary & system sounds")
}

enum class VideoOrientation(val label: String) {
    AUTO("Auto"),
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class AppThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class PastelTheme(
    val displayName: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val lightBgColor: Long,
    val lightContainerColor: Long,
    val darkPrimaryColor: Long,
    val darkContainerColor: Long
) {
    LAVENDER(
        displayName = "Lavender Dream",
        primaryColor = 0xFF6366F1,
        secondaryColor = 0xFF8B5CF6,
        lightBgColor = 0xFFF8F9FE,
        lightContainerColor = 0xFFEEF2FF,
        darkPrimaryColor = 0xFF818CF8,
        darkContainerColor = 0xFF312E81
    ),
    MINT_SAGE(
        displayName = "Mint Sage",
        primaryColor = 0xFF10B981,
        secondaryColor = 0xFF14B8A6,
        lightBgColor = 0xFFF4FDF9,
        lightContainerColor = 0xFFECFDF5,
        darkPrimaryColor = 0xFF34D399,
        darkContainerColor = 0xFF064E3B
    ),
    PEACH_ROSE(
        displayName = "Peach Rose",
        primaryColor = 0xFFF43F5E,
        secondaryColor = 0xFFFB7185,
        lightBgColor = 0xFFFFF8F8,
        lightContainerColor = 0xFFFFF1F2,
        darkPrimaryColor = 0xFFFDA4AF,
        darkContainerColor = 0xFF881337
    ),
    SKY_BLUE(
        displayName = "Sky Breeze",
        primaryColor = 0xFF0284C7,
        secondaryColor = 0xFF38BDF8,
        lightBgColor = 0xFFF4F9FF,
        lightContainerColor = 0xFFE0F2FE,
        darkPrimaryColor = 0xFF38BDF8,
        darkContainerColor = 0xFF075985
    ),
    COTTON_CANDY(
        displayName = "Cotton Candy",
        primaryColor = 0xFFC026D3,
        secondaryColor = 0xFFE879F9,
        lightBgColor = 0xFFFDF7FF,
        lightContainerColor = 0xFFFDF4FF,
        darkPrimaryColor = 0xFFF0ABFC,
        darkContainerColor = 0xFF701A75
    ),
    MATCHA_HONEY(
        displayName = "Matcha Honey",
        primaryColor = 0xFFD97706,
        secondaryColor = 0xFFF59E0B,
        lightBgColor = 0xFFFFFDF5,
        lightContainerColor = 0xFFFEF3C7,
        darkPrimaryColor = 0xFFFBBF24,
        darkContainerColor = 0xFF78350F
    )
}

data class RecordingConfig(
    val resolution: VideoResolution = VideoResolution.RES_1080P,
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.MATCH_DEVICE,
    val fps: VideoFps = VideoFps.FPS_60,
    val bitrateMbps: Int = 20, // 1 to 100 Mbps
    val audioSource: AudioSourceOption = AudioSourceOption.MICROPHONE,
    val audioSampleRate: Int = 44100, // 44100 or 48000 Hz
    val audioBitrateKbps: Int = 128, // 128, 192, 256
    val orientation: VideoOrientation = VideoOrientation.AUTO,
    val countdownSeconds: Int = 3, // 0 (Off), 3, 5, 10
    val showTouches: Boolean = false,
    val floatingControls: Boolean = true,
    val autoHideFloatingBar: Boolean = true, // Automatically hide floating bar while recording
    val autoStopMinutes: Int = 0, // 0 is disabled
    val hideStatusBar: Boolean = false,
    val autoSaveToGallery: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val pastelTheme: PastelTheme = PastelTheme.LAVENDER,
    val userName: String = "Creator",
    val userAvatarEmoji: String = "🎬"
) {
    val summaryString: String
        get() = "${resolution.label} • ${aspectRatio.label} • ${fps.fps} FPS • ${bitrateMbps} Mbps"
}

data class DeviceCapability(
    val deviceWidth: Int = 1080,
    val deviceHeight: Int = 2400,
    val refreshRate: Float = 60f,
    val maxSupportedFps: Int = 60,
    val supportedFpsList: List<VideoFps> = listOf(VideoFps.FPS_24, VideoFps.FPS_30, VideoFps.FPS_60),
    val supportedResolutions: List<VideoResolution> = listOf(VideoResolution.RES_DEVICE, VideoResolution.RES_720P, VideoResolution.RES_1080P),
    val supportedAspectRatios: List<VideoAspectRatio> = VideoAspectRatio.entries,
    val is4kSupported: Boolean = false,
    val is120FpsSupported: Boolean = false,
    val isHevcSupported: Boolean = true,
    val isHdrSupported: Boolean = false,
    val availableStorageBytes: Long = 0L,
    val totalStorageBytes: Long = 0L,
    val hardwareEncoders: List<String> = emptyList(),
    val recommendedConfig: RecordingConfig = RecordingConfig()
)

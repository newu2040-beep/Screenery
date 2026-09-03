package com.example.engine

import android.content.Context
import android.hardware.display.DisplayManager
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.VideoFps
import com.example.data.model.VideoResolution

object DeviceDetector {

    fun detectCapabilities(context: Context): DeviceCapability {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        var screenWidth = 1080
        var screenHeight = 2400
        var maxRefreshRate = 60f
        var isHdrSupported = false

        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            if (display != null) {
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                display.getRealMetrics(metrics)
                if (metrics.widthPixels > 0) screenWidth = metrics.widthPixels
                if (metrics.heightPixels > 0) screenHeight = metrics.heightPixels

                val modes = display.supportedModes
                if (modes != null) {
                    var highestRate = display.refreshRate
                    for (mode in modes) {
                        if (mode.refreshRate > highestRate) {
                            highestRate = mode.refreshRate
                        }
                    }
                    maxRefreshRate = highestRate
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val hdrCapabilities = display.hdrCapabilities
                    if (hdrCapabilities != null && hdrCapabilities.supportedHdrTypes.isNotEmpty()) {
                        isHdrSupported = true
                    }
                }
            }
        } catch (_: Throwable) {}

        // Detect Hardware Encoders
        val hardwareEncoders = mutableListOf<String>()
        var isHevcSupported = false
        var isAvcSupported = false
        var maxEncoderWidth = 1920
        var maxEncoderHeight = 1080
        var maxEncoderFps = 60

        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            val codecInfos = codecList.codecInfos
            for (info in codecInfos) {
                if (!info.isEncoder) continue
                val types = info.supportedTypes
                for (type in types) {
                    if (type.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true)) {
                        isAvcSupported = true
                        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isHardwareAccelerated) {
                            "${info.name} (HW AVC)"
                        } else {
                            "${info.name} (AVC)"
                        }
                        if (!hardwareEncoders.contains(name)) hardwareEncoders.add(name)

                        try {
                            val capabilities = info.getCapabilitiesForType(type)
                            val videoCaps = capabilities.videoCapabilities
                            if (videoCaps != null) {
                                maxEncoderWidth = maxOf(maxEncoderWidth, videoCaps.supportedWidths.upper)
                                maxEncoderHeight = maxOf(maxEncoderHeight, videoCaps.supportedHeights.upper)
                                maxEncoderFps = maxOf(maxEncoderFps, videoCaps.supportedFrameRates.upper.toInt())
                            }
                        } catch (_: Exception) {}
                    } else if (type.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true)) {
                        isHevcSupported = true
                        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info.isHardwareAccelerated) {
                            "${info.name} (HW HEVC)"
                        } else {
                            "${info.name} (HEVC)"
                        }
                        if (!hardwareEncoders.contains(name)) hardwareEncoders.add(name)

                        try {
                            val capabilities = info.getCapabilitiesForType(type)
                            val videoCaps = capabilities.videoCapabilities
                            if (videoCaps != null) {
                                maxEncoderWidth = maxOf(maxEncoderWidth, videoCaps.supportedWidths.upper)
                                maxEncoderHeight = maxOf(maxEncoderHeight, videoCaps.supportedHeights.upper)
                                maxEncoderFps = maxOf(maxEncoderFps, videoCaps.supportedFrameRates.upper.toInt())
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Throwable) {
            // Fallback for Robolectric or restricted environments
            isAvcSupported = true
            hardwareEncoders.add("MediaCodec (Software/Standard)")
        }

        // Supported resolutions based on screen and encoder limits
        val supportedResolutions = mutableListOf<VideoResolution>()
        supportedResolutions.add(VideoResolution.RES_DEVICE)
        supportedResolutions.add(VideoResolution.RES_720P)
        supportedResolutions.add(VideoResolution.RES_1080P)

        val is4kSupported = (maxEncoderWidth >= 3840 || maxEncoderHeight >= 3840) &&
                (screenWidth >= 2160 || screenHeight >= 2160 || maxEncoderWidth >= 3840)
        val is1440pSupported = (maxEncoderWidth >= 2560 || maxEncoderHeight >= 2560) &&
                (screenWidth >= 1440 || screenHeight >= 1440 || maxEncoderWidth >= 2560)

        if (is1440pSupported) {
            supportedResolutions.add(VideoResolution.RES_1440P)
        }
        if (is4kSupported) {
            supportedResolutions.add(VideoResolution.RES_4K)
        }

        // Supported FPS based on display refresh rate and encoder capability
        val effectiveMaxFps = minOf(maxRefreshRate.toInt(), maxEncoderFps).coerceAtLeast(30)
        val supportedFpsList = mutableListOf<VideoFps>()
        supportedFpsList.add(VideoFps.FPS_24)
        supportedFpsList.add(VideoFps.FPS_30)
        if (effectiveMaxFps >= 55) {
            supportedFpsList.add(VideoFps.FPS_60)
        }
        if (effectiveMaxFps >= 85) {
            supportedFpsList.add(VideoFps.FPS_90)
        }
        val is120FpsSupported = effectiveMaxFps >= 115
        if (is120FpsSupported) {
            supportedFpsList.add(VideoFps.FPS_120)
        }

        // Storage detection
        var availableStorageBytes = 0L
        var totalStorageBytes = 0L
        try {
            val statFs = StatFs(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath ?: context.filesDir.absolutePath)
            availableStorageBytes = statFs.availableBlocksLong * statFs.blockSizeLong
            totalStorageBytes = statFs.blockCountLong * statFs.blockSizeLong
        } catch (_: Exception) {}

        // Recommend best stable configuration:
        // 1080p, 60fps (or 30fps if 60 not supported), 20 Mbps bitrate
        val recommendedResolution = if (supportedResolutions.contains(VideoResolution.RES_1080P)) {
            VideoResolution.RES_1080P
        } else {
            VideoResolution.RES_720P
        }

        val recommendedFps = when {
            supportedFpsList.contains(VideoFps.FPS_60) -> VideoFps.FPS_60
            supportedFpsList.contains(VideoFps.FPS_30) -> VideoFps.FPS_30
            else -> VideoFps.FPS_24
        }

        val recommendedConfig = RecordingConfig(
            resolution = recommendedResolution,
            fps = recommendedFps,
            bitrateMbps = if (recommendedResolution == VideoResolution.RES_4K) 40 else 20
        )

        return DeviceCapability(
            deviceWidth = screenWidth,
            deviceHeight = screenHeight,
            refreshRate = maxRefreshRate,
            maxSupportedFps = effectiveMaxFps,
            supportedFpsList = supportedFpsList,
            supportedResolutions = supportedResolutions,
            is4kSupported = is4kSupported,
            is120FpsSupported = is120FpsSupported,
            isHevcSupported = isHevcSupported,
            isHdrSupported = isHdrSupported,
            availableStorageBytes = availableStorageBytes,
            totalStorageBytes = totalStorageBytes,
            hardwareEncoders = hardwareEncoders,
            recommendedConfig = recommendedConfig
        )
    }
}

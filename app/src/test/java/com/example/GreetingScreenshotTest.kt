package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.RecordingItem
import com.example.service.RecordingStatus
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun home_screen_screenshot() {
        val sampleItem = RecordingItem(
            id = 1,
            title = "Gameplay_2024-05-20",
            filePath = "/test/gameplay.mp4",
            durationMs = 332000L,
            width = 3840,
            height = 2160,
            fps = 120,
            bitrateMbps = 50,
            sizeBytes = 1288490188L,
            codec = "HEVC / H.265",
            audioSource = "Both",
            dateAdded = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    config = RecordingConfig(),
                    deviceCapability = DeviceCapability(
                        deviceWidth = 1080,
                        deviceHeight = 2400,
                        refreshRate = 120f,
                        is4kSupported = true,
                        is120FpsSupported = true,
                        isHevcSupported = true
                    ),
                    recordingStatus = RecordingStatus(),
                    recentRecordings = listOf(sampleItem),
                    onStartRecordingClick = {},
                    onPauseResumeClick = {},
                    onStopRecordingClick = {},
                    onSeeAllQuickSettings = {},
                    onSeeAllRecordings = {},
                    onPlayRecording = {},
                    onTrimRecording = {},
                    onShareRecording = {},
                    onDeleteRecording = {},
                    onDetailsRecording = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}

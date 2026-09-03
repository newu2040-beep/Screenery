package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.RecordingDatabase
import com.example.data.model.AudioSourceOption
import com.example.data.model.RecordingConfig
import com.example.data.model.RecordingItem
import com.example.data.model.VideoFps
import com.example.data.model.VideoOrientation
import com.example.data.model.VideoResolution
import com.example.engine.DeviceDetector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Screenery", appName)
    }

    @Test
    fun `test device capability detector`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val capabilities = DeviceDetector.detectCapabilities(context)
        assertNotNull(capabilities)
        assertTrue(capabilities.deviceWidth > 0)
        assertTrue(capabilities.deviceHeight > 0)
        assertTrue(capabilities.supportedResolutions.isNotEmpty())
    }

    @Test
    fun `test room database insertion and retrieval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = RecordingDatabase.getDatabase(context)
        val dao = db.recordingDao()

        val item = RecordingItem(
            title = "Test_Recording",
            filePath = "/sdcard/test.mp4",
            durationMs = 120000L,
            width = 1920,
            height = 1080,
            fps = 60,
            bitrateMbps = 20,
            sizeBytes = 50000000L,
            codec = "H.264",
            audioSource = "Microphone",
            dateAdded = System.currentTimeMillis()
        )

        val id = dao.insertRecording(item)
        assertTrue(id > 0)

        val retrieved = dao.getRecordingById(id)
        assertNotNull(retrieved)
        assertEquals("Test_Recording", retrieved?.title)
        assertEquals(60, retrieved?.fps)

        dao.renameRecording(id, "Renamed_Recording")
        val renamed = dao.getRecordingById(id)
        assertEquals("Renamed_Recording", renamed?.title)

        dao.deleteRecording(renamed!!)
        val all = dao.getAllRecordings().first()
        assertTrue(all.none { it.id == id })
    }

    @Test
    fun `test recording config defaults`() {
        val config = RecordingConfig(
            resolution = VideoResolution.RES_1080P,
            fps = VideoFps.FPS_60,
            bitrateMbps = 20,
            audioSource = AudioSourceOption.MICROPHONE,
            orientation = VideoOrientation.AUTO
        )
        assertEquals("1080p • 60 FPS • 20 Mbps", config.summaryString)
        assertEquals("Microphone", config.audioSource.label)
    }
}

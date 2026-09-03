package com.example.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class ScreeneryTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var statusJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(ScreenRecordingService.recordingStatus.value)

        statusJob?.cancel()
        statusJob = serviceScope.launch {
            ScreenRecordingService.recordingStatus.collectLatest { status ->
                updateTileState(status)
            }
        }
    }

    override fun onStopListening() {
        statusJob?.cancel()
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val status = ScreenRecordingService.recordingStatus.value
        if (status.isRecording) {
            // Stop recording
            val stopIntent = Intent(this, ScreenRecordingService::class.java).apply {
                action = ScreenRecordingService.ACTION_STOP
            }
            startService(stopIntent)
        } else {
            // Open Screenery to start recording
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("auto_start_prompt", true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ startActivityAndCollapse requires PendingIntent
                val pendingIntent = android.app.PendingIntent.getActivity(
                    this,
                    0,
                    mainIntent,
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(mainIntent)
            }
        }
    }

    private fun updateTileState(status: RecordingStatus) {
        val tile = qsTile ?: return
        if (status.isRecording) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Screenery Record"
            val minutes = status.elapsedSeconds / 60
            val seconds = status.elapsedSeconds % 60
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (status.isPaused) "Paused" else "%02d:%02d".format(minutes, seconds)
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_record)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Screenery Record"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Tap to record"
            }
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_record)
        }
        tile.updateTile()
    }
}

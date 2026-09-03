package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class RecordingActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val activeService = ScreenRecordingService.activeServiceInstance
        if (activeService != null) {
            when (action) {
                ScreenRecordingService.ACTION_PAUSE -> activeService.pauseRecording()
                ScreenRecordingService.ACTION_RESUME -> activeService.resumeRecording()
                ScreenRecordingService.ACTION_STOP -> activeService.stopRecording()
            }
        } else {
            val serviceIntent = Intent(context, ScreenRecordingService::class.java).apply {
                this.action = action
            }
            try {
                context.startService(serviceIntent)
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        context.startForegroundService(serviceIntent)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}


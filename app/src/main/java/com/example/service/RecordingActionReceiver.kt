package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecordingActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, ScreenRecordingService::class.java)
        when (intent.action) {
            ScreenRecordingService.ACTION_PAUSE -> {
                serviceIntent.action = ScreenRecordingService.ACTION_PAUSE
                context.startService(serviceIntent)
            }
            ScreenRecordingService.ACTION_RESUME -> {
                serviceIntent.action = ScreenRecordingService.ACTION_RESUME
                context.startService(serviceIntent)
            }
            ScreenRecordingService.ACTION_STOP -> {
                serviceIntent.action = ScreenRecordingService.ACTION_STOP
                context.startService(serviceIntent)
            }
        }
    }
}

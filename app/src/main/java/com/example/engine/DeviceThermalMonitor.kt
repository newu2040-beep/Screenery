package com.example.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeviceThermalState(
    val temperatureCelsius: Float = 33.5f,
    val thermalStatus: Int = 0,
    val isWarm: Boolean = false,
    val isOverheating: Boolean = false,
    val warningMessage: String? = null
) {
    val formattedTemp: String
        get() = "%.1f°C".format(temperatureCelsius)

    val statusLabel: String
        get() = when {
            isOverheating -> "Critical Overheat"
            isWarm -> "High Temperature"
            temperatureCelsius > 37.0f -> "Elevated"
            else -> "Optimal"
        }
}

class DeviceThermalMonitor(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val _thermalState = MutableStateFlow(readCurrentThermalState())
    val thermalState: StateFlow<DeviceThermalState> = _thermalState.asStateFlow()

    private var thermalListener: Any? = null
    private var batteryReceiver: BroadcastReceiver? = null

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        // Battery temperature receiver
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 335)
                    val celsius = rawTemp / 10.0f
                    updateState(celsius = celsius)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val sticky = context.registerReceiver(batteryReceiver, filter)
            if (sticky != null) {
                val rawTemp = sticky.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 335)
                updateState(celsius = rawTemp / 10.0f)
            }
        } catch (_: Exception) {}

        // PowerManager Thermal Status Listener on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try {
                val listener = PowerManager.OnThermalStatusChangedListener { status ->
                    updateState(status = status)
                }
                powerManager.addThermalStatusListener(listener)
                thermalListener = listener
            } catch (_: Exception) {}
        }
    }

    private fun readCurrentThermalState(): DeviceThermalState {
        var temp = 33.5f
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, filter)
            if (batteryStatus != null) {
                val rawTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 335)
                temp = rawTemp / 10.0f
            }
        } catch (_: Exception) {}

        val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try { powerManager.currentThermalStatus } catch (_: Exception) { 0 }
        } else {
            0
        }

        val isWarm = temp >= 39.0f || status >= 2 // MODERATE
        val isOverheat = temp >= 42.0f || status >= 3 // SEVERE
        val msg = when {
            isOverheat -> "⚠️ Overheat Alert: Device is at %.1f°C. Recording may experience frame drops or throttling.".format(temp)
            isWarm -> "⚠️ High Temperature: Device is at %.1f°C. Consider lowering resolution or cooling device.".format(temp)
            else -> null
        }

        return DeviceThermalState(
            temperatureCelsius = temp,
            thermalStatus = status,
            isWarm = isWarm,
            isOverheating = isOverheat,
            warningMessage = msg
        )
    }

    private fun updateState(celsius: Float? = null, status: Int? = null) {
        val current = _thermalState.value
        val newTemp = celsius ?: current.temperatureCelsius
        val newStatus = status ?: current.thermalStatus

        val isWarm = newTemp >= 39.0f || newStatus >= 2
        val isOverheat = newTemp >= 42.0f || newStatus >= 3
        val msg = when {
            isOverheat -> "⚠️ Overheat Alert: Device is at %.1f°C. Recording may experience frame drops or throttling.".format(newTemp)
            isWarm -> "⚠️ High Temperature: Device is at %.1f°C. Consider lowering resolution or cooling device.".format(newTemp)
            else -> null
        }

        _thermalState.value = DeviceThermalState(
            temperatureCelsius = newTemp,
            thermalStatus = newStatus,
            isWarm = isWarm,
            isOverheating = isOverheat,
            warningMessage = msg
        )
    }

    fun stop() {
        if (batteryReceiver != null) {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (_: Exception) {}
            batteryReceiver = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalListener != null && powerManager != null) {
            try {
                powerManager.removeThermalStatusListener(thermalListener as PowerManager.OnThermalStatusChangedListener)
            } catch (_: Exception) {}
            thermalListener = null
        }
    }
}

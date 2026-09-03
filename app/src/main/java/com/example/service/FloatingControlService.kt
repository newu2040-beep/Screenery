package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.data.repository.RecordingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingControlService : Service() {

    private var windowManager: WindowManager? = null
    private var floatView: LinearLayout? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var statusJob: Job? = null
    private var autoHideJob: Job? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isExpanded = false
    private var autoHideEnabled = true

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        try {
            if (!Settings.canDrawOverlays(this)) {
                stopSelf()
                return
            }

            val repo = RecordingRepository(applicationContext)
            autoHideEnabled = repo.currentConfig.value.autoHideFloatingBar

            windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            createFloatingBubbleView()

            statusJob = serviceScope.launch {
                ScreenRecordingService.recordingStatus.collectLatest { status ->
                    if (!status.isRecording) {
                        stopSelf()
                    } else {
                        updateView(status)
                    }
                }
            }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingBubbleView() {
        floatView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            elevation = dpToPx(10).toFloat()

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(28).toFloat()
                setColor(0xF0111827.toInt()) // Sleek dark slate
                setStroke(dpToPx(1), 0x40FFFFFF.toInt())
            }
            background = shape
        }

        // Circular Indicator Icon / Pill
        val circleBubble = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))

            val bubbleBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(20).toFloat()
                setColor(0x33EF4444.toInt())
            }
            background = bubbleBg
        }

        val dotIcon = TextView(this).apply {
            id = View.generateViewId()
            text = "●"
            setTextColor(0xFFEF4444.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }

        val timerText = TextView(this).apply {
            id = View.generateViewId()
            text = " 00:00"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        circleBubble.addView(dotIcon)
        circleBubble.addView(timerText)
        floatView?.addView(circleBubble)

        // Expanded Action Controls Layout
        val controlsLayout = LinearLayout(this).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            visibility = View.GONE
            setPadding(dpToPx(6), 0, dpToPx(4), 0)
        }

        val pauseResumeBtn = Button(this).apply {
            id = View.generateViewId()
            text = "Pause"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(Color.WHITE)
            val btnShape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(14).toFloat()
                setColor(0x446366F1.toInt())
            }
            background = btnShape
            setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))
            setOnClickListener {
                val isPaused = ScreenRecordingService.recordingStatus.value.isPaused
                val action = if (isPaused) ScreenRecordingService.ACTION_RESUME else ScreenRecordingService.ACTION_PAUSE
                startService(Intent(this@FloatingControlService, ScreenRecordingService::class.java).apply {
                    this.action = action
                })
            }
        }

        val stopBtn = Button(this).apply {
            id = View.generateViewId()
            text = "Stop & Save"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            val btnShape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(14).toFloat()
                setColor(0xFFEF4444.toInt())
            }
            background = btnShape
            setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))
            setOnClickListener {
                startService(Intent(this@FloatingControlService, ScreenRecordingService::class.java).apply {
                    action = ScreenRecordingService.ACTION_STOP
                })
            }
        }

        val btnParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(34)
        ).apply {
            marginStart = dpToPx(6)
        }

        controlsLayout.addView(pauseResumeBtn, btnParams)
        controlsLayout.addView(stopBtn, btnParams)
        floatView?.addView(controlsLayout)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 180
        }

        floatView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    scheduleAutoHideTimer()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager?.updateViewLayout(floatView, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 12 && diffY < 12) {
                        // Toggle control panel expansion on tap
                        isExpanded = !isExpanded
                        controlsLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                        try {
                            windowManager?.updateViewLayout(floatView, params)
                        } catch (_: Exception) {}
                    }
                    scheduleAutoHideTimer()
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(floatView, params)
            if (autoHideEnabled) {
                scheduleAutoHideTimer(delayMillis = 2500L)
            }
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun scheduleAutoHideTimer(delayMillis: Long = 3000L) {
        if (!autoHideEnabled) return
        autoHideJob?.cancel()
        autoHideJob = serviceScope.launch {
            delay(delayMillis)
            val currentStatus = ScreenRecordingService.recordingStatus.value
            // Only auto-hide if recording is active and not paused
            if (currentStatus.isRecording && !currentStatus.isPaused && floatView != null) {
                try {
                    floatView?.animate()?.alpha(0f)?.setDuration(400)?.withEndAction {
                        floatView?.visibility = View.GONE
                    }?.start()
                } catch (_: Exception) {
                    floatView?.visibility = View.GONE
                }
            }
        }
    }

    private fun updateView(status: RecordingStatus) {
        val minutes = status.elapsedSeconds / 60
        val seconds = status.elapsedSeconds % 60
        val timeFormatted = "%02d:%02d".format(minutes, seconds)

        val bubble = floatView?.getChildAt(0) as? LinearLayout
        val dotIcon = bubble?.getChildAt(0) as? TextView
        val timerText = bubble?.getChildAt(1) as? TextView

        if (status.isPaused) {
            // When paused, make sure the view is visible so the user is aware
            autoHideJob?.cancel()
            floatView?.visibility = View.VISIBLE
            floatView?.alpha = 1f
            dotIcon?.text = "⏸"
            dotIcon?.setTextColor(0xFFFBBF24.toInt()) // Amber
            timerText?.text = " $timeFormatted (Paused)"
        } else {
            dotIcon?.text = "●"
            dotIcon?.setTextColor(0xFFEF4444.toInt()) // Red
            timerText?.text = " $timeFormatted"

            // If the view just transitioned to active and autoHide is enabled, hide after delay
            if (autoHideEnabled && floatView?.visibility == View.VISIBLE && (autoHideJob == null || !autoHideJob!!.isActive)) {
                scheduleAutoHideTimer(delayMillis = 2000L)
            }
        }

        val controls = floatView?.getChildAt(1) as? LinearLayout
        val pauseResumeBtn = controls?.getChildAt(0) as? Button
        if (status.isPaused) {
            pauseResumeBtn?.text = "Resume"
        } else {
            pauseResumeBtn?.text = "Pause"
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        autoHideJob?.cancel()
        statusJob?.cancel()
        if (floatView != null) {
            try {
                windowManager?.removeView(floatView)
            } catch (_: Exception) {}
            floatView = null
        }
        super.onDestroy()
    }
}


package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val contentUri: String = "",
    val durationMs: Long = 0L,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Int = 60,
    val bitrateMbps: Int = 20,
    val sizeBytes: Long = 0L,
    val codec: String = "H.264 (AVC)",
    val audioSource: String = "Microphone",
    val dateAdded: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> "%.1f GB".format(gb)
                mb >= 1.0 -> "%.1f MB".format(mb)
                else -> "%.1f KB".format(kb)
            }
        }

    val specsSummary: String
        get() = "${width}x${height} • ${fps}FPS • ${bitrateMbps}Mbps"
}

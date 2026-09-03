package com.example.engine

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object VideoTrimmer {
    private const val TAG = "VideoTrimmer"
    private const val DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024 // 2MB buffer for high-res frames

    suspend fun trimVideo(
        srcPath: String,
        dstPath: String,
        startMs: Long,
        endMs: Long,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val srcFile = File(srcPath)
        if (!srcFile.exists() || srcFile.length() == 0L) {
            Log.e(TAG, "Source file does not exist: $srcPath")
            return@withContext false
        }

        val dstFile = File(dstPath)
        if (dstFile.exists()) {
            dstFile.delete()
        }

        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(srcPath)
            val trackCount = extractor.trackCount
            if (trackCount <= 0) {
                Log.e(TAG, "No tracks found in source video")
                extractor.release()
                return@withContext false
            }

            muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val indexMap = HashMap<Int, Int>()
            var videoTrackIndex = -1

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/")) {
                    val dstIndex = muxer.addTrack(format)
                    indexMap[i] = dstIndex
                    videoTrackIndex = i
                } else if (mime.startsWith("audio/")) {
                    val dstIndex = muxer.addTrack(format)
                    indexMap[i] = dstIndex
                }
            }

            if (indexMap.isEmpty()) {
                Log.e(TAG, "No video/audio tracks found to mux")
                muxer.release()
                extractor.release()
                return@withContext false
            }

            muxer.start()

            val startUs = (startMs * 1000L).coerceAtLeast(0L)
            val endUs = (endMs * 1000L).coerceAtLeast(startUs + 500_000L)
            val totalDurationUs = (endUs - startUs).coerceAtLeast(1L)

            val buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            // Process each track with its own dedicated offset
            for (trackIndex in 0 until trackCount) {
                val dstIndex = indexMap[trackIndex] ?: continue

                extractor.selectTrack(trackIndex)
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

                var firstPtsUs: Long = -1L
                var lastPtsUs: Long = -1L

                while (true) {
                    buffer.clear()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    val sampleTimeUs = extractor.sampleTime
                    if (sampleTimeUs > endUs) {
                        break
                    }

                    // Only write if at or past start point (or keyframe for video)
                    if (sampleTimeUs >= startUs || (trackIndex == videoTrackIndex && firstPtsUs < 0)) {
                        if (firstPtsUs < 0) {
                            firstPtsUs = sampleTimeUs
                        }

                        val pts = (sampleTimeUs - firstPtsUs).coerceAtLeast(0L)
                        // Guarantee strictly non-decreasing PTS for muxer
                        val monotonicPts = if (pts > lastPtsUs) pts else lastPtsUs + 1000L
                        lastPtsUs = monotonicPts

                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = monotonicPts
                        bufferInfo.flags = extractor.sampleFlags

                        muxer.writeSampleData(dstIndex, buffer, bufferInfo)

                        if (trackIndex == videoTrackIndex) {
                            val progress = ((sampleTimeUs - startUs).toFloat() / totalDurationUs).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }

                    if (!extractor.advance()) {
                        break
                    }
                }

                extractor.unselectTrack(trackIndex)
            }

            try {
                muxer.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Muxer stop warning", e)
            }
            muxer.release()
            extractor.release()

            val success = dstFile.exists() && dstFile.length() > 0
            if (success) {
                onProgress(1f)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error trimming video", e)
            try {
                muxer?.release()
            } catch (_: Exception) {}
            try {
                extractor.release()
            } catch (_: Exception) {}
            false
        }
    }
}

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
    private const val DEFAULT_BUFFER_SIZE = 1024 * 1024

    suspend fun trimVideo(
        srcPath: String,
        dstPath: String,
        startMs: Long,
        endMs: Long,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val srcFile = File(srcPath)
        if (!srcFile.exists()) {
            Log.e(TAG, "Source file does not exist: $srcPath")
            return@withContext false
        }

        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        try {
            extractor.setDataSource(srcPath)
            val trackCount = extractor.trackCount
            muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val indexMap = HashMap<Int, Int>(trackCount)
            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/") || mime.startsWith("audio/")) {
                    val dstIndex = muxer.addTrack(format)
                    indexMap[i] = dstIndex
                }
            }

            muxer.start()

            val startUs = startMs * 1000
            val endUs = endMs * 1000
            val totalDurationUs = (endUs - startUs).coerceAtLeast(1)

            val buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            val bufferInfo = MediaCodec.BufferInfo()

            for (trackIndex in 0 until trackCount) {
                if (!indexMap.containsKey(trackIndex)) continue

                extractor.selectTrack(trackIndex)
                extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

                val dstIndex = indexMap[trackIndex]!!

                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    val sampleTimeUs = extractor.sampleTime
                    if (sampleTimeUs > endUs) break

                    if (sampleTimeUs >= startUs) {
                        bufferInfo.offset = 0
                        bufferInfo.size = sampleSize
                        bufferInfo.presentationTimeUs = sampleTimeUs - startUs
                        bufferInfo.flags = extractor.sampleFlags
                        muxer.writeSampleData(dstIndex, buffer, bufferInfo)

                        val progress = ((sampleTimeUs - startUs).toFloat() / totalDurationUs).coerceIn(0f, 1f)
                        onProgress(progress)
                    }
                    extractor.advance()
                }
                extractor.unselectTrack(trackIndex)
            }

            muxer.stop()
            muxer.release()
            extractor.release()
            true
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

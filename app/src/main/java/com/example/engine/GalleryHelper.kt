package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object GalleryHelper {
    private const val TAG = "GalleryHelper"

    suspend fun saveVideoToGallery(
        context: Context,
        videoFile: File,
        title: String
    ): Uri? = withContext(Dispatchers.IO) {
        if (!videoFile.exists() || videoFile.length() == 0L) {
            Log.e(TAG, "Source video file does not exist or is empty: ${videoFile.absolutePath}")
            return@withContext null
        }

        try {
            val contentResolver = context.contentResolver
            val displayName = if (title.endsWith(".mp4", ignoreCase = true)) title else "$title.mp4"
            val nowSeconds = System.currentTimeMillis() / 1000

            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.TITLE, title)
                put(MediaStore.Video.Media.DATE_ADDED, nowSeconds)
                put(MediaStore.Video.Media.DATE_MODIFIED, nowSeconds)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/Screenery")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val itemUri = contentResolver.insert(collectionUri, contentValues)
            if (itemUri != null) {
                contentResolver.openOutputStream(itemUri)?.use { outStream ->
                    FileInputStream(videoFile).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    contentResolver.update(itemUri, contentValues, null, null)
                }

                Log.d(TAG, "Successfully saved video to MediaStore Gallery: $itemUri")
            }

            // Always scan both original and external copies for complete gallery index
            MediaScannerConnection.scanFile(
                context,
                arrayOf(videoFile.absolutePath),
                arrayOf("video/mp4"),
                null
            )

            itemUri
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video to Gallery MediaStore", e)
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(videoFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
            } catch (_: Exception) {}
            null
        }
    }
}

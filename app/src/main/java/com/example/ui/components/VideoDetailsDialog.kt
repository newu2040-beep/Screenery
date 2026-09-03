package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecordingItem
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.theme.ScreeneryTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsDialog(
    item: RecordingItem,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatted = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(item.dateAdded))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("video_details_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ScreeneryPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Video Details",
                            tint = ScreeneryPrimary
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = "Video Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScreeneryTextPrimary
                        )
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            color = ScreeneryTextSecondary,
                            maxLines = 1
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ScreeneryTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ScreenerySurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Resolution",
                            value = "${item.width} x ${item.height} (${getResLabel(item.width, item.height)})"
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Frame Rate",
                            value = "${item.fps} FPS"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Bitrate",
                            value = "${item.bitrateMbps} Mbps"
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "File Size",
                            value = item.formattedSize
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Duration",
                            value = item.formattedDuration
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Codec",
                            value = item.codec
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Audio",
                            value = item.audioSource
                        )
                        DetailItem(
                            modifier = Modifier.weight(1f),
                            label = "Date Recorded",
                            value = dateFormatted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "File Path",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextSecondary
            )
            Text(
                text = item.filePath,
                fontSize = 12.sp,
                color = ScreeneryTextTertiary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary)
            ) {
                Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun DetailItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ScreeneryTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ScreeneryTextPrimary
        )
    }
}

private fun getResLabel(width: Int, height: Int): String {
    val maxDim = maxOf(width, height)
    return when {
        maxDim >= 3840 -> "4K"
        maxDim >= 2560 -> "2K"
        maxDim >= 1920 -> "1080p"
        maxDim >= 1280 -> "720p"
        else -> "SD"
    }
}

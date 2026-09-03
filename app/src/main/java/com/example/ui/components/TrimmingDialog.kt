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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.ScreenerySecondary
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimmingDialog(
    item: RecordingItem,
    isTrimming: Boolean,
    trimProgress: Float,
    onTrim: (startMs: Long, endMs: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxDuration = item.durationMs.toFloat().coerceAtLeast(1000f)

    var startVal by remember { mutableFloatStateOf(0f) }
    var endVal by remember { mutableFloatStateOf(maxDuration) }

    ModalBottomSheet(
        onDismissRequest = { if (!isTrimming) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("trimming_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
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
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Trim Video",
                            tint = ScreeneryPrimary
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = "Trim Recording",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScreeneryTextPrimary
                        )
                        Text(
                            text = "Lossless fast trim (No re-encode)",
                            fontSize = 13.sp,
                            color = ScreeneryTextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isTrimming
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ScreeneryTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ScreenerySurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Start Time",
                                fontSize = 12.sp,
                                color = ScreeneryTextSecondary
                            )
                            Text(
                                text = formatMs(startVal.toLong()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScreeneryPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Trimmed Length",
                                fontSize = 12.sp,
                                color = ScreeneryTextSecondary
                            )
                            Text(
                                text = formatMs((endVal - startVal).toLong()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScreeneryTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "End Time",
                                fontSize = 12.sp,
                                color = ScreeneryTextSecondary
                            )
                            Text(
                                text = formatMs(endVal.toLong()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScreenerySecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    RangeSlider(
                        value = startVal..endVal,
                        onValueChange = { range ->
                            val start = range.start
                            val end = range.endInclusive
                            // Minimum 1 second duration
                            if (end - start >= 1000f) {
                                startVal = start
                                endVal = end
                            }
                        },
                        valueRange = 0f..maxDuration,
                        enabled = !isTrimming,
                        colors = SliderDefaults.colors(
                            thumbColor = ScreeneryPrimary,
                            activeTrackColor = ScreeneryPrimary,
                            inactiveTrackColor = Color(0xFFD1D5DB)
                        ),
                        modifier = Modifier.testTag("trim_range_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "00:00", fontSize = 11.sp, color = ScreeneryTextSecondary)
                        Text(text = formatMs(maxDuration.toLong()), fontSize = 11.sp, color = ScreeneryTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isTrimming) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { trimProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ScreeneryPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Trimming video... ${(trimProgress * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = ScreeneryTextSecondary
                    )
                }
            } else {
                Button(
                    onClick = {
                        onTrim(startVal.toLong(), endVal.toLong())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("export_trimmed_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Export Trimmed Video",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (ms % 1000) / 100
    return "%02d:%02d.%d".format(minutes, seconds, millis)
}

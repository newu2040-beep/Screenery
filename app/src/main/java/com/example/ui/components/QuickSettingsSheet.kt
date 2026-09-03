package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioSourceOption
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.VideoFps
import com.example.data.model.VideoResolution
import com.example.ui.theme.ScreeneryPillActiveBg
import com.example.ui.theme.ScreeneryPillActiveBorder
import com.example.ui.theme.ScreeneryPillInactive
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    config: RecordingConfig,
    deviceCapability: DeviceCapability,
    onConfigChanged: (RecordingConfig) -> Unit,
    onOpenFullSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Quick Config",
                            tint = ScreeneryPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Quick Configuration",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScreeneryTextPrimary
                    )
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

            // Resolution chips
            Text(
                text = "Resolution",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(VideoResolution.RES_720P, VideoResolution.RES_1080P, VideoResolution.RES_1440P, VideoResolution.RES_4K).forEach { res ->
                    val isSupported = deviceCapability.supportedResolutions.contains(res)
                    val isSelected = config.resolution == res
                    ChipItem(
                        modifier = Modifier.weight(1f),
                        title = res.label,
                        subtitle = res.subLabel,
                        selected = isSelected,
                        enabled = isSupported,
                        onClick = {
                            if (isSupported) {
                                onConfigChanged(config.copy(resolution = res))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // FPS chips
            Text(
                text = "Frame Rate (FPS)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(VideoFps.FPS_24, VideoFps.FPS_30, VideoFps.FPS_60, VideoFps.FPS_120).forEach { fps ->
                    val isSupported = deviceCapability.supportedFpsList.contains(fps)
                    val isSelected = config.fps == fps
                    ChipItem(
                        modifier = Modifier.weight(1f),
                        title = "${fps.fps}",
                        subtitle = fps.subLabel,
                        selected = isSelected,
                        enabled = isSupported,
                        onClick = {
                            if (isSupported) {
                                onConfigChanged(config.copy(fps = fps))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bitrate Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bitrate",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ScreeneryTextPrimary
                )
                Text(
                    text = "${config.bitrateMbps} Mbps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScreeneryPrimary
                )
            }
            Slider(
                value = config.bitrateMbps.toFloat(),
                onValueChange = { onConfigChanged(config.copy(bitrateMbps = it.toInt())) },
                valueRange = 1f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = ScreeneryPrimary,
                    activeTrackColor = ScreeneryPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Audio chips
            Text(
                text = "Audio Source",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AudioSourceOption.MICROPHONE, AudioSourceOption.SYSTEM, AudioSourceOption.BOTH, AudioSourceOption.NONE).forEach { src ->
                    val isSelected = config.audioSource == src
                    ChipItem(
                        modifier = Modifier.weight(1f),
                        title = src.label.split(" ").first(),
                        subtitle = "",
                        selected = isSelected,
                        enabled = true,
                        onClick = { onConfigChanged(config.copy(audioSource = src)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onOpenFullSettings()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPillActiveBg, contentColor = ScreeneryPrimary)
                ) {
                    Text("More Settings", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ChipItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        !enabled -> Color(0xFFF1F3F5)
        selected -> ScreeneryPillActiveBg
        else -> ScreeneryPillInactive
    }
    val border = when {
        !enabled -> Color.Transparent
        selected -> ScreeneryPillActiveBorder
        else -> Color.Transparent
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    !enabled -> Color(0xFF9CA3AF)
                    selected -> ScreeneryPrimary
                    else -> ScreeneryTextPrimary
                }
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = when {
                        !enabled -> Color(0xFF9CA3AF)
                        selected -> ScreeneryPrimary
                        else -> ScreeneryTextSecondary
                    }
                )
            }
        }
    }
}

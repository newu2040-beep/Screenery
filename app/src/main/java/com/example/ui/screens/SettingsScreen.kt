package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioSourceOption
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.VideoFps
import com.example.data.model.VideoOrientation
import com.example.data.model.VideoResolution
import com.example.ui.theme.ScreeneryBg
import com.example.ui.theme.ScreeneryPillActiveBg
import com.example.ui.theme.ScreeneryPillActiveBorder
import com.example.ui.theme.ScreeneryPillInactive
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreenerySecondary
import com.example.ui.theme.ScreenerySurface
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.theme.ScreeneryTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentConfig: RecordingConfig,
    deviceCapability: DeviceCapability,
    onConfigChanged: (RecordingConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var localConfig by remember(currentConfig) { mutableStateOf(currentConfig) }
    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreeneryBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Recording Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ScreeneryTextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ScreeneryTextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreeneryBg)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Resolution Selection
            item {
                SectionHeader(title = "Resolution")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val resolutions = listOf(
                        VideoResolution.RES_1080P,
                        VideoResolution.RES_1440P,
                        VideoResolution.RES_4K,
                        VideoResolution.RES_DEVICE
                    )
                    resolutions.forEach { res ->
                        val isSupported = deviceCapability.supportedResolutions.contains(res)
                        val isSelected = localConfig.resolution == res
                        PillSelector(
                            modifier = Modifier.weight(1f),
                            title = res.label,
                            subtitle = if (res == VideoResolution.RES_DEVICE) "Custom" else res.subLabel,
                            selected = isSelected,
                            enabled = isSupported,
                            onClick = {
                                if (isSupported) {
                                    localConfig = localConfig.copy(resolution = res)
                                    onConfigChanged(localConfig)
                                }
                            }
                        )
                    }
                }
                if (!deviceCapability.is4kSupported) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 4K resolution is locked because this device's hardware encoder does not support 4K.",
                        fontSize = 11.sp,
                        color = ScreeneryTextSecondary
                    )
                }
            }

            // Frame Rate (FPS) Selection
            item {
                SectionHeader(title = "Frame Rate (FPS)")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val fpsList = listOf(
                        VideoFps.FPS_24,
                        VideoFps.FPS_30,
                        VideoFps.FPS_60,
                        VideoFps.FPS_120
                    )
                    fpsList.forEach { fps ->
                        val isSupported = deviceCapability.supportedFpsList.contains(fps)
                        val isSelected = localConfig.fps == fps
                        PillSelector(
                            modifier = Modifier.weight(1f),
                            title = "${fps.fps}",
                            subtitle = fps.subLabel,
                            selected = isSelected,
                            enabled = isSupported,
                            onClick = {
                                if (isSupported) {
                                    localConfig = localConfig.copy(fps = fps)
                                    onConfigChanged(localConfig)
                                }
                            }
                        )
                    }
                }
                if (!deviceCapability.is120FpsSupported) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 120 FPS is disabled because your display refresh rate is ${deviceCapability.refreshRate.toInt()}Hz.",
                        fontSize = 11.sp,
                        color = ScreeneryTextSecondary
                    )
                }
            }

            // Bitrate (Mbps) Slider
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = "Bitrate (Mbps)")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScreeneryPillActiveBg)
                            .border(1.dp, ScreeneryPillActiveBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${localConfig.bitrateMbps}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScreeneryPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = localConfig.bitrateMbps.toFloat(),
                    onValueChange = {
                        localConfig = localConfig.copy(bitrateMbps = it.toInt())
                        onConfigChanged(localConfig)
                    },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = ScreeneryPrimary,
                        activeTrackColor = ScreeneryPrimary,
                        inactiveTrackColor = Color(0xFFE2E8F0)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "1 Mbps", fontSize = 12.sp, color = ScreeneryTextSecondary)
                    Text(text = "100 Mbps", fontSize = 12.sp, color = ScreeneryTextSecondary)
                }
            }

            // Orientation Selection
            item {
                SectionHeader(title = "Orientation")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrientationCard(
                        modifier = Modifier.weight(1f),
                        title = "Auto",
                        icon = Icons.Default.ScreenRotation,
                        selected = localConfig.orientation == VideoOrientation.AUTO,
                        onClick = {
                            localConfig = localConfig.copy(orientation = VideoOrientation.AUTO)
                            onConfigChanged(localConfig)
                        }
                    )
                    OrientationCard(
                        modifier = Modifier.weight(1f),
                        title = "Portrait",
                        icon = Icons.Default.StayCurrentPortrait,
                        selected = localConfig.orientation == VideoOrientation.PORTRAIT,
                        onClick = {
                            localConfig = localConfig.copy(orientation = VideoOrientation.PORTRAIT)
                            onConfigChanged(localConfig)
                        }
                    )
                    OrientationCard(
                        modifier = Modifier.weight(1f),
                        title = "Landscape",
                        icon = Icons.Default.StayCurrentLandscape,
                        selected = localConfig.orientation == VideoOrientation.LANDSCAPE,
                        onClick = {
                            localConfig = localConfig.copy(orientation = VideoOrientation.LANDSCAPE)
                            onConfigChanged(localConfig)
                        }
                    )
                }
            }

            // Audio Source Selection
            item {
                SectionHeader(title = "Audio Source")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AudioSourceOption.SYSTEM,
                        AudioSourceOption.MICROPHONE,
                        AudioSourceOption.BOTH,
                        AudioSourceOption.NONE
                    ).forEach { source ->
                        val isSelected = localConfig.audioSource == source
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    localConfig = localConfig.copy(audioSource = source)
                                    onConfigChanged(localConfig)
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) ScreeneryPillActiveBg else ScreeneryPillInactive
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) ScreeneryPillActiveBorder else Color.Transparent
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = source.label.split(" ").first(),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ScreeneryPrimary else ScreeneryTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Additional Options
            item {
                SectionHeader(title = "Additional Options")
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ScreenerySurface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        // Show touches
                        OptionSwitchRow(
                            title = "Show Touches",
                            subtitle = "Visual touch feedback during recording",
                            checked = localConfig.showTouches,
                            onCheckedChange = {
                                localConfig = localConfig.copy(showTouches = it)
                                onConfigChanged(localConfig)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Countdown
                        OptionPickerRow(
                            title = "Countdown",
                            value = if (localConfig.countdownSeconds == 0) "Off" else "${localConfig.countdownSeconds}s",
                            onClick = {
                                val next = when (localConfig.countdownSeconds) {
                                    0 -> 3
                                    3 -> 5
                                    5 -> 10
                                    else -> 0
                                }
                                localConfig = localConfig.copy(countdownSeconds = next)
                                onConfigChanged(localConfig)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Floating controls
                        OptionSwitchRow(
                            title = "Floating Controls",
                            subtitle = "Overlay pill (● 00:42) with quick pause/stop",
                            checked = localConfig.floatingControls,
                            onCheckedChange = {
                                if (it && !Settings.canDrawOverlays(context)) {
                                    try {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                                localConfig = localConfig.copy(floatingControls = it)
                                onConfigChanged(localConfig)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Auto-stop timer
                        OptionPickerRow(
                            title = "Auto-stop Timer",
                            value = if (localConfig.autoStopMinutes == 0) "Off" else "${localConfig.autoStopMinutes} min",
                            onClick = {
                                val next = when (localConfig.autoStopMinutes) {
                                    0 -> 1
                                    1 -> 5
                                    5 -> 10
                                    10 -> 30
                                    else -> 0
                                }
                                localConfig = localConfig.copy(autoStopMinutes = next)
                                onConfigChanged(localConfig)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Hide status bar
                        OptionSwitchRow(
                            title = "Hide Status Bar",
                            subtitle = "Full screen record without top system indicators",
                            checked = localConfig.hideStatusBar,
                            onCheckedChange = {
                                localConfig = localConfig.copy(hideStatusBar = it)
                                onConfigChanged(localConfig)
                            }
                        )
                    }
                }
            }

            // Device Diagnostics & Detection
            item {
                SectionHeader(title = "Hardware Detection")
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ScreenerySurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiagnosticRow(label = "Display Refresh Rate", value = "${deviceCapability.refreshRate.toInt()} Hz")
                        DiagnosticRow(label = "Native Resolution", value = "${deviceCapability.deviceWidth} x ${deviceCapability.deviceHeight}")
                        DiagnosticRow(label = "Hardware HEVC/H.265", value = if (deviceCapability.isHevcSupported) "Supported" else "Not available")
                        DiagnosticRow(label = "HDR Display", value = if (deviceCapability.isHdrSupported) "Supported" else "SDR")
                        val availMb = deviceCapability.availableStorageBytes / (1024 * 1024)
                        DiagnosticRow(label = "Free Storage Space", value = if (availMb > 1024) "%.1f GB".format(availMb / 1024.0) else "$availMb MB")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Bottom Save Settings Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScreeneryBg)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    onConfigChanged(localConfig)
                    showSavedMessage = true
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary)
            ) {
                Text(
                    text = "Save Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = ScreeneryTextPrimary
    )
}

@Composable
private fun PillSelector(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        !enabled -> Color(0xFFF3F4F6)
        selected -> ScreeneryPillActiveBg
        else -> ScreenerySurface
    }
    val border = when {
        !enabled -> Color.Transparent
        selected -> ScreeneryPillActiveBorder
        else -> Color(0xFFE5E7EB)
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
                .padding(vertical = 12.dp, horizontal = 4.dp),
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
            Spacer(modifier = Modifier.height(2.dp))
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

@Composable
private fun OrientationCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) ScreeneryPillActiveBg else ScreenerySurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (selected) ScreeneryPillActiveBorder else Color(0xFFE5E7EB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) ScreeneryPrimary else ScreeneryTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) ScreeneryPrimary else ScreeneryTextPrimary
            )
        }
    }
}

@Composable
private fun OptionSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = ScreeneryTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ScreeneryPrimary
            )
        )
    }
}

@Composable
private fun OptionPickerRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ScreeneryTextPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ScreeneryTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = ScreeneryTextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ScreeneryTextPrimary)
    }
}

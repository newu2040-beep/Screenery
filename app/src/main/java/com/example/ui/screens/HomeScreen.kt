package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceCapability
import com.example.data.model.RecordingConfig
import com.example.data.model.RecordingItem
import com.example.service.RecordingStatus
import com.example.ui.theme.ScreeneryBg
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreeneryRecordRed
import com.example.ui.theme.ScreenerySurface
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.theme.ScreeneryTextTertiary

@Composable
fun HomeScreen(
    config: RecordingConfig,
    deviceCapability: DeviceCapability,
    recordingStatus: RecordingStatus,
    recentRecordings: List<RecordingItem>,
    onStartRecordingClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onStopRecordingClick: () -> Unit,
    onSeeAllQuickSettings: () -> Unit,
    onSeeAllRecordings: () -> Unit,
    onPlayRecording: (RecordingItem) -> Unit,
    onTrimRecording: (RecordingItem) -> Unit,
    onShareRecording: (RecordingItem) -> Unit,
    onDeleteRecording: (RecordingItem) -> Unit,
    onDetailsRecording: (RecordingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ScreeneryBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Welcome Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Rahul \uD83D\uDC4B",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScreeneryTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ready to record something great?",
                        fontSize = 14.sp,
                        color = ScreeneryTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)), // Soft warm crown pill
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium Status",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Hero Recording Card
        item {
            HeroRecordingCard(
                config = config,
                status = recordingStatus,
                onStartClick = onStartRecordingClick,
                onPauseResumeClick = onPauseResumeClick,
                onStopClick = onStopRecordingClick
            )
            Spacer(modifier = Modifier.height(26.dp))
        }

        // Quick Settings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScreeneryTextPrimary
                )
                TextButton(onClick = onSeeAllQuickSettings) {
                    Text(
                        text = "See All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScreeneryPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2x2 Grid of Quick Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickSettingCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Tv,
                        title = "Resolution",
                        value = "${config.resolution.label} (${config.resolution.width}p)".replace("0p", "Native"),
                        iconTint = Color(0xFF6366F1),
                        onClick = onSeeAllQuickSettings
                    )
                    QuickSettingCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.GraphicEq,
                        title = "Frame Rate",
                        value = "${config.fps.fps} FPS",
                        iconTint = Color(0xFF8B5CF6),
                        onClick = onSeeAllQuickSettings
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickSettingCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        title = "Bitrate",
                        value = "${config.bitrateMbps} Mbps",
                        iconTint = Color(0xFF3B82F6),
                        onClick = onSeeAllQuickSettings
                    )
                    QuickSettingCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ScreenRotation,
                        title = "Orientation",
                        value = config.orientation.label,
                        iconTint = Color(0xFFA855F7),
                        onClick = onSeeAllQuickSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }

        // Recent Recordings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Recordings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScreeneryTextPrimary
                )
                TextButton(onClick = onSeeAllRecordings) {
                    Text(
                        text = "See All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ScreeneryPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recentRecordings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ScreenerySurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No recordings yet",
                            fontWeight = FontWeight.Bold,
                            color = ScreeneryTextPrimary,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the big button above to start your first recording!",
                            color = ScreeneryTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(recentRecordings, key = { it.id }) { item ->
                RecentRecordingItemRow(
                    item = item,
                    onPlay = { onPlayRecording(item) },
                    onTrim = { onTrimRecording(item) },
                    onShare = { onShareRecording(item) },
                    onDelete = { onDeleteRecording(item) },
                    onDetails = { onDetailsRecording(item) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun HeroRecordingCard(
    config: RecordingConfig,
    status: RecordingStatus,
    onStartClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .testTag("hero_recording_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B61F4),
                            Color(0xFF7E44F3)
                        )
                    )
                )
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!status.isRecording) {
                    // Start Recording state
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .clickable(onClick = onStartClick)
                            .testTag("start_recording_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.40f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Start Recording",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Tap to start",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Configuration pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.18f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = config.summaryString,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                } else {
                    // Actively recording state
                    val minutes = status.elapsedSeconds / 60
                    val seconds = status.elapsedSeconds % 60
                    val timeFormatted = "%02d:%02d".format(minutes, seconds)

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(if (!status.isPaused) pulseScale else 1f)
                            .clip(CircleShape)
                            .background(
                                if (status.isPaused) Color(0x33F59E0B) else Color(0x44EF4444)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (status.isPaused) Color(0xFFF59E0B) else ScreeneryRecordRed
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (status.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (status.isPaused) "Recording Paused" else "Recording in Progress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeFormatted,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Controls row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable(onClick = onPauseResumeClick)
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                .testTag("hero_pause_resume_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (status.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (status.isPaused) "Resume" else "Pause",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEF4444))
                                .clickable(onClick = onStopClick)
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .testTag("hero_stop_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stop",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSettingCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenerySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                color = ScreeneryTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ScreeneryTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun RecentRecordingItemRow(
    item: RecordingItem,
    onPlay: () -> Unit,
    onTrim: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPlay)
            .testTag("recent_recording_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenerySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF312E81), Color(0xFF6B21A8))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScreeneryTextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.specsSummary,
                    fontSize = 12.sp,
                    color = ScreeneryTextSecondary,
                    maxLines = 1
                )
            }

            // Duration
            Text(
                text = item.formattedDuration,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextTertiary
            )

            // Overflow Menu
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = ScreeneryTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play") },
                        onClick = {
                            menuExpanded = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Trim") },
                        onClick = {
                            menuExpanded = false
                            onTrim()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            menuExpanded = false
                            onDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ScreeneryRecordRed) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

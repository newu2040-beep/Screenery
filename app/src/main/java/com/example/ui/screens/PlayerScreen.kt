package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.RecordingItem
import com.example.ui.theme.ScreeneryBg
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreeneryRecordRed
import com.example.ui.theme.ScreenerySurface
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.theme.ScreeneryTextTertiary
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(UnstableApi::class)
@ExperimentalMaterial3Api
@Composable
fun PlayerScreen(
    item: RecordingItem,
    onBack: () -> Unit,
    onTrim: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoFile = remember(item.filePath) { File(item.filePath) }
    val fileExists = remember(item.filePath) { videoFile.exists() && videoFile.length() > 0 }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val exoPlayer = remember(item.filePath) {
        ExoPlayer.Builder(context).build().apply {
            if (fileExists) {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(videoFile)))
                prepare()
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Timer loop for tracking progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            delay(200)
        }
    }

    val totalDurationMs = if (fileExists && exoPlayer.duration > 0) exoPlayer.duration else item.durationMs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreeneryBg)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ScreeneryTextPrimary,
                    maxLines = 1
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
            actions = {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = ScreeneryTextPrimary
                    )
                }
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = {
                            showMoreMenu = false
                            onDetails()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            showMoreMenu = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ScreeneryRecordRed) },
                        onClick = {
                            showMoreMenu = false
                            onDelete()
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreeneryBg)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Player Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (fileExists) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = false
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Cinematic simulation / placeholder view
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Preview • ${item.width}x${item.height} @ ${item.fps}fps",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Center Play/Pause button
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                        isPlaying = false
                                    } else {
                                        if (fileExists) {
                                            exoPlayer.play()
                                        }
                                        isPlaying = !isPlaying
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Bottom Player Controls Strip
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Slider(
                                    value = currentPositionMs.toFloat().coerceIn(0f, totalDurationMs.toFloat().coerceAtLeast(1f)),
                                    onValueChange = { pos ->
                                        currentPositionMs = pos.toLong()
                                        if (fileExists) {
                                            exoPlayer.seekTo(currentPositionMs)
                                        }
                                    },
                                    valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = ScreeneryPrimary,
                                        activeTrackColor = ScreeneryPrimary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${formatTimer(currentPositionMs)} / ${formatTimer(totalDurationMs)}",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Speed button
                                        Box {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.2f))
                                                    .clickable { showSpeedMenu = true }
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "${playbackSpeed}x",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showSpeedMenu,
                                                onDismissRequest = { showSpeedMenu = false }
                                            ) {
                                                listOf(0.5f, 1f, 1.25f, 1.5f, 2f).forEach { spd ->
                                                    DropdownMenuItem(
                                                        text = { Text("${spd}x") },
                                                        onClick = {
                                                            playbackSpeed = spd
                                                            exoPlayer.setPlaybackSpeed(spd)
                                                            showSpeedMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action Row: Trim, Share, Delete, Details
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlayerActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ContentCut,
                        label = "Trim",
                        onClick = onTrim
                    )
                    PlayerActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = onShare
                    )
                    PlayerActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        onClick = onDelete
                    )
                    PlayerActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Info,
                        label = "Details",
                        onClick = onDetails
                    )
                }
            }

            // Video Info Card (matching Screen 4 of mockup)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ScreenerySurface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Video Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScreeneryTextPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val dateFormatted = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(item.dateAdded))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoCol(modifier = Modifier.weight(1f), label = "Resolution", value = "${item.width} x ${item.height}")
                            InfoCol(modifier = Modifier.weight(1f), label = "Frame Rate", value = "${item.fps} FPS")
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoCol(modifier = Modifier.weight(1f), label = "Bitrate", value = "${item.bitrateMbps} Mbps")
                            InfoCol(modifier = Modifier.weight(1f), label = "File Size", value = item.formattedSize)
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoCol(modifier = Modifier.weight(1f), label = "Duration", value = item.formattedDuration)
                            InfoCol(modifier = Modifier.weight(1f), label = "Date", value = dateFormatted)
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            InfoCol(modifier = Modifier.weight(1f), label = "Codec", value = item.codec)
                            InfoCol(modifier = Modifier.weight(1f), label = "Audio Source", value = item.audioSource)
                        }
                    }
                }
            }

            // Primary Button: Edit Recording
            item {
                Button(
                    onClick = onTrim,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("edit_recording_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScreeneryPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Recording",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlayerActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenerySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (label == "Delete") ScreeneryRecordRed else ScreeneryPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (label == "Delete") ScreeneryRecordRed else ScreeneryTextPrimary
            )
        }
    }
}

@Composable
private fun InfoCol(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = ScreeneryTextSecondary
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ScreeneryTextPrimary
        )
    }
}

private fun formatTimer(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%02d:%02d".format(min, sec)
}

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecordingItem
import com.example.ui.theme.ScreeneryBg
import com.example.ui.theme.ScreeneryPrimary
import com.example.ui.theme.ScreeneryRecordRed
import com.example.ui.theme.ScreenerySurface
import com.example.ui.theme.ScreenerySurfaceVariant
import com.example.ui.theme.ScreeneryTextPrimary
import com.example.ui.theme.ScreeneryTextSecondary
import com.example.ui.theme.ScreeneryTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    recordings: List<RecordingItem>,
    onPlay: (RecordingItem) -> Unit,
    onTrim: (RecordingItem) -> Unit,
    onRename: (RecordingItem) -> Unit,
    onShare: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    onDetails: (RecordingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredList = recordings.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "4K" -> item.width >= 3840 || item.height >= 3840
            "1080p" -> item.width == 1920 || item.height == 1920
            "60+ FPS" -> item.fps >= 60
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalSizeMb = recordings.sumOf { it.sizeBytes } / (1024 * 1024.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreeneryBg)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title & Summary Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recordings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ScreeneryTextPrimary
                )
                Text(
                    text = "${recordings.size} videos • ${if (totalSizeMb >= 1024) "%.1f GB".format(totalSizeMb / 1024.0) else "%.1f MB".format(totalSizeMb)} used",
                    fontSize = 13.sp,
                    color = ScreeneryTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(ScreeneryPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = ScreeneryPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search recordings...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ScreeneryTextSecondary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ScreenerySurface,
                unfocusedContainerColor = ScreenerySurface,
                focusedBorderColor = ScreeneryPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recordings_search_bar")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "4K", "1080p", "60+ FPS").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScreeneryPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = ScreenerySurface
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of recordings
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(ScreenerySurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = ScreeneryTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching recordings found" else "No recordings yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScreeneryTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recorded videos will appear here ready to play and edit",
                        fontSize = 13.sp,
                        color = ScreeneryTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    RecordingCardItem(
                        item = item,
                        onPlay = { onPlay(item) },
                        onTrim = { onTrim(item) },
                        onRename = { onRename(item) },
                        onShare = { onShare(item) },
                        onDelete = { onDelete(item) },
                        onDetails = { onDetails(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingCardItem(
    item: RecordingItem,
    onPlay: () -> Unit,
    onTrim: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDetails: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateString = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(item.dateAdded))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onPlay)
            .testTag("recording_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenerySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Box
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 62.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF3730A3), Color(0xFF6B21A8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Duration overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.formattedDuration,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScreeneryTextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.specsSummary} • ${item.formattedSize}",
                        fontSize = 12.sp,
                        color = ScreeneryTextSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateString,
                        fontSize = 11.sp,
                        color = ScreeneryTextTertiary
                    )
                }

                // 3-dots Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = ScreeneryTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Video") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                            onClick = {
                                menuExpanded = false
                                onPlay()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Trim (Edit)") },
                            leadingIcon = { Icon(Icons.Default.ContentCut, null) },
                            onClick = {
                                menuExpanded = false
                                onTrim()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Video Details") },
                            leadingIcon = { Icon(Icons.Default.Info, null) },
                            onClick = {
                                menuExpanded = false
                                onDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ScreeneryRecordRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ScreeneryRecordRed) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    title = "Play",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlay
                )
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    title = "Trim",
                    icon = Icons.Default.ContentCut,
                    onClick = onTrim
                )
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    title = "Share",
                    icon = Icons.Default.Share,
                    onClick = onShare
                )
                QuickActionPill(
                    modifier = Modifier.weight(1f),
                    title = "Details",
                    icon = Icons.Default.Info,
                    onClick = onDetails
                )
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ScreenerySurfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ScreeneryPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ScreeneryTextPrimary
            )
        }
    }
}

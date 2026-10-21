package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.MobileFrameStyle

@Composable
fun MobileDeviceFrame(
    frameStyle: MobileFrameStyle,
    isPortrait: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    if (frameStyle == MobileFrameStyle.NONE) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val cornerRadius: Dp = frameStyle.cornerRadiusDp.dp
    val bezelWidth: Dp = frameStyle.bezelWidthDp.dp
    val frameColor = Color(frameStyle.frameColorLong)

    val frameBorderBrush = when (frameStyle) {
        MobileFrameStyle.CHROME_LUXURY -> Brush.linearGradient(
            listOf(
                Color(0xFFE2E8F0),
                Color(0xFF94A3B8),
                Color(0xFFF8FAFC),
                Color(0xFF64748B)
            )
        )
        MobileFrameStyle.FLAGSHIP_PRO -> Brush.linearGradient(
            listOf(
                Color(0xFF334155),
                Color(0xFF0F172A),
                Color(0xFF1E293B)
            )
        )
        MobileFrameStyle.DARK_STEALTH -> Brush.linearGradient(
            listOf(
                Color(0xFF18181B),
                Color(0xFF09090B),
                Color(0xFF27272A)
            )
        )
        else -> Brush.linearGradient(
            listOf(
                frameColor.copy(alpha = 0.95f),
                frameColor,
                frameColor.copy(alpha = 0.85f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(cornerRadius + bezelWidth),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(RoundedCornerShape(cornerRadius + bezelWidth))
            .background(frameBorderBrush)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius + bezelWidth)
            )
            .padding(bezelWidth)
    ) {
        // Inner Screen Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Black)
        ) {
            // Render the video player or preview content inside the screen
            content()

            // Top Hardware Elements (Dynamic Island / Camera Punch Hole / Speaker)
            if (isPortrait) {
                // Portrait Top Notch / Camera
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                ) {
                    if (frameStyle.hasDynamicIsland) {
                        // Dynamic Island pill
                        Box(
                            modifier = Modifier
                                .size(width = 72.dp, height = 18.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                                .border(0.7.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A))
                                )
                            }
                        }
                    } else if (frameStyle.hasCameraPunchHole) {
                        // Minimal camera punch hole
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF09090B))
                                .border(0.5.dp, Color(0xFF27272A), CircleShape)
                        )
                    }
                }

                // Speaker slit at absolute top bezel
                if (frameStyle.hasSpeakerGrille) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 1.dp)
                            .size(width = 32.dp, height = 2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF334155))
                    )
                }

                // Bottom Home Indicator Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .size(width = 76.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.35f))
                )
            } else {
                // Landscape Hardware Elements (minimal side camera indicator)
                if (frameStyle.hasCameraPunchHole || frameStyle.hasDynamicIsland) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF09090B))
                            .border(0.5.dp, Color(0xFF27272A), CircleShape)
                    )
                }
            }
        }
    }
}

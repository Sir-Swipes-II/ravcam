package com.ravcam.vcam.ui.screens.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.ui.theme.RavAmber
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavBlue
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavGreen
import com.ravcam.vcam.ui.theme.RavMagenta
import com.ravcam.vcam.ui.theme.RavSurface
import com.ravcam.vcam.ui.theme.RavTextMuted
import com.ravcam.vcam.ui.theme.RavTextPrimary
import com.ravcam.vcam.ui.theme.RavTextSecondary

@Composable
fun PreviewScreen(
    activeSource: RavMediaSource?,
    modifier: Modifier = Modifier
) {
    var isPreviewRunning by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(activeSource?.id) {
        isPreviewRunning = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RavBackgroundDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 140.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            PreviewHeader(
                isPreviewRunning = isPreviewRunning
            )

            PreviewViewport(
                activeSource = activeSource,
                isPreviewRunning = isPreviewRunning
            )

            PreviewControlCard(
                activeSource = activeSource,
                isPreviewRunning = isPreviewRunning,
                onTogglePreview = {
                    if (activeSource != null) {
                        isPreviewRunning = !isPreviewRunning
                    }
                }
            )

            PreviewDiagnosticsCard(
                activeSource = activeSource,
                isPreviewRunning = isPreviewRunning
            )
        }
    }
}

@Composable
private fun PreviewHeader(
    isPreviewRunning: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "PREVIEW",
                    color = RavTextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "MEDIA MONITOR",
                    color = RavCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            PreviewStatusPill(
                text = if (isPreviewRunning) "RUNNING" else "STANDBY",
                color = if (isPreviewRunning) RavGreen else RavAmber
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Inspect the currently active source before output routing.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun PreviewViewport(
    activeSource: RavMediaSource?,
    isPreviewRunning: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF020610),
                        RavSurface.copy(alpha = 0.82f),
                        Color(0xFF030711)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        RavCyan.copy(alpha = 0.82f),
                        RavBlue.copy(alpha = 0.3f),
                        RavMagenta.copy(alpha = 0.62f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    activeSource == null -> "NO SIGNAL"
                    isPreviewRunning -> activeSource.type.shortCode
                    else -> "SOURCE READY"
                },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = when {
                    activeSource == null -> RavTextMuted
                    isPreviewRunning -> RavCyan
                    else -> RavTextSecondary
                },
                fontSize = if (isPreviewRunning) 32.sp else 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    activeSource == null ->
                        "Activate a source from the Sources screen."

                    isPreviewRunning ->
                        activeSource.name

                    else ->
                        "Press Start Preview to initialize the renderer."
                },
                color = RavTextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun PreviewControlCard(
    activeSource: RavMediaSource?,
    isPreviewRunning: Boolean,
    onTogglePreview: () -> Unit
) {
    PreviewGlassPanel {
        Text(
            text = "PREVIEW CONTROL",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = activeSource?.name ?: "No Active Source",
            color = RavTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = activeSource?.location
                ?: "Return to Sources and activate an MP4, image, GIF, or network stream.",
            color = RavTextMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onTogglePreview,
            enabled = activeSource != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = when {
                    activeSource == null -> RavTextMuted.copy(alpha = 0.3f)
                    isPreviewRunning -> RavMagenta
                    else -> RavCyan
                }
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = when {
                    activeSource == null -> RavSurface.copy(alpha = 0.3f)
                    isPreviewRunning -> RavMagenta.copy(alpha = 0.13f)
                    else -> RavCyan.copy(alpha = 0.13f)
                },
                disabledContainerColor = RavSurface.copy(alpha = 0.28f),
                contentColor = RavTextPrimary,
                disabledContentColor = RavTextMuted
            )
        ) {
            Text(
                text = when {
                    activeSource == null -> "NO ACTIVE SOURCE"
                    isPreviewRunning -> "STOP PREVIEW"
                    else -> "START PREVIEW"
                },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        }
    }
}

@Composable
private fun PreviewDiagnosticsCard(
    activeSource: RavMediaSource?,
    isPreviewRunning: Boolean
) {
    PreviewGlassPanel {
        Text(
            text = "RENDER STATUS",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        PreviewDataRow(
            label = "Source Type",
            value = activeSource?.type?.shortCode ?: "None"
        )

        PreviewDataRow(
            label = "Source State",
            value = if (activeSource != null) "Active" else "Inactive"
        )

        PreviewDataRow(
            label = "Renderer",
            value = if (isPreviewRunning) "Initialized" else "Standby"
        )

        PreviewDataRow(
            label = "Output",
            value = "Preview Only"
        )

        PreviewDataRow(
            label = "Frame Surface",
            value = if (isPreviewRunning) "Prepared" else "Waiting"
        )
    }
}

@Composable
private fun PreviewDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label.uppercase(),
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Text(
            text = value,
            color = RavTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PreviewStatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.76f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun PreviewGlassPanel(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        RavSurface.copy(alpha = 0.82f),
                        RavSurface.copy(alpha = 0.54f),
                        Color.White.copy(alpha = 0.035f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        RavCyan.copy(alpha = 0.72f),
                        RavBlue.copy(alpha = 0.22f),
                        RavMagenta.copy(alpha = 0.42f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(padding)
    ) {
        content()
    }
}
package com.ravcam.vcam.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravcam.vcam.ui.theme.RavAmber
import com.ravcam.vcam.ui.theme.RavBackground
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavBlue
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavCyanSoft
import com.ravcam.vcam.ui.theme.RavGreen
import com.ravcam.vcam.ui.theme.RavMagenta
import com.ravcam.vcam.ui.theme.RavPurple
import com.ravcam.vcam.ui.theme.RavRed
import com.ravcam.vcam.ui.theme.RavSurface
import com.ravcam.vcam.ui.theme.RavTextMuted
import com.ravcam.vcam.ui.theme.RavTextPrimary
import com.ravcam.vcam.ui.theme.RavTextSecondary
import android.os.Build
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.RavOutputProfile
import com.ravcam.vcam.ui.state.RavPreviewSessionState
import com.ravcam.vcam.ui.state.supportsInAppPreview

@Composable
fun RavCamDashboard(
    activeSource: RavMediaSource?,
    outputProfile: RavOutputProfile,
    previewSessionState: RavPreviewSessionState,
    onOpenSources: () -> Unit,
    onOpenPreview: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            HeaderSection(
                activeSource = activeSource,
                isPreviewRunning =
                    previewSessionState.isRunning
            )

            VCamStatusCard(
                activeSource = activeSource,
                outputProfile = outputProfile,
                isPreviewRunning =
                    previewSessionState.isRunning
            )

            PrimaryActions(
                activeSource = activeSource,
                previewSessionState =
                    previewSessionState,
                onOpenSources = onOpenSources,
                onOpenPreview = onOpenPreview,
                onOpenSettings = onOpenSettings
            )

            QuickStatusGrid(
                activeSource = activeSource,
                outputProfile = outputProfile
            )

            SystemSignalCard(
                activeSource = activeSource,
                isPreviewRunning =
                    previewSessionState.isRunning,
                onOpenDiagnostics =
                    onOpenDiagnostics
            )
        }
    }
}

@Composable
private fun HeaderSection(
    activeSource: RavMediaSource?,
    isPreviewRunning: Boolean
) {
    val statusText = when {
        isPreviewRunning -> "RUNNING"
        activeSource != null -> "READY"
        else -> "IDLE"
    }

    val statusColor = when {
        isPreviewRunning -> RavGreen
        activeSource != null -> RavCyan
        else -> RavAmber
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "RAVCAM",
                    color = RavTextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "MEDIA CONTROL CORE",
                    color = RavCyanSoft,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            StatusPill(
                text = statusText,
                color = statusColor
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text =
                "Source routing, media preview, transformation, and output profile management.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun VCamStatusCard(
    activeSource: RavMediaSource?,
    outputProfile: RavOutputProfile,
    isPreviewRunning: Boolean
) {
    val statusTitle = when {
        isPreviewRunning ->
            "Preview Active"

        activeSource != null ->
            "Source Ready"

        else ->
            "No Active Source"
    }

    val statusSubtitle = when {
        isPreviewRunning ->
            "In-app media renderer running"

        activeSource != null ->
            "Ready to initialize preview"

        else ->
            "Configure and activate a source"
    }

    val progress = when {
        isPreviewRunning -> 1f
        activeSource != null -> 0.68f
        else -> 0.16f
    }

    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "SYSTEM STATUS",
                    color = RavCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = statusTitle,
                    color = RavTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = statusSubtitle,
                    color = RavTextSecondary,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                RavCyan.copy(alpha = 0.35f),
                                RavBlue.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                RavCyan,
                                RavMagenta
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPreviewRunning) {
                        "▶"
                    } else {
                        "R"
                    },
                    color = RavCyan,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        HudDataRow(
            label = "Selected Source",
            value = activeSource?.name ?: "None"
        )

        HudDataRow(
            label = "Source Type",
            value =
                activeSource?.type?.shortCode
                    ?: "None"
        )

        HudDataRow(
            label = "Active Profile",
            value =
                "${outputProfile.resolution.shortLabel} • " +
                        outputProfile.frameRate.label
        )

        HudDataRow(
            label = "Output Layer",
            value = if (isPreviewRunning) {
                "Preview Renderer"
            } else {
                "Standby"
            }
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        LinearProgressIndicator(
            progress = {
                progress
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(
                    RoundedCornerShape(999.dp)
                ),
            color = if (isPreviewRunning) {
                RavGreen
            } else {
                RavCyan
            },
            trackColor =
                RavCyan.copy(alpha = 0.12f)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = when {
                isPreviewRunning ->
                    "Media renderer active"

                activeSource != null ->
                    "Source and profile prepared"

                else ->
                    "Awaiting source selection"
            },
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PrimaryActions(
    activeSource: RavMediaSource?,
    previewSessionState: RavPreviewSessionState,
    onOpenSources: () -> Unit,
    onOpenPreview: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val mainButtonText = when {
        activeSource == null ->
            "SELECT A SOURCE"

        activeSource.type.supportsInAppPreview() ->
            "START PREVIEW"

        else ->
            "OPEN OBS PREVIEW INFO"
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        HudButton(
            text = mainButtonText,
            accent = RavCyan,
            onClick = {
                when {
                    activeSource == null -> {
                        onOpenSources()
                    }

                    activeSource.type
                        .supportsInAppPreview() -> {
                        previewSessionState.start(
                            source = activeSource
                        )

                        onOpenPreview()
                    }

                    else -> {
                        onOpenPreview()
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            HudButton(
                text = "SELECT SOURCE",
                accent = RavMagenta,
                modifier = Modifier.weight(1f),
                onClick = onOpenSources
            )

            HudButton(
                text = "SETTINGS",
                accent = RavBlue,
                modifier = Modifier.weight(1f),
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun QuickStatusGrid(
    activeSource: RavMediaSource?,
    outputProfile: RavOutputProfile
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        MiniStatusCard(
            label = "ANDROID",
            value = Build.VERSION.RELEASE
                ?: "SDK ${Build.VERSION.SDK_INT}",
            color = RavCyan,
            modifier = Modifier.weight(1f)
        )

        MiniStatusCard(
            label = "SOURCE",
            value =
                activeSource?.type?.shortCode
                    ?: "NONE",
            color = if (activeSource != null) {
                RavGreen
            } else {
                RavAmber
            },
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        MiniStatusCard(
            label = "PROFILE",
            value =
                outputProfile.resolution
                    .shortLabel
                    .replace(" × ", "x"),
            color = RavBlue,
            modifier = Modifier.weight(1f)
        )

        MiniStatusCard(
            label = "STORAGE",
            value = "READY",
            color = RavGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SystemSignalCard(
    activeSource: RavMediaSource?,
    isPreviewRunning: Boolean,
    onOpenDiagnostics: () -> Unit
) {
    GlassCard {
        Text(
            text = "SYSTEM SIGNAL",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        HudDataRow(
            label = "Media Engine",
            value = when {
                isPreviewRunning -> "Running"
                activeSource != null -> "Ready"
                else -> "Standby"
            }
        )

        HudDataRow(
            label = "Preview Session",
            value = if (isPreviewRunning) {
                "Active"
            } else {
                "Stopped"
            }
        )

        HudDataRow(
            label = "Output Adapter",
            value = "Preview Only"
        )

        HudDataRow(
            label = "External Output",
            value = "Not Attached"
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        HudButton(
            text = "OPEN DIAGNOSTICS",
            accent = RavGreen,
            onClick = onOpenDiagnostics
        )
    }
}

@Composable
private fun BottomNavMock() {
    GlassCard(
        padding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavItem("HOME", true)
            NavItem("SOURCES", false)
            NavItem("PREVIEW", false)
            NavItem("SETTINGS", false)
            NavItem("DIAG", false)
        }
    }
}

@Composable
private fun NavItem(
    text: String,
    active: Boolean
) {
    Text(
        text = text,
        color = if (active) RavCyan else RavTextMuted,
        fontSize = 10.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
    )
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.095f),
                        RavSurface.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.045f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        RavCyan.copy(alpha = 0.72f),
                        RavBlue.copy(alpha = 0.2f),
                        RavMagenta.copy(alpha = 0.48f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(padding),
        content = content
    )
}

@Composable
private fun HudButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    accent.copy(alpha = 0.95f),
                    RavCyan.copy(alpha = 0.35f)
                )
            )
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent.copy(alpha = 0.13f),
            contentColor = RavTextPrimary,
            disabledContainerColor = RavSurface.copy(alpha = 0.3f),
            disabledContentColor = RavTextMuted
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun MiniStatusCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        padding = PaddingValues(14.dp)
    ) {
        Text(
            text = label,
            color = RavTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun HudDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.width(12.dp))

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
private fun StatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.7f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun GlowOrb(
    color: Color,
    size: Int,
    x: Int,
    y: Int
) {
    Box(
        modifier = Modifier
            .offset(x.dp, y.dp)
            .size(size.dp)
            .blur(95.dp)
            .background(
                color = color.copy(alpha = 0.22f),
                shape = CircleShape
            )
    )
}
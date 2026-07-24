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
import androidx.compose.foundation.layout.navigationBarsPadding
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

@Composable
fun RavCamDashboard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(
//                Brush.radialGradient(
//                    colors = listOf(
//                        Color(0xFF0B2440),
//                        RavBackground,
//                        RavBackgroundDeep
//                    ),
//                    radius = 1300f
//                )
//            )
            .background(RavBackgroundDeep)
    ) {
//        GlowOrb(
//            color = RavCyan,
//            size = 260,
//            x = (-80),
//            y = 40
//        )
//
//        GlowOrb(
//            color = RavMagenta,
//            size = 220,
//            x = 250,
//            y = 130
//        )
//
//        GlowOrb(
//            color = RavBlue,
//            size = 260,
//            x = 120,
//            y = 520
//        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            HeaderSection()

            VCamStatusCard()

            PrimaryActions()

            QuickStatusGrid()

            SystemSignalCard()

            BottomNavMock()
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    text = "VCAM CONTROL CORE",
                    color = RavCyanSoft,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            StatusPill(
                text = "IDLE",
                color = RavAmber
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Virtual source routing, preview control, and profile management.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun VCamStatusCard() {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "VCAM STATUS",
                    color = RavCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Not Active",
                    color = RavTextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Preview Only Mode",
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
                            listOf(RavCyan, RavMagenta)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = RavCyan,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        HudDataRow(label = "Selected Source", value = "None")
        HudDataRow(label = "Active Profile", value = "Default 720p")
        HudDataRow(label = "Output Layer", value = "Standby")

        Spacer(modifier = Modifier.height(18.dp))

        LinearProgressIndicator(
            progress = { 0.38f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = RavCyan,
            trackColor = RavCyan.copy(alpha = 0.12f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Core systems initialized • awaiting source selection",
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PrimaryActions() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HudButton(
            text = "START PREVIEW",
            accent = RavCyan
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HudButton(
                text = "SELECT SOURCE",
                accent = RavMagenta,
                modifier = Modifier.weight(1f)
            )

            HudButton(
                text = "SETTINGS",
                accent = RavBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickStatusGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatusCard(
            label = "ANDROID",
            value = "10+",
            color = RavCyan,
            modifier = Modifier.weight(1f)
        )

        MiniStatusCard(
            label = "ROOT",
            value = "CHECK",
            color = RavMagenta,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniStatusCard(
            label = "LSPOSED",
            value = "PENDING",
            color = RavAmber,
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
private fun SystemSignalCard() {
    GlassCard {
        Text(
            text = "SYSTEM SIGNAL",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        HudDataRow(label = "Media Engine", value = "Offline")
        HudDataRow(label = "Frame Queue", value = "0 frames")
        HudDataRow(label = "Renderer", value = "Standing by")
        HudDataRow(label = "Diagnostics", value = "Ready")
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {},
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
            contentColor = RavTextPrimary
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
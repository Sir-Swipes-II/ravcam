package com.ravcam.vcam.ui.screens.diagnostics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravcam.vcam.domain.models.DiagnosticStatus
import com.ravcam.vcam.domain.feed.RavFeedSnapshot
import com.ravcam.vcam.domain.models.RavDiagnosticItem
import com.ravcam.vcam.domain.models.RavDiagnosticsReport
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.RavOutputProfile
import com.ravcam.vcam.domain.models.SourceSlot
import com.ravcam.vcam.ui.theme.RavAmber
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavBlue
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavGreen
import com.ravcam.vcam.ui.theme.RavMagenta
import com.ravcam.vcam.ui.theme.RavRed
import com.ravcam.vcam.ui.theme.RavSurface
import com.ravcam.vcam.ui.theme.RavTextMuted
import com.ravcam.vcam.ui.theme.RavTextPrimary
import com.ravcam.vcam.ui.theme.RavTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    sourcesBySlot:
    Map<SourceSlot, RavMediaSource>,
    activeSource: RavMediaSource?,
    sourceStateLoaded: Boolean,
    outputProfile: RavOutputProfile,
    outputProfileLoaded: Boolean,
    isPreviewRunning: Boolean,
    feedSnapshot: RavFeedSnapshot,
    modifier: Modifier = Modifier
) {
    val context =
        LocalContext.current.applicationContext

    val coroutineScope =
        rememberCoroutineScope()

    var report by remember {
        mutableStateOf<RavDiagnosticsReport?>(
            null
        )
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var didAutoRun by remember {
        mutableStateOf(false)
    }

    fun runDiagnostics() {
        if (isRunning) {
            return
        }

        val sourceSnapshot =
            sourcesBySlot.toMap()

        val activeSourceSnapshot =
            activeSource

        val profileSnapshot =
            outputProfile
        val feedSnapshotAtRun =
            feedSnapshot

        coroutineScope.launch {
            isRunning = true

            report = runCatching {
                RavDiagnosticsEngine.run(
                    context = context,
                    sourcesBySlot =
                        sourceSnapshot,
                    activeSource =
                        activeSourceSnapshot,
                    sourceStateLoaded =
                        sourceStateLoaded,
                    outputProfile =
                        profileSnapshot,
                    outputProfileLoaded =
                        outputProfileLoaded,
                    isPreviewRunning =
                        isPreviewRunning,
                    feedSnapshot =
                        feedSnapshotAtRun
                )
            }.getOrElse { error ->
                RavDiagnosticsReport(
                    generatedAtMillis =
                        System.currentTimeMillis(),
                    items = listOf(
                        RavDiagnosticItem(
                            id =
                                "diagnostics_failure",
                            title =
                                "Diagnostics Engine",
                            detail =
                                error.localizedMessage
                                    ?: "Diagnostics could not be completed.",
                            status =
                                DiagnosticStatus.ERROR
                        )
                    )
                )
            }

            isRunning = false
        }
    }

    LaunchedEffect(
        sourceStateLoaded,
        outputProfileLoaded
    ) {
        if (
            sourceStateLoaded &&
            outputProfileLoaded &&
            !didAutoRun
        ) {
            didAutoRun = true
            runDiagnostics()
        }
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
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 18.dp,
                    bottom = 140.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(18.dp)
        ) {
            DiagnosticsHeader(
                report = report,
                isRunning = isRunning
            )

            if (isRunning) {
                DiagnosticsRunningCard()
            } else {
                report?.let {
                    DiagnosticsSummaryCard(
                        report = it
                    )
                }
            }

            Button(
                onClick = {
                    runDiagnostics()
                },
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape =
                    RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color =
                        RavCyan.copy(
                            alpha = 0.76f
                        )
                ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            RavCyan.copy(
                                alpha = 0.13f
                            ),
                        contentColor =
                            RavTextPrimary,
                        disabledContainerColor =
                            RavSurface.copy(
                                alpha = 0.3f
                            ),
                        disabledContentColor =
                            RavTextMuted
                    )
            ) {
                Text(
                    text = if (isRunning) {
                        "RUNNING CHECKS..."
                    } else {
                        "RUN DIAGNOSTICS AGAIN"
                    },
                    fontFamily =
                        FontFamily.Monospace,
                    fontWeight =
                        FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            if (
                report == null &&
                !isRunning
            ) {
                DiagnosticsGlassPanel {
                    Text(
                        text =
                            "DIAGNOSTICS STANDBY",
                        color = RavCyan,
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.Bold,
                        fontFamily =
                            FontFamily.Monospace,
                        letterSpacing = 1.4.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "Waiting for saved sources and output settings to finish loading.",
                        color = RavTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            report?.items?.forEach { item ->
                DiagnosticResultCard(
                    item = item
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsHeader(
    report: RavDiagnosticsReport?,
    isRunning: Boolean
) {
    val statusText: String
    val statusColor: Color

    when {
        isRunning -> {
            statusText = "SCANNING"
            statusColor = RavCyan
        }

        report?.hasBlockingErrors == true -> {
            statusText = "ATTENTION"
            statusColor = RavRed
        }

        report?.warningCount
            ?.let { it > 0 } == true -> {
            statusText = "REVIEW"
            statusColor = RavAmber
        }

        report != null -> {
            statusText = "READY"
            statusColor = RavGreen
        }

        else -> {
            statusText = "STANDBY"
            statusColor = RavAmber
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.Top
        ) {
            Column {
                Text(
                    text = "DIAGNOSTICS",
                    color = RavTextPrimary,
                    fontSize = 32.sp,
                    fontWeight =
                        FontWeight.Black,
                    letterSpacing = 3.sp
                )

                Text(
                    text = "SYSTEM INSPECTION",
                    color = RavCyan,
                    fontSize = 12.sp,
                    fontFamily =
                        FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            DiagnosticsStatusPill(
                text = statusText,
                color = statusColor
            )
        }

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text =
                "Inspect source access, connectivity, storage, media readiness, and output configuration.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun DiagnosticsRunningCard() {
    DiagnosticsGlassPanel {
        Text(
            text = "RUNNING SYSTEM CHECKS",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(
                    RoundedCornerShape(999.dp)
                ),
            color = RavCyan,
            trackColor =
                RavCyan.copy(alpha = 0.12f)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text =
                "Checking saved URIs, source slots, network state, storage, and renderer configuration.",
            color = RavTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DiagnosticsSummaryCard(
    report: RavDiagnosticsReport
) {
    val formattedTime = remember(
        report.generatedAtMillis
    ) {
        SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date(report.generatedAtMillis)
        )
    }

    DiagnosticsGlassPanel {
        Text(
            text = "DIAGNOSTIC SUMMARY",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Last inspection: $formattedTime",
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetric(
                label = "PASS",
                value = report.passCount,
                color = RavGreen,
                modifier = Modifier.weight(1f)
            )

            SummaryMetric(
                label = "ERROR",
                value = report.errorCount,
                color = RavRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetric(
                label = "WARN",
                value = report.warningCount,
                color = RavAmber,
                modifier = Modifier.weight(1f)
            )

            SummaryMetric(
                label = "INFO",
                value = report.infoCount,
                color = RavBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(18.dp)
            )
            .background(
                color.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color =
                    color.copy(alpha = 0.42f),
                shape =
                    RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = value.toString(),
            color = RavTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun DiagnosticResultCard(
    item: RavDiagnosticItem
) {
    val color =
        item.status.statusColor()

    DiagnosticsGlassPanel(
        padding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.Top
        ) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                color = RavTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.padding(4.dp)
            )

            DiagnosticsStatusPill(
                text = item.status.label,
                color = color
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = item.detail,
            color = RavTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DiagnosticsStatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(999.dp)
            )
            .background(
                color.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color =
                    color.copy(alpha = 0.72f),
                shape =
                    RoundedCornerShape(999.dp)
            )
            .padding(
                horizontal = 11.dp,
                vertical = 7.dp
            )
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

private fun DiagnosticStatus.statusColor():
        Color {
    return when (this) {
        DiagnosticStatus.PASS ->
            RavGreen

        DiagnosticStatus.WARNING ->
            RavAmber

        DiagnosticStatus.ERROR ->
            RavRed

        DiagnosticStatus.INFO ->
            RavBlue
    }
}

@Composable
private fun DiagnosticsGlassPanel(
    modifier: Modifier = Modifier,
    padding: PaddingValues =
        PaddingValues(18.dp),
    content:
    @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(26.dp)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        RavSurface.copy(
                            alpha = 0.82f
                        ),
                        RavSurface.copy(
                            alpha = 0.54f
                        ),
                        Color.White.copy(
                            alpha = 0.035f
                        )
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        RavCyan.copy(
                            alpha = 0.72f
                        ),
                        RavBlue.copy(
                            alpha = 0.22f
                        ),
                        RavMagenta.copy(
                            alpha = 0.42f
                        )
                    )
                ),
                shape =
                    RoundedCornerShape(26.dp)
            )
            .padding(padding),
        content = content
    )
}

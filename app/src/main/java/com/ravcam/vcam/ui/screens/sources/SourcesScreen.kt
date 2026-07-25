package com.ravcam.vcam.ui.screens.sources

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravcam.vcam.domain.models.MediaSourceType
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
fun SourcesScreen(
    modifier: Modifier = Modifier
) {
    var selectedType by remember {
        mutableStateOf<MediaSourceType?>(null)
    }

    val demoSources = remember {
        listOf(
            RavMediaSource(
                id = "demo_mp4",
                name = "Demo Local MP4",
                type = MediaSourceType.MP4,
                location = "/storage/emulated/0/Movies/demo.mp4",
                isActive = false
            ),
            RavMediaSource(
                id = "obs_rtmp",
                name = "OBS Stream",
                type = MediaSourceType.RTMP,
                location = "rtmp://192.168.1.10/live/ravcam",
                isActive = false
            ),
            RavMediaSource(
                id = "static_image",
                name = "Fallback Image",
                type = MediaSourceType.IMAGE,
                location = "/storage/emulated/0/Pictures/ravcam.jpg",
                isActive = false
            )
        )
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
            SourcesHeader()

            ActiveSourceCard()

            SourceTypeDropdown(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                }
            )

            AddSourceActionCard(
                selectedType = selectedType
            )

            SavedSourcesList(
                sources = demoSources
            )
        }
    }
}

@Composable
private fun SourcesHeader() {
    Column {
        Text(
            text = "SOURCES",
            color = RavTextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )

        Text(
            text = "INPUT ROUTING BAY",
            color = RavCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Choose local files, static images, GIF loops, or network streams as RavCam input sources.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ActiveSourceCard() {
    GlassPanel {
        Text(
            text = "ACTIVE SOURCE",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "No Source Selected",
            color = RavTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select or add a source below to prepare the preview engine.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatusLine(label = "Engine", value = "Standby")
        StatusLine(label = "Routing", value = "Inactive")
        StatusLine(label = "Preview", value = "Waiting")
    }
}


@Composable
private fun SourceTypeDropdown(
    selectedType: MediaSourceType?,
    onTypeSelected: (MediaSourceType) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SOURCE TYPE",
            color = RavTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    expanded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            RavCyan.copy(alpha = 0.78f),
                            RavBlue.copy(alpha = 0.26f),
                            RavMagenta.copy(alpha = 0.48f)
                        )
                    )
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RavSurface.copy(alpha = 0.68f),
                    contentColor = RavTextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedType?.shortCode ?: "SELECT",
                            color = if (selectedType != null) RavCyan else RavTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.4.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = selectedType?.label ?: "Choose source type",
                            color = RavTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "⌄",
                        color = RavCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier
                    .background(RavSurface)
                    .border(
                        width = 1.dp,
                        color = RavCyan.copy(alpha = 0.42f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                MediaSourceType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Column(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = type.label,
                                        color = RavTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = type.shortCode,
                                        color = RavCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = type.description,
                                    color = RavTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun AddSourceActionCard(
    selectedType: MediaSourceType?
) {
    GlassPanel {
        Text(
            text = "ADD SOURCE",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = selectedType?.label ?: "Choose a source type",
            color = RavTextPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = selectedType?.description
                ?: "Select MP4, image, GIF, RTMP, RTSP, or HTTP before adding a new source.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {},
            enabled = selectedType != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedType != null) RavCyan else RavTextMuted.copy(alpha = 0.4f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType != null) {
                    RavCyan.copy(alpha = 0.14f)
                } else {
                    RavSurface.copy(alpha = 0.42f)
                },
                disabledContainerColor = RavSurface.copy(alpha = 0.42f),
                contentColor = RavTextPrimary,
                disabledContentColor = RavTextMuted
            )
        ) {
            Text(
                text = if (selectedType != null) "CONTINUE SETUP" else "SELECT TYPE FIRST",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        }
    }
}

@Composable
private fun SavedSourcesList(
    sources: List<RavMediaSource>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SAVED SOURCES",
            color = RavTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        sources.forEach { source ->
            SavedSourceCard(source = source)
        }
    }
}

@Composable
private fun SavedSourceCard(
    source: RavMediaSource
) {
    GlassPanel(
        padding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = source.name,
                    color = RavTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = source.location,
                    color = RavTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            SourceBadge(
                text = source.type.shortCode
            )
        }
    }
}

@Composable
private fun SourceBadge(
    text: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(RavCyan.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = RavCyan.copy(alpha = 0.7f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = RavCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatusLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
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
private fun GlassPanel(
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
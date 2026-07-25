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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
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
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
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

    var sourceName by remember {
        mutableStateOf("")
    }

    var sourceLocation by remember {
        mutableStateOf("")
    }

    val savedSources = remember {
        mutableStateListOf(
            RavMediaSource(
                id = "demo_mp4",
                name = "Demo Local MP4",
                type = MediaSourceType.MP4,
                location = "/storage/emulated/0/Movies/demo.mp4"
            ),
            RavMediaSource(
                id = "obs_rtmp",
                name = "OBS Stream",
                type = MediaSourceType.RTMP,
                location = "rtmp://192.168.1.10/live/ravcam"
            ),
            RavMediaSource(
                id = "static_image",
                name = "Fallback Image",
                type = MediaSourceType.IMAGE,
                location = "/storage/emulated/0/Pictures/ravcam.jpg"
            )
        )
    }

    val activeSource = savedSources.firstOrNull { it.isActive }

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

            ActiveSourceCard(
                activeSource = activeSource
            )

            SourceTypeDropdown(
                selectedType = selectedType,
                onTypeSelected = { type ->
                    selectedType = type
                    sourceName = defaultNameForType(type)
                    sourceLocation = ""
                }
            )

            SourceSetupCard(
                selectedType = selectedType,
                sourceName = sourceName,
                sourceLocation = sourceLocation,
                onSourceNameChange = { sourceName = it },
                onSourceLocationChange = { sourceLocation = it },
                onAddSource = {
                    val type = selectedType ?: return@SourceSetupCard

                    savedSources.add(
                        RavMediaSource(
                            id = "source_${System.currentTimeMillis()}",
                            name = sourceName.trim(),
                            type = type,
                            location = sourceLocation.trim()
                        )
                    )

                    selectedType = null
                    sourceName = ""
                    sourceLocation = ""
                }
            )

            SavedSourcesList(
                sources = savedSources,
                onActivateSource = { selectedSource ->
                    val updated = savedSources.map { source ->
                        source.copy(
                            isActive = source.id == selectedSource.id
                        )
                    }

                    savedSources.clear()
                    savedSources.addAll(updated)
                },
                onDeleteSource = { selectedSource ->
                    savedSources.removeAll { it.id == selectedSource.id }
                }
            )
        }
    }
}

private fun defaultNameForType(type: MediaSourceType): String {
    return when (type) {
        MediaSourceType.MP4 -> "Local MP4 Source"
        MediaSourceType.IMAGE -> "Static Image Source"
        MediaSourceType.GIF -> "GIF Loop Source"
        MediaSourceType.RTMP -> "RTMP Stream Source"
        MediaSourceType.RTSP -> "RTSP Stream Source"
        MediaSourceType.HTTP -> "HTTP Stream Source"
    }
}

private fun locationLabelForType(type: MediaSourceType?): String {
    return when (type) {
        MediaSourceType.MP4 -> "Video file path or URI"
        MediaSourceType.IMAGE -> "Image file path or URI"
        MediaSourceType.GIF -> "GIF file path or URI"
        MediaSourceType.RTMP -> "RTMP stream URL"
        MediaSourceType.RTSP -> "RTSP stream URL"
        MediaSourceType.HTTP -> "HTTP stream URL"
        null -> "Source location"
    }
}

private fun locationPlaceholderForType(type: MediaSourceType?): String {
    return when (type) {
        MediaSourceType.MP4 -> "/storage/emulated/0/Movies/video.mp4"
        MediaSourceType.IMAGE -> "/storage/emulated/0/Pictures/image.jpg"
        MediaSourceType.GIF -> "/storage/emulated/0/Download/loop.gif"
        MediaSourceType.RTMP -> "rtmp://server/live/stream"
        MediaSourceType.RTSP -> "rtsp://camera-ip:554/stream"
        MediaSourceType.HTTP -> "https://server/video.mjpeg"
        null -> "Select a source type first"
    }
}

private fun MediaSourceType.isLocalPickerType(): Boolean {
    return this == MediaSourceType.MP4 ||
            this == MediaSourceType.IMAGE ||
            this == MediaSourceType.GIF
}

private fun mimeTypesForType(type: MediaSourceType?): Array<String> {
    return when (type) {
        MediaSourceType.MP4 -> arrayOf("video/mp4", "video/*")
        MediaSourceType.IMAGE -> arrayOf("image/*")
        MediaSourceType.GIF -> arrayOf("image/gif")
        else -> arrayOf("*/*")
    }
}

private fun pickedFileLabel(type: MediaSourceType?): String {
    return when (type) {
        MediaSourceType.MP4 -> "Pick MP4 Video"
        MediaSourceType.IMAGE -> "Pick Image"
        MediaSourceType.GIF -> "Pick GIF"
        else -> "Pick File"
    }
}

private fun pickedUriDisplay(uriText: String): String {
    return Uri.parse(uriText).lastPathSegment ?: uriText
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
private fun ActiveSourceCard(
    activeSource: RavMediaSource?
) {
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
            text = activeSource?.name ?: "No Source Selected",
            color = RavTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = activeSource?.location ?: "Select or add a source below to prepare the preview engine.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            fontFamily = if (activeSource != null) FontFamily.Monospace else FontFamily.Default
        )

        Spacer(modifier = Modifier.height(16.dp))

        StatusLine(label = "Type", value = activeSource?.type?.shortCode ?: "None")
        StatusLine(label = "Engine", value = if (activeSource != null) "Ready" else "Standby")
        StatusLine(label = "Routing", value = if (activeSource != null) "Prepared" else "Inactive")
        StatusLine(label = "Preview", value = if (activeSource != null) "Waiting" else "Waiting")
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
private fun SourceSetupCard(
    selectedType: MediaSourceType?,
    sourceName: String,
    sourceLocation: String,
    onSourceNameChange: (String) -> Unit,
    onSourceLocationChange: (String) -> Unit,
    onAddSource: () -> Unit
) {
    val context = LocalContext.current
    val isLocalPicker = selectedType?.isLocalPickerType() == true

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers may not grant persistable permission.
                // The URI can still be used during the current session.
            }

            onSourceLocationChange(uri.toString())
        }
    }

    val canAddSource = selectedType != null &&
            sourceName.trim().isNotEmpty() &&
            sourceLocation.trim().isNotEmpty()

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

        RavTextField(
            value = sourceName,
            onValueChange = onSourceNameChange,
            label = "Source Name",
            placeholder = "Example: OBS Stream",
            enabled = selectedType != null
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLocalPicker) {
            FilePickerPanel(
                selectedType = selectedType,
                sourceLocation = sourceLocation,
                onPickFile = {
                    filePickerLauncher.launch(
                        mimeTypesForType(selectedType)
                    )
                }
            )
        } else {
            RavTextField(
                value = sourceLocation,
                onValueChange = onSourceLocationChange,
                label = locationLabelForType(selectedType),
                placeholder = locationPlaceholderForType(selectedType),
                enabled = selectedType != null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddSource,
            enabled = canAddSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (canAddSource) RavCyan else RavTextMuted.copy(alpha = 0.4f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canAddSource) {
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
                text = if (canAddSource) "ADD SOURCE" else "COMPLETE SOURCE DETAILS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
        }
    }
}

@Composable
private fun FilePickerPanel(
    selectedType: MediaSourceType?,
    sourceLocation: String,
    onPickFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RavSurface.copy(alpha = 0.38f))
            .border(
                width = 1.dp,
                color = RavCyan.copy(alpha = 0.32f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = locationLabelForType(selectedType).uppercase(),
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (sourceLocation.isBlank()) {
                "No file selected"
            } else {
                pickedUriDisplay(sourceLocation)
            },
            color = if (sourceLocation.isBlank()) RavTextSecondary else RavTextPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onPickFile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = RavCyan.copy(alpha = 0.8f)
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = RavCyan.copy(alpha = 0.12f),
                contentColor = RavTextPrimary
            )
        ) {
            Text(
                text = pickedFileLabel(selectedType).uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun RavTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = label,
                color = RavTextSecondary
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = RavTextMuted,
                fontSize = 12.sp
            )
        },
        singleLine = false,
        minLines = 1,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = RavTextPrimary,
            unfocusedTextColor = RavTextPrimary,
            disabledTextColor = RavTextMuted,
            focusedBorderColor = RavCyan,
            unfocusedBorderColor = RavCyan.copy(alpha = 0.32f),
            disabledBorderColor = RavTextMuted.copy(alpha = 0.24f),
            cursorColor = RavCyan,
            focusedContainerColor = RavSurface.copy(alpha = 0.42f),
            unfocusedContainerColor = RavSurface.copy(alpha = 0.32f),
            disabledContainerColor = RavSurface.copy(alpha = 0.18f)
        )
    )
}

@Composable
private fun SavedSourcesList(
    sources: List<RavMediaSource>,
    onActivateSource: (RavMediaSource) -> Unit,
    onDeleteSource: (RavMediaSource) -> Unit
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
            SavedSourceCard(
                source = source,
                onActivateSource = {
                    onActivateSource(source)
                },
                onDeleteSource = {
                    onDeleteSource(source)
                }
            )
        }
    }
}

@Composable
private fun SavedSourceCard(
    source: RavMediaSource,
    onActivateSource: () -> Unit,
    onDeleteSource: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = source.name,
                        color = RavTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    SourceBadge(
                        text = source.type.shortCode,
                        active = source.isActive
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = source.location,
                    color = RavTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallActionButton(
                        text = if (source.isActive) "ACTIVE" else "ACTIVATE",
                        color = if (source.isActive) RavGreen else RavCyan,
                        enabled = !source.isActive,
                        modifier = Modifier.weight(1f),
                        onClick = onActivateSource
                    )

                    SmallActionButton(
                        text = "DELETE",
                        color = RavMagenta,
                        modifier = Modifier.weight(1f),
                        onClick = onDeleteSource
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (enabled) 0.82f else 0.38f)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = if (enabled) 0.12f else 0.06f),
            disabledContainerColor = color.copy(alpha = 0.06f),
            contentColor = RavTextPrimary,
            disabledContentColor = color.copy(alpha = 0.62f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SourceBadge(
    text: String,
    active: Boolean
) {
    val color = if (active) RavGreen else RavCyan

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.7f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (active) "$text • LIVE" else text,
            color = color,
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
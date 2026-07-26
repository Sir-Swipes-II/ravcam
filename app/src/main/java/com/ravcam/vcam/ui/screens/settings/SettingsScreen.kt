package com.ravcam.vcam.ui.screens.settings

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravcam.vcam.domain.models.OutputFitMode
import com.ravcam.vcam.domain.models.OutputFrameRate
import com.ravcam.vcam.domain.models.OutputResolution
import com.ravcam.vcam.domain.models.OutputRotation
import com.ravcam.vcam.domain.models.RavOutputProfile
import com.ravcam.vcam.ui.state.RavOutputProfileState
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
fun SettingsScreen(
    outputProfileState: RavOutputProfileState,
    modifier: Modifier = Modifier
) {
    val profile = outputProfileState.profile

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
            SettingsHeader(
                isLoaded = outputProfileState.isLoaded
            )

            ProfileSummaryCard(
                profile = profile
            )

            SettingsSection(
                title = "OUTPUT FORMAT",
                description =
                    "Configure the desired dimensions and frame rate for the output pipeline."
            ) {
                SettingDropdown(
                    label = "Resolution",
                    selectedValue = profile.resolution,
                    options = OutputResolution.entries,
                    optionTitle = { it.label },
                    optionDescription = { it.shortLabel },
                    onSelected = {
                        outputProfileState.setResolution(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingDropdown(
                    label = "Frame Rate",
                    selectedValue = profile.frameRate,
                    options = OutputFrameRate.entries,
                    optionTitle = { it.label },
                    optionDescription = {
                        "${it.fps} frames per second"
                    },
                    onSelected = {
                        outputProfileState.setFrameRate(it)
                    }
                )
            }

            SettingsSection(
                title = "FRAME TRANSFORM",
                description =
                    "Control how the active media source is placed inside the output frame."
            ) {
                SettingDropdown(
                    label = "Fit Mode",
                    selectedValue = profile.fitMode,
                    options = OutputFitMode.entries,
                    optionTitle = { it.label },
                    optionDescription = { it.description },
                    onSelected = {
                        outputProfileState.setFitMode(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                SettingDropdown(
                    label = "Rotation",
                    selectedValue = profile.rotation,
                    options = OutputRotation.entries,
                    optionTitle = { it.label },
                    optionDescription = {
                        "${it.degrees} degree rotation"
                    },
                    onSelected = {
                        outputProfileState.setRotation(it)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ToggleSettingRow(
                    title = "Mirror Horizontally",
                    description =
                        "Flip the rendered source from left to right.",
                    checked = profile.mirrorHorizontal,
                    onCheckedChange = {
                        outputProfileState
                            .setMirrorHorizontal(it)
                    }
                )
            }

            SettingsSection(
                title = "PLAYBACK",
                description =
                    "Configure preview playback behavior for supported media sources."
            ) {
                ToggleSettingRow(
                    title = "Loop Media",
                    description =
                        "Restart local video when playback reaches the end.",
                    checked = profile.loopMedia,
                    onCheckedChange = {
                        outputProfileState.setLoopMedia(it)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                ToggleSettingRow(
                    title = "Preview Audio",
                    description =
                        "Allow audio while monitoring media inside RavCam.",
                    checked = profile.previewAudio,
                    onCheckedChange = {
                        outputProfileState.setPreviewAudio(it)
                    }
                )
            }

            Button(
                onClick = {
                    outputProfileState.resetProfile()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = RavAmber.copy(alpha = 0.78f)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        RavAmber.copy(alpha = 0.1f),
                    contentColor = RavTextPrimary
                )
            ) {
                Text(
                    text = "RESET OUTPUT PROFILE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    isLoaded: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "SETTINGS",
                    color = RavTextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "OUTPUT CONTROL",
                    color = RavCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            SettingsStatusPill(
                text = if (isLoaded) {
                    "SAVED"
                } else {
                    "LOADING"
                },
                color = if (isLoaded) {
                    RavGreen
                } else {
                    RavAmber
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text =
                "Define how RavCam should prepare frames for preview and future output adapters.",
            color = RavTextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ProfileSummaryCard(
    profile: RavOutputProfile
) {
    SettingsGlassPanel {
        Text(
            text = "ACTIVE OUTPUT PROFILE",
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = profile.resolution.label,
            color = RavTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text =
                "${profile.resolution.shortLabel} • ${profile.frameRate.label}",
            color = RavTextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsDataRow(
            label = "Fit Mode",
            value = profile.fitMode.label
        )

        SettingsDataRow(
            label = "Rotation",
            value = profile.rotation.label
        )

        SettingsDataRow(
            label = "Mirror",
            value = if (profile.mirrorHorizontal) {
                "Enabled"
            } else {
                "Disabled"
            }
        )

        SettingsDataRow(
            label = "Loop",
            value = if (profile.loopMedia) {
                "Enabled"
            } else {
                "Disabled"
            }
        )

        SettingsDataRow(
            label = "Audio",
            value = if (profile.previewAudio) {
                "Enabled"
            } else {
                "Muted"
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    SettingsGlassPanel {
        Text(
            text = title,
            color = RavCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            color = RavTextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        content()
    }
}

@Composable
private fun <T> SettingDropdown(
    label: String,
    selectedValue: T,
    options: List<T>,
    optionTitle: (T) -> String,
    optionDescription: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column {
        Text(
            text = label.uppercase(),
            color = RavTextMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    expanded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = RavCyan.copy(alpha = 0.45f)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        RavSurface.copy(alpha = 0.54f),
                    contentColor = RavTextPrimary
                ),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 10.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = optionTitle(selectedValue),
                            color = RavTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text =
                                optionDescription(selectedValue),
                            color = RavTextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "⌄",
                        color = RavCyan,
                        fontSize = 22.sp,
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
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column(
                                modifier =
                                    Modifier.padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = optionTitle(option),
                                    color = RavTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(3.dp)
                                )

                                Text(
                                    text =
                                        optionDescription(option),
                                    color = RavTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(RavSurface.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = RavCyan.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = RavTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = RavTextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RavBackgroundDeep,
                checkedTrackColor = RavCyan,
                uncheckedThumbColor = RavTextMuted,
                uncheckedTrackColor =
                    RavSurface.copy(alpha = 0.9f),
                uncheckedBorderColor =
                    RavTextMuted.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun SettingsDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement =
            Arrangement.SpaceBetween
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
private fun SettingsStatusPill(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.74f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(
                horizontal = 13.dp,
                vertical = 8.dp
            )
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
private fun SettingsGlassPanel(
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
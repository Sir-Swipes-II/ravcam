package com.ravcam.vcam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavBlue
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavMagenta
import com.ravcam.vcam.ui.theme.RavSurface
import com.ravcam.vcam.ui.theme.RavTextMuted
import com.ravcam.vcam.ui.theme.RavTextPrimary
import com.ravcam.vcam.ui.theme.RavTextSecondary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun RavPlaceholderScreen(
    title: String,
    subtitle: String,
    status: String,
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column {
                Text(
                    text = title.uppercase(),
                    color = RavTextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = RavTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                RavSurface.copy(alpha = 0.82f),
                                RavSurface.copy(alpha = 0.54f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                RavCyan.copy(alpha = 0.72f),
                                RavBlue.copy(alpha = 0.24f),
                                RavMagenta.copy(alpha = 0.42f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(PaddingValues(18.dp))
            ) {
                Text(
                    text = "MODULE STATUS",
                    color = RavCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = status,
                    color = RavTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This screen is prepared for the next RavCam build phase.",
                    color = RavTextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
package com.ravcam.vcam.ui.screens.preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavCyan
import com.ravcam.vcam.ui.theme.RavMagenta
import com.ravcam.vcam.ui.theme.RavTextPrimary
import com.ravcam.vcam.ui.theme.RavTextSecondary
import java.io.File

private enum class RendererLoadState {
    LOADING,
    READY,
    ERROR
}

@Composable
internal fun PreviewMediaRenderer(
    source: RavMediaSource,
    modifier: Modifier = Modifier
) {
    when (source.type) {
        MediaSourceType.MP4 -> {
            Mp4PreviewRenderer(
                source = source,
                modifier = modifier
            )
        }

        MediaSourceType.IMAGE,
        MediaSourceType.GIF -> {
            ImagePreviewRenderer(
                source = source,
                modifier = modifier
            )
        }

        MediaSourceType.RTMP -> {
            RendererMessage(
                title = "OBS PREVIEW",
                message = "RTMP preview is handled externally in OBS.",
                color = RavCyan,
                modifier = modifier
            )
        }

        MediaSourceType.RTSP,
        MediaSourceType.HTTP -> {
            RendererMessage(
                title = "NETWORK SOURCE",
                message = "Network preview will be added in the next phase.",
                color = RavCyan,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun Mp4PreviewRenderer(
    source: RavMediaSource,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaUri = remember(source.location) {
        sourceLocationToUri(source.location)
    }

    var rendererState by remember(source.location) {
        mutableStateOf(RendererLoadState.LOADING)
    }

    var errorMessage by remember(source.location) {
        mutableStateOf<String?>(null)
    }

    val player = remember(source.location) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE

            // Avoid audio feedback while using RavCam as a preview monitor.
            volume = 0f

            setMediaItem(
                MediaItem.fromUri(mediaUri)
            )

            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                rendererState = when (playbackState) {
                    Player.STATE_BUFFERING -> RendererLoadState.LOADING
                    Player.STATE_READY,
                    Player.STATE_ENDED -> RendererLoadState.READY

                    else -> rendererState
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                rendererState = RendererLoadState.ERROR
                errorMessage = error.localizedMessage
                    ?: "Unable to play the selected MP4."
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    keepScreenOn = true
                    this.player = player
                }
            },
            update = { playerView ->
                playerView.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        when (rendererState) {
            RendererLoadState.LOADING -> {
                RendererLoadingOverlay(
                    title = "LOADING MP4"
                )
            }

            RendererLoadState.ERROR -> {
                RendererMessage(
                    title = "PLAYBACK ERROR",
                    message = errorMessage
                        ?: "Unable to play the selected MP4.",
                    color = RavMagenta,
                    modifier = Modifier.fillMaxSize()
                )
            }

            RendererLoadState.READY -> Unit
        }
    }
}

@Composable
private fun ImagePreviewRenderer(
    source: RavMediaSource,
    modifier: Modifier = Modifier
) {
    val mediaUri = remember(source.location) {
        sourceLocationToUri(source.location)
    }

    var rendererState by remember(source.location) {
        mutableStateOf(RendererLoadState.LOADING)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = mediaUri,
            contentDescription = source.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onLoading = {
                rendererState = RendererLoadState.LOADING
            },
            onSuccess = {
                rendererState = RendererLoadState.READY
            },
            onError = {
                rendererState = RendererLoadState.ERROR
            }
        )

        when (rendererState) {
            RendererLoadState.LOADING -> {
                RendererLoadingOverlay(
                    title = if (source.type == MediaSourceType.GIF) {
                        "LOADING GIF"
                    } else {
                        "LOADING IMAGE"
                    }
                )
            }

            RendererLoadState.ERROR -> {
                RendererMessage(
                    title = "IMAGE ERROR",
                    message = "Unable to decode the selected image or GIF.",
                    color = RavMagenta,
                    modifier = Modifier.fillMaxSize()
                )
            }

            RendererLoadState.READY -> Unit
        }
    }
}

@Composable
private fun RendererLoadingOverlay(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                RavBackgroundDeep.copy(alpha = 0.72f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = RavCyan
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = title,
                color = RavTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp
            )
        }
    }
}

@Composable
private fun RendererMessage(
    title: String,
    message: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RavBackgroundDeep)
            .padding(
                horizontal = 22.dp,
                vertical = 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = message,
                color = RavTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun sourceLocationToUri(
    location: String
): Uri {
    val parsedUri = Uri.parse(location)

    return if (parsedUri.scheme.isNullOrBlank()) {
        Uri.fromFile(
            File(location)
        )
    } else {
        parsedUri
    }
}
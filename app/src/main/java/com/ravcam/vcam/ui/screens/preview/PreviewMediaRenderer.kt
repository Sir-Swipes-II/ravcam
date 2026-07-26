package com.ravcam.vcam.ui.screens.preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.ui.theme.RavBackgroundDeep
import com.ravcam.vcam.ui.theme.RavBlue
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
        MediaSourceType.MP4,
        MediaSourceType.RTSP,
        MediaSourceType.HTTP -> {
            ExoPlayerPreviewRenderer(
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
                color = RavBlue,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ExoPlayerPreviewRenderer(
    source: RavMediaSource,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mediaUri = remember(source.location) {
        sourceLocationToUri(source.location)
    }

    val mediaItem = remember(
        source.type,
        source.location
    ) {
        buildMediaItem(
            source = source,
            uri = mediaUri
        )
    }

    var rendererState by remember(
        source.type,
        source.location
    ) {
        mutableStateOf(RendererLoadState.LOADING)
    }

    var errorMessage by remember(
        source.type,
        source.location
    ) {
        mutableStateOf<String?>(null)
    }

    val player = remember(
        source.id,
        source.type,
        source.location
    ) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = if (source.type == MediaSourceType.MP4) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }

            // Preview is muted to prevent monitor feedback.
            volume = 0f
        }
    }

    DisposableEffect(
        player,
        mediaItem
    ) {
        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {
                rendererState = when (playbackState) {
                    Player.STATE_BUFFERING ->
                        RendererLoadState.LOADING

                    Player.STATE_READY,
                    Player.STATE_ENDED ->
                        RendererLoadState.READY

                    else ->
                        rendererState
                }
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {
                rendererState = RendererLoadState.ERROR

                errorMessage = error.localizedMessage
                    ?: defaultPlaybackError(source.type)
            }
        }

        player.addListener(listener)

        rendererState = RendererLoadState.LOADING
        errorMessage = null

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

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

                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_FIT

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
                    title = loadingTitle(source.type)
                )
            }

            RendererLoadState.ERROR -> {
                RendererMessage(
                    title = errorTitle(source.type),
                    message = errorMessage
                        ?: defaultPlaybackError(source.type),
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
                    title = if (
                        source.type == MediaSourceType.GIF
                    ) {
                        "LOADING GIF"
                    } else {
                        "LOADING IMAGE"
                    }
                )
            }

            RendererLoadState.ERROR -> {
                RendererMessage(
                    title = "IMAGE ERROR",
                    message =
                        "Unable to decode the selected image or GIF.",
                    color = RavMagenta,
                    modifier = Modifier.fillMaxSize()
                )
            }

            RendererLoadState.READY -> Unit
        }
    }
}

private fun buildMediaItem(
    source: RavMediaSource,
    uri: Uri
): MediaItem {
    val builder = MediaItem.Builder()
        .setUri(uri)

    val path = uri.path
        ?.lowercase()
        .orEmpty()

    if (
        source.type == MediaSourceType.HTTP &&
        path.endsWith(".m3u8")
    ) {
        builder.setMimeType(
            MimeTypes.APPLICATION_M3U8
        )
    }

    return builder.build()
}

private fun loadingTitle(
    type: MediaSourceType
): String {
    return when (type) {
        MediaSourceType.MP4 -> "LOADING MP4"
        MediaSourceType.RTSP -> "CONNECTING RTSP"
        MediaSourceType.HTTP -> "CONNECTING HTTP"
        else -> "LOADING MEDIA"
    }
}

private fun errorTitle(
    type: MediaSourceType
): String {
    return when (type) {
        MediaSourceType.RTSP -> "RTSP ERROR"
        MediaSourceType.HTTP -> "HTTP ERROR"
        else -> "PLAYBACK ERROR"
    }
}

private fun defaultPlaybackError(
    type: MediaSourceType
): String {
    return when (type) {
        MediaSourceType.RTSP ->
            "Unable to connect to the RTSP source."

        MediaSourceType.HTTP ->
            "Unable to play the HTTP or HLS source."

        MediaSourceType.MP4 ->
            "Unable to play the selected MP4."

        else ->
            "Unable to initialize this media source."
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
            horizontalAlignment =
                Alignment.CenterHorizontally
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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
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
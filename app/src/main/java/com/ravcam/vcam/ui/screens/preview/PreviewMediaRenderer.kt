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
import android.view.LayoutInflater
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.ravcam.vcam.R
import com.ravcam.vcam.domain.models.OutputFitMode
import com.ravcam.vcam.domain.models.RavOutputProfile
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
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
    outputProfile: RavOutputProfile,
    modifier: Modifier = Modifier
) {
    when (source.type) {
        MediaSourceType.MP4,
        MediaSourceType.RTSP,
        MediaSourceType.HTTP -> {
            ExoPlayerPreviewRenderer(
                source = source,
                outputProfile = outputProfile,
                modifier = modifier
            )
        }

        MediaSourceType.IMAGE,
        MediaSourceType.GIF -> {
            ImagePreviewRenderer(
                source = source,
                outputProfile = outputProfile,
                modifier = modifier
            )
        }

        MediaSourceType.RTMP -> {
            RendererMessage(
                title = "OBS PREVIEW",
                message =
                    "RTMP preview is handled externally in OBS.",
                color = RavBlue,
                modifier = modifier
            )
        }
    }
}

@Composable
@androidx.annotation.OptIn(
    markerClass = [UnstableApi::class]
)
private fun ExoPlayerPreviewRenderer(
    source: RavMediaSource,
    outputProfile: RavOutputProfile,
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

    val rtspMediaSource = remember(
        source.type,
        mediaItem
    ) {
        if (source.type == MediaSourceType.RTSP) {
            /*
             * Emulator and VM networking commonly blocks or
             * misroutes the UDP RTP ports negotiated by RTSP.
             * Interleaved RTP-over-TCP keeps control and media
             * on the reachable RTSP connection.
             */
            RtspMediaSource.Factory()
                .setForceUseRtpTcp(true)
                .createMediaSource(mediaItem)
        } else {
            null
        }
    }

    val mediaMtxHlsFallback = remember(
        source.type,
        source.location
    ) {
        buildMediaMtxHlsFallback(source)
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

    val exoPlayer = remember(
        source.id,
        source.type,
        source.location
    ) {
        ExoPlayer.Builder(context).build()
    }

    /*
     * Update playback preferences without recreating
     * the player or restarting the active media.
     */
    SideEffect {
        exoPlayer.repeatMode =
            if (
                outputProfile.loopMedia &&
                source.type == MediaSourceType.MP4
            ) {
                Player.REPEAT_MODE_ONE
            } else {
                Player.REPEAT_MODE_OFF
            }

        exoPlayer.volume =
            if (outputProfile.previewAudio) {
                1f
            } else {
                0f
            }
    }

    DisposableEffect(
        exoPlayer,
        mediaItem,
        rtspMediaSource,
        mediaMtxHlsFallback
    ) {
        var attemptedMediaMtxFallback = false

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
                if (
                    !attemptedMediaMtxFallback &&
                    mediaMtxHlsFallback != null &&
                    hasMalformedEmptySdp(error)
                ) {
                    /*
                     * Some MediaMTX releases emit a whitespace-only
                     * mandatory SDP session name ("s= "). Media3
                     * rejects it before SETUP, while MediaMTX exposes
                     * the same path as HLS. Preserve generic RTSP
                     * support, but recover this identifiable local
                     * MediaMTX interoperability failure.
                     */
                    attemptedMediaMtxFallback = true
                    rendererState =
                        RendererLoadState.LOADING
                    errorMessage = null
                    exoPlayer.setMediaItem(
                        mediaMtxHlsFallback
                    )
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                    return
                }

                rendererState = RendererLoadState.ERROR

                errorMessage =
                    safePlaybackError(
                        source.type,
                        error
                    )
            }
        }

        exoPlayer.addListener(listener)

        rendererState = RendererLoadState.LOADING
        errorMessage = null

        if (rtspMediaSource != null) {
            exoPlayer.setMediaSource(
                rtspMediaSource
            )
        } else {
            exoPlayer.setMediaItem(mediaItem)
        }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TransformedMediaContent(
            outputProfile = outputProfile,
            modifier = Modifier.fillMaxSize()
        ) { mediaModifier ->
            AndroidView(
                factory = { viewContext ->
                    val playerView =
                        LayoutInflater
                            .from(viewContext)
                            .inflate(
                                R.layout
                                    .rav_preview_player_view,
                                null,
                                false
                            ) as PlayerView

                    playerView.apply {
                        keepScreenOn = true
                        resizeMode =
                            outputProfile
                                .fitMode
                                .toPlayerResizeMode()

                        player = exoPlayer
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer

                    playerView.resizeMode =
                        outputProfile
                            .fitMode
                            .toPlayerResizeMode()
                },
                modifier = mediaModifier
            )
        }

        when (rendererState) {
            RendererLoadState.LOADING -> {
                RendererLoadingOverlay(
                    title = loadingTitle(source.type)
                )
            }

            RendererLoadState.ERROR -> {
                RendererMessage(
                    title = errorTitle(source.type),
                    message =
                        errorMessage
                            ?: defaultPlaybackError(
                                source.type
                            ),
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
    outputProfile: RavOutputProfile,
    modifier: Modifier = Modifier
) {
    val mediaUri = remember(source.location) {
        sourceLocationToUri(source.location)
    }

    var rendererState by remember(source.location) {
        mutableStateOf(RendererLoadState.LOADING)
    }

    val imageContentScale =
        outputProfile.fitMode.toImageContentScale()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TransformedMediaContent(
            outputProfile = outputProfile,
            modifier = Modifier.fillMaxSize()
        ) { mediaModifier ->
            AsyncImage(
                model = mediaUri,
                contentDescription = source.name,
                contentScale = imageContentScale,
                modifier = mediaModifier,
                onLoading = {
                    rendererState =
                        RendererLoadState.LOADING
                },
                onSuccess = {
                    rendererState =
                        RendererLoadState.READY
                },
                onError = {
                    rendererState =
                        RendererLoadState.ERROR
                }
            )
        }

        when (rendererState) {
            RendererLoadState.LOADING -> {
                RendererLoadingOverlay(
                    title =
                        if (
                            source.type ==
                            MediaSourceType.GIF
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

@Composable
private fun TransformedMediaContent(
    outputProfile: RavOutputProfile,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    /*
     * Mirror is applied to the completed output frame so
     * it remains a horizontal mirror regardless of rotation.
     */
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX =
                    if (
                        outputProfile
                            .mirrorHorizontal
                    ) {
                        -1f
                    } else {
                        1f
                    }
            },
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val rotationDegrees =
                outputProfile.rotation.degrees

            val isQuarterTurn =
                rotationDegrees == 90 ||
                        rotationDegrees == 270

            /*
             * A 90° or 270° rotation swaps the media
             * container's width and height before rotating.
             *
             * This prevents the rotated content from being
             * incorrectly clipped by the target frame.
             */
            val mediaModifier =
                if (isQuarterTurn) {
                    Modifier
                        .width(maxHeight)
                        .height(maxWidth)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            rotationZ =
                                rotationDegrees
                                    .toFloat()
                        }
                } else {
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .graphicsLayer {
                            rotationZ =
                                rotationDegrees
                                    .toFloat()
                        }
                }

            content(mediaModifier)
        }
    }
}

@androidx.annotation.OptIn(
    markerClass = [UnstableApi::class]
)
private fun OutputFitMode.toPlayerResizeMode(): Int {
    return when (this) {
        OutputFitMode.FIT ->
            AspectRatioFrameLayout.RESIZE_MODE_FIT

        OutputFitMode.CROP ->
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM

        OutputFitMode.STRETCH ->
            AspectRatioFrameLayout.RESIZE_MODE_FILL
    }
}

private fun OutputFitMode.toImageContentScale():
        ContentScale {
    return when (this) {
        OutputFitMode.FIT ->
            ContentScale.Fit

        OutputFitMode.CROP ->
            ContentScale.Crop

        OutputFitMode.STRETCH ->
            ContentScale.FillBounds
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

private fun safePlaybackError(
    type: MediaSourceType,
    error: PlaybackException
): String {
    if (type != MediaSourceType.RTSP) {
        return defaultPlaybackError(type)
    }

    val causeText = playbackCauseText(error)

    return when {
        causeText.contains(
            "DESCRIBE 404",
            ignoreCase = true
        ) ->
            "The RTSP path is offline or does not exist. " +
                "Keep the publisher running and include its path, " +
                "for example rtsp://host:8554/ravcam."

        causeText.contains(
            "Malformed SDP line: s=",
            ignoreCase = true
        ) ->
            "The RTSP server returned an empty SDP session name " +
                "that Android Media3 cannot parse. Update the " +
                "server or use its HLS endpoint for this stream."

        causeText.contains(
            "400 Bad Request",
            ignoreCase = true
        ) ->
            "The RTSP address was rejected. Verify that the URL " +
                "includes the published stream path."

        else ->
            defaultPlaybackError(type)
    }
}

private fun hasMalformedEmptySdp(
    error: PlaybackException
): Boolean {
    return playbackCauseText(error).contains(
        "Malformed SDP line: s=",
        ignoreCase = true
    )
}

private fun playbackCauseText(
    error: PlaybackException
): String {
    return buildString {
        var current: Throwable? = error
        var depth = 0

        while (current != null && depth < 8) {
            append(
                current.message.orEmpty()
            )
            append('\n')
            current = current.cause
            depth += 1
        }
    }
}

private fun buildMediaMtxHlsFallback(
    source: RavMediaSource
): MediaItem? {
    if (source.type != MediaSourceType.RTSP) {
        return null
    }

    val sourceUri = Uri.parse(source.location)
    val host = sourceUri.host
        ?.takeIf { it.isNotBlank() }
        ?: return null
    val sourcePath = sourceUri.path
        ?.trim('/')
        ?.takeIf { it.isNotBlank() }
        ?: return null

    /*
     * Only infer MediaMTX's documented default HLS endpoint
     * from its default RTSP port. Do not rewrite arbitrary
     * RTSP servers or credential-bearing addresses.
     */
    if (
        sourceUri.scheme != "rtsp" ||
        sourceUri.port != 8554 ||
        sourceUri.userInfo != null
    ) {
        return null
    }

    val hlsUri = Uri.Builder()
        .scheme("http")
        .encodedAuthority(
            if (host.contains(':')) {
                "[$host]:8888"
            } else {
                "$host:8888"
            }
        )
        .appendEncodedPath(sourcePath)
        .appendPath("index.m3u8")
        .build()

    return MediaItem.Builder()
        .setUri(hlsUri)
        .setMimeType(MimeTypes.APPLICATION_M3U8)
        .build()
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

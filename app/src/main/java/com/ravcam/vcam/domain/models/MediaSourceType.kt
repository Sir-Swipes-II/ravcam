package com.ravcam.vcam.domain.models

enum class MediaSourceType(
    val label: String,
    val shortCode: String,
    val description: String
) {
    MP4(
        label = "MP4 Video",
        shortCode = "MP4",
        description = "Use a local video file as the virtual camera source."
    ),

    IMAGE(
        label = "Static Image",
        shortCode = "IMG",
        description = "Use a still image as the virtual camera source."
    ),

    GIF(
        label = "GIF Loop",
        shortCode = "GIF",
        description = "Use an animated GIF as a looping source."
    ),

    RTMP(
        label = "RTMP Stream",
        shortCode = "RTMP",
        description = "Use a live RTMP stream URL."
    ),

    RTSP(
        label = "RTSP Stream",
        shortCode = "RTSP",
        description = "Use a camera or network RTSP stream."
    ),

    HTTP(
        label = "HTTP Stream",
        shortCode = "HTTP",
        description = "Use an HTTP media or MJPEG stream."
    )
}
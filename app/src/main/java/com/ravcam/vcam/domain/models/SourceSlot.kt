package com.ravcam.vcam.domain.models

enum class SourceSlot(
    val label: String,
    val shortCode: String,
    val emptyMessage: String
) {
    VIDEO(
        label = "MP4 Video",
        shortCode = "MP4",
        emptyMessage = "No MP4 video configured."
    ),

    IMAGE(
        label = "Image / GIF",
        shortCode = "IMG",
        emptyMessage = "No image or GIF configured."
    ),

    STREAM(
        label = "Network Stream",
        shortCode = "NET",
        emptyMessage = "No RTMP, RTSP, or HTTP stream configured."
    )
}

fun MediaSourceType.toSourceSlot(): SourceSlot {
    return when (this) {
        MediaSourceType.MP4 -> SourceSlot.VIDEO

        MediaSourceType.IMAGE,
        MediaSourceType.GIF -> SourceSlot.IMAGE

        MediaSourceType.RTMP,
        MediaSourceType.RTSP,
        MediaSourceType.HTTP -> SourceSlot.STREAM
    }
}
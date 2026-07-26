package com.ravcam.vcam.domain.models

enum class OutputResolution(
    val label: String,
    val shortLabel: String,
    val width: Int,
    val height: Int
) {
    HD_720P(
        label = "HD 720p",
        shortLabel = "1280 × 720",
        width = 1280,
        height = 720
    ),

    FULL_HD_1080P(
        label = "Full HD 1080p",
        shortLabel = "1920 × 1080",
        width = 1920,
        height = 1080
    ),

    VERTICAL_HD(
        label = "Vertical HD",
        shortLabel = "720 × 1280",
        width = 720,
        height = 1280
    ),

    VERTICAL_FULL_HD(
        label = "Vertical Full HD",
        shortLabel = "1080 × 1920",
        width = 1080,
        height = 1920
    ),

    SQUARE_1080(
        label = "Square",
        shortLabel = "1080 × 1080",
        width = 1080,
        height = 1080
    )
}

enum class OutputFrameRate(
    val label: String,
    val fps: Int
) {
    FPS_24(
        label = "24 FPS",
        fps = 24
    ),

    FPS_25(
        label = "25 FPS",
        fps = 25
    ),

    FPS_30(
        label = "30 FPS",
        fps = 30
    ),

    FPS_50(
        label = "50 FPS",
        fps = 50
    ),

    FPS_60(
        label = "60 FPS",
        fps = 60
    )
}

enum class OutputFitMode(
    val label: String,
    val description: String
) {
    FIT(
        label = "Fit",
        description = "Show the entire source and preserve its aspect ratio."
    ),

    CROP(
        label = "Crop",
        description = "Fill the target frame and crop any excess edges."
    ),

    STRETCH(
        label = "Stretch",
        description = "Fill the complete frame without preserving aspect ratio."
    )
}

enum class OutputRotation(
    val label: String,
    val degrees: Int
) {
    DEGREE_0(
        label = "0°",
        degrees = 0
    ),

    DEGREE_90(
        label = "90°",
        degrees = 90
    ),

    DEGREE_180(
        label = "180°",
        degrees = 180
    ),

    DEGREE_270(
        label = "270°",
        degrees = 270
    )
}

data class RavOutputProfile(
    val resolution: OutputResolution =
        OutputResolution.FULL_HD_1080P,

    val frameRate: OutputFrameRate =
        OutputFrameRate.FPS_30,

    val fitMode: OutputFitMode =
        OutputFitMode.FIT,

    val rotation: OutputRotation =
        OutputRotation.DEGREE_0,

    val mirrorHorizontal: Boolean = false,

    val loopMedia: Boolean = true,

    val previewAudio: Boolean = false
) {
    val width: Int
        get() = resolution.width

    val height: Int
        get() = resolution.height

    val fps: Int
        get() = frameRate.fps
}
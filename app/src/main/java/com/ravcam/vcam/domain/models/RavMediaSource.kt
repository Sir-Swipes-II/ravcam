package com.ravcam.vcam.domain.models

data class RavMediaSource(
    val id: String,
    val name: String,
    val type: MediaSourceType,
    val location: String,
    val isActive: Boolean = false
)
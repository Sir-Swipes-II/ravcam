package com.ravcam.vcam.ui.screens.diagnostics

import android.content.ContentResolver
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.StatFs
import com.ravcam.vcam.RavCamApplication
import com.ravcam.vcam.domain.feed.RavFeedAdapterStatus
import com.ravcam.vcam.domain.feed.RavFeedSnapshot
import com.ravcam.vcam.domain.feed.RavFeedState
import com.ravcam.vcam.domain.feed.RavFeedTransport
import com.ravcam.vcam.domain.models.DiagnosticStatus
import com.ravcam.vcam.domain.models.MediaSourceType
import com.ravcam.vcam.domain.models.RavDiagnosticItem
import com.ravcam.vcam.domain.models.RavDiagnosticsReport
import com.ravcam.vcam.domain.models.RavMediaSource
import com.ravcam.vcam.domain.models.RavOutputProfile
import com.ravcam.vcam.domain.models.SourceSlot
import com.ravcam.vcam.domain.models.toSourceSlot
import com.ravcam.vcam.feed.provider.RavFeedContentProvider
import com.ravcam.vcam.feed.service.RavFeedService
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RavDiagnosticsEngine {

    suspend fun run(
        context: Context,
        sourcesBySlot: Map<SourceSlot, RavMediaSource>,
        activeSource: RavMediaSource?,
        sourceStateLoaded: Boolean,
        outputProfile: RavOutputProfile,
        outputProfileLoaded: Boolean,
        isPreviewRunning: Boolean,
        feedSnapshot: RavFeedSnapshot
    ): RavDiagnosticsReport = withContext(
        Dispatchers.IO
    ) {
        val requiresNetwork =
            sourcesBySlot.values.any { source ->
                source.type == MediaSourceType.RTMP ||
                        source.type == MediaSourceType.RTSP ||
                        source.type == MediaSourceType.HTTP
            }

        val results = buildList {
            add(
                buildAndroidCheck()
            )

            add(
                buildPersistenceCheck(
                    sourceStateLoaded =
                        sourceStateLoaded,
                    outputProfileLoaded =
                        outputProfileLoaded
                )
            )

            add(
                buildStorageCheck(context)
            )

            add(
                buildNetworkCheck(
                    context = context,
                    requiresNetwork =
                        requiresNetwork
                )
            )

            add(
                buildSlotSummaryCheck(
                    sourcesBySlot =
                        sourcesBySlot
                )
            )

            add(
                buildActiveSourceCheck(
                    activeSource =
                        activeSource
                )
            )

            SourceSlot.entries.forEach { slot ->
                add(
                    buildSourceSlotCheck(
                        context = context,
                        slot = slot,
                        source =
                            sourcesBySlot[slot]
                    )
                )
            }

            add(
                buildProfileCheck(
                    outputProfile =
                        outputProfile
                )
            )

            add(
                buildPreviewSessionCheck(
                    activeSource =
                        activeSource,
                    isPreviewRunning =
                        isPreviewRunning
                )
            )

            add(
                buildFeedSessionCheck(feedSnapshot)
            )

            add(
                buildFeedAdapterCheck(feedSnapshot)
            )

            add(
                buildFeedDescriptorCheck(feedSnapshot)
            )

            add(
                buildFeedSourceAccessCheck(
                    context,
                    feedSnapshot
                )
            )

            add(
                buildFeedHeartbeatCheck(feedSnapshot)
            )

            add(
                buildFeedSecurityCheck(
                    context,
                    feedSnapshot
                )
            )

            add(
                buildFeedConfigurationCheck(
                    feedSnapshot
                )
            )
        }

        RavDiagnosticsReport(
            generatedAtMillis =
                System.currentTimeMillis(),
            items = results
        )
    }

    private fun buildAndroidCheck():
            RavDiagnosticItem {
        val supported =
            Build.VERSION.SDK_INT >= 29

        val deviceName = listOf(
            Build.MANUFACTURER,
            Build.MODEL
        )
            .filter {
                it.isNotBlank()
            }
            .joinToString(" ")

        return RavDiagnosticItem(
            id = "android_version",
            title = "Android Environment",
            detail =
                "$deviceName • Android " +
                        "${Build.VERSION.RELEASE} • " +
                        "API ${Build.VERSION.SDK_INT}",
            status = if (supported) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.ERROR
            }
        )
    }

    private fun buildPersistenceCheck(
        sourceStateLoaded: Boolean,
        outputProfileLoaded: Boolean
    ): RavDiagnosticItem {
        val bothLoaded =
            sourceStateLoaded &&
                    outputProfileLoaded

        return RavDiagnosticItem(
            id = "persistent_state",
            title = "Persistent State",
            detail = when {
                bothLoaded ->
                    "Sources and output profile loaded from DataStore."

                !sourceStateLoaded &&
                        !outputProfileLoaded ->
                    "Sources and output profile are still loading."

                !sourceStateLoaded ->
                    "Saved source state is still loading."

                else ->
                    "Output profile is still loading."
            },
            status = if (bothLoaded) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.WARNING
            }
        )
    }

    private fun buildStorageCheck(
        context: Context
    ): RavDiagnosticItem {
        return runCatching {
            val statFs = StatFs(
                context.filesDir.absolutePath
            )

            val availableBytes =
                statFs.availableBytes

            val warningThreshold =
                500L * 1024L * 1024L

            RavDiagnosticItem(
                id = "storage",
                title = "App Storage",
                detail =
                    "${formatBytes(availableBytes)} " +
                            "available for RavCam data.",
                status =
                    if (
                        availableBytes >=
                        warningThreshold
                    ) {
                        DiagnosticStatus.PASS
                    } else {
                        DiagnosticStatus.WARNING
                    }
            )
        }.getOrElse { error ->
            RavDiagnosticItem(
                id = "storage",
                title = "App Storage",
                detail = error.localizedMessage
                    ?: "Unable to inspect app storage.",
                status = DiagnosticStatus.ERROR
            )
        }
    }

    private fun buildNetworkCheck(
        context: Context,
        requiresNetwork: Boolean
    ): RavDiagnosticItem {
        val manager =
            context.getSystemService(
                ConnectivityManager::class.java
            )

        if (manager == null) {
            return RavDiagnosticItem(
                id = "network",
                title = "Active Network",
                detail =
                    "Connectivity service is unavailable.",
                status = if (requiresNetwork) {
                    DiagnosticStatus.ERROR
                } else {
                    DiagnosticStatus.INFO
                }
            )
        }

        val network = manager.activeNetwork

        if (network == null) {
            return RavDiagnosticItem(
                id = "network",
                title = "Active Network",
                detail = if (requiresNetwork) {
                    "No active network is available for configured stream sources."
                } else {
                    "No active network. Local media sources can still operate."
                },
                status = if (requiresNetwork) {
                    DiagnosticStatus.ERROR
                } else {
                    DiagnosticStatus.INFO
                }
            )
        }

        val capabilities =
            manager.getNetworkCapabilities(
                network
            )

        if (capabilities == null) {
            return RavDiagnosticItem(
                id = "network",
                title = "Active Network",
                detail =
                    "An active network exists, but its capabilities could not be read.",
                status = DiagnosticStatus.WARNING
            )
        }

        val transport =
            networkTransportLabel(
                capabilities
            )

        val hasInternetCapability =
            capabilities.hasCapability(
                NetworkCapabilities
                    .NET_CAPABILITY_INTERNET
            )

        val isValidated =
            capabilities.hasCapability(
                NetworkCapabilities
                    .NET_CAPABILITY_VALIDATED
            )

        return RavDiagnosticItem(
            id = "network",
            title = "Active Network",
            detail =
                "$transport • Internet capability: " +
                        "${yesNo(hasInternetCapability)} • " +
                        "Validated: ${yesNo(isValidated)}",
            status = if (requiresNetwork) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.INFO
            }
        )
    }

    private fun buildSlotSummaryCheck(
        sourcesBySlot:
        Map<SourceSlot, RavMediaSource>
    ): RavDiagnosticItem {
        val configured =
            sourcesBySlot.size

        return RavDiagnosticItem(
            id = "source_slots",
            title = "Source Slots",
            detail =
                "$configured of " +
                        "${SourceSlot.entries.size} " +
                        "source slots configured.",
            status = if (configured > 0) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.WARNING
            }
        )
    }

    private fun buildActiveSourceCheck(
        activeSource: RavMediaSource?
    ): RavDiagnosticItem {
        return when {
            activeSource == null -> {
                RavDiagnosticItem(
                    id = "active_source",
                    title = "Active Source",
                    detail =
                        "No saved source is currently activated.",
                    status =
                        DiagnosticStatus.WARNING
                )
            }

            activeSource.type ==
                    MediaSourceType.RTMP -> {
                RavDiagnosticItem(
                    id = "active_source",
                    title = "Active Source",
                    detail =
                        "${activeSource.name} • RTMP is monitored externally in OBS.",
                    status =
                        DiagnosticStatus.INFO
                )
            }

            else -> {
                RavDiagnosticItem(
                    id = "active_source",
                    title = "Active Source",
                    detail =
                        "${activeSource.name} • " +
                                activeSource.type.label,
                    status =
                        DiagnosticStatus.PASS
                )
            }
        }
    }

    private fun buildSourceSlotCheck(
        context: Context,
        slot: SourceSlot,
        source: RavMediaSource?
    ): RavDiagnosticItem {
        if (source == null) {
            return RavDiagnosticItem(
                id =
                    "source_${slot.name.lowercase()}",
                title = "${slot.label} Source",
                detail = slot.emptyMessage,
                status = DiagnosticStatus.INFO
            )
        }

        if (source.type.toSourceSlot() != slot) {
            return RavDiagnosticItem(
                id =
                    "source_${slot.name.lowercase()}",
                title = "${slot.label} Source",
                detail =
                    "Saved type ${source.type.label} does not match this source slot.",
                status = DiagnosticStatus.ERROR
            )
        }

        return when (source.type) {
            MediaSourceType.MP4,
            MediaSourceType.IMAGE,
            MediaSourceType.GIF -> {
                buildLocalSourceCheck(
                    context = context,
                    slot = slot,
                    source = source
                )
            }

            MediaSourceType.RTMP,
            MediaSourceType.RTSP,
            MediaSourceType.HTTP -> {
                buildNetworkSourceCheck(
                    slot = slot,
                    source = source
                )
            }
        }
    }

    private fun buildLocalSourceCheck(
        context: Context,
        slot: SourceSlot,
        source: RavMediaSource
    ): RavDiagnosticItem {
        val result =
            checkLocalLocation(
                context = context,
                location = source.location
            )

        return RavDiagnosticItem(
            id =
                "source_${slot.name.lowercase()}",
            title = "${slot.label} Source",
            detail =
                "${source.name} • ${result.detail}",
            status = if (result.readable) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.ERROR
            }
        )
    }

    private fun buildNetworkSourceCheck(
        slot: SourceSlot,
        source: RavMediaSource
    ): RavDiagnosticItem {
        val uri = runCatching {
            Uri.parse(source.location)
        }.getOrNull()

        val allowedSchemes =
            when (source.type) {
                MediaSourceType.RTMP ->
                    setOf("rtmp", "rtmps")

                MediaSourceType.RTSP ->
                    setOf("rtsp", "rtsps")

                MediaSourceType.HTTP ->
                    setOf("http", "https")

                else ->
                    emptySet()
            }

        val scheme = uri
            ?.scheme
            ?.lowercase(Locale.US)

        val host = uri?.host

        val valid =
            scheme in allowedSchemes &&
                    !host.isNullOrBlank()

        val endpoint = when {
            host.isNullOrBlank() ->
                "unknown endpoint"

            uri.port > 0 ->
                "$host:${uri.port}"

            else ->
                host
        }

        val detail = when {
            !valid ->
                "${source.name} has an invalid ${source.type.shortCode} endpoint."

            source.type ==
                    MediaSourceType.RTMP ->
                "${source.name} • $endpoint • Syntax valid; preview remains in OBS."

            source.type ==
                    MediaSourceType.HTTP ->
                "${source.name} • $endpoint • Syntax valid; playback depends on the HTTP media format."

            else ->
                "${source.name} • $endpoint • Endpoint syntax valid."
        }

        return RavDiagnosticItem(
            id =
                "source_${slot.name.lowercase()}",
            title = "${slot.label} Source",
            detail = detail,
            status = when {
                !valid ->
                    DiagnosticStatus.ERROR

                source.type ==
                        MediaSourceType.RTMP ->
                    DiagnosticStatus.INFO

                else ->
                    DiagnosticStatus.PASS
            }
        )
    }

    private fun buildProfileCheck(
        outputProfile: RavOutputProfile
    ): RavDiagnosticItem {
        val valid =
            outputProfile.width > 0 &&
                    outputProfile.height > 0 &&
                    outputProfile.fps > 0

        return RavDiagnosticItem(
            id = "output_profile",
            title = "Output Profile",
            detail =
                "${outputProfile.resolution.shortLabel} • " +
                        "${outputProfile.frameRate.label} • " +
                        "${outputProfile.fitMode.label} • " +
                        "${outputProfile.rotation.label} • " +
                        if (
                            outputProfile
                                .mirrorHorizontal
                        ) {
                            "Mirrored"
                        } else {
                            "Not mirrored"
                        },
            status = if (valid) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.ERROR
            }
        )
    }

    private fun buildPreviewSessionCheck(
        activeSource: RavMediaSource?,
        isPreviewRunning: Boolean
    ): RavDiagnosticItem {
        return when {
            isPreviewRunning &&
                    activeSource == null -> {
                RavDiagnosticItem(
                    id = "preview_session",
                    title = "Preview Session",
                    detail =
                        "Preview is marked as running without an active source.",
                    status =
                        DiagnosticStatus.ERROR
                )
            }

            isPreviewRunning &&
                    activeSource != null &&
                    !activeSource.type
                        .supportsPreview() -> {
                RavDiagnosticItem(
                    id = "preview_session",
                    title = "Preview Session",
                    detail =
                        "${activeSource.type.label} does not use the in-app preview renderer.",
                    status =
                        DiagnosticStatus.ERROR
                )
            }

            isPreviewRunning -> {
                RavDiagnosticItem(
                    id = "preview_session",
                    title = "Preview Session",
                    detail =
                        "Preview renderer is active and synchronized with the selected source.",
                    status =
                        DiagnosticStatus.PASS
                )
            }

            else -> {
                RavDiagnosticItem(
                    id = "preview_session",
                    title = "Preview Session",
                    detail =
                        "Preview renderer is currently stopped.",
                    status =
                        DiagnosticStatus.INFO
                )
            }
        }
    }

    private fun buildFeedSessionCheck(
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val status = when (snapshot.state) {
            RavFeedState.RUNNING ->
                DiagnosticStatus.PASS

            RavFeedState.ERROR ->
                DiagnosticStatus.ERROR

            RavFeedState.READY ->
                DiagnosticStatus.WARNING

            else -> DiagnosticStatus.INFO
        }

        return RavDiagnosticItem(
            id = "feed_session",
            title = "Feed Session",
            detail =
                "State ${snapshot.state.name} • " +
                        (
                            snapshot.descriptor
                                ?.sessionId
                                ?.take(8)
                                ?.let { "Session $it" }
                                ?: "No active session"
                            ),
            status = status
        )
    }

    private fun buildFeedAdapterCheck(
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val status = when (
            snapshot.adapterStatus
        ) {
            RavFeedAdapterStatus.CONNECTED ->
                DiagnosticStatus.PASS

            RavFeedAdapterStatus.STALE ->
                DiagnosticStatus.WARNING

            RavFeedAdapterStatus.ERROR ->
                DiagnosticStatus.ERROR

            else -> DiagnosticStatus.INFO
        }

        return RavDiagnosticItem(
            id = "feed_adapter",
            title = "Adapter Contract",
            detail =
                "Adapter state ${snapshot.adapterStatus.name}.",
            status = status
        )
    }

    private fun buildFeedDescriptorCheck(
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val descriptor = snapshot.descriptor
            ?: return RavDiagnosticItem(
                id = "feed_descriptor",
                title = "Feed Descriptor",
                detail =
                    "No descriptor is published while the feed is stopped.",
                status = DiagnosticStatus.INFO
            )

        return RavDiagnosticItem(
            id = "feed_descriptor",
            title = "Feed Descriptor",
            detail =
                "${descriptor.sourceType.shortCode} • " +
                        "${descriptor.transport} • " +
                        "${descriptor.width}x${descriptor.height}" +
                        "@${descriptor.fps} • " +
                        "${descriptor.fitMode} • " +
                        "${descriptor.rotationDegrees}° • " +
                        "mirror ${yesNo(descriptor.mirrorHorizontal)} • " +
                        "revision ${descriptor.revision}",
            status = DiagnosticStatus.PASS
        )
    }

    private fun buildFeedSourceAccessCheck(
        context: Context,
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val descriptor = snapshot.descriptor
            ?: return RavDiagnosticItem(
                id = "feed_source_access",
                title = "Feed Source Access",
                detail = "No active source access record.",
                status = DiagnosticStatus.INFO
            )

        val runtime =
            (context.applicationContext as
                    RavCamApplication).feedRuntime
        val record = runtime.sourceRegistry.get(
            descriptor.sessionId
        )

        val valid = when (descriptor.transport) {
            RavFeedTransport.CONTENT_PROVIDER -> {
                val providerUri =
                    record?.providerUri
                providerUri != null &&
                        runCatching {
                            context.contentResolver
                                .openFileDescriptor(
                                    providerUri,
                                    "r"
                                )
                                ?.use { true }
                                ?: false
                        }.getOrDefault(false)
            }

            RavFeedTransport.NETWORK_URI -> {
                val scheme = runCatching {
                    Uri.parse(
                        record
                            ?.originalSourceLocation
                            .orEmpty()
                    ).scheme?.lowercase()
                }.getOrNull()
                scheme in setOf(
                    "http",
                    "https",
                    "rtsp",
                    "rtsps"
                )
            }

            else -> false
        }

        return RavDiagnosticItem(
            id = "feed_source_access",
            title = "Feed Source Access",
            detail = if (valid) {
                "Controlled ${descriptor.transport} access is registered; source details are redacted."
            } else {
                "The active source registry record is unavailable."
            },
            status = if (valid) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.ERROR
            }
        )
    }

    private fun buildFeedHeartbeatCheck(
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val heartbeat = snapshot.heartbeat
            ?: return RavDiagnosticItem(
                id = "feed_heartbeat",
                title = "Consumer Heartbeat",
                detail = "No consumer has registered.",
                status = DiagnosticStatus.INFO
            )

        val age = snapshot.heartbeatAgeMillis
            ?: 0L
        val stale =
            snapshot.adapterStatus ==
                    RavFeedAdapterStatus.STALE

        return RavDiagnosticItem(
            id = "feed_heartbeat",
            title = "Consumer Heartbeat",
            detail =
                "${heartbeat.consumerPackage} • " +
                        "${age / 1_000L}s old • " +
                        if (stale) "stale" else "connected",
            status = if (stale) {
                DiagnosticStatus.WARNING
            } else {
                DiagnosticStatus.PASS
            }
        )
    }

    private fun buildFeedSecurityCheck(
        context: Context,
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        val packageManager =
            context.packageManager
        val packageInfo =
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS
            )
        val broadPermissions = packageInfo
            .requestedPermissions
            .orEmpty()
            .filter {
                it ==
                        "android.permission.MANAGE_EXTERNAL_STORAGE" ||
                        it == android.Manifest.permission
                            .READ_EXTERNAL_STORAGE ||
                        it == android.Manifest.permission
                            .WRITE_EXTERNAL_STORAGE
            }

        val signaturePermissionConfigured =
            runCatching {
                val permission =
                    packageManager.getPermissionInfo(
                        "com.ravcam.vcam.permission.ACCESS_FEED",
                        0
                    )
                permission.protection ==
                        android.content.pm.PermissionInfo
                            .PROTECTION_SIGNATURE
            }.getOrDefault(false)

        val serviceConfigured =
            runCatching {
                packageManager.getServiceInfo(
                    ComponentName(
                        context,
                        RavFeedService::class.java
                    ),
                    0
                ).exported
            }.getOrDefault(false)

        val providerConfigured =
            runCatching {
                val provider =
                    packageManager.getProviderInfo(
                        ComponentName(
                            context,
                            RavFeedContentProvider::class.java
                        ),
                        0
                    )
                !provider.exported &&
                        provider.grantUriPermissions &&
                        provider.writePermission == null
            }.getOrDefault(false)

        val runtime =
            (context.applicationContext as
                    RavCamApplication).feedRuntime
        val activeConsumers =
            snapshot.descriptor
                ?.sessionId
                ?.let(
                    runtime.sourceRegistry::
                        activeConsumerCount
                )
                ?: 0

        val secure =
            broadPermissions.isEmpty() &&
                    signaturePermissionConfigured &&
                    serviceConfigured &&
                    providerConfigured

        return RavDiagnosticItem(
            id = "feed_security",
            title = "Feed Security",
            detail = if (secure) {
                "Signature permission declared; capability service enabled; read-only URI grants enabled; $activeConsumers active consumer(s); no broad storage permission."
            } else {
                "Feed component security or storage permission configuration needs attention."
            },
            status = if (secure) {
                DiagnosticStatus.PASS
            } else {
                DiagnosticStatus.ERROR
            }
        )
    }

    private fun buildFeedConfigurationCheck(
        snapshot: RavFeedSnapshot
    ): RavDiagnosticItem {
        return RavDiagnosticItem(
            id = "feed_configuration",
            title = "Feed Configuration",
            detail = if (
                snapshot.configurationChanged
            ) {
                "Source or output profile changed; restart the feed to apply it."
            } else {
                "The active descriptor matches the selected configuration."
            },
            status = if (
                snapshot.configurationChanged
            ) {
                DiagnosticStatus.WARNING
            } else {
                DiagnosticStatus.PASS
            }
        )
    }

    private fun checkLocalLocation(
        context: Context,
        location: String
    ): LocalReadResult {
        return runCatching {
            val uri = Uri.parse(location)

            when (
                uri.scheme
                    ?.lowercase(Locale.US)
            ) {
                ContentResolver.SCHEME_CONTENT -> {
                    val descriptor =
                        context.contentResolver
                            .openAssetFileDescriptor(
                                uri,
                                "r"
                            )

                    if (descriptor == null) {
                        LocalReadResult(
                            readable = false,
                            detail =
                                "The content URI could not be opened."
                        )
                    } else {
                        descriptor.use {
                            val sizeDetail =
                                if (it.length >= 0L) {
                                    formatBytes(
                                        it.length
                                    )
                                } else {
                                    "size unavailable"
                                }

                            LocalReadResult(
                                readable = true,
                                detail =
                                    "Content URI is readable • $sizeDetail."
                            )
                        }
                    }
                }

                ContentResolver.SCHEME_FILE -> {
                    inspectFile(
                        File(
                            uri.path.orEmpty()
                        )
                    )
                }

                null,
                "" -> {
                    inspectFile(
                        File(location)
                    )
                }

                else -> {
                    LocalReadResult(
                        readable = false,
                        detail =
                            "Unsupported local URI scheme: ${uri.scheme}."
                    )
                }
            }
        }.getOrElse { error ->
            LocalReadResult(
                readable = false,
                detail = error.localizedMessage
                    ?: "RavCam no longer has read access to this source."
            )
        }
    }

    private fun inspectFile(
        file: File
    ): LocalReadResult {
        return when {
            !file.exists() -> {
                LocalReadResult(
                    readable = false,
                    detail =
                        "The saved file no longer exists."
                )
            }

            !file.canRead() -> {
                LocalReadResult(
                    readable = false,
                    detail =
                        "The saved file exists but cannot be read."
                )
            }

            else -> {
                LocalReadResult(
                    readable = true,
                    detail =
                        "File is readable • " +
                                "${formatBytes(file.length())}."
                )
            }
        }
    }

    private fun networkTransportLabel(
        capabilities: NetworkCapabilities
    ): String {
        val transports = buildList {
            if (
                capabilities.hasTransport(
                    NetworkCapabilities
                        .TRANSPORT_WIFI
                )
            ) {
                add("Wi-Fi")
            }

            if (
                capabilities.hasTransport(
                    NetworkCapabilities
                        .TRANSPORT_CELLULAR
                )
            ) {
                add("Cellular")
            }

            if (
                capabilities.hasTransport(
                    NetworkCapabilities
                        .TRANSPORT_ETHERNET
                )
            ) {
                add("Ethernet")
            }

            if (
                capabilities.hasTransport(
                    NetworkCapabilities
                        .TRANSPORT_VPN
                )
            ) {
                add("VPN")
            }
        }

        return transports
            .ifEmpty {
                listOf("Other transport")
            }
            .joinToString(" + ")
    }

    private fun MediaSourceType.supportsPreview():
            Boolean {
        return this == MediaSourceType.MP4 ||
                this == MediaSourceType.IMAGE ||
                this == MediaSourceType.GIF ||
                this == MediaSourceType.RTSP ||
                this == MediaSourceType.HTTP
    }

    private fun formatBytes(
        bytes: Long
    ): String {
        val kb =
            bytes / 1024.0

        val mb =
            kb / 1024.0

        val gb =
            mb / 1024.0

        return when {
            gb >= 1.0 ->
                String.format(
                    Locale.US,
                    "%.1f GB",
                    gb
                )

            mb >= 1.0 ->
                String.format(
                    Locale.US,
                    "%.1f MB",
                    mb
                )

            kb >= 1.0 ->
                String.format(
                    Locale.US,
                    "%.1f KB",
                    kb
                )

            else ->
                "$bytes bytes"
        }
    }

    private fun yesNo(
        value: Boolean
    ): String {
        return if (value) {
            "Yes"
        } else {
            "No"
        }
    }

    private data class LocalReadResult(
        val readable: Boolean,
        val detail: String
    )
}

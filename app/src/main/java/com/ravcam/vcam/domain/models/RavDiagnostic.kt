package com.ravcam.vcam.domain.models

enum class DiagnosticStatus(
    val label: String
) {
    PASS("PASS"),
    WARNING("WARN"),
    ERROR("ERROR"),
    INFO("INFO")
}

data class RavDiagnosticItem(
    val id: String,
    val title: String,
    val detail: String,
    val status: DiagnosticStatus
)

data class RavDiagnosticsReport(
    val generatedAtMillis: Long,
    val items: List<RavDiagnosticItem>
) {
    val passCount: Int
        get() = items.count {
            it.status == DiagnosticStatus.PASS
        }

    val warningCount: Int
        get() = items.count {
            it.status == DiagnosticStatus.WARNING
        }

    val errorCount: Int
        get() = items.count {
            it.status == DiagnosticStatus.ERROR
        }

    val infoCount: Int
        get() = items.count {
            it.status == DiagnosticStatus.INFO
        }

    val hasBlockingErrors: Boolean
        get() = errorCount > 0
}
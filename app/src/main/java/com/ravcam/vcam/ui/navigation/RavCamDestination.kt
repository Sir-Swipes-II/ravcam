package com.ravcam.vcam.ui.navigation

sealed class RavCamDestination(
    val route: String,
    val label: String,
    val navCode: String
) {
    data object Home : RavCamDestination(
        route = "home",
        label = "Home",
        navCode = "H"
    )

    data object Sources : RavCamDestination(
        route = "sources",
        label = "Sources",
        navCode = "S"
    )

    data object Preview : RavCamDestination(
        route = "preview",
        label = "Preview",
        navCode = "P"
    )

    data object Settings : RavCamDestination(
        route = "settings",
        label = "Settings",
        navCode = "CFG"
    )

    data object Diagnostics : RavCamDestination(
        route = "diagnostics",
        label = "Diag",
        navCode = "D"
    )

    data object About : RavCamDestination(
        route = "about",
        label = "About",
        navCode = "A"
    )
}

val bottomNavDestinations = listOf(
    RavCamDestination.Home,
    RavCamDestination.Sources,
    RavCamDestination.Preview,
    RavCamDestination.Settings,
    RavCamDestination.Diagnostics
)
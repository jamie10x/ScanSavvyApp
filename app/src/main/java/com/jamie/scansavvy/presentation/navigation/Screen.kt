package com.jamie.scansavvy.presentation.navigation

sealed class Screen(val route: String) {
    object Camera : Screen("camera_screen")
    object Gallery : Screen("gallery_screen")
    object Settings : Screen("settings_screen") // Added
    object Viewer : Screen("viewer_screen/{documentId}") {
        fun createRoute(documentId: Int) = "viewer_screen/$documentId"
    }
}
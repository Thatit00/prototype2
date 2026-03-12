package com.example.prototype2.ui

data class JunkCategory(
    val id: String,
    val name: String,
    val size: Long,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

sealed class AppStep {
    object Agreement : AppStep()
    object Splash : AppStep()
    object Scanning : AppStep()
    data class CleanResult(val junkSize: String, val categories: List<JunkCategory>) : AppStep()
    object Cleaning : AppStep()
    object SetupLauncher : AppStep()
    object LauncherHome : AppStep()
}

data class HomeUiState(
    val currentStep: AppStep = AppStep.Agreement,
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val currentPackageName: String = "",
    val isIslandEnabled: Boolean = false,
    val foundCategories: List<JunkCategory> = emptyList()
)

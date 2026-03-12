package com.example.prototype2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototype2.core.clean.JunkItem
import com.example.prototype2.core.clean.StorageScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val scanner = StorageScanner(application)
    private var scannedJunkItems = listOf<JunkItem>()

    init {
        // 不再自动扫描，等待用户点击协议页按钮
    }

    fun startRealScan() {
        _uiState.update { it.copy(
            currentStep = AppStep.Scanning, 
            isScanning = true, 
            scanProgress = 0f,
            foundCategories = emptyList()
        ) }
        
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            
            scanner.startScan(object : StorageScanner.ScanListener {
                override fun onProgress(path: String, currentSize: Long) {
                    _uiState.update { it.copy(
                        currentPackageName = path,
                        // 缓慢增长进度条，确保视觉平滑
                        scanProgress = (it.scanProgress + 0.002f).coerceAtMost(0.98f)
                    ) }
                }

                override fun onCompleted(items: List<JunkItem>, totalSize: Long) {
                    scannedJunkItems = items
                    
                    viewModelScope.launch {
                        // 强制至少扫描 4 秒，解决“秒完”导致的不信任感
                        val elapsedTime = System.currentTimeMillis() - startTime
                        if (elapsedTime < 4000) {
                            delay(4000 - elapsedTime)
                        }

                        val logSize = items.filter { it.path.endsWith(".log") }.sumOf { it.size }
                        val tempSize = items.filter { it.path.endsWith(".tmp") }.sumOf { it.size }
                        val apkSize = items.filter { it.path.endsWith(".apk") }.sumOf { it.size }
                        val otherSize = totalSize - logSize - tempSize - apkSize

                        val displayTotal = (totalSize / (1024 * 1024) + Random.nextInt(200, 800)).toString() + " MB"

                        val cats = listOf(
                            JunkCategory("logs", "Log Files", logSize + Random.nextInt(50, 150)*1024*1024L, Icons.Default.Info),
                            JunkCategory("temp", "Temp Files", tempSize + Random.nextInt(100, 300)*1024*1024L, Icons.Default.DateRange),
                            JunkCategory("apks", "Junk APKs", apkSize + Random.nextInt(10, 50)*1024*1024L, Icons.Default.Build),
                            JunkCategory("cache", "System Cache", otherSize + Random.nextInt(150, 450)*1024*1024L, Icons.Default.Settings)
                        )
                        
                        _uiState.update { it.copy(
                            currentStep = AppStep.CleanResult(displayTotal, cats),
                            isScanning = false,
                            scanProgress = 1.0f,
                            foundCategories = cats
                        ) }
                    }
                }
            })
        }
    }

    fun completeOptimization() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentStep = AppStep.Cleaning) }
            
            scanner.performClean(scannedJunkItems) { deletedSize ->
                viewModelScope.launch {
                    delay(3000) // 清理动画 (物理感更强，所以加到 3 秒)
                    _uiState.update { it.copy(currentStep = AppStep.SetupLauncher) }
                }
            }
        }
    }

    fun onLauncherActivated() {
        _uiState.update { it.copy(currentStep = AppStep.LauncherHome) }
    }
    
    fun resetToScanning() {
        startRealScan()
    }

    fun toggleIsland(enabled: Boolean) {
        _uiState.update { it.copy(isIslandEnabled = enabled) }
    }
}

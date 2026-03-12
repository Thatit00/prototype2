package com.example.prototype2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prototype2.ui.*
import com.example.prototype2.ui.theme.Prototype2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Prototype2Theme {
                MainAppFlow()
            }
        }
    }
}

@Composable
fun MainAppFlow(vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    // Observe launcher state to auto-transition
    LaunchedEffect(state.currentStep) {
        if (state.currentStep == AppStep.SetupLauncher) {
            if (isDefaultLauncher(context)) {
                vm.onLauncherActivated()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { step ->
            when (step) {
                is AppStep.Agreement -> AgreementScreen { vm.startRealScan() }
                is AppStep.Scanning -> ScanningScreen(state.scanProgress, state.currentPackageName)
                is AppStep.CleanResult -> ResultScreen(step.junkSize, step.categories) { vm.completeOptimization() }
                is AppStep.Cleaning -> CleaningAnimationScreen(state.foundCategories)
                is AppStep.SetupLauncher -> ActivationScreen(
                    onActivate = { requestLauncherPermission(context) },
                    onSkip = { vm.onLauncherActivated() }
                )
                is AppStep.LauncherHome -> LauncherHomeScreen()
                else -> Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AgreementScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo (Placeholder)
        Box(
            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF38BDF8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Build, null, tint = Color.White, modifier = Modifier.size(50.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Prototype2 Launcher", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text("Fast • Clean • Smart Island", color = Color(0xFF38BDF8), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Value Prop
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF34C759))
                Spacer(Modifier.width(12.dp))
                Text("Clean system junk effectively", color = Color.Gray, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF34C759))
                Spacer(Modifier.width(12.dp))
                Text("Optimize battery and memory", color = Color.Gray, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("GET STARTED", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compliance Footer
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "By clicking \"Get Started\", you agree to our",
                color = Color.Gray,
                fontSize = 11.sp
            )
            Row {
                Text(
                    "Privacy Policy", 
                    color = Color(0xFF38BDF8), 
                    fontSize = 11.sp, 
                    modifier = Modifier.clickable { /* Link to URL */ }
                )
                Text(" & ", color = Color.Gray, fontSize = 11.sp)
                Text(
                    "Terms of Service", 
                    color = Color(0xFF38BDF8), 
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { /* Link to URL */ }
                )
            }
        }
    }
}

@Composable
fun ActivationScreen(onActivate: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(140.dp).background(Color(0xFF38BDF8).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Home, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(70.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Final Step", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("Activate Smart Launcher", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Set Prototype2 as your default home to keep your phone fast and enable Smart Island features.",
            color = Color.Gray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onActivate,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("SET AS DEFAULT", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        
        TextButton(onClick = onSkip, modifier = Modifier.padding(top = 8.dp)) {
            Text("Maybe Later", color = Color.Gray)
        }
    }
}

@Composable
fun CleaningAnimationScreen(categories: List<JunkCategory>) {
    var currentIndex by remember { mutableStateOf(0) }
    
    // 模拟逐项清理的视觉效果 (对标顶级竞品)
    LaunchedEffect(Unit) {
        for (i in categories.indices) {
            delay(600)
            currentIndex = i + 1
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(160.dp),
                    color = Color(0xFF34C759),
                    strokeWidth = 6.dp
                )
                Icon(Icons.Default.Delete, null, tint = Color(0xFF34C759), modifier = Modifier.size(60.dp))
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(modifier = Modifier.padding(horizontal = 40.dp)) {
                categories.forEachIndexed { index, cat ->
                    val isDone = index < currentIndex
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp).alpha(if (isDone) 0.3f else 1f)
                    ) {
                        Icon(cat.icon, null, tint = if (isDone) Color.Gray else Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(cat.name, color = if (isDone) Color.Gray else Color.White, modifier = Modifier.weight(1f))
                        if (isDone) {
                            Icon(Icons.Default.Check, null, tint = Color(0xFF34C759), modifier = Modifier.size(16.dp))
                        } else {
                            Text("Cleaning...", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanningScreen(progress: Float, currentPath: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = Color(0xFF38BDF8),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("Scanning...", color = Color.Gray, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = currentPath,
                color = Color.Gray.copy(alpha = 0.6f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ResultScreen(junkSize: String, categories: List<JunkCategory>, onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(junkSize, color = Color(0xFF38BDF8), fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
            Text("Junk Files Found", color = Color.Gray, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 分类清单列表 (对标最优竞品)
        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
            items(categories.size) { index ->
                val cat = categories[index]
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(cat.icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(cat.name, color = Color.White, modifier = Modifier.weight(1f))
                        Text("${cat.size / (1024 * 1024)} MB", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("BOOST NOW", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Composable
fun LauncherHomeScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val state by vm.uiState.collectAsState()
    val apps = remember { getInstalledApps(context) }
    
    // 使用 navigationBarsPadding 确保底部 Dock 不会被手势栏遮挡
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF020617)))
    ).navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 强化后的 Phone Status 卡片 (核心变现入口)
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp).fillMaxWidth().height(150.dp),
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                onClick = { vm.resetToScanning() } // 桌面二次点击触发清理
            ) {
                Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Phone Status", color = Color.Gray, fontSize = 14.sp)
                        Text("Ready to Boost", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                            Spacer(Modifier.width(8.dp))
                            Text("Optimization Recommended", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }
                    // 巨大的一键加速按钮
                    Box(
                        modifier = Modifier.size(64.dp).background(Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // Smart Island Toggle Card (Keep as is)
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp).fillMaxWidth(),
                color = if (state.isIslandEnabled) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp),
                onClick = {
                    val isGranted = android.provider.Settings.canDrawOverlays(context)
                    if (!isGranted) {
                        requestOverlayPermission(context)
                    } else {
                        val newEnabled = !state.isIslandEnabled
                        vm.toggleIsland(newEnabled)
                        if (newEnabled) {
                            context.startService(android.content.Intent(context, com.example.prototype2.service.DynamicIslandService::class.java))
                        } else {
                            context.stopService(android.content.Intent(context, com.example.prototype2.service.DynamicIslandService::class.java))
                        }
                    }
                }
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(Color.Black, CircleShape), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(32.dp, 12.dp).clip(CircleShape).background(Color.White))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Island", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Enable for better experience", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = state.isIslandEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                    )
                }
            }

            // App Grid (Polished)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(apps) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { app.launchIntent?.let { context.startActivity(it) } }
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            Image(
                                bitmap = app.icon.toBitmap().asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.label,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        // Bottom Dock
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(100.dp)
                .background(Color.Black.copy(alpha = 0.4f)).padding(bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    val launchIntent: android.content.Intent?
)

fun getInstalledApps(context: android.content.Context): List<AppInfo> {
    val pm = context.packageManager
    val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(mainIntent, 0).map { info ->
        AppInfo(
            info.loadLabel(pm).toString(),
            info.activityInfo.packageName,
            info.loadIcon(pm),
            pm.getLaunchIntentForPackage(info.activityInfo.packageName)
        )
    }.filter { it.packageName != context.packageName }.sortedBy { it.label }
}

fun isDefaultLauncher(context: android.content.Context): Boolean {
    val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME)
    val res = context.packageManager.resolveActivity(intent, 0)
    return res?.activityInfo?.packageName == context.packageName
}

fun requestLauncherPermission(context: android.content.Context) {
    android.widget.Toast.makeText(context, "Opening System Home Settings...", android.widget.Toast.LENGTH_SHORT).show()
    
    // 方案 1: 直接跳转系统桌面设置 (最直接的路径)
    val intentHome = android.content.Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
    intentHome.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    
    // 方案 2: 跳转到“默认应用”管理页 (兼容性更好的路径)
    val intentDefaultApps = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        android.content.Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    } else {
        android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
    }
    intentDefaultApps.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

    // 方案 3: Android 10+ 角色请求 (弹窗路径)
    val intentRole = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val rm = context.getSystemService(android.app.role.RoleManager::class.java)
        if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)) {
            rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else null
    } else null

    try {
        // 依次尝试：桌面设置 -> 默认应用 -> 角色弹窗
        try {
            context.startActivity(intentHome)
        } catch (e: Exception) {
            try {
                context.startActivity(intentDefaultApps)
            } catch (e2: Exception) {
                if (intentRole != null) {
                    context.startActivity(intentRole)
                } else {
                    // 终极兜底：跳转到普通设置
                    val intentSettings = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                    intentSettings.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentSettings)
                }
            }
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error: Please set manually in Settings", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun requestOverlayPermission(context: android.content.Context) {
    val intent = android.content.Intent(
        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        android.net.Uri.parse("package:${context.packageName}")
    )
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun requestNotificationPermission(context: android.content.Context) {
    val intent = android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun requestAccessibilityPermission(context: android.content.Context) {
    val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

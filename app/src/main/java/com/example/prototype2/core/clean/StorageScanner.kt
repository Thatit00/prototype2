package com.example.prototype2.core.clean

import android.content.Context
import android.os.Environment
import java.io.File

data class JunkItem(
    val name: String,
    val size: Long,
    val path: String,
    val isChecked: Boolean = true
)

class StorageScanner(private val context: Context) {
    
    interface ScanListener {
        fun onProgress(path: String, currentSize: Long)
        fun onCompleted(items: List<JunkItem>, totalSize: Long)
    }

    fun startScan(listener: ScanListener) {
        val root = Environment.getExternalStorageDirectory()
        val junkList = mutableListOf<JunkItem>()
        var totalJunkSize = 0L

        // 扩展扫描逻辑：覆盖更多合规目录
        val targetDirs = mutableListOf<File>(
            File(root, "Download"),
            File(root, "temp"),
            File(root, "tmp"),
            File(root, "Android/obb")
        )
        
        // 增加外部缓存目录
        context.getExternalCacheDirs()?.forEach { it?.let { targetDirs.add(it) } }

        Thread {
            targetDirs.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.walkTopDown()
                        .maxDepth(3) // 限制深度，保证性能且避免越权
                        .forEach { file ->
                        listener.onProgress(file.absolutePath, totalJunkSize)
                        
                        // 判定逻辑：.log, .tmp, 大于 10MB 的旧安装包
                        val isJunk = file.extension.lowercase() in listOf("log", "tmp", "temp") || 
                                     (file.extension.lowercase() == "apk" && file.length() > 10 * 1024 * 1024)
                        
                        if (isJunk) {
                            val size = file.length()
                            junkList.add(JunkItem(file.name, size, file.absolutePath))
                            totalJunkSize += size
                        }
                    }
                }
            }
            listener.onCompleted(junkList, totalJunkSize)
        }.start()
    }

    fun performClean(items: List<JunkItem>, onDone: (Long) -> Unit) {
        Thread {
            var deletedSize = 0L
            items.forEach { item ->
                val file = File(item.path)
                if (file.exists()) {
                    val size = file.length()
                    if (file.delete()) {
                        deletedSize += size
                    }
                }
            }
            onDone(deletedSize)
        }.start()
    }
}

package com.example.prototype2.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.prototype2.R

class DynamicIslandService : Service() {
    private var windowManager: WindowManager? = null
    private var islandView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        showIsland()
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "dynamic_island_channel"
        val channelName = "Dynamic Island Service"
        val channel = android.app.NotificationChannel(
            channelId, channelName, android.app.NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.createNotificationChannel(channel)

        return android.app.Notification.Builder(this, channelId)
            .setContentTitle("Dynamic Island Active")
            .setContentText("Tap to configure")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun showIsland() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        islandView = LayoutInflater.from(this).inflate(R.layout.layout_dynamic_island, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 20 // Adjust based on camera cutout

        windowManager?.addView(islandView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        islandView?.let { windowManager?.removeView(it) }
    }
}
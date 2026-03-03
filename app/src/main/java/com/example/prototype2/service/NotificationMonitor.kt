package com.example.prototype2.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationMonitor : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val title = it.notification.extras.getString("android.title")
            val text = it.notification.extras.getString("android.text")
            Log.d("NotificationMonitor", "Received: $title - $text")
            // Send broadcast or update service to show in Dynamic Island
        }
    }
}
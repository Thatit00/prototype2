package com.example.prototype2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prototype2.service.DynamicIslandService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, DynamicIslandService::class.java)
            context?.startForegroundService(serviceIntent)
        }
    }
}
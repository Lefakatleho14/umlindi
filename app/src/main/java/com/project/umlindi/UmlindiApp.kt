package com.project.umlindi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class UmlindiApp : Application() {

    companion object {
        const val CHANNEL_ID = "umlindi_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new nearby safety reports"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
package org.moxxy.moxxy_native.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

class PlatformImplementation(private val context: Context) : MoxxyPlatformApi {
    override fun getPersistentDataPath(): String {
        return context.filesDir.path
    }

    override fun getCacheDataPath(): String {
        return context.cacheDir.path
    }

    override fun openBatteryOptimisationSettings() {
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
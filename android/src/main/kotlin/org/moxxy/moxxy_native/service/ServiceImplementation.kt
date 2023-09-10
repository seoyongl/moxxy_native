package org.moxxy.moxxy_native.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import org.moxxy.moxxy_native.MoxxyNativePlugin
import org.moxxy.moxxy_native.TAG
import org.moxxy.moxxy_native.service.BackgroundServiceStatic.setStartAtBoot

object PluginTracker {
    var instances: MutableList<MoxxyNativePlugin> = mutableListOf()
}

class ServiceImplementation(private val context: Context) : MoxxyServiceApi {
    override fun configure(handle: Long, extraData: String) {
        BackgroundServiceStatic.setConfiguration(
            context,
            handle,
            extraData,
        )
    }

    override fun isRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (info in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BackgroundService::class.java.name == info.service.className) {
                return true
            }
        }

        return false
    }

    override fun start() {
        setStartAtBoot(context, true)
        BackgroundServiceStatic.enqueue(context)
        ContextCompat.startForegroundService(
            context,
            Intent(context, BackgroundService::class.java),
        )
        Log.d(TAG, "Background service started")
    }

    override fun sendData(data: String) {
        for (plugin in PluginTracker.instances) {
            val service = plugin.service
            if (service != null) {
                service.receiveData(data)
                break
            }
        }
    }
}

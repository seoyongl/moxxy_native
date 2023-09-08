package org.moxxy.moxxy_native.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import org.moxxy.moxxy_native.service.BackgroundServiceStatic.getManuallyStopped

class WatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!getManuallyStopped(context)) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, BackgroundService::class.java),
            )
        }
    }
}

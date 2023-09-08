package org.moxxy.moxxy_native.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import org.moxxy.moxxy_native.TAG

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (BackgroundServiceStatic.getStartAtBoot(context)) {
            if (BackgroundServiceStatic.wakeLock == null) {
                Log.d(TAG, "WakeLock is null. Acquiring it...")
                BackgroundServiceStatic.acquireWakeLock(context)
                Log.d(TAG, "WakeLock acquired")
            }

            ContextCompat.startForegroundService(
                context,
                Intent(context, BackgroundService::class.java),
            )
        }
    }
}

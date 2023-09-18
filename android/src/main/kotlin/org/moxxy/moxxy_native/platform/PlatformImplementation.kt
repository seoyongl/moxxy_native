package org.moxxy.moxxy_native.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ShareCompat
import org.moxxy.moxxy_native.content.MoxxyFileProvider

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

    override fun shareItems(items: List<ShareItem>, genericMimeType: String) {
        // Empty lists make no sense
        assert(items.isNotEmpty())

        // Convert the paths to content URIs
        val builder = ShareCompat.IntentBuilder(context).setType(genericMimeType)
        for (item in items) {
            assert(item.text == null && item.path != null || item.text != null && item.path == null)

            if (item.text != null) {
                builder.setText(item.text)
            } else if (item.path != null) {
                builder.addStream(MoxxyFileProvider.getUriForPath(context, item.path))
            }
        }

        // We cannot just use startChooser() because then Android complains that we're not attached
        // to an Activity. So, we just ask it to start a new one.
        val intent = builder.createChooserIntent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

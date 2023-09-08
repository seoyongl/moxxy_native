package org.moxxy.moxxy_native.notifications

import android.content.Intent
import android.util.Log
import org.moxxy.moxxy_native.TAG

/*
 * Extract all user-added extra key-value pairs from @intent.
 * */
fun extractPayloadMapFromIntent(intent: Intent): Map<String?, String?> {
    val extras = mutableMapOf<String?, String?>()
    intent.extras?.keySet()!!.forEach {
        Log.d(TAG, "Checking $it -> ${intent.extras!!.get(it)}")
        if (it.startsWith("payload_")) {
            Log.d(TAG, "Adding $it")
            extras[it.substring(8)] = intent.extras!!.getString(it)
        }
    }

    return extras
}

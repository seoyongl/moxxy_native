package org.moxxy.moxxy_native.notifications

import android.content.Intent

/*
 * Extract all user-added extra key-value pairs from @intent.
 * */
fun extractPayloadMapFromIntent(intent: Intent): Map<String?, String?> {
    val extras = mutableMapOf<String?, String?>()
    intent.extras?.keySet()!!.forEach {
        if (it.startsWith("payload_")) {
            extras[it.substring(8)] = intent.extras!!.getString(it)
        }
    }

    return extras
}

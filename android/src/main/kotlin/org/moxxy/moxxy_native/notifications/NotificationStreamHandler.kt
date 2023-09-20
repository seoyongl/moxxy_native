package org.moxxy.moxxy_native.notifications

import android.util.Log
import io.flutter.plugin.common.EventChannel
import org.moxxy.moxxy_native.TAG

object NotificationStreamHandler : EventChannel.StreamHandler {
    // The event sink to use for sending notification events to the service.
    var sink: EventChannel.EventSink? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        // "register" the event sink
        sink = events

        Log.d(TAG, "NotificationStreamHandler: Attached stream")
    }

    override fun onCancel(arguments: Any?) {
        sink = null
        Log.d(TAG, "NotificationStreamHandler: Detached stream")
    }
}

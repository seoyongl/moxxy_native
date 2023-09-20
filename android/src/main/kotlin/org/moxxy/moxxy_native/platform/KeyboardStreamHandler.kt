package org.moxxy.moxxy_native.platform

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.flutter.plugin.common.EventChannel
import org.moxxy.moxxy_native.TAG

object KeyboardStreamHandler : EventChannel.StreamHandler {
    // The currently active activity. Set by @MoxxyNativePlugin.
    var activity: Activity? = null

    // The current bottom inset.
    private var bottomInset: Int = 0

    // The current event sink to use for sending events to the UI.
    private var sink: EventChannel.EventSink? = null

    private fun handleKeyboardHeightCheck(rootView: View?) {
        rootView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)

                val screenHeight = rootView.height
                // Also subtract the height of the bottom inset as the SafeArea with "bottom: false"
                // allows us to draw under the bottom system bar, if it is there.
                val keypadHeight = screenHeight - r.bottom - bottomInset

                val displayMetrics = activity?.resources?.displayMetrics
                val logicalKeypadHeight = keypadHeight / (displayMetrics?.density ?: 1f)

                if (keypadHeight > screenHeight * 0.15) {
                    sink?.success(logicalKeypadHeight.toDouble())
                } else {
                    sink?.success(0.0)
                }
            }
        })
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        // "register" the event sink
        sink = events

        val rootView = activity?.window?.decorView?.rootView
        handleKeyboardHeightCheck(rootView)

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView!!) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val triggerEvent = bottomInset != insets.bottom
                bottomInset = insets.bottom

                // Notify in case the inset changed
                if (triggerEvent) handleKeyboardHeightCheck(rootView)

                WindowInsetsCompat.CONSUMED
            }
        }
        Log.d(TAG, "KeyboardStreamHandler: Attached stream")
    }

    override fun onCancel(arguments: Any?) {
        sink = null
        Log.d(TAG, "KeyboardStreamHandler: Detached stream")
    }
}

package org.moxxy.moxxy_native_example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("moxxy_native", "onCreate intent ${intent?.action}")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        Log.d("moxxy_native", "New intent ${intent.action}")
    }
}

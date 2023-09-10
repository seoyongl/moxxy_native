package org.moxxy.moxxy_native

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.service.ServiceAware
import io.flutter.embedding.engine.plugins.service.ServicePluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import org.moxxy.moxxy_native.contacts.ContactsImplementation
import org.moxxy.moxxy_native.contacts.MoxxyContactsApi
import org.moxxy.moxxy_native.cryptography.CryptographyImplementation
import org.moxxy.moxxy_native.cryptography.MoxxyCryptographyApi
import org.moxxy.moxxy_native.media.MediaImplementation
import org.moxxy.moxxy_native.media.MoxxyMediaApi
import org.moxxy.moxxy_native.notifications.MoxxyNotificationsApi
import org.moxxy.moxxy_native.notifications.NotificationEvent
import org.moxxy.moxxy_native.notifications.NotificationsImplementation
import org.moxxy.moxxy_native.picker.FilePickerType
import org.moxxy.moxxy_native.picker.MoxxyPickerApi
import org.moxxy.moxxy_native.picker.PickerResultListener
import org.moxxy.moxxy_native.platform.MoxxyPlatformApi
import org.moxxy.moxxy_native.platform.PlatformImplementation
import org.moxxy.moxxy_native.service.BackgroundService
import org.moxxy.moxxy_native.service.MoxxyServiceApi
import org.moxxy.moxxy_native.service.PluginTracker
import org.moxxy.moxxy_native.service.ServiceImplementation

object MoxxyEventChannels {
    var notificationChannel: EventChannel? = null
    var notificationEventSink: EventChannel.EventSink? = null
}

object NotificationStreamHandler : EventChannel.StreamHandler {
    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.d(TAG, "NotificationStreamHandler: Attached stream")
        MoxxyEventChannels.notificationEventSink = events
    }

    override fun onCancel(arguments: Any?) {
        Log.d(TAG, "NotificationStreamHandler: Detached stream")
        MoxxyEventChannels.notificationEventSink = null
    }
}

/*
 * Hold the last notification event in case we did a cold start.
 */
object NotificationCache {
    var lastEvent: NotificationEvent? = null
}

class MoxxyNativePlugin : FlutterPlugin, ActivityAware, ServiceAware, BroadcastReceiver(), MoxxyPickerApi {
    private var context: Context? = null
    private var activity: Activity? = null
    private lateinit var pickerListener: PickerResultListener
    private val cryptographyImplementation = CryptographyImplementation()
    private lateinit var contactsImplementation: ContactsImplementation
    private lateinit var platformImplementation: PlatformImplementation
    private val mediaImplementation = MediaImplementation()
    private lateinit var notificationsImplementation: NotificationsImplementation
    private lateinit var serviceImplementation: ServiceImplementation

    var service: BackgroundService? = null

    var channel: MethodChannel? = null

    init {
        PluginTracker.instances.add(this)
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        contactsImplementation = ContactsImplementation(context!!)
        platformImplementation = PlatformImplementation(context!!)
        notificationsImplementation = NotificationsImplementation(context!!)
        serviceImplementation = ServiceImplementation(context!!)

        // Register the pigeon handlers
        MoxxyPickerApi.setUp(flutterPluginBinding.binaryMessenger, this)
        MoxxyNotificationsApi.setUp(flutterPluginBinding.binaryMessenger, notificationsImplementation)
        MoxxyCryptographyApi.setUp(flutterPluginBinding.binaryMessenger, cryptographyImplementation)
        MoxxyContactsApi.setUp(flutterPluginBinding.binaryMessenger, contactsImplementation)
        MoxxyPlatformApi.setUp(flutterPluginBinding.binaryMessenger, platformImplementation)
        MoxxyMediaApi.setUp(flutterPluginBinding.binaryMessenger, mediaImplementation)
        MoxxyServiceApi.setUp(flutterPluginBinding.binaryMessenger, serviceImplementation)

        // Special handling for the service APIs
        channel = MethodChannel(flutterPluginBinding.getBinaryMessenger(), SERVICE_FOREGROUND_METHOD_CHANNEL_KEY)
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
            this,
            IntentFilter(SERVICE_FOREGROUND_METHOD_CHANNEL_KEY),
        )

        // Register the picker handler
        pickerListener = PickerResultListener(context!!)
        Log.d(TAG, "Attached to engine")
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
            this,
            IntentFilter(SERVICE_FOREGROUND_METHOD_CHANNEL_KEY),
        )
        Log.d(TAG, "Detached from engine")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(pickerListener)
        Log.d(TAG, "Attached to activity")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        Log.d(TAG, "Detached from activity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
        Log.d(TAG, "Detached from activity")
    }

    override fun onAttachedToService(binding: ServicePluginBinding) {
        Log.d(TAG, "Attached to service")
        service = binding.getService() as BackgroundService
    }

    override fun onDetachedFromService() {
        Log.d(TAG, "Detached from service")
        service = null
    }

    override fun pickFiles(
        type: FilePickerType,
        multiple: Boolean,
        callback: (Result<List<String>>) -> Unit,
    ) {
        val requestCode = if (multiple) PICK_FILES_REQUEST else PICK_FILE_REQUEST
        AsyncRequestTracker.requestTracker[requestCode] = callback as (Result<Any>) -> Unit
        if (type == FilePickerType.GENERIC) {
            val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                this.type = "*/*"

                // Allow/disallow picking multiple files
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple)
            }
            activity?.startActivityForResult(pickIntent, requestCode)
            return
        }

        val contract = when (multiple) {
            false -> ActivityResultContracts.PickVisualMedia()
            true -> ActivityResultContracts.PickMultipleVisualMedia()
        }
        val pickType = when (type) {
            // We keep FilePickerType.GENERIC here, even though we know that @type will never be
            // GENERIC to make Kotlin happy.
            FilePickerType.GENERIC, FilePickerType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
            FilePickerType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
            FilePickerType.IMAGEANDVIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }
        val pickIntent = contract.createIntent(context!!, PickVisualMediaRequest(pickType))
        activity?.startActivityForResult(pickIntent, requestCode)
    }

    override fun pickFileWithData(type: FilePickerType, callback: (Result<ByteArray?>) -> Unit) {
        AsyncRequestTracker.requestTracker[PICK_FILE_WITH_DATA_REQUEST] = callback as (Result<Any>) -> Unit
        if (type == FilePickerType.GENERIC) {
            val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                this.type = "*/*"
            }
            activity?.startActivityForResult(pickIntent, PICK_FILE_WITH_DATA_REQUEST)
            return
        }

        val pickType = when (type) {
            // We keep FilePickerType.GENERIC here, even though we know that @type will never be
            // GENERIC to make Kotlin happy.
            FilePickerType.GENERIC, FilePickerType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
            FilePickerType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
            FilePickerType.IMAGEANDVIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
        }
        val contract = ActivityResultContracts.PickVisualMedia()
        val pickIntent = contract.createIntent(context!!, PickVisualMediaRequest(pickType))
        activity?.startActivityForResult(pickIntent, PICK_FILE_WITH_DATA_REQUEST)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent with ${intent.action}")
        if (intent.action?.equals(SERVICE_FOREGROUND_METHOD_CHANNEL_KEY) == true) {
            val data = intent.getStringExtra("data")
            channel?.invokeMethod("dataReceived", data)
        }
    }
}

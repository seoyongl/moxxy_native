package org.moxxy.moxxy_native

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.app.NotificationManagerCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import org.moxxy.moxxy_native.contacts.ContactsImplementation
import org.moxxy.moxxy_native.contacts.MoxxyContactsApi
import org.moxxy.moxxy_native.cryptography.CryptographyImplementation
import org.moxxy.moxxy_native.cryptography.MoxxyCryptographyApi
import org.moxxy.moxxy_native.notifications.MessagingNotification
import org.moxxy.moxxy_native.notifications.MoxxyNotificationsApi
import org.moxxy.moxxy_native.notifications.NotificationChannel
import org.moxxy.moxxy_native.notifications.NotificationDataManager
import org.moxxy.moxxy_native.notifications.NotificationEvent
import org.moxxy.moxxy_native.notifications.NotificationGroup
import org.moxxy.moxxy_native.notifications.NotificationI18nData
import org.moxxy.moxxy_native.notifications.RegularNotification
import org.moxxy.moxxy_native.notifications.createNotificationChannelsImpl
import org.moxxy.moxxy_native.notifications.createNotificationGroupsImpl
import org.moxxy.moxxy_native.notifications.showNotificationImpl
import org.moxxy.moxxy_native.picker.FilePickerType
import org.moxxy.moxxy_native.picker.MoxxyPickerApi
import org.moxxy.moxxy_native.picker.PickerResultListener
import org.moxxy.moxxy_native.platform.MoxxyPlatformApi
import org.moxxy.moxxy_native.platform.PlatformImplementation

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

class MoxxyNativePlugin : FlutterPlugin, ActivityAware, MoxxyPickerApi, MoxxyNotificationsApi {
    private var context: Context? = null
    private var activity: Activity? = null
    private lateinit var activityClass: Class<Any>
    private lateinit var pickerListener: PickerResultListener
    private val cryptographyImplementation = CryptographyImplementation()
    private lateinit var contactsImplementation: ContactsImplementation
    private lateinit var platformImplementation: PlatformImplementation

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        contactsImplementation = ContactsImplementation(context!!)
        platformImplementation = PlatformImplementation(context!!)

        // Register the pigeon handlers
        MoxxyPickerApi.setUp(flutterPluginBinding.binaryMessenger, this)
        MoxxyNotificationsApi.setUp(flutterPluginBinding.binaryMessenger, this)
        MoxxyCryptographyApi.setUp(flutterPluginBinding.binaryMessenger, cryptographyImplementation)
        MoxxyContactsApi.setUp(flutterPluginBinding.binaryMessenger, contactsImplementation)
        MoxxyPlatformApi.setUp(flutterPluginBinding.binaryMessenger, platformImplementation)

        // Register the picker handler
        pickerListener = PickerResultListener(context!!)
        Log.d(TAG, "Attached to engine")
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(TAG, "Detached from engine")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activityClass = activity!!.javaClass
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

    override fun createNotificationGroups(groups: List<NotificationGroup>) {
        createNotificationGroupsImpl(context!!, groups)
    }

    override fun deleteNotificationGroups(ids: List<String>) {
        val notificationManager = context!!.getSystemService(NotificationManager::class.java)
        for (id in ids) {
            notificationManager.deleteNotificationChannelGroup(id)
        }
    }

    override fun createNotificationChannels(channels: List<NotificationChannel>) {
        createNotificationChannelsImpl(context!!, channels)
    }

    override fun deleteNotificationChannels(ids: List<String>) {
        val notificationManager = context!!.getSystemService(NotificationManager::class.java)
        for (id in ids) {
            notificationManager.deleteNotificationChannel(id)
        }
    }

    override fun showMessagingNotification(notification: MessagingNotification) {
        org.moxxy.moxxy_native.notifications.showMessagingNotification(context!!, notification)
    }

    override fun showNotification(notification: RegularNotification) {
        showNotificationImpl(context!!, notification)
    }

    override fun dismissNotification(id: Long) {
        NotificationManagerCompat.from(context!!).cancel(id.toInt())
    }

    override fun setNotificationSelfAvatar(path: String) {
        NotificationDataManager.setAvatarPath(context!!, path)
    }

    override fun setNotificationI18n(data: NotificationI18nData) {
        NotificationDataManager.apply {
            setYou(context!!, data.you)
            setReply(context!!, data.reply)
            setMarkAsRead(context!!, data.markAsRead)
        }
    }

    override fun notificationStub(event: NotificationEvent) {
        TODO("Not yet implemented")
    }
}

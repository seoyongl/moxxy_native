package org.moxxy.moxxy_native.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import org.moxxy.moxxy_native.R
import org.moxxy.moxxy_native.SERVICE_BACKGROUND_METHOD_CHANNEL_KEY
import org.moxxy.moxxy_native.SERVICE_DEFAULT_BODY
import org.moxxy.moxxy_native.SERVICE_DEFAULT_TITLE
import org.moxxy.moxxy_native.SERVICE_ENTRYPOINT_KEY
import org.moxxy.moxxy_native.SERVICE_EXTRA_DATA_KEY
import org.moxxy.moxxy_native.SERVICE_MANUALLY_STOPPED_KEY
import org.moxxy.moxxy_native.SERVICE_SHARED_PREFERENCES_KEY
import org.moxxy.moxxy_native.SERVICE_START_AT_BOOT_KEY
import org.moxxy.moxxy_native.SERVICE_WAKELOCK_DURATION
import org.moxxy.moxxy_native.TAG
import org.moxxy.moxxy_native.service.background.MoxxyBackgroundServiceApi
import java.util.concurrent.atomic.AtomicBoolean

object BackgroundServiceStatic {
    @Volatile
    var wakeLock: WakeLock? = null

    fun acquireWakeLock(context: Context): WakeLock {
        if (wakeLock == null) {
            val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock =
                manager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "${this.javaClass.name}.class")
            wakeLock!!.setReferenceCounted(true)
        }

        return wakeLock!!
    }

    fun enqueue(context: Context) {
        val mutable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            111,
            Intent(context, WatchdogReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or mutable,
        )

        AlarmManagerCompat.setAndAllowWhileIdle(
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 5000,
            pendingIntent,
        )
    }

    fun getStartAtBoot(context: Context): Boolean {
        return context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .getBoolean(
                SERVICE_START_AT_BOOT_KEY,
                false,
            )
    }

    fun setStartAtBoot(context: Context, value: Boolean) {
        context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
            .putBoolean(SERVICE_START_AT_BOOT_KEY, value)
            .apply()
    }

    fun getManuallyStopped(context: Context): Boolean {
        return context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .getBoolean(
                SERVICE_MANUALLY_STOPPED_KEY,
                false,
            )
    }
}

class BackgroundService : Service(), MoxxyBackgroundServiceApi {

    // Indicates whether the background service is running or not
    private var isRunning = AtomicBoolean(false)

    // Indicates whether the service was stopped manually
    private var isManuallyStopped = false

    // If non-null, the Flutter Engine that is running the background service's code
    private var engine: FlutterEngine? = null

    // The callback for Dart to start execution at
    private var dartCallback: DartExecutor.DartCallback? = null

    // Method channel for Java -> Dart
    private var methodChannel: MethodChannel? = null

    // Data for the notification
    private var notificationTitle: String = SERVICE_DEFAULT_TITLE
    private var notificationBody: String = SERVICE_DEFAULT_BODY

    private fun setManuallyStopped(context: Context, value: Boolean) {
        context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
            .putBoolean(SERVICE_MANUALLY_STOPPED_KEY, value)
            .apply()
    }

    private fun getHandle(): Long {
        return getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).getLong(
            SERVICE_ENTRYPOINT_KEY,
            0,
        )
    }

    private fun updateNotificationInfo() {
        val mutable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            99778,
            packageManager.getLaunchIntentForPackage(applicationContext.packageName),
            PendingIntent.FLAG_CANCEL_CURRENT or mutable,
        )

        val notification = NotificationCompat.Builder(this, "foreground_service").apply {
            setSmallIcon(R.drawable.ic_service)
            setAutoCancel(false)
            setOngoing(true)
            setContentTitle(notificationTitle)
            setContentText(notificationBody)
            setContentIntent(pendingIntent)
        }.build()
        startForeground(99778, notification)
    }

    private fun runService() {
        try {
            if (isRunning.get() || (engine?.getDartExecutor()?.isExecutingDart() ?: false)) return

            if (BackgroundServiceStatic.wakeLock == null) {
                Log.d(TAG, "WakeLock is null. Acquiring and grabbing WakeLock...")
                BackgroundServiceStatic.acquireWakeLock(applicationContext)
                    .acquire(SERVICE_WAKELOCK_DURATION)
                Log.d(TAG, "WakeLock grabbed")
            }

            // Update the notification
            updateNotificationInfo()

            // Set-up the Flutter Engine, if it's not already set up
            if (!FlutterInjector.instance().flutterLoader().initialized()) {
                FlutterInjector.instance().flutterLoader().startInitialization(applicationContext)
            }
            FlutterInjector.instance().flutterLoader().ensureInitializationComplete(
                applicationContext,
                null,
            )
            val callback: FlutterCallbackInformation =
                FlutterCallbackInformation.lookupCallbackInformation(getHandle())
            if (callback == null) {
                Log.e(TAG, "Callback handle not found")
                return
            }
            isRunning.set(true)
            engine = FlutterEngine(this)
            engine!!.getServiceControlSurface().attachToService(this, null, true)
            methodChannel = MethodChannel(
                engine!!.getDartExecutor()!!.getBinaryMessenger(),
                SERVICE_BACKGROUND_METHOD_CHANNEL_KEY,
            )

            MoxxyBackgroundServiceApi.setUp(engine!!.getDartExecutor()!!.getBinaryMessenger(), this)
            Log.d(TAG, "MoxxyBackgroundServiceApi ready")

            dartCallback = DartExecutor.DartCallback(
                assets,
                FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                callback,
            )
            engine!!.getDartExecutor().executeDartCallback(dartCallback!!)
        } catch (ex: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to set up background service: ${ex.message}")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        notificationBody = SERVICE_DEFAULT_BODY
        updateNotificationInfo()
    }

    override fun onDestroy() {
        if (!isManuallyStopped) {
            BackgroundServiceStatic.enqueue(this)
        } else {
            setManuallyStopped(applicationContext, true)
        }

        // Dispose of the engine
        engine?.apply {
            getServiceControlSurface().detachFromService()
            destroy()
        }
        engine = null
        dartCallback = null

        // Stop the service
        stopForeground(true)
        isRunning.set(false)

        super.onDestroy()
    }

    fun receiveData(data: String) {
        methodChannel?.invokeMethod("dataReceived", data)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        setManuallyStopped(this, false)
        BackgroundServiceStatic.enqueue(this)
        runService()

        return START_STICKY
    }

    override fun getHandler(): Long {
        return getHandle()
    }

    override fun getExtraData(): String {
        return getSharedPreferences(SERVICE_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).getString(
            SERVICE_EXTRA_DATA_KEY,
            "",
        )!!
    }

    override fun setNotificationBody(body: String) {
        notificationBody = body
        updateNotificationInfo()
    }

    override fun sendData(data: String) {
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(SERVICE_BACKGROUND_METHOD_CHANNEL_KEY).apply {
                putExtra("data", data)
            },
        )
    }

    override fun stop() {
        isManuallyStopped = true
        val mutable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            111,
            Intent(this, WatchdogReceiver::class.java),
            PendingIntent.FLAG_CANCEL_CURRENT or mutable,
        )
        val stopManager = getSystemService(ALARM_SERVICE) as AlarmManager
        stopManager.cancel(pendingIntent)
        stopSelf()
        BackgroundServiceStatic.setStartAtBoot(applicationContext, false)
    }
}

package org.moxxy.moxxy_native.notifications

import android.app.Notification
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.IconCompat
import org.moxxy.moxxy_native.MARK_AS_READ_ACTION
import org.moxxy.moxxy_native.MOXXY_FILEPROVIDER_ID
import org.moxxy.moxxy_native.NOTIFICATION_EXTRA_ID_KEY
import org.moxxy.moxxy_native.NOTIFICATION_EXTRA_JID_KEY
import org.moxxy.moxxy_native.NOTIFICATION_MESSAGE_EXTRA_MIME
import org.moxxy.moxxy_native.NOTIFICATION_MESSAGE_EXTRA_PATH
import org.moxxy.moxxy_native.R
import org.moxxy.moxxy_native.REPLY_ACTION
import org.moxxy.moxxy_native.REPLY_TEXT_KEY
import org.moxxy.moxxy_native.TAG
import org.moxxy.moxxy_native.TAP_ACTION
import java.io.File

class NotificationsImplementation(private val context: Context) : MoxxyNotificationsApi {
    override fun createNotificationGroups(groups: List<NotificationGroup>) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        for (group in groups) {
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(group.id, group.description),
            )
        }
    }

    override fun deleteNotificationGroups(ids: List<String>) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        for (id in ids) {
            notificationManager.deleteNotificationChannelGroup(id)
        }
    }

    override fun createNotificationChannels(channels: List<NotificationChannel>) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        for (channel in channels) {
            val importance = when (channel.importance) {
                NotificationChannelImportance.DEFAULT -> NotificationManager.IMPORTANCE_DEFAULT
                NotificationChannelImportance.MIN -> NotificationManager.IMPORTANCE_MIN
                NotificationChannelImportance.HIGH -> NotificationManager.IMPORTANCE_HIGH
            }
            val notificationChannel =
                android.app.NotificationChannel(channel.id, channel.title, importance).apply {
                    description = channel.description

                    enableVibration(channel.vibration)
                    enableLights(channel.enableLights)
                    setShowBadge(channel.showBadge)

                    if (channel.groupId != null) {
                        group = channel.groupId
                    }
                }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun deleteNotificationChannels(ids: List<String>) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        for (id in ids) {
            notificationManager.deleteNotificationChannel(id)
        }
    }

    override fun showMessagingNotification(notification: MessagingNotification) {
        // Build the actions
        // -> Reply action
        val remoteInput = RemoteInput.Builder(REPLY_TEXT_KEY).apply {
            setLabel(NotificationDataManager.getReply(context))
        }.build()
        val replyIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = REPLY_ACTION
            putExtra(NOTIFICATION_EXTRA_JID_KEY, notification.jid)
            putExtra(NOTIFICATION_EXTRA_ID_KEY, notification.id)

            notification.extra?.forEach {
                putExtra("payload_${it.key}", it.value)
            }
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            replyIntent,
            PendingIntent.FLAG_MUTABLE,
        )
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.reply,
            NotificationDataManager.getReply(context),
            replyPendingIntent,
        ).apply {
            addRemoteInput(remoteInput)
            setAllowGeneratedReplies(true)
        }.build()

        // -> Mark as read action
        val markAsReadIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = MARK_AS_READ_ACTION
            putExtra(NOTIFICATION_EXTRA_JID_KEY, notification.jid)
            putExtra(NOTIFICATION_EXTRA_ID_KEY, notification.id)

            notification.extra?.forEach {
                putExtra("payload_${it.key}", it.value)
            }
        }
        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            markAsReadIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )
        val markAsReadAction = NotificationCompat.Action.Builder(
            R.drawable.mark_as_read,
            NotificationDataManager.getMarkAsRead(context),
            markAsReadPendingIntent,
        ).build()

        // -> Tap action
        // Thanks to flutter_local_notifications for this "workaround"
        val tapIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)!!.apply {
                action = TAP_ACTION
                putExtra(NOTIFICATION_EXTRA_JID_KEY, notification.jid)
                putExtra(NOTIFICATION_EXTRA_ID_KEY, notification.id)

                notification.extra?.forEach {
                    putExtra("payload_${it.key}", it.value)
                }

                // Do not launch a new task
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val tapPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(tapIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        // Build the notification
        val selfPerson = Person.Builder().apply {
            setName(NotificationDataManager.getYou(context))

            // Set an avatar, if we have one
            val avatarPath = NotificationDataManager.getAvatarPath(context)
            if (avatarPath != null) {
                setIcon(
                    IconCompat.createWithAdaptiveBitmap(
                        BitmapFactory.decodeFile(avatarPath),
                    ),
                )
            }
        }.build()
        val style = NotificationCompat.MessagingStyle(selfPerson)
        style.isGroupConversation = notification.isGroupchat
        if (notification.isGroupchat) {
            style.conversationTitle = notification.title
        }

        for (i in notification.messages.indices) {
            val message = notification.messages[i]!!

            // Build the sender
            // NOTE: Note that we set it to null if message.sender == null because otherwise this results in
            //       a bogus Person object which messes with the "self-message" display as Android expects
            //       null in that case.
            val sender = if (message.sender == null) {
                null
            } else {
                Person.Builder().apply {
                    setName(message.sender)
                    setKey(message.jid)

                    // Set the avatar, if available
                    if (message.avatarPath != null) {
                        try {
                            setIcon(
                                IconCompat.createWithAdaptiveBitmap(
                                    BitmapFactory.decodeFile(message.avatarPath),
                                ),
                            )
                        } catch (ex: Throwable) {
                            Log.w(TAG, "Failed to open avatar at ${message.avatarPath}")
                        }
                    }
                }.build()
            }

            // Build the message
            val body = message.content.body ?: ""
            val msg = NotificationCompat.MessagingStyle.Message(
                body,
                message.timestamp,
                sender,
            )
            // If we got an image, turn it into a content URI and set it
            if (message.content.mime != null && message.content.path != null) {
                val fileUri = FileProvider.getUriForFile(
                    context,
                    MOXXY_FILEPROVIDER_ID,
                    File(message.content.path),
                )
                msg.apply {
                    setData(message.content.mime, fileUri)

                    extras.apply {
                        putString(NOTIFICATION_MESSAGE_EXTRA_MIME, message.content.mime)
                        putString(NOTIFICATION_MESSAGE_EXTRA_PATH, message.content.path)
                    }
                }
            }

            // Append the message
            style.addMessage(msg)
        }

        // Assemble the notification
        val finalNotification = NotificationCompat.Builder(context, notification.channelId).apply {
            setStyle(style)
            // NOTE: It's okay to use the service icon here as I cannot get Android to display the
            //       actual logo. So we'll have to make do with the silhouette and the color purple.
            setSmallIcon(R.drawable.ic_service)
            color = Color.argb(255, 207, 74, 255)
            setColorized(true)

            // Tap action
            setContentIntent(tapPendingIntent)

            // Notification actions
            addAction(replyAction)
            addAction(markAsReadAction)

            // Groupchat title
            if (notification.isGroupchat) {
                setContentTitle(notification.title)
            }

            // Prevent grouping with the foreground service
            if (notification.groupId != null) {
                setGroup(notification.groupId)
            }

            setAllowSystemGeneratedContextualActions(true)
            setCategory(Notification.CATEGORY_MESSAGE)

            // Prevent no notification when we replied before
            setOnlyAlertOnce(false)

            // Automatically dismiss the notification on tap
            setAutoCancel(true)
        }.build()

        // Post the notification
        try {
            NotificationManagerCompat.from(context).notify(
                notification.id.toInt(),
                finalNotification,
            )
        } catch (ex: SecurityException) {
            // Should never happen as Moxxy checks for the permission before posting the notification
            Log.e(TAG, "Failed to post notification: ${ex.message}")
        }
    }

    override fun showNotification(notification: RegularNotification) {
        val builtNotification = NotificationCompat.Builder(context, notification.channelId).apply {
            setContentTitle(notification.title)
            setContentText(notification.body)

            when (notification.icon) {
                NotificationIcon.ERROR -> setSmallIcon(R.drawable.error)
                NotificationIcon.WARNING -> setSmallIcon(R.drawable.warning)
                NotificationIcon.NONE -> {}
            }

            if (notification.groupId != null) {
                setGroup(notification.groupId)
            }
        }.build()

        // Post the notification
        try {
            NotificationManagerCompat.from(context)
                .notify(notification.id.toInt(), builtNotification)
        } catch (ex: SecurityException) {
            // Should never happen as Moxxy checks for the permission before posting the notification
            Log.e(TAG, "Failed to post notification: ${ex.message}")
        }
    }

    override fun dismissNotification(id: Long) {
        NotificationManagerCompat.from(context).cancel(id.toInt())
    }

    override fun setNotificationSelfAvatar(path: String) {
        NotificationDataManager.setAvatarPath(context, path)
    }

    override fun setNotificationI18n(data: NotificationI18nData) {
        NotificationDataManager.apply {
            setYou(
                context,
                data.you,
            )
            setReply(
                context,
                data.reply,
            )
            setMarkAsRead(
                context,
                data.markAsRead,
            )
        }
    }

    override fun notificationStub(event: NotificationEvent) {
        // N/A
    }
}

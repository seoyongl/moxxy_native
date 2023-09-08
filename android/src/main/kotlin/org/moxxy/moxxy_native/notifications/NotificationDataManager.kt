package org.moxxy.moxxy_native.notifications

import android.content.Context
import org.moxxy.moxxy_native.SHARED_PREFERENCES_AVATAR_KEY
import org.moxxy.moxxy_native.SHARED_PREFERENCES_KEY
import org.moxxy.moxxy_native.SHARED_PREFERENCES_MARK_AS_READ_KEY
import org.moxxy.moxxy_native.SHARED_PREFERENCES_REPLY_KEY
import org.moxxy.moxxy_native.SHARED_PREFERENCES_YOU_KEY

/*
 * Holds "persistent" data for notifications, like i18n strings. While not useful now, this is
 * useful for when the app is dead and we receive a notification.
 * */
object NotificationDataManager {
    private var you: String? = null
    private var markAsRead: String? = null
    private var reply: String? = null

    private var fetchedAvatarPath = false
    private var avatarPath: String? = null

    private fun getString(context: Context, key: String, fallback: String): String {
        return context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)!!.getString(key, fallback)!!
    }

    private fun setString(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(key, value)
            .apply()
    }

    fun getYou(context: Context): String {
        if (you == null) you = getString(context, SHARED_PREFERENCES_YOU_KEY, "You")
        return you!!
    }

    fun setYou(context: Context, value: String) {
        setString(context, SHARED_PREFERENCES_YOU_KEY, value)
        you = value
    }

    fun getMarkAsRead(context: Context): String {
        if (markAsRead == null) markAsRead = getString(context, SHARED_PREFERENCES_MARK_AS_READ_KEY, "Mark as read")
        return markAsRead!!
    }

    fun setMarkAsRead(context: Context, value: String) {
        setString(context, SHARED_PREFERENCES_MARK_AS_READ_KEY, value)
        markAsRead = value
    }

    fun getReply(context: Context): String {
        if (reply != null) reply = getString(context, SHARED_PREFERENCES_REPLY_KEY, "Reply")
        return reply!!
    }

    fun setReply(context: Context, value: String) {
        setString(context, SHARED_PREFERENCES_REPLY_KEY, value)
        reply = value
    }

    fun getAvatarPath(context: Context): String? {
        if (avatarPath == null && !fetchedAvatarPath) {
            val path = getString(context, SHARED_PREFERENCES_AVATAR_KEY, "")
            if (path.isNotEmpty()) {
                avatarPath = path
            }
        }

        return avatarPath
    }

    fun setAvatarPath(context: Context, value: String) {
        setString(context, SHARED_PREFERENCES_AVATAR_KEY, value)
        fetchedAvatarPath = true
        avatarPath = value
    }
}

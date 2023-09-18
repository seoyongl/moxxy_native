package org.moxxy.moxxy_native

const val TAG = "moxxy_native"

// The size of buffers to use for various operations
const val BUFFER_SIZE = 4096

// The data key for text entered in the notification's reply field
const val REPLY_TEXT_KEY = "key_reply_text"

// The key for the notification id to mark as read
const val MARK_AS_READ_ID_KEY = "notification_id"

// Values for actions performed through the notification
const val REPLY_ACTION = "reply"
const val MARK_AS_READ_ACTION = "mark_as_read"
const val TAP_ACTION = "tap"

// Extra data keys for the intents that reach the NotificationReceiver
const val NOTIFICATION_EXTRA_JID_KEY = "jid"
const val NOTIFICATION_EXTRA_ID_KEY = "notification_id"

// Extra data keys for messages embedded inside the notification style
const val NOTIFICATION_MESSAGE_EXTRA_MIME = "mime"
const val NOTIFICATION_MESSAGE_EXTRA_PATH = "path"

const val MOXXY_FILEPROVIDER_ID = "org.moxxy.moxxyv2.fileprovider2"

// Shared preferences keys
const val SHARED_PREFERENCES_KEY = "org.moxxy.moxxyv2"
const val SHARED_PREFERENCES_YOU_KEY = "you"
const val SHARED_PREFERENCES_MARK_AS_READ_KEY = "mark_as_read"
const val SHARED_PREFERENCES_REPLY_KEY = "reply"
const val SHARED_PREFERENCES_AVATAR_KEY = "avatar_path"

// Request codes
const val PICK_FILE_REQUEST = 42
const val PICK_FILES_REQUEST = 43
const val PICK_FILE_WITH_DATA_REQUEST = 44

// Service
const val SERVICE_SHARED_PREFERENCES_KEY = "me.polynom.moxplatform_android"
const val SERVICE_ENTRYPOINT_KEY = "entrypoint_handle"
const val SERVICE_EXTRA_DATA_KEY = "extra_data"
const val SERVICE_START_AT_BOOT_KEY = "auto_start_at_boot"
const val SERVICE_MANUALLY_STOPPED_KEY = "manually_stopped"

// https://github.com/ekasetiawans/flutter_background_service/blob/e427f3b70138ec26f9671c2617f9061f25eade6f/packages/flutter_background_service_android/android/src/main/java/id/flutter/flutter_background_service/BootReceiver.java#L20
const val SERVICE_WAKELOCK_DURATION = 10 * 60 * 1000L
const val SERVICE_DEFAULT_TITLE = "Moxxy"
const val SERVICE_DEFAULT_BODY = "Preparing..."
const val SERVICE_FOREGROUND_METHOD_CHANNEL_KEY = "org.moxxy.moxxy_native/foreground"
const val SERVICE_BACKGROUND_METHOD_CHANNEL_KEY = "org.moxxy.moxxy_native/background"

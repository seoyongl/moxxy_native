package org.moxxy.moxxy_native.contacts

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import org.moxxy.moxxy_native.R

/*
 * Implementation of Moxxy's contact APIs.
 * */
class ContactsImplementation(private val context: Context) : MoxxyContactsApi {
    override fun recordSentMessage(
        name: String,
        jid: String,
        avatarPath: String?,
        fallbackIcon: FallbackIconType
    ) {
        val pkgName = context.packageName
        val intent = Intent(context, Class.forName("$pkgName.MainActivity")).apply {
            action = Intent.ACTION_SEND

            // Compatibility with share_handler
            putExtra("conversationIdentifier", jid)
        }

        val shortcutTarget = "$pkgName.dynamic_share_target"
        val shortcutBuilder = ShortcutInfoCompat.Builder(context, jid).apply {
            setShortLabel(name)
            setIsConversation()
            setCategories(setOf(shortcutTarget))
            setIntent(intent)
            setLongLived(true)
        }

        val personBuilder = Person.Builder().apply {
            setKey(jid)
            setName(name)
        }

        // Either set an avatar image OR a fallback icon
        if (avatarPath != null) {
            val icon = IconCompat.createWithAdaptiveBitmap(
                BitmapFactory.decodeFile(avatarPath),
            )
            shortcutBuilder.setIcon(icon)
            personBuilder.setIcon(icon)
        } else {
            val resourceId = when(fallbackIcon) {
                FallbackIconType.NONE, FallbackIconType.PERSON -> R.mipmap.person
                FallbackIconType.NOTES -> R.mipmap.notes
            }
            val icon = IconCompat.createWithResource(context, resourceId)
            shortcutBuilder.setIcon(icon)
            personBuilder.setIcon(icon)
        }

        shortcutBuilder.setPerson(personBuilder.build())
        ShortcutManagerCompat.addDynamicShortcuts(
            context,
            listOf(shortcutBuilder.build()),
        )
    }
}
package org.moxxy.moxxy_native.media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Log
import org.moxxy.moxxy_native.TAG
import java.io.FileOutputStream

class MediaImplementation : MoxxyMediaApi {
    override fun generateVideoThumbnail(src: String, dest: String, maxWidth: Long): Boolean {
        try {
            // Get a frame as a thumbnail
            val mmr = MediaMetadataRetriever().apply {
                setDataSource(src)
            }
            val unscaledThumbnail = mmr.getFrameAtTime(0) ?: return false

            // Scale down the thumbnail while keeping the aspect ratio
            val scalingFactor = maxWidth.toDouble() / unscaledThumbnail.width
            Log.d(TAG, "Scaling to $maxWidth from ${unscaledThumbnail.width} with scalingFactor $scalingFactor")
            val thumbnail = Bitmap.createScaledBitmap(
                unscaledThumbnail,
                (unscaledThumbnail.width * scalingFactor).toInt(),
                (unscaledThumbnail.height * scalingFactor).toInt(),
                false,
            )

            // Write it to the destination file
            val fileOutputStream = FileOutputStream(dest)
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, fileOutputStream)

            // Clean up
            fileOutputStream.apply {
                flush()
                close()
            }

            // Success
            return true
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to create thumbnail for $src: ${ex.message}")
            return false
        }
    }
}

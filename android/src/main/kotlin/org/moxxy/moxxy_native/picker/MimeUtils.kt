package org.moxxy.moxxy_native.picker

object MimeUtils {
    // A reverse-mapping of image mime types to their commonly used file extension.
    val imageMimeTypesToFileExtension = mapOf(
        "image/png" to ".png",
        "image/apng" to ".apng",
        "image/avif" to ".avif",
        "image/gif" to ".gif",
        "image/jpeg" to ".jpg",
        "image/webp" to ".webp",
    )
}

package org.moxxy.moxxy_native

object AsyncRequestTracker {
    val requestTracker: MutableMap<Int, (Result<Any>) -> Unit> = mutableMapOf()
}

package org.moxxy.moxxy_native.cryptography

import java.io.FileOutputStream
import java.security.MessageDigest

/*
 * A FileOutputStream that continuously hashes whatever it writes to the file.
 */
class HashedFileOutputStream(name: String, hashAlgorithm: String) : FileOutputStream(name) {
    private val digest: MessageDigest

    init {
        this.digest = MessageDigest.getInstance(hashAlgorithm)
    }

    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        super.write(buffer, offset, length)

        digest.update(buffer, offset, length)
    }

    fun digest(): ByteArray {
        return digest.digest()
    }
}

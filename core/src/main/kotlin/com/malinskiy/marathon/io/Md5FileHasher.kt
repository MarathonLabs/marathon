package com.malinskiy.marathon.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class Md5FileHasher : FileHasher {

    override suspend fun getHash(file: File): String =
        withContext(Dispatchers.Default) {
            file.calculateHash()
        }

    private fun File.calculateHash(): String {
        val messageDigest = MessageDigest.getInstance("MD5")

        val digest = inputStream()
            .use {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int

                do {
                    bytesRead = it.read(buffer);
                    if (bytesRead > 0) {
                        messageDigest.update(buffer, 0, bytesRead);
                    }
                } while (bytesRead != -1)

                messageDigest.digest()
            }

        return BigInteger(1, digest).toString(16)
    }
}

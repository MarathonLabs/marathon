package com.malinskiy.marathon.extension

import java.math.BigInteger
import java.security.MessageDigest

internal fun String.withPrefix(prefix: String?): String {
    return if (prefix.isNullOrEmpty()) {
        this
    } else {
        "$prefix.$this"
    }
}

fun String.md5(): BigInteger {
    return BigInteger(MD5.md5.digest(toByteArray(charset = Charsets.UTF_8)))
}

object MD5 {
    val md5: MessageDigest by lazy { MessageDigest.getInstance("MD5") }
}

fun String.escape(): String {
    return replace(regex = escapeRegex, "-")
}

val escapeRegex = "[^a-zA-Z0-9\\.\\#]".toRegex()

fun String.addFileNumberForVideo(fileNumber: String): String {
    return with(this.split(".mp4")) {return@with "${this[0]}$fileNumber.mp4"}
}

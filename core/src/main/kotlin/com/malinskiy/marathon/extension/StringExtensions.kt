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

// `#` used to be preserved so `toTestName()`'s `Class#method` separator
// survived into the on-disk filename. That backfires under `file://`: even
// URL-encoded as `%23`, Chromium's local file loader mis-handles the
// character inside `<video>` and `<img>` src attributes — `<video>` requests
// return 200 with 0 bytes; `<img>` sometimes fails silently. Collapse `#`
// into `-` here so no artifact path ever contains it. `#` was never a
// meaningful separator to preserve — everything downstream (allure, junit,
// html-report) reconstructs the class/method split from `Test.pkg/clazz/method`
// directly rather than parsing filenames.
val escapeRegex = "[^a-zA-Z0-9\\.]".toRegex()

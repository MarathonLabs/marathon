package com.malinskiy.marathon.ios.xctestrun

interface PropertyListKey {
    fun toKeyString(): String
}

fun PropertyListKey.toEntry(): Pair<String, PropertyListKey> = toKeyString() to this

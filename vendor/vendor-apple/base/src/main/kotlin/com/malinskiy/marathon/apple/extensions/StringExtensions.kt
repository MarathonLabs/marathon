package com.malinskiy.marathon.apple.extensions

fun String.bashEscape() = "'" + replace("'", "'\\''") + "'"

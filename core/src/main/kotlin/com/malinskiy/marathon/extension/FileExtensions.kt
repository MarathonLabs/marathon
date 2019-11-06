package com.malinskiy.marathon.extension

import java.io.File

/*
 * Fixed version of `toRelativeString()` from Kotlin stdlib that forces use of absolute file paths.
 * See https://youtrack.jetbrains.com/issue/KT-14056
*/
fun File.relativePathTo(base: File): String = absoluteFile.toRelativeString(base.absoluteFile)
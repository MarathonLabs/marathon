package com.malinskiy.marathon.test.assert

import org.amshove.kluent.shouldBeEqualTo
import java.io.File

fun File.shouldBeEqualTo(expected: File) = readText().shouldBeEqualTo(expected.readText())
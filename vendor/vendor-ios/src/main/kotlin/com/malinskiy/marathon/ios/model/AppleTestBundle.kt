package com.malinskiy.marathon.ios.model

import com.malinskiy.marathon.execution.bundle.TestBundle
import java.io.File

class AppleTestBundle(
    val application: File?,
    val testApplication: File,
    val testBinary: File,
) : TestBundle() {
    override val id: String
        get() = testApplication.absolutePath
}

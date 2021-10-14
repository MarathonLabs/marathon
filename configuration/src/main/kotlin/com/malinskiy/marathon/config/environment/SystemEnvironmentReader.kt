package com.malinskiy.marathon.config.environment

import java.io.File

class SystemEnvironmentReader(
    private val environment: (String) -> String? = { System.getenv(it) }
) : EnvironmentReader {
    override fun read() = EnvironmentConfiguration(
        androidSdk = androidSdkPath()?.let { File(it) }
    )

    private fun androidSdkPath() =
        environment("ANDROID_HOME") ?: environment("ANDROID_SDK_ROOT")
}

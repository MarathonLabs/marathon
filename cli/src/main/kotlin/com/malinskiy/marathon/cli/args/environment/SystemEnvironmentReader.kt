package com.malinskiy.marathon.cli.args.environment

import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import java.io.File

class SystemEnvironmentReader(
    private val environment: (String) -> String?
) : EnvironmentReader {
    override fun read() = EnvironmentConfiguration(
        androidSdk = androidSdkPath()?.let { File(it) }
    )

    private fun androidSdkPath() =
        environment("ANDROID_HOME") ?: environment("ANDROID_SDK_ROOT")
}

package com.malinskiy.marathon.cli.args.environment

import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import java.io.File

class SystemEnvironmentReader(
    private val environment: (String) -> String? = { System.getenv(it) },
) : EnvironmentReader {
    override fun read() = EnvironmentConfiguration(
        androidSdk = androidSdkPath()?.let { File(it) },
        javaHome = javaHomePath()?.let { File(it) },
    )

    private fun androidSdkPath() =
        environment("ANDROID_HOME") ?: environment("ANDROID_SDK_ROOT")

    private fun javaHomePath() = environment("JAVA_HOME")
}

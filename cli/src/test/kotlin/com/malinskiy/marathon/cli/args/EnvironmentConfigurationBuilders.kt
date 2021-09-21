package com.malinskiy.marathon.cli.args

import java.io.File

fun environmentConfiguration(
    androidSdk: File? = null,
    javaHome: File? = null,
) = EnvironmentConfiguration(
    androidSdk = androidSdk,
    javaHome = javaHome,
)

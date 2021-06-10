package com.malinskiy.marathon.cli.args

import java.io.File

fun environmentConfiguration(
    androidSdk: File? = null
) = EnvironmentConfiguration(
    androidSdk = androidSdk
)

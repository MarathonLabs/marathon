package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.config.environment.EnvironmentConfiguration
import java.io.File

fun environmentConfiguration(
    androidSdk: File? = null
) = EnvironmentConfiguration(
    androidSdk = androidSdk
)

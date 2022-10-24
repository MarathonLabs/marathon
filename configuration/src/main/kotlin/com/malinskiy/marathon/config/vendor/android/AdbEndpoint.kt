package com.malinskiy.marathon.config.vendor.android

import java.io.Serializable

data class AdbEndpoint(
    val host: String = "127.0.0.1",
    val port: Int = 5037,
) : Serializable

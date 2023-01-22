package com.malinskiy.marathon.ios.cmd

import java.nio.charset.Charset

interface CommandHost {
    val id: String
    val charset: Charset
}

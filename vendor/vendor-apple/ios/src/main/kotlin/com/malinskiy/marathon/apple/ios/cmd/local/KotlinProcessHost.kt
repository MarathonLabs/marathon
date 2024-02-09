package com.malinskiy.marathon.apple.ios.cmd.local

import com.malinskiy.marathon.apple.ios.cmd.CommandHost
import java.nio.charset.Charset

data class KotlinProcessHost(override val id: String = "local", override val charset: Charset = Charset.defaultCharset()) : CommandHost

package com.malinskiy.marathon.apple.cmd.local

import com.malinskiy.marathon.apple.cmd.CommandHost
import java.nio.charset.Charset

data class KotlinProcessHost(override val id: String = "local", override val charset: Charset = Charset.defaultCharset()) : CommandHost

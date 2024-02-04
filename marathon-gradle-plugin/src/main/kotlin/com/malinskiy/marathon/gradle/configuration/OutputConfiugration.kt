package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.OUTPUT_MAX_FILENAME
import com.malinskiy.marathon.config.OUTPUT_MAX_PATH
import com.malinskiy.marathon.config.OutputConfiguration

open class OutputConfiguration {
    fun toStrategy(): OutputConfiguration {
        return OutputConfiguration(maxPath ?: OUTPUT_MAX_PATH, maxFilename ?: OUTPUT_MAX_FILENAME)
    }

    var maxPath: Int? = null
    var maxFilename: Int? = null
}

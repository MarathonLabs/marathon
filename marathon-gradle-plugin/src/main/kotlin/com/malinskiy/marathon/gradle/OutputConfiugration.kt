package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.OutputConfiguration

open class OutputConfiguration {
    fun toStrategy(): OutputConfiguration {
        return maxPath?.let {
            OutputConfiguration(maxPath = it)
        } ?: OutputConfiguration()
    }

    var maxPath: Int? = null
}

package com.malinskiy.marathon.execution

interface ComponentInfoExtractor {
    fun extract(configuration: Configuration): ComponentInfo
}

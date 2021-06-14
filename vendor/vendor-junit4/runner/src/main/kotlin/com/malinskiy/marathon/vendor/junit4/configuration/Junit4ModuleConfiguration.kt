package com.malinskiy.marathon.vendor.junit4.configuration

import java.io.File

data class Junit4ModuleConfiguration(
    val applicationClasspath: List<File>?,
    val testClasspath: List<File>?,
)

package com.malinskiy.marathon.vendor.junit4.parsing

import java.io.File

data class TestBundle(
    val name: String,
    val applicationClasspath: List<File>? = null,
    val testClasspath: List<File>? = null,
)

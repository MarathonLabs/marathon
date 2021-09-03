package com.malinskiy.marathon.vendor.junit4.model

import com.malinskiy.marathon.execution.bundle.TestBundle
import java.io.File

class JUnit4TestBundle(
    override val id: String,
    val applicationClasspath: List<File>? = null,
    val testClasspath: List<File>? = null,
    val workdir: String? = null,
) : TestBundle()

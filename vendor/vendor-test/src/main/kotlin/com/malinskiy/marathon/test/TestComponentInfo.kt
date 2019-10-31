package com.malinskiy.marathon.test

import com.malinskiy.marathon.execution.ComponentInfo
import java.io.File

data class TestComponentInfo(
    val someInfo: String = "test",
    override val outputDir: File = File(".")
) : ComponentInfo
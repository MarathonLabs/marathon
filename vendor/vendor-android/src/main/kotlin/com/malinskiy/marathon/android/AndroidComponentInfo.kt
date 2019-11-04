package com.malinskiy.marathon.android

import com.malinskiy.marathon.execution.ComponentInfo
import java.io.File

data class AndroidComponentInfo(
    override val name: String,
    val applicationOutput: File?,
    val testApplicationOutput: File
) : ComponentInfo

package com.malinskiy.marathon.gradle

import org.gradle.api.tasks.Delete
import java.io.File

open class MarathonCleanTask : Delete() {
    init {
        group = GROUP
        val buildDir = project.buildDir
        val marathonDir = File(buildDir, "marathon")
        delete(marathonDir)
    }

    companion object {
        const val GROUP = "marathon"
        const val NAME = "cleanMarathonWrapper"
    }
}

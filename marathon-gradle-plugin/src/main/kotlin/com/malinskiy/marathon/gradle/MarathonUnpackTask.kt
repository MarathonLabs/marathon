package com.malinskiy.marathon.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class MarathonUnpackTask : DefaultTask() {
    init {
        group = GROUP
    }

    @TaskAction
    fun unpack() {
        val buildDir = project.buildDir
        val marathonBuildDir = File(buildDir, "marathon").apply { mkdirs() }
        DistributionInstaller().install(marathonBuildDir)
    }

    companion object {
        const val GROUP = "marathon"
        const val NAME = "marathonWrapper"
    }
}

package com.malinskiy.marathon.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

open class MarathonUnpackTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
    init {
        group = GROUP
    }
    
    @TaskAction
    fun unpack() {
        val buildDir = project.buildDir
        val marathonBuildDir = File(buildDir, "marathon").apply { mkdirs() }
        
        val (dir, script) = DistributionInstaller().install(marathonBuildDir)
    }
    
    companion object {
        const val GROUP = "marathon"
        const val NAME = "marathonWrapper"
    }
}

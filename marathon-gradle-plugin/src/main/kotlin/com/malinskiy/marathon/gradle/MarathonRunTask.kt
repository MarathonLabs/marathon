package com.malinskiy.marathon.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.VerificationTask
import org.gradle.internal.os.OperatingSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import javax.inject.Inject

open class MarathonRunTask @Inject constructor(objects: ObjectFactory) : AbstractExecTask<MarathonRunTask>(MarathonRunTask::class.java),
    VerificationTask {
    
    @InputFile
    val marathonfile = objects.fileProperty()

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    private var ignoreFailure: Boolean = false

    override fun exec() {
        setExecutable(getPlatformScript(Paths.get(project.rootProject.buildDir.canonicalPath, "marathon").toFile()))
        setArgs(listOf("-m", marathonfile.get().asFile.canonicalPath))
        
        super.exec()
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }

    private fun getPlatformScript(marathonBuildDir: File) = when (OperatingSystem.current()) {
        OperatingSystem.WINDOWS -> {
            Paths.get(marathonBuildDir.canonicalPath, "cli", "bin", "marathon.bat").toFile()
        }
        else -> {
            val cliPath = Paths.get(marathonBuildDir.canonicalPath, "cli", "bin", "marathon")
            cliPath.apply {
                val permissions = Files.getPosixFilePermissions(this)
                Files.setPosixFilePermissions(this, permissions + PosixFilePermission.OWNER_EXECUTE)
            }.toFile()
        }
    }
}

package com.malinskiy.marathon.tasks.prepare

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class UnzipTask : DefaultTask() {
    var destination: Any? = null

    @TaskAction
    fun greet() {
        val file = getDestination()

        val pb = ProcessBuilder()
        val process = pb.command("unzip", "${archivePath}/marathon.zip")
            .start()
        val exited = process.waitFor(1, TimeUnit.MINUTES)
        if (!exited) {
            throw RuntimeException("Cannot unzip marathon archive. Exit code: ${process.exitValue()}")
        }
    }
}

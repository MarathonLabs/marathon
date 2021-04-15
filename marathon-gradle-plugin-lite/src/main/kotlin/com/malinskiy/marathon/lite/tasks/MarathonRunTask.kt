package com.malinskiy.marathon.lite.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.property
import org.gradle.process.internal.shutdown.ShutdownHooks
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject


open class MarathonRunTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout
) : DefaultTask(), VerificationTask {
    private var ignoreFailure: Boolean = false

    @get:InputFile
    val marathonConfigFile: RegularFileProperty = objects.fileProperty()

//    @get:InputDirectory
//    @get:PathSensitive(PathSensitivity.NAME_ONLY)
//    val marathonOutputDirectory: DirectoryProperty = objects.directoryProperty()

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")
    @get:Input
    var randomProperty: Property<Int> = objects.property()

    @TaskAction
    fun runMarathon() {
        val success = startMarathon(marathonConfigFile.get().asFile.path)
        val shouldReportFailure = !ignoreFailure
        if (!success) {
            if (!success && shouldReportFailure) {
//                throw GradleException("Tests failed! See ${marathonOutputDirectory.asFile.get().path}/html/index.html")
                throw GradleException("Tests failed!")
            }
        }
    }

    private fun startMarathon(path: String): Boolean {
        val pb = ProcessBuilder().command("marathon", "--proto", path)
        executeProcessWorking(pb);
        return true
    }


    open fun executeProcessWorking(processBuilder: ProcessBuilder) {
        val process = processBuilder.redirectErrorStream(true).start()
        ShutdownHooks.addShutdownHook {
            process.destroy()
        }
        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
            }
        }
        val exitCode = process.waitFor()
        println("Working Exit Code: $exitCode")
    }
    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}

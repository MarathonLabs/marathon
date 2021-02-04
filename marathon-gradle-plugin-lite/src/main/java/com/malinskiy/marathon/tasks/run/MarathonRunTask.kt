package com.malinskiy.marathon.tasks.run

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant
import com.malinskiy.marathon.extensions.extractApplication
import com.malinskiy.marathon.extensions.extractTestApplication
import com.malinskiy.marathon.log.MarathonLogging
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import java.io.File

private val log = MarathonLogging.logger {}

open class MarathonRunTask : DefaultTask(), VerificationTask {
    lateinit var flavorName: String
    lateinit var applicationVariant: BaseVariant
    lateinit var testVariant: TestVariant
    lateinit var extensionConfig: MarathonExtension
    lateinit var sdk: File
    lateinit var cnf: Configuration

    var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val instrumentationApk = testVariant.extractTestApplication()
        val applicationApk = applicationVariant.extractApplication()

        val baseOutputDir =
            if (extensionConfig.baseOutputDir != null) File(extensionConfig.baseOutputDir) else File(project.buildDir, "reports/marathon")
        val output = File(baseOutputDir, flavorName)



    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}

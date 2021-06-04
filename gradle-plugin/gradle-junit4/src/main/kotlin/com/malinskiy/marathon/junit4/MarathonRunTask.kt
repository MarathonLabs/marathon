package com.malinskiy.marathon.junit4

import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.property
import java.io.File
import java.io.InputStream
import java.io.PrintStream
import java.util.Scanner
import javax.inject.Inject


open class MarathonRunTask @Inject constructor(objects: ObjectFactory) : DefaultTask(), VerificationTask {
    @Input
    val mainSourceSet: Property<String> = objects.property()

    @Input
    val testSourceSet: Property<String> = objects.property()

    private var ignoreFailure: Boolean = false

    @OutputDirectory
    var fakeLockingOutput = File(project.rootProject.buildDir, "fake-marathon-locking-output")

    @TaskAction
    fun runMarathon() {
        val container = project.extensions.findByType<SourceSetContainer>() ?: throw IllegalStateException("No SourceSetContainer is found")
        val mainClasspath = container.getByName(mainSourceSet.get()).runtimeClasspath
        val testClasspath = container.getByName(testSourceSet.get()).runtimeClasspath

        val process = ProcessBuilder("marathon", "--application-classpath", mainClasspath.joinToString(separator = ":") { it.absolutePath },
                                     "--test-classpath", testClasspath.joinToString(separator = ":") { it.absolutePath })
            .apply {
                directory(project.projectDir)
                println("Application classpath")
                mainClasspath.forEach {
                    println("- \"${it.absolutePath}\"")
                }
                println("Test classpath")
                testClasspath.forEach {
                    println("- \"${it.absolutePath}\"")
                }
                println("Workdir: ${project.projectDir}")
            }.start()

        inheritIO(process.inputStream, System.out);
        inheritIO(process.errorStream, System.err);

        process.waitFor()
    }

    private fun inheritIO(src: InputStream, dest: PrintStream) {
        Thread {
            val sc = Scanner(src)
            while (sc.hasNextLine()) {
                dest.println(sc.nextLine())
            }
        }.start()
    }

    override fun getIgnoreFailures(): Boolean = ignoreFailure

    override fun setIgnoreFailures(ignoreFailures: Boolean) {
        ignoreFailure = ignoreFailures
    }
}

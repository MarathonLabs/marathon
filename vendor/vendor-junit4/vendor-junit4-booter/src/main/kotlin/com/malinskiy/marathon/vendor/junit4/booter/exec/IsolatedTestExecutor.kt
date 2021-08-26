package com.malinskiy.marathon.vendor.junit4.booter.exec

import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class IsolatedTestExecutor : TestExecutor {
    override fun run(
        tests: MutableList<TestDescription>,
        javaHome: String?,
        javaOptions: List<String>,
        classpathList: MutableList<String>
    ): Flow<TestEvent> {
        val port = listen()

        val classpath = "$runnerJar${classpathList.joinToString(separator = ":")}"
        val javaBinary = javaHome?.let { Paths.get(it, "bin", "java") } ?: Paths.get("java")
        val args = StringBuilder().apply {
            append(javaOptions.joinToString(separator = "") { "$it " })
            append("-cp ")
            append(classpath)
            append(" com.malinskiy.marathon.vendor.junit4.runner.Runner")
        }.toString()

        val testList = Files.createTempFile("marathon", "testlist").toFile()
            .apply { writeText(tests.joinToString(separator = "\n") { it.fqtn }) }

        val process = ProcessBuilder(javaBinary.toString(), *args.split(" ").toTypedArray())
            .apply {
                environment()["PORT"] = port.toString()
                environment()["FILTER"] = testList.absolutePath
            }.inheritIO().start()

        return flow<TestEvent> { }
    }

    private fun listen(): Int {
        TODO("Not yet implemented")
    }

    companion object {
        val runnerJar: File by lazy {
            val tempFile = File.createTempFile("marathon", "runner.jar")
            javaClass.getResourceAsStream("/vendor-junit4-runner-all.jar").copyTo(tempFile.outputStream())
            tempFile.deleteOnExit()
            tempFile
        }
    }
}

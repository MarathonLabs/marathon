package com.malinskiy.marathon.ios

import com.malinskiy.marathon.log.MarathonLogging
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executors

interface DockerTestParser {
    fun listTests(testRunnerPaths: List<File>): List<String>
}

class BinaryTestParser(private val binaryParserDockerImageName: String): DockerTestParser {
    private val logger = MarathonLogging.logger(BinaryTestParser::class.java.simpleName)

    private class DockerOutputReader(private val inputStream: InputStream,
                                     private val consumer: (String) -> Unit) : Runnable {
        override fun run() {
            BufferedReader(InputStreamReader(inputStream)).lines()
                    .forEach(consumer)
        }
    }

    override fun listTests(testRunnerPaths: List<File>): List<String> {
        val pathMappings = testRunnerPaths.map {
            it.absolutePath to File("/tmp").resolve(it.name).absolutePath }

        val command =
                listOf("docker", "run", "--rm") +
                    pathMappings.map {
                    listOf("-v", "${it.first}:${it.second}:ro") }
                        .flatten() + binaryParserDockerImageName + pathMappings.map { it.second }

        val builder = ProcessBuilder()
                .command(command)
                .directory(File(System.getProperty("user.dir")))
        val process = builder.start()

        val output = mutableListOf<String>()
        val outputReader = DockerOutputReader(process.inputStream) { output.add(it) }
        Executors.newSingleThreadExecutor().submit(outputReader)
        return if (process.waitFor() == 0) {
            output.toList()
        } else {
            throw IllegalStateException("Unable to read list of tests")
        }
    }
}

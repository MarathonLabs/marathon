package com.malinskiy.marathon.vendor.junit4.booter.exec

import com.malinskiy.marathon.vendor.junit4.booter.contract.EventType
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.runner.contract.Message
import com.malinskiy.marathon.vendor.junit4.runner.contract.TestIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry

class IsolatedTestExecutor : TestExecutor {
    override fun run(
        tests: MutableList<TestDescription>,
        javaHome: String?,
        javaOptions: List<String>,
        classpathList: MutableList<String>,
        workdir: String
    ): Flow<TestEvent> {
        return flow {
            val socket = ServerSocket(0)
            val port = socket.localPort

            /**
             * This solution is similar to https://github.com/bazelbuild/bazel/commit/d9a7d3a789be559bd6972208af21adae871d7a44
             */
            val classpathJar = writePathingJarFile(classpathList.map { File(it) })
            val classpath = "$runnerJar:$classpathJar"

            val javaBinary = if (javaHome.isNullOrEmpty()) {
                Paths.get("java")
            } else {
                Paths.get(javaHome, "bin", "java")
            }
            val args = StringBuilder().apply {
                append(javaOptions.joinToString(separator = "") { "$it " })
                append("-cp ")
                append(classpath)
                append(" com.malinskiy.marathon.vendor.junit4.runner.Runner")
            }.toString()

            val testList = Files.createTempFile("marathon", "testlist").toFile()
                .apply {
                    outputStream().buffered().use { stream ->
                        tests.map {
                            TestIdentifier.newBuilder()
                                .setFqtn(it.fqtn)
                                .build()
                        }.forEach { it.writeDelimitedTo(stream) }
                    }
                }

            val process = ProcessBuilder(javaBinary.toString(), *args.split(" ").toTypedArray())
                .apply {
                    environment()["OUTPUT"] = port.toString()
                    environment()["FILTER"] = testList.absolutePath
                    if (!workdir.isNullOrEmpty()) {
                        //This will work only locally
                        directory(File(workdir))
                    }
                }
                .inheritIO()
                .start()

            socket.accept().use {
                val inputStream = it.getInputStream()

                while (!it.isInputShutdown) {
                    val message: Message = Message.parseDelimitedFrom(inputStream) ?: break
                    emit(message.toTestEvent())
                }

                inputStream.close()
            }
            socket.close()
            process.waitFor()
        }
    }

    private fun writePathingJarFile(classPath: List<File>): File {
        val pathingJarFile = File.createTempFile("classpath", ".jar").apply { deleteOnExit() }
        FileOutputStream(pathingJarFile).use { fileOutputStream ->
            JarOutputStream(fileOutputStream, toManifest(classPath)).use { jarOutputStream ->
                jarOutputStream.putNextEntry(ZipEntry("META-INF/"))
            }
        }
        return pathingJarFile
    }

    private fun toManifest(classPath: List<File>): Manifest {
        val manifest = Manifest()
        val attributes = manifest.mainAttributes
        attributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        attributes.putValue(
            "Class-Path",
            classPath.map(File::toURI).map(URI::toString).joinToString(" ")
        )
        return manifest
    }

    companion object {
        val runnerJar: File by lazy {
            val tempFile = File.createTempFile("marathon", "runner.jar")
            javaClass.getResourceAsStream("/vendor-junit4-runner-all").copyTo(tempFile.outputStream())
            tempFile.deleteOnExit()
            tempFile
        }
    }
}

private fun Message.toTestEvent(): TestEvent {
    return when (type) {
        Message.Type.RUN_STARTED -> TestEvent.newBuilder()
            .setEventType(EventType.RUN_STARTED)
            .build()
        Message.Type.RUN_FINISHED -> TestEvent.newBuilder()
            .setEventType(EventType.RUN_FINISHED)
            .setTotalDurationMillis(totalDurationMillis)
            .build()
        Message.Type.TEST_STARTED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_STARTED)
            .setClassname(classname)
            .setMethod(method)
            .setTestCount(testCount)
            .build()
        Message.Type.TEST_FINISHED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_FINISHED)
            .setClassname(classname)
            .setMethod(method)
            .build()
        Message.Type.TEST_FAILURE -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_FAILURE)
            .setClassname(classname)
            .setMethod(method)
            .setMessage(message)
            .setStacktrace(stacktrace)
            .build()
        Message.Type.TEST_ASSUMPTION_FAILURE -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_ASSUMPTION_FAILURE)
            .setClassname(classname)
            .setMethod(method)
            .setMessage(message)
            .setStacktrace(stacktrace)
            .build()
        Message.Type.TEST_IGNORED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_IGNORED)
            .setClassname(classname)
            .setMethod(method)
            .build()
        Message.Type.UNRECOGNIZED -> TODO()
    }
}

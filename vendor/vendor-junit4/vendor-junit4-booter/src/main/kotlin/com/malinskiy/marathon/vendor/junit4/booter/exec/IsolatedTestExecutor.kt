package com.malinskiy.marathon.vendor.junit4.booter.exec

import com.malinskiy.marathon.vendor.junit4.booter.contract.EventType
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestDescription
import com.malinskiy.marathon.vendor.junit4.booter.contract.TestEvent
import com.malinskiy.marathon.vendor.junit4.runner.contract.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths

class IsolatedTestExecutor : TestExecutor {
    override fun run(
        tests: MutableList<TestDescription>,
        javaHome: String?,
        javaOptions: List<String>,
        classpathList: MutableList<String>
    ): Flow<TestEvent> {
        val socket = ServerSocket(0)
        val port = socket.localPort

        val classpath = "$runnerJar:${classpathList.joinToString(separator = ":")}"
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
            .apply { writeText(tests.joinToString(separator = "\n") { it.fqtn }) }

        val process = ProcessBuilder(javaBinary.toString(), *args.split(" ").toTypedArray())
            .apply {
                environment()["OUTPUT"] = port.toString()
                environment()["FILTER"] = testList.absolutePath
            }
            .inheritIO()
            .start()

        return flow {
            socket.accept().use {
                val inputStream = it.getInputStream()

                while (!it.isInputShutdown) {
                    val message = Frame.Message.parseDelimitedFrom(inputStream)
                    emit(message.toTestEvent())
                }

                inputStream.close()
            }
            socket.close()
            process.waitFor()

        }
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

private fun Frame.Message.toTestEvent(): TestEvent {
    return when (type) {
        Frame.Message.Type.RUN_STARTED -> TestEvent.newBuilder()
            .setEventType(EventType.RUN_STARTED)
            .build()
        Frame.Message.Type.RUN_FINISHED -> TestEvent.newBuilder()
            .setEventType(EventType.RUN_FINISHED)
            .setTotalDurationMillis(totalDurationMillis)
            .build()
        Frame.Message.Type.TEST_STARTED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_STARTED)
            .setClassname(classname)
            .setMethod(method)
            .setTestCount(testCount)
            .build()
        Frame.Message.Type.TEST_FINISHED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_FINISHED)
            .setClassname(classname)
            .setMethod(method)
            .build()
        Frame.Message.Type.TEST_FAILURE -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_FAILURE)
            .setClassname(classname)
            .setMethod(method)
            .setMessage(message)
            .setStacktrace(stacktrace)
            .build()
        Frame.Message.Type.TEST_ASSUMPTION_FAILURE -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_ASSUMPTION_FAILURE)
            .setClassname(classname)
            .setMethod(method)
            .setMessage(message)
            .setStacktrace(stacktrace)
            .build()
        Frame.Message.Type.TEST_IGNORED -> TestEvent.newBuilder()
            .setEventType(EventType.TEST_IGNORED)
            .setClassname(classname)
            .setMethod(method)
            .build()
        Frame.Message.Type.UNRECOGNIZED -> TODO()
    }
}

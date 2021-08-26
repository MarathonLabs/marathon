package com.malinskiy.marathon.vendor.junit4.executor.local

import com.malinskiy.marathon.vendor.junit4.booter.Mode
import com.malinskiy.marathon.vendor.junit4.client.TestDiscoveryClient
import com.malinskiy.marathon.vendor.junit4.client.TestExecutorClient
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.configuration.executor.ExecutorConfigurationAdapter
import com.malinskiy.marathon.vendor.junit4.executor.Booter
import com.malinskiy.marathon.vendor.junit4.executor.listener.LineListener
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Scanner
import java.util.concurrent.ConcurrentLinkedQueue


class LocalhostBooter(
    private val conf: Junit4Configuration,
    private val controlPort: Int = 50051,
    private val mode: Mode = Mode.RUNNER,
    private val debug: Boolean? = null,
) : Booter {
    private val logListeners = ConcurrentLinkedQueue<LineListener>()

    private var useArgfiles: Boolean = true
    private lateinit var process: Process
    private lateinit var args: String
    private lateinit var argsFile: File
    private lateinit var javaBinary: Path
    override var testExecutorClient: TestExecutorClient? = null
    override var testDiscoveryClient: TestDiscoveryClient? = null

    override fun prepare() {
        val executorConfiguration = conf.executorConfiguration as ExecutorConfigurationAdapter

        javaBinary = executorConfiguration.javaHome?.let { Paths.get(it.absolutePath, "bin", "java") } ?: Paths.get("java")
        val classpath = "$booterJar"

        args = StringBuilder().apply {
            if (debug ?: conf.debugBooter) {
                append("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044 ")
            }
            when (mode) {
                Mode.RUNNER -> {
                    append(executorConfiguration.javaOptions.joinToString(separator = "") { "$it " })
                }
                Mode.DISCOVER -> {
                }
            }
            append("-cp ")
            append(classpath)
            append(" com.malinskiy.marathon.vendor.junit4.booter.BooterKt")
        }.toString()

        useArgfiles = executorConfiguration.useArgfiles
        if (useArgfiles) {
            argsFile = File("argsfile-${controlPort}").apply {
                deleteOnExit()
                delete()
                appendText(args)
            }
        }

        fork(false)
    }

    override fun recreate() {
        fork(true)
    }

    override fun addLogListener(logListener: LineListener) {
        logListeners.add(logListener)
    }

    override fun removeLogListener(logListener: LineListener) {
        logListeners.remove(logListener)
    }

    override fun dispose() {
        testExecutorClient?.close()
        testDiscoveryClient?.close()
        process.destroy()
        process.waitFor()
    }

    private fun fork(clean: Boolean) {
        if (clean) {
            dispose()
        }

        process = if (useArgfiles) {
            ProcessBuilder(javaBinary.toString(), "@${argsFile.absolutePath}")
        } else {
            ProcessBuilder(javaBinary.toString(), *args.split(" ").toTypedArray())
        }.apply {
            environment()["PORT"] = controlPort.toString()
            environment()["MODE"] = mode.toString()
        }.start()


        inheritIO(process.inputStream)
        inheritIO(process.errorStream)

        val localChannel = ManagedChannelBuilder.forAddress("localhost", controlPort).apply {
            usePlaintext()
            executor(Dispatchers.IO.asExecutor())
        }.build()

        when (mode) {
            Mode.RUNNER -> testExecutorClient = TestExecutorClient(localChannel)
            Mode.DISCOVER -> testDiscoveryClient = TestDiscoveryClient(localChannel)
        }
    }

    private fun inheritIO(src: InputStream) {
        Thread {
            val scanner = Scanner(src)
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                logListeners.forEach { listener ->
                    listener.onLine(line)
                }
            }
        }.start()
    }

    companion object {
        val booterJar: File by lazy {
            val tempFile = File.createTempFile("marathon", "booter.jar")
            javaClass.getResourceAsStream("/booter-all.jar").copyTo(tempFile.outputStream())
            tempFile.deleteOnExit()
            tempFile
        }
    }
}

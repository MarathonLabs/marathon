package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.junit4.client.TestExecutorClient
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.executor.listener.CompositeTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.DebugTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.LineListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.LogListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.ProgressTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.TestRunResultsListener
import com.malinskiy.marathon.vendor.junit4.install.Junit4AppInstaller
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.io.InputStream
import java.util.Scanner
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

class Junit4Device(protected val timer: Timer, private val controlPort: Int = 50051) : Device {
    override val operatingSystem: OperatingSystem = OperatingSystem(System.getProperty("os.name") ?: "")
    override val serialNumber: String = UUID.randomUUID().toString()
    override val model: String = System.getProperty("java.version")
    override val manufacturer: String = System.getProperty("java.vendor")
    override val networkState: NetworkState = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> = emptySet()
    override val healthy: Boolean = true
    override val abi: String = System.getProperty("os.arch")
    private val forkEvery: Int = 1000
    private var current: Int = 0

    private lateinit var client: TestExecutorClient
    private lateinit var process: Process
    private lateinit var argsFile: File

    private val logger = MarathonLogging.logger {}

    override suspend fun prepare(configuration: Configuration) {
        val conf = configuration.vendorConfiguration as Junit4Configuration
        val installer = Junit4AppInstaller(conf)
        installer.install()

        val booterFile = booterJar()

        val applicationClasspath = mutableListOf<File>().apply {
            conf.testBundles?.forEach { bundle ->
                bundle.applicationClasspath?.let { addAll(it) }
            }
            conf.applicationClasspath?.let { addAll(it) }
        }.joinToString(separator = ":") { File(it.toURI()).toString() }

        val testClasspath = mutableListOf<File>().apply {
            conf.testBundles?.forEach { bundle ->
                bundle.testClasspath?.let { addAll(it) }
            }
            conf.testClasspath?.let { addAll(it) }
        }.joinToString(separator = ":") { File(it.toURI()).toString() }

        val classpath = "$booterFile:$applicationClasspath:$testClasspath"
        argsFile = File("argsfile-${controlPort}").apply {
            deleteOnExit()
            delete()
            if(conf.debugBooter) {
                appendText("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044 ")
            }
            appendText("-cp ")
            appendText(classpath)
            appendText(" com.malinskiy.marathon.vendor.junit4.booter.BooterKt")
        }

        if (configuration.debug) {
            addLogListener(object : LineListener {
                override fun onLine(line: String) {
                    logger.debug { line }
                }
            })
        }

        fork(false)
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        if (current >= forkEvery) {
            fork(true)
            current = 0
        }

        val fileManager = FileManager(configuration.outputDir)
        val listener = CompositeTestRunListener(
            listOf(
                DebugTestRunListener(this),
                ProgressTestRunListener(this, devicePoolId, progressReporter),
                TestRunResultsListener(testBatch, this, deferred, timer, emptyList()),
                LogListener(this, devicePoolId, testBatch.id, LogWriter(fileManager))
            )
        )

        client.execute(testBatch.tests, listener)
        current += testBatch.tests.size
    }

    private fun fork(clean: Boolean) {
        if (clean) {
            dispose()
        }

        //TODO: allow specifying java executable
        process = ProcessBuilder("java", "@${argsFile.absolutePath}")
            .apply {
                environment()["PORT"] = controlPort.toString()
            }.start()

        inheritIO(process.inputStream)
        inheritIO(process.errorStream)

        val localChannel = ManagedChannelBuilder.forAddress("localhost", controlPort).apply {
            usePlaintext()
            executor(Dispatchers.IO.asExecutor())
        }.build()

        client = TestExecutorClient(localChannel)
    }

    private fun booterJar(): File {
        val tempFile = File.createTempFile("marathon", "booter.jar")
        javaClass.getResourceAsStream("/booter-all.jar").copyTo(tempFile.outputStream())
        return tempFile
    }

    override fun dispose() {
        client.close()
        process.destroy()
        process.waitFor()
    }

    private fun inheritIO(src: InputStream) {
        Thread {
            val sc = Scanner(src)
            while (sc.hasNextLine()) {
                logListeners.forEach { it.onLine(sc.nextLine()) }
            }
        }.start()
    }

    private val logListeners = ConcurrentLinkedQueue<LineListener>()

    fun addLogListener(logListener: LineListener) {
        logListeners.add(logListener)
    }

    fun removeLogListener(logListener: LineListener) {
        logListeners.remove(logListener)
    }
}

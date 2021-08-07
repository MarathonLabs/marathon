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
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.junit4.client.TestExecutorClient
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.executor.listener.CompositeTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.DebugTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.DeviceLogListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.JUnit4TestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.LineListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.LogListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.ProgressTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.TestRunResultsListener
import com.malinskiy.marathon.vendor.junit4.extensions.isIgnored
import com.malinskiy.marathon.vendor.junit4.install.Junit4AppInstaller
import com.malinskiy.marathon.vendor.junit4.model.TestIdentifier
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Scanner
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

class Junit4Device(
    configuration: Configuration,
    protected val timer: Timer,
    private val testBundleIdentifier: Junit4TestBundleIdentifier,
    private val controlPort: Int = 50051
) : Device {
    override val operatingSystem: OperatingSystem = OperatingSystem(System.getProperty("os.name") ?: "")
    override val serialNumber: String = UUID.randomUUID().toString()
    override val model: String = System.getProperty("java.version")
    override val manufacturer: String = System.getProperty("java.vendor")
    override val networkState: NetworkState = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> = emptySet()
    override val healthy: Boolean = true
    override val abi: String = System.getProperty("os.arch")
    private var forkEvery: Int = 1000
    private var current: Int = 0
    private var useArgfiles: Boolean = true
    private val fileManager = FileManager(configuration.outputDir)
    private val logWriter = LogWriter(fileManager)


    private lateinit var client: TestExecutorClient
    private lateinit var process: Process
    private lateinit var args: String
    private lateinit var argsFile: File
    private lateinit var javaBinary: Path
    private var deviceLogListener: DeviceLogListener? = null

    private val logger = MarathonLogging.logger {}

    override suspend fun prepare(configuration: Configuration) {
        val conf = configuration.vendorConfiguration as Junit4Configuration

        javaBinary = conf.javaHome?.let { Paths.get(it.absolutePath, "bin", "java") } ?: Paths.get("java")
        forkEvery = conf.forkEvery

        val installer = Junit4AppInstaller(conf)
        installer.install()

        val booterFile = booterJar()

        val classpath = "$booterFile"

        args = StringBuilder().apply {
            if (conf.debugBooter) {
                append("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044 ")
            }
            append(conf.javaOptions.joinToString(separator = "") { "$it " })
            append("-cp ")
            append(classpath)
            append(" com.malinskiy.marathon.vendor.junit4.booter.BooterKt")
        }.toString()

        useArgfiles = conf.useArgfiles
        if (useArgfiles) {
            argsFile = File("argsfile-${controlPort}").apply {
                deleteOnExit()
                delete()
                appendText(args)
            }
        }

        fork(false)
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        rawTestBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        if (configuration.debug && deviceLogListener == null) {
            val logListener = DeviceLogListener(this, devicePoolId, logWriter)
            deviceLogListener = logListener
            addLogListener(logListener)
        }

        if (current >= forkEvery) {
            fork(true)
            current = 0
        }

        val listener = CompositeTestRunListener(
            listOf(
                DebugTestRunListener(this, rawTestBatch.tests),
                ProgressTestRunListener(this, devicePoolId, progressReporter),
                TestRunResultsListener(rawTestBatch, this, deferred, timer, emptyList()),
                LogListener(this, devicePoolId, rawTestBatch.id, logWriter),
            )
        )

        val ignoredTests = rawTestBatch.tests.filter { test -> test.isIgnored() }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests, rawTestBatch.id)
        if (testBatch.tests.isEmpty()) {
            notifyIgnoredTest(ignoredTests, listener)
            listener.testRunEnded(0, emptyMap())
            return
        }

        val applicationClasspath = mutableListOf<File>()
        val testClasspath = mutableListOf<File>()
        testBatch.tests.forEach {
            val bundle = testBundleIdentifier.identify(it)
            bundle.applicationClasspath?.let { list -> applicationClasspath.addAll(list) }
            bundle.testClasspath?.let { list -> testClasspath.addAll(list) }
        }

        notifyIgnoredTest(ignoredTests, listener)
        client.execute(testBatch.tests, applicationClasspath, testClasspath, listener)
        current += testBatch.tests.size
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
            val scanner = Scanner(src)
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                logListeners.forEach { listener ->
                    listener.onLine(line)
                }
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

    private suspend fun notifyIgnoredTest(ignoredTests: List<Test>, listeners: JUnit4TestRunListener) {
        ignoredTests.forEach {
            val identifier = TestIdentifier("${it.pkg}.${it.clazz}", it.method)
            listeners.testStarted(identifier)
            listeners.testIgnored(identifier)
            listeners.testEnded(identifier, hashMapOf())
        }
    }
}

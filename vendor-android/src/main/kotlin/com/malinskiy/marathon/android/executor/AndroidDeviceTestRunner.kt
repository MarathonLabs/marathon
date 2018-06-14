package com.malinskiy.marathon.android.executor

import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.AnalyticsListener
import com.malinskiy.marathon.android.executor.listeners.RetryListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.QueueMessage
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.channels.SendChannel
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

class AndroidDeviceTestRunner(private val device: AndroidDevice,
                              private val analytics: Analytics) {

    private val logger = KotlinLogging.logger("AndroidDeviceTestRunner")

    fun execute(configuration: Configuration,
                devicePoolId: DevicePoolId,
                testBatch: TestBatch,
                queueChannel: SendChannel<QueueMessage.FromDevice>) {
        val info = ApkParser().parseInstrumentationInfo(configuration.testApplicationOutput)
        val runner = RemoteAndroidTestRunner(info.instrumentationPackage, info.testRunnerClass, device.ddmsDevice)
        runner.setRunName("TestRunName")
        runner.setMaxTimeToOutputResponse(configuration.testOutputTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)

        val tests = testBatch.tests.map {
            "${it.pkg}.${it.clazz}#${it.method}"
        }.toTypedArray()

        logger.debug { "tests = ${tests.toList()}" }

        runner.setClassNames(tests)
        val fileManager = FileManager(configuration.outputDir)
        runner.run(CompositeTestRunListener(listOf(
                RetryListener(testBatch, device, queueChannel),
                DebugTestRunListener(device.ddmsDevice),
                AnalyticsListener(device, devicePoolId, analytics),
                LogCatListener(device, devicePoolId, LogWriter(fileManager)))
        ))
    }
}

package com.malinskiy.marathon.android.executor

import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.malinskiy.marathon.analytics.Tracker
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.android.executor.listeners.LogCatListener
import com.malinskiy.marathon.android.executor.listeners.XmlListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

class AndroidDeviceTestRunner(private val device: AndroidDevice,
                              private val tracker: Tracker) {

    private val logger = KotlinLogging.logger("AndroidDeviceTestRunner")

    fun execute(configuration: Configuration,
                devicePoolId: DevicePoolId,
                testBatch: TestBatch) {
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
                DebugTestRunListener(device.ddmsDevice),
                XmlListener(device, devicePoolId, tracker),
                LogCatListener(device, devicePoolId, LogWriter(fileManager)))
        ))
    }
}

package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.executor.listeners.CompositeTestRunListener
import com.malinskiy.marathon.android.executor.listeners.DebugTestRunListener
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.TestBatch
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

class AndroidDeviceTestRunner(private val device: IDevice) {

    private val logger = KotlinLogging.logger("AndroidDeviceTestRunner")

    fun execute(configuration: Configuration, testBatch: TestBatch) {
        val info = ApkParser().parseInstrumentationInfo(configuration.testApplicationOutput)
        val runner = RemoteAndroidTestRunner(info.instrumentationPackage, info.testRunnerClass, device)
        runner.setRunName("TestRunName")
        runner.setMaxTimeToOutputResponse(20, TimeUnit.SECONDS)

        val tests = testBatch.tests.map {
            "${it.pkg}.${it.clazz}#${it.method}"
        }.toTypedArray()

        logger.debug { "tests = ${tests.toList()}" }

        runner.setClassNames(tests)
        runner.run(CompositeTestRunListener(listOf(DebugTestRunListener(device))))
    }
}
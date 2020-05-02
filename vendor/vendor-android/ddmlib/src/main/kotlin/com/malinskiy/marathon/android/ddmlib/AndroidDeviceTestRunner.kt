package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import java.io.IOException
import java.util.concurrent.TimeUnit

const val JUNIT_IGNORE_META_PROPERTY_NAME = "org.junit.Ignore"
const val ERROR_STUCK = "Test got stuck. You can increase the timeout in settings if it's too strict"

class AndroidDeviceTestRunner(private val device: DdmlibAndroidDevice) {
    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    fun execute(
        configuration: Configuration,
        rawTestBatch: TestBatch,
        listener: ITestRunListener
    ) {

        val ignoredTests = rawTestBatch.tests.filter { test ->
            test.metaProperties.any { it.name == JUNIT_IGNORE_META_PROPERTY_NAME }
        }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests)

        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val info = ApkParser().parseInstrumentationInfo(androidConfiguration.testApplicationOutput)
        val runner = prepareTestRunner(configuration, androidConfiguration, info, testBatch)

        try {
            notifyIgnoredTest(ignoredTests, listener)
            if (testBatch.tests.isNotEmpty()) {
                clearData(androidConfiguration, info)
                runner.run(listener)
            } else {
                listener.testRunEnded(0, emptyMap())
            }
        } catch (e: ShellCommandUnresponsiveException) {
            logger.warn(ERROR_STUCK)
            listener.testRunFailed(ERROR_STUCK)
        } catch (e: TimeoutException) {
            logger.warn(ERROR_STUCK)
            listener.testRunFailed(ERROR_STUCK)
        } catch (e: AdbCommandRejectedException) {
            val errorMessage = "adb error while running tests ${testBatch.tests.map { it.toTestName() }}"
            logger.error(e) { errorMessage }
            listener.testRunFailed(errorMessage)
            if (e.isDeviceOffline) {
                throw DeviceLostException(e)
            }
        } catch (e: IOException) {
            val errorMessage = "adb error while running tests ${testBatch.tests.map { it.toTestName() }}"
            logger.error(e) { errorMessage }
            listener.testRunFailed(errorMessage)
        } finally {

        }
    }

    private fun notifyIgnoredTest(ignoredTests: List<Test>, listeners: ITestRunListener) {
        ignoredTests.forEach {
            val identifier = it.toTestIdentifier()
            listeners.testStarted(identifier)
            listeners.testIgnored(identifier)
            listeners.testEnded(identifier, hashMapOf())
        }
    }

    private fun clearData(androidConfiguration: AndroidConfiguration, info: InstrumentationInfo) {
        if (androidConfiguration.applicationPmClear) {
            device.ddmsDevice.safeClearPackage(info.applicationPackage)?.also {
                logger.debug { "Package ${info.applicationPackage} cleared: $it" }
            }
        }
        if (androidConfiguration.testApplicationPmClear) {
            device.ddmsDevice.safeClearPackage(info.instrumentationPackage)?.also {
                logger.debug { "Package ${info.applicationPackage} cleared: $it" }
            }
        }
    }

    private fun prepareTestRunner(
        configuration: Configuration,
        androidConfiguration: AndroidConfiguration,
        info: InstrumentationInfo,
        testBatch: TestBatch
    ): RemoteAndroidTestRunner {

        val runner = RemoteAndroidTestRunner(info.instrumentationPackage, info.testRunnerClass, device.ddmsDevice)

        val tests = testBatch.tests.map {
            "${it.pkg}.${it.clazz}#${it.method}"
        }.toTypedArray()

        logger.debug { "tests = ${tests.toList()}" }

        runner.setRunName("TestRunName")
        runner.setMaxTimeToOutputResponse(configuration.testOutputTimeoutMillis * testBatch.tests.size, TimeUnit.MILLISECONDS)
        runner.setClassNames(tests)

        androidConfiguration.instrumentationArgs.forEach { (key, value) ->
            runner.addInstrumentationArg(key, value)
        }

        if (androidConfiguration.noWindowAnimations) {
            runner.runOptions += "--no_window_animation"
        }

        return runner
    }
}

internal fun Test.toTestIdentifier(): TestIdentifier = TestIdentifier("$pkg.$clazz", method)

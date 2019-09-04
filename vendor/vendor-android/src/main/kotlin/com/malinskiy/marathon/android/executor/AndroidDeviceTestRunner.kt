package com.malinskiy.marathon.android.executor

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.safeClearPackage
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import java.io.IOException
import java.util.concurrent.TimeUnit

val JUNIT_IGNORE_META_PROPERY = MetaProperty("org.junit.Ignore")

class AndroidDeviceTestRunner(private val device: AndroidDevice) {

    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    fun execute(
        configuration: Configuration,
        rawTestBatch: TestBatch,
        listener: ITestRunListener
    ) {

        val ignoredTests = rawTestBatch.tests.filter { it.metaProperties.contains(JUNIT_IGNORE_META_PROPERY) }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests)

        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val info = ApkParser().parseInstrumentationInfo(androidConfiguration.testApplicationOutput)
        val runner = prepareTestRunner(configuration, androidConfiguration, info, testBatch)


        try {
            clearData(androidConfiguration, info)
            notifyIgnoredTest(ignoredTests, listener)
            runner.run(listener)
        } catch (e: ShellCommandUnresponsiveException) {
            logger.warn("Test got stuck. You can increase the timeout in settings if it's too strict")
            throw TestBatchExecutionException(e)
        } catch (e: TimeoutException) {
            logger.warn("Test got stuck. You can increase the timeout in settings if it's too strict")
            throw TestBatchExecutionException(e)
        } catch (e: AdbCommandRejectedException) {
            logger.error(e) { "adb error while running tests ${testBatch.tests.map { it.toTestName() }}" }
            if (e.isDeviceOffline) {
                throw DeviceLostException(e)
            } else {
                throw TestBatchExecutionException(e)
            }
        } catch (e: IOException) {
            logger.error(e) { "Error while running tests ${testBatch.tests.map { it.toTestName() }}" }
            throw TestBatchExecutionException(e)
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

        androidConfiguration.instrumentationArgs.forEach { key, value ->
            runner.addInstrumentationArg(key, value)
        }

        return runner
    }
}

internal fun Test.toTestIdentifier(): TestIdentifier = TestIdentifier("$pkg.$clazz", method)
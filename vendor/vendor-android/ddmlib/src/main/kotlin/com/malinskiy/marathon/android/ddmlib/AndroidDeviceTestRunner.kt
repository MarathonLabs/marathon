package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.ITestRunListener
import com.android.ddmlib.testrunner.RemoteAndroidTestRunner
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.android.extension.isIgnored
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.concurrent.TimeUnit

const val ERROR_STUCK = "Test got stuck. You can increase the timeout in settings if it's too strict"

class AndroidDeviceTestRunner(private val device: DdmlibAndroidDevice, private val bundleIdentifier: AndroidTestBundleIdentifier) {
    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    suspend fun execute(
        configuration: Configuration,
        rawTestBatch: TestBatch,
        listener: AndroidTestRunListener
    ) {

        val ignoredTests = rawTestBatch.tests.filter { test -> test.isIgnored() }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests)
        val listenerAdapter = listener.toDdmlibTestListener()
        if (testBatch.tests.isEmpty()) {
            listener.beforeTestRun()
            notifyIgnoredTest(ignoredTests, listenerAdapter)
            listener.testRunEnded(0, emptyMap())
            listener.afterTestRun()
            return
        }

        val androidConfiguration = configuration.vendorConfiguration as VendorConfiguration.AndroidConfiguration
        val infoToTestMap: Map<InstrumentationInfo, Test> = testBatch.tests.associateBy {
            bundleIdentifier.identify(it).instrumentationInfo
        }

        infoToTestMap.keys.forEach { info ->
            val runner = prepareTestRunner(configuration, androidConfiguration, info, testBatch)

            try {
                notifyIgnoredTest(ignoredTests, listenerAdapter)
                if (testBatch.tests.isNotEmpty()) {
                    clearData(androidConfiguration, info)
                    listener.beforeTestRun()
                    runner.run(listenerAdapter)
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
                listener.afterTestRun()
            }
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

    private suspend fun clearData(androidConfiguration: VendorConfiguration.AndroidConfiguration, info: InstrumentationInfo) {
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
        if (androidConfiguration.fileSyncConfiguration.pull.isNotEmpty() || androidConfiguration.fileSyncConfiguration.push.isNotEmpty()) {
            when {
                device.version.isGreaterOrEqualThan(30) -> {
                    val command = "appops set --uid ${info.applicationPackage} MANAGE_EXTERNAL_STORAGE allow"
                    device.criticalExecuteShellCommand(command).also {
                        logger.debug { "File pull requested. Granted MANAGE_EXTERNAL_STORAGE to ${info.applicationPackage}: ${it.trim()}" }
                    }
                }
                device.version.equals(29) -> {
                    //API 29 doesn't have MANAGE_EXTERNAL_STORAGE, force legacy storage
                    val command = "appops set --uid ${info.applicationPackage} LEGACY_STORAGE allow"
                    device.criticalExecuteShellCommand(command).also {
                        logger.debug { "File pull requested. Granted LEGACY_STORAGE to ${info.applicationPackage}: ${it.trim()}" }
                    }
                }
            }
        }
    }

    private fun prepareTestRunner(
        configuration: Configuration,
        androidConfiguration: VendorConfiguration.AndroidConfiguration,
        info: InstrumentationInfo,
        testBatch: TestBatch
    ): RemoteAndroidTestRunner {

        val runner = RemoteAndroidTestRunner(info.instrumentationPackage, info.testRunnerClass, device.ddmsDevice)

        val tests = testBatch.tests.map {
            "${it.toClassName()}#${it.method}"
        }.toTypedArray()

        logger.debug { "tests = ${tests.toList()}" }

        runner.setRunName("TestRunName")
        runner.setMaxTimeToOutputResponse(configuration.testOutputTimeoutMillis, TimeUnit.MILLISECONDS)
        runner.setMaxTimeout(configuration.testBatchTimeoutMillis, TimeUnit.MILLISECONDS)
        runner.setClassNames(tests)

        androidConfiguration.instrumentationArgs.forEach { (key, value) ->
            runner.addInstrumentationArg(key, value)
        }

        return runner
    }

    private fun AndroidTestRunListener.toDdmlibTestListener(): ITestRunListener {
        return object : ITestRunListener {
            override fun testRunStarted(runName: String?, testCount: Int) {
                runBlocking {
                    this@toDdmlibTestListener.testRunStarted(runName ?: "", testCount)
                }
            }

            override fun testStarted(test: TestIdentifier) {
                runBlocking {
                    this@toDdmlibTestListener.testStarted(test.toMarathonTestIdentifier())
                }
            }

            override fun testAssumptionFailure(test: TestIdentifier, trace: String?) {
                runBlocking {
                    this@toDdmlibTestListener.testAssumptionFailure(test.toMarathonTestIdentifier(), trace ?: "")
                }
            }

            override fun testRunStopped(elapsedTime: Long) {
                runBlocking {
                    this@toDdmlibTestListener.testRunStopped(elapsedTime)
                }
            }

            override fun testFailed(test: TestIdentifier, trace: String?) {
                runBlocking {
                    this@toDdmlibTestListener.testFailed(test.toMarathonTestIdentifier(), trace ?: "")
                }
            }

            override fun testEnded(test: TestIdentifier, testMetrics: MutableMap<String, String>?) {
                runBlocking {
                    this@toDdmlibTestListener.testEnded(test.toMarathonTestIdentifier(), testMetrics ?: emptyMap())
                }
            }

            override fun testIgnored(test: TestIdentifier) {
                runBlocking {
                    this@toDdmlibTestListener.testIgnored(test.toMarathonTestIdentifier())
                }
            }

            override fun testRunFailed(errorMessage: String?) {
                runBlocking {
                    this@toDdmlibTestListener.testRunFailed(errorMessage ?: "")
                }
            }

            override fun testRunEnded(elapsedTime: Long, runMetrics: MutableMap<String, String>?) {
                runBlocking {
                    this@toDdmlibTestListener.testRunEnded(elapsedTime, runMetrics ?: emptyMap())
                }
            }
        }
    }

    private fun TestIdentifier.toMarathonTestIdentifier() =
        com.malinskiy.marathon.android.model.TestIdentifier(this.className, this.testName)
}

internal fun Test.toTestIdentifier(): TestIdentifier = TestIdentifier(toClassName(), method)

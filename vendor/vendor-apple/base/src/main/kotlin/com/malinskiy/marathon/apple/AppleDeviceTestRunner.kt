package com.malinskiy.marathon.apple

import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.apple.model.AppleTestBundle
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.apple.test.TestFailed
import com.malinskiy.marathon.apple.test.TestIgnored
import com.malinskiy.marathon.apple.test.TestPassed
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.apple.test.TestRunEnded
import com.malinskiy.marathon.apple.test.TestRunFailed
import com.malinskiy.marathon.apple.test.TestRunStartedEvent
import com.malinskiy.marathon.apple.test.TestStarted
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ReceiveChannel

class AppleDeviceTestRunner(private val device: AppleDevice, private val bundleIdentifier: AppleTestBundleIdentifier) {
    private val logger = MarathonLogging.logger {}

    suspend fun execute(
        configuration: Configuration,
        testBundle: AppleTestBundle,
        rawTestBatch: TestBatch,
        listener: AppleTestRunListener,
    ) {
        val remoteXcresultPath = device.remoteFileManager.remoteXcresultFile(rawTestBatch)
        val remoteXctestrunFile = device.remoteFileManager.remoteXctestrunFile()
        val remoteDir = device.remoteFileManager.parentOf(remoteXctestrunFile)

        logger.debug("Remote xctestrun = $remoteXctestrunFile")
        logger.debug("Tests = ${rawTestBatch.tests.toList()}")

        val runnerRequest = TestRequest(
            workdir = remoteDir,
            remoteXctestrun = remoteXctestrunFile,
            tests = rawTestBatch.tests,
            xcresult = remoteXcresultPath,
            coverage = configuration.isCodeCoverageEnabled,
            testTargetName = testBundle.testBundleId,
        )
        var channel: ReceiveChannel<List<TestEvent>>? = null
        try {
            listener.beforeTestRun()

            val localChannel = device.executeTestRequest(runnerRequest)
            channel = localChannel
            for (events in localChannel) {
                processEvents(events, listener)
            }

            logger.debug { "Execution finished" }
        } catch (e: CancellationException) {
            val errorMessage = "Tests ${rawTestBatch.tests.map { it.toTestName() }} got stuck. " +
                "You can increase the timeout in settings if it's too strict"
            logger.error(e) { errorMessage }
            listener.testRunFailed(errorMessage)
        } finally {
            listener.afterTestRun()
            channel?.cancel()
        }
    }

    private suspend fun processEvents(events: List<TestEvent>, listener: AppleTestRunListener) {
        for (event in events) {
            when (event) {
                is TestRunStartedEvent -> listener.testRunStarted()
                is TestStarted -> listener.testStarted(event.id)
                is TestPassed -> listener.testPassed(event.id, event.startTime, event.endTime)
                is TestFailed -> listener.testFailed(event.id, event.startTime, event.endTime, event.trace)
                is TestIgnored -> listener.testIgnored(event.id, event.startTime, event.endTime)
                is TestRunFailed -> listener.testRunFailed(event.message, event.reason)
                is TestRunEnded -> listener.testRunEnded()
            }
        }
    }
}

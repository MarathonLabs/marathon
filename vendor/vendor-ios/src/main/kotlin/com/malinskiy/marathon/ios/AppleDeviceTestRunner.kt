package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.ios.executor.listener.AppleTestRunListener
import com.malinskiy.marathon.ios.test.TestEnded
import com.malinskiy.marathon.ios.test.TestFailed
import com.malinskiy.marathon.ios.test.TestRequest
import com.malinskiy.marathon.ios.test.TestRunEnded
import com.malinskiy.marathon.ios.test.TestRunStartedEvent
import com.malinskiy.marathon.ios.test.TestStarted
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch

class AppleDeviceTestRunner(private val device: AppleDevice) {
    private val logger = MarathonLogging.logger {}

    suspend fun execute(
        configuration: Configuration,
        vendorConfiguration: VendorConfiguration.IOSConfiguration,
        rawTestBatch: TestBatch,
        listener: AppleTestRunListener,
    ) {
        if (vendorConfiguration.alwaysEraseSimulators) {
            device.shutdown()
            device.erase()
        }

        val remoteXcresultPath = RemoteFileManager.remoteXcresultFile(device, rawTestBatch)
        val remoteXctestrunFile = RemoteFileManager.remoteXctestrunFile(device)
        val remoteDir = remoteXctestrunFile.parent

        logger.debug("Remote xctestrun = $remoteXctestrunFile")
        logger.debug("Tests = ${rawTestBatch.tests.toList()}")
        
        listener.beforeTestRun()
        
        val channel = device.executeTestRequest(
            TestRequest(
                workdir = remoteDir,
                xctestrun = remoteXctestrunFile.path,
                tests = rawTestBatch.tests,
                xcresult = remoteXcresultPath.path,
            )
        )
        
        for (events in channel) {
            for(event in events) {
                when(event) {
                    is TestRunStartedEvent -> listener.testRunStarted()
                    is TestStarted -> listener.testStarted(event.id)
                    is TestEnded -> listener.testPassed(event.id, event.startTime, event.endTime)
                    is TestFailed -> listener.testFailed(event.id, event.startTime, event.endTime)
                    is TestRunEnded -> listener.testRunEnded()
                }
            }
        }
        
        listener.afterTestRun()
    }
}

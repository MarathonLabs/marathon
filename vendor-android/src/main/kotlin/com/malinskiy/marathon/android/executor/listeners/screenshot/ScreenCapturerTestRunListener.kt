package com.malinskiy.marathon.android.executor.listeners.screenshot

import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.toSimpleSafeTestName
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext

class ScreenCapturerTestRunListener(private val fileManager: FileManager,
                                    private val pool: DevicePoolId,
                                    private val device: AndroidDevice) : NoOpTestRunListener() {

    private var screenCapturerJob: Job? = null
    private val logger = MarathonLogging.logger(ScreenCapturerTestRunListener::class.java.simpleName)
    private val threadPoolDispatcher = newFixedThreadPoolContext(1, "ScreenCapturer - ${device.serialNumber}")

    override fun testStarted(test: TestIdentifier) {
        super.testStarted(test)
        logger.debug { "Starting recording for ${test.toTest().toSimpleSafeTestName()}" }
        screenCapturerJob = async(context = threadPoolDispatcher) {
            ScreenCapturer(device, pool, fileManager, test).start()
        }
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        super.testEnded(test, testMetrics)
        logger.debug { "Finished recording for ${test.toTest().toSimpleSafeTestName()}" }
        screenCapturerJob?.cancel()
    }
}
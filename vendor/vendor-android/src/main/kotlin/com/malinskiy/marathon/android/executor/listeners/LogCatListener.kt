package com.malinskiy.marathon.android.executor.listeners

import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.report.steps.StepsJsonListener
import com.malinskiy.marathon.report.steps.StepsJsonProvider
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class LogCatListener(
    private val device: AndroidDevice,
    private val devicePoolId: DevicePoolId,
    private val logWriter: LogWriter
) : NoOpTestRunListener(), AttachmentProvider, StepsJsonProvider {

    companion object {
        private const val ALLURE_STEPS_JSON_PREFIX = "#AllureStepsInfoJson#:"
        private const val TAG_KASPRESSO = "KASPRESSO"
        private const val KASPRESSO_AFTER_TEST_SECTION = "AFTER TEST SECTION"
    }

    private val logger = MarathonLogging.logger {}
    private val attachmentListeners = mutableListOf<AttachmentListener>()
    private val stepsJsonListeners = mutableListOf<StepsJsonListener>()

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    override fun registerListener(listener: StepsJsonListener) {
        stepsJsonListeners.add(listener)
    }

    private val receiver = LogCatReceiverTask(device.ddmsDevice)

    private val ref = AtomicReference<MutableList<LogCatMessage>>(mutableListOf())
    private var thread: Thread? = null

    private val listener: (MutableList<LogCatMessage>) -> Unit = {
        ref.get().addAll(it)
    }

    override fun testRunStarted(runName: String, testCount: Int) {
        receiver.addLogCatListener(listener)
        thread = thread(name = "LogCatLogger-$runName-${device.serialNumber}") {
            receiver.run()
        }
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        val messages = ref.getAndSet(mutableListOf())

        val stepsJson = StringBuilder("")
        val needSearchStepsJsonMessages = stepsJsonListeners.isNotEmpty()
        val logMessages = messages.map {
            if (needSearchStepsJsonMessages) {
                when {
                    it.message.startsWith(ALLURE_STEPS_JSON_PREFIX) -> {
                        stepsJson.append(it.message.substring(ALLURE_STEPS_JSON_PREFIX.length))
                    }

                    it.tag == TAG_KASPRESSO && it.message == KASPRESSO_AFTER_TEST_SECTION -> {
                        logger.info { "Find logs from another test! Reset stepsJson." }
                        stepsJson.setLength(0)
                    }
                }
            }
            "${it.timestamp} ${it.pid}-${it.tid}/${it.appName} ${it.logLevel.priorityLetter}/${it.tag}: ${it.message}"
        }
        val testIdentifier = test.toTest()
        val file = logWriter.saveLogs(testIdentifier, devicePoolId, device.toDeviceInfo(), logMessages)

        attachmentListeners.forEach { it.onAttachment(testIdentifier, Attachment(file, AttachmentType.LOG)) }
        if (stepsJson.isNotBlank()) {
            stepsJsonListeners.forEach { it.onStepsJsonAttached(testIdentifier, stepsJson.toString()) }
        }
    }

    override fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        receiver.stop()
        receiver.removeLogCatListener(listener)
        thread?.interrupt()
    }
}

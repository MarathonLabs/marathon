package com.malinskiy.marathon.android.executor.listeners.tracing

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.vendor.android.TracingConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.TransferException
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis


class PerfettoRunListener(
    private val fileManager: FileManager,
    private val pool: DevicePoolId,
    private val testBatch: TestBatch,
    private val device: AndroidDevice,
    private val tracingConfiguration: TracingConfiguration,
    private val testBundleIdentifier: AndroidTestBundleIdentifier,
    coroutineScope: CoroutineScope
) : NoOpTestRunListener(), AttachmentProvider, CoroutineScope by coroutineScope {
    private val logger = MarathonLogging.logger("PerfettoRunListener")

    private var job: Job? = null
    private val attachmentListeners = mutableListOf<AttachmentListener>()
    private var targetPid: Int? = null
    private var tracingConfig: String? = null
    private var renderedConfig: String? = null

    override fun registerListener(listener: AttachmentListener) {
        attachmentListeners.add(listener)
    }

    override suspend fun beforeTestRun(info: InstrumentationInfo?) {
        super.beforeTestRun(info)
        tracingConfig = tracingConfiguration.pbtxt?.readText()
    }

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        super.testRunStarted(runName, testCount)

        // Assumption is that we can never execute test batches with multiple packages
        val testBundle = testBundleIdentifier.identify(testBatch.tests.first())
        val result = device.criticalExecuteShellCommand("pidof ${testBundle.instrumentationInfo.applicationPackage}")
        //TODO: check app is profileable and debuggable for java heap profiling

        targetPid = result.output.trim().toIntOrNull()
        logger.debug { "Target pid: $targetPid" }

        if (tracingConfig != null) {
            val lookup = StringSubstitutor(
                StringLookupFactory.INSTANCE.mapStringLookup(
                    mapOf(
                        "TRACING_TARGET_PID" to targetPid,
                        "TRACING_TARGET_PACKAGE" to testBundle.instrumentationInfo.applicationPackage
                    )
                )
            )
            renderedConfig = lookup.replace(tracingConfig)

            logger.debug { "Rendered config:\n$renderedConfig" }
        }
    }

    override suspend fun testStarted(test: TestIdentifier) {
        super.testStarted(test)

        val remoteFile = device.fileManager.remoteTracingForTest(test.toTest(), testBatch.id)

        job = async(coroutineContext + CoroutineName("perfetto ${device.serialNumber}")) {
            supervisorScope {
                try {
                    val result =
                        device.executeShellCommand("echo '${renderedConfig}' | perfetto --txt -c - -o $remoteFile")
                    logger.debug { "perfetto process finished: $result" }
                } catch (e: CancellationException) {
                    logger.warn(e) { "perfetto start was interrupted" }
                    throw e
                } catch (e: Exception) {
                    logger.error("Something went wrong while recording perfetto trace", e)
                    throw e
                }
            }
        }
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        super.testEnded(test, testMetrics)
        pullTrace(test)
        logger.debug { "Finished processing" }
    }

    private suspend fun pullTrace(test: TestIdentifier) {
        try {
            stop()

            val test = test.toTest()
            val remoteFile = device.fileManager.remoteTracingForTest(test, testBatch.id)
            val localFile = fileManager.createFile(FileType.TRACING, pool, device.toDeviceInfo(), test, testBatch.id)
            val millis = measureTimeMillis {
                device.safePullFile(remoteFile, localFile.toString())
            }
            logger.trace { "Pulling finished in ${millis}ms $remoteFile " }

            attachmentListeners.forEach {
                it.onAttachment(
                    test,
                    Attachment(localFile, AttachmentType.TRACING, name = Attachment.Name.TRACING)
                )
            }

            /**
             * Read-only partition hence -f is required
             */
            device.safeExecuteShellCommand("rm -f $remoteFile")
        } catch (e: TransferException) {
            logger.warn { "Can't pull tracing file" }
        }
    }

    private suspend fun stop() {
        logger.debug { "Stopping perfetto" }
        val stop = measureTimeMillis {
            device.safeExecuteShellCommand("killall perfetto")
        }
        logger.debug { "Stopped perfetto: ${stop}ms" }
        val join = measureTimeMillis {
            job?.join()
        }
        logger.debug { "Joining perfetto: ${join}ms" }
    }
}

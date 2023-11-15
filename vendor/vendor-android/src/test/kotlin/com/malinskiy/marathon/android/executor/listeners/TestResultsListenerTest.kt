package com.malinskiy.marathon.android.executor.listeners

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.attachment.AttachmentProvider
import com.malinskiy.marathon.test.TestBatch
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

@AdbTest
class TestResultsListenerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testDefault() {
        val id = UUID.randomUUID().toString()
        val batch = TestBatch(listOf(test1.toTest(), test2.toTest(), test3.toTest(), test4.toTest(), test5.toTest()), id)

        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())

        val deferred = CompletableDeferred<TestBatchResults>()

        val attachmentProvider = mock<AttachmentProvider>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), listOf(attachmentProvider))

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val attachment = Attachment(File(temp, "log"), AttachmentType.LOG)

            listener.apply {
                beforeTestRun()

                testFailed(test0, "trace")

                testRunStarted("testing", 3)
                testStarted(test1)
                testEnded(test1, mapOf("metric" to "value"))

                testStarted(test2)
                testFailed(test2, "trace")
                onAttachment(test2.toTest(), attachment)

                testStarted(test3)
                testIgnored(test3)

                testStarted(test4)
                testAssumptionFailure(test4, "trace")

                testStarted(test5)

                testRunEnded(1234, mapOf("metric1" to "value1"))
                afterTestRun()
            }

            verify(attachmentProvider).registerListener(any())
            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).containsOnly(
                TestResult(test1.toTest(), device.toDeviceInfo(), batch.id, TestStatus.PASSED, 0, 0),
                TestResult(test3.toTest(), device.toDeviceInfo(), batch.id, TestStatus.IGNORED, 0, 0),
                TestResult(test4.toTest(), device.toDeviceInfo(), batch.id, TestStatus.ASSUMPTION_FAILURE, 0, 0, "trace"),
            )
            assertThat(result.failed).containsOnly(
                TestResult(test0.toTest(), device.toDeviceInfo(), batch.id, TestStatus.FAILURE, 0, 0, "trace"),
                TestResult(test2.toTest(), device.toDeviceInfo(), batch.id, TestStatus.FAILURE, 0, 0, "trace", listOf(attachment)),
            )
            assertThat(result.uncompleted).containsOnly(
                TestResult(test5.toTest(), device.toDeviceInfo(), batch.id, TestStatus.INCOMPLETE, 0, 0),
            )
        }
    }

    @Test
    fun testSingleIncomplete() {
        val id = UUID.randomUUID().toString()
        val batch = TestBatch(listOf(test1.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            listener.apply {
                beforeTestRun()
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).isEmpty()
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).containsOnly(
                TestResult(test1.toTest(), device.toDeviceInfo(), batch.id, TestStatus.INCOMPLETE, 0, 0, "Test didn't complete. Either the test results are missing due to timeout or testing runtime crashed"),
            )
        }
    }

    @Test
    fun testParameterizedSuccess() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testEnded(id0, emptyMap())
                testStarted(id1)
                testEnded(id1, emptyMap())
                testStarted(id2)
                testEnded(id2, emptyMap())
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.PASSED, 0, 0),
            )
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).isEmpty()
        }
    }

    @Test
    fun testRunFailed() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testRunFailed("Problems")
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).isEmpty()
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.INCOMPLETE, 0, 0, "Problems")
            )
        }
    }

    @Test
    fun testRunStopped() {
        val batch = TestBatch(listOf(test1.toTest(), test2.toTest()))
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testStarted(test1)
                testEnded(test1, emptyMap())
                testRunFailed("Problems")
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).containsOnly(
                TestResult(test1.toTest(), device.toDeviceInfo(), batch.id, TestStatus.PASSED, 0, 0)
            )
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).containsOnly(
                TestResult(test2.toTest(), device.toDeviceInfo(), batch.id, TestStatus.INCOMPLETE, 0, 0, "Problems")
            )
        }
    }

    @Test
    fun testParameterizedFailure() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testEnded(id0, emptyMap())
                testStarted(id1)
                testEnded(id1, emptyMap())
                testStarted(id2)
                testFailed(id2, "trace")
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).isEmpty()
            assertThat(result.failed).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.FAILURE, 0, 0),
            )
            assertThat(result.uncompleted).isEmpty()
        }
    }

    @Test
    fun testParameterizedFailureAccumulation() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testFailed(id0, "trace")
                testStarted(id1)
                testFailed(id1, "trace")
                testStarted(id2)
                testFailed(id2, "trace")
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).isEmpty()
            assertThat(result.failed).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.FAILURE, 0, 0, "trace"),
            )
            assertThat(result.uncompleted).isEmpty()
        }
    }

    @Test
    fun testParameterizedIncompleteAccumulation() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testStarted(id0)
                testStarted(id1)
                testStarted(id2)
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).isEmpty()
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.INCOMPLETE, 0, 0),
            )
        }
    }

    @Test
    fun testParameterizedAssumptionFailureAccumulation() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val listener = TestResultsListener(batch, device, deferred, mock(), DevicePoolId("omni"), emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testStarted(id0)
                testAssumptionFailure(id0, "trace0")
                testStarted(id1)
                testAssumptionFailure(id1, "trace1")
                testStarted(id2)
                testAssumptionFailure(id2, "trace2")
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.ASSUMPTION_FAILURE, 0, 0, "trace0"),
            )
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).isEmpty()
        }
    }

    @Test
    fun testParameterizedIgnoredAccumulation() {
        val id = UUID.randomUUID().toString()
        val parameterizedTestId = TestIdentifier("com.example.Class", "method")
        val batch = TestBatch(listOf(parameterizedTestId.toTest()), id)
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val deferred = CompletableDeferred<TestBatchResults>()
        val poolId = DevicePoolId("omni")
        val listener = TestResultsListener(batch, device, deferred, mock(), poolId, emptyList())

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()
            val id0 = TestIdentifier("com.example.Class", "method[0]")
            val id1 = TestIdentifier("com.example.Class", "method[1]")
            val id2 = TestIdentifier("com.example.Class", "method[2]")

            listener.apply {
                beforeTestRun()
                testRunStarted("Testing", 3)
                testStarted(id0)
                testIgnored(id0)
                testStarted(id1)
                testIgnored(id1)
                testStarted(id2)
                testIgnored(id2)
                testRunEnded(1000, emptyMap())
                afterTestRun()
            }

            val result = deferred.await()
            assertThat(result.device).isEqualTo(device)
            assertThat(result.finished).containsOnly(
                TestResult(parameterizedTestId.toTest(), device.toDeviceInfo(), batch.id, TestStatus.IGNORED, 0, 0),
            )
            assertThat(result.failed).isEmpty()
            assertThat(result.uncompleted).isEmpty()
        }
    }

    companion object {
        val test0 = TestIdentifier("com.example.Class", "method0")
        val test1 = TestIdentifier("com.example.Class", "method1")
        val test2 = TestIdentifier("com.example.Class", "method2")
        val test3 = TestIdentifier("com.example.Class", "method3")
        val test4 = TestIdentifier("com.example.Class", "method4")
        val test5 = TestIdentifier("com.example.Class", "method5")
    }
}

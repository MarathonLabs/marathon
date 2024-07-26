package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.config.vendor.android.FileSyncConfiguration
import com.malinskiy.marathon.config.vendor.android.FileSyncEntry
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.listener.LogListener
import com.malinskiy.marathon.report.attachment.AttachmentListener
import com.malinskiy.marathon.report.logs.LogWriter
import com.malinskiy.marathon.test.TestBatch
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

@AdbTest
class LogListenerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @TempDir
    lateinit var temp: File

    @Test
    fun testDefault() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test0))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val deviceInfo = device.toDeviceInfo()
        val listener = LogListener(deviceInfo, device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            val logFile = File(temp, "log")
            whenever(logWriter.saveLogs(test0, poolId, batch.id, deviceInfo, listOf("1\n2\n"))).thenReturn(logFile)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0)
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0)
            listener.onLine("3")

            verify(attachmentListener, times(1)).onAttachment(test0, Attachment(logFile, AttachmentType.LOG, "log"))
        }
    }

    @Test
    fun testRunFailed() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test0))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val deviceInfo = device.toDeviceInfo()
        val listener = LogListener(deviceInfo, device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            val logFile = File(temp, "log")
            whenever(logWriter.saveLogs(test0, poolId, batch.id, deviceInfo, listOf("1\n2\n"))).thenReturn(logFile)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0)
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0)
            listener.onLine("3")

            verify(attachmentListener, times(1)).onAttachment(test0, Attachment(logFile, AttachmentType.LOG, "log"))
        }
    }

    @Test
    fun testBatchTestIsolation() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableSetOf(
                    FileSyncEntry("screenshots")
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test0, test1))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val deviceInfo = device.toDeviceInfo()
        val listener = LogListener(deviceInfo, device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            val logFile1 = File(temp, "log1")
            val logFile2 = File(temp, "log2")
            whenever(logWriter.saveLogs(test0, poolId, batch.id, deviceInfo, listOf("1\n2\n"))).thenReturn(logFile1)
            whenever(logWriter.saveLogs(test1, poolId, batch.id, deviceInfo, listOf("b\nc\n"))).thenReturn(logFile2)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0)
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0)
            listener.onLine("3")

            listener.onLine("a")
            listener.testStarted(test1)
            listener.onLine("b")
            listener.onLine("c")
            listener.testEnded(test1)
            listener.onLine("d")


            verify(attachmentListener, times(1)).onAttachment(test0, Attachment(logFile1, AttachmentType.LOG, "log"))
            verify(attachmentListener, times(1)).onAttachment(test1, Attachment(logFile2, AttachmentType.LOG, "log"))
        }
    }

    @Test
    fun testRunnerCrash() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test0))
        val logWriter = mock<LogWriter>()
        val deviceInfo = device.toDeviceInfo()
        val listener = LogListener(deviceInfo, device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            listener.beforeTestRun()
            listener.onLine("Important crash-related information")
            listener.afterTestRun()

            verify(logWriter, times(1)).saveLogs(poolId, batch.id, deviceInfo, listOf("Important crash-related information\n"))
        }
    }

    companion object {
        val test0 = MarathonTest("com.example", "Class", "method0", emptyList())
        val test1 = MarathonTest("com.example", "Class", "method1", emptyList())
    }
}

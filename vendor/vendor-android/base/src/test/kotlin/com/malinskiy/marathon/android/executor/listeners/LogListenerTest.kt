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
import com.malinskiy.marathon.android.model.TestIdentifier
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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

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
        val batch = TestBatch(listOf(test0.toTest()))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val listener = LogListener(device.toDeviceInfo(), device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            val logFile = File(temp, "log")
            whenever(logWriter.saveLogs(test0.toTest(), poolId, batch.id, device.toDeviceInfo(), listOf("1\n2\n"))).thenReturn(logFile)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0.toTest())
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0.toTest())
            listener.onLine("3")

            verify(attachmentListener, times(1)).onAttachment(test0.toTest(), Attachment(logFile, AttachmentType.LOG))
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
        val batch = TestBatch(listOf(test0.toTest()))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val listener = LogListener(device.toDeviceInfo(), device, poolId, batch.id, logWriter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            val logFile = File(temp, "log")
            whenever(logWriter.saveLogs(test0.toTest(), poolId, batch.id, device.toDeviceInfo(), listOf("1\n2\n"))).thenReturn(logFile)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0.toTest())
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0.toTest())
            listener.onLine("3")

            verify(attachmentListener, times(1)).onAttachment(test0.toTest(), Attachment(logFile, AttachmentType.LOG))
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
        val batch = TestBatch(listOf(test0.toTest(), test1.toTest()))
        val logWriter = mock<LogWriter>()
        val attachmentListener = mock<AttachmentListener>()
        val listener = LogListener(device.toDeviceInfo(), device, poolId, batch.id, logWriter)

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
            whenever(logWriter.saveLogs(test0.toTest(), poolId, batch.id, device.toDeviceInfo(), listOf("1\n2\n"))).thenReturn(logFile1)
            whenever(logWriter.saveLogs(test1.toTest(), poolId, batch.id, device.toDeviceInfo(), listOf("b\nc\n"))).thenReturn(logFile2)

            listener.registerListener(attachmentListener)
            listener.onLine("0")
            listener.testStarted(test0.toTest())
            listener.onLine("1")
            listener.onLine("2")
            listener.testEnded(test0.toTest())
            listener.onLine("3")

            listener.onLine("a")
            listener.testStarted(test1.toTest())
            listener.onLine("b")
            listener.onLine("c")
            listener.testEnded(test1.toTest())
            listener.onLine("d")


            verify(attachmentListener, times(1)).onAttachment(test0.toTest(), Attachment(logFile1, AttachmentType.LOG))
            verify(attachmentListener, times(1)).onAttachment(test1.toTest(), Attachment(logFile2, AttachmentType.LOG))
        }
    }

    @Test
    fun testRunnerCrash() {
        val configuration = TestConfigurationFactory.create()
        val device = TestDeviceFactory.create(client, configuration, mock())
        val poolId = DevicePoolId("testpool")
        val batch = TestBatch(listOf(test0.toTest()))
        val logWriter = mock<LogWriter>()
        val listener = LogListener(device.toDeviceInfo(), device, poolId, batch.id, logWriter)

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

            verify(logWriter, times(1)).saveLogs(poolId, batch.id, device.toDeviceInfo(), listOf("Important crash-related information\n"))
        }
    }

    companion object {
        val test0 = TestIdentifier("com.example.Class", "method0")
        val test1 = TestIdentifier("com.example.Class", "method1")
    }
}

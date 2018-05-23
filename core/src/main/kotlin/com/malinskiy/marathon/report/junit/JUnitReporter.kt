package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.xml.stream.XMLOutputFactory

class JUnitReporter(private val fileManager: FileManager) {
    fun testFinished(devicePoolId: DevicePoolId, device: Device, testResult: TestResult) {
        val test = testResult.test
        val duration = testResult.endTime - testResult.startTime

        val failures = if (testResult.status == TestStatus.FAILURE) 1 else 0
        val ignored = if (testResult.status == TestStatus.IGNORED) 1 else 0

        fun Long.toJUnitSeconds(): String = (TimeUnit.NANOSECONDS.toMillis(this) / 1000.0).toString()

        val file = fileManager.createFile(FileType.TEST, devicePoolId, device, testResult.test)
        file.createNewFile()

        val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(FileWriter(file))

        writer.document {
            element("testsuite") {
                attribute("name", "${test.clazz}.${test.method}")
                attribute("tests", "1")
                attribute("failures", "$failures")
                attribute("errors", "0")
                attribute("skipped", "$ignored")
                attribute("time", duration.toJUnitSeconds())
                attribute("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(testResult.endTime)))
                element("properties") {}
                element("testcase") {
                    attribute("classname", "${test.pkg}.${test.clazz}")
                    attribute("name", test.method)
                    attribute("time", duration.toJUnitSeconds())
                    when (testResult.status) {
                        TestStatus.IGNORED -> {
                            element("skipped") {
                                testResult.stacktrace?.takeIf { it.isNotEmpty() }?.let {
                                    writeCData(it)
                                }
                            }
                        }
                        TestStatus.FAILURE -> {
                            element("failure") {
                                writeCData(testResult.stacktrace!!)
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
        writer.flush()
        writer.close()
    }
}
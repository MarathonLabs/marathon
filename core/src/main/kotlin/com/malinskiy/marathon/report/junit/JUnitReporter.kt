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
import javax.xml.stream.XMLStreamWriter

class JUnitReporter(private val fileManager: FileManager) {
    fun testFinished(devicePoolId: DevicePoolId, device: Device, testResult: TestResult) {
        val file = fileManager.createFile(FileType.TEST, devicePoolId, device, testResult.test)
        file.createNewFile()

        val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(FileWriter(file))

        generateXml(writer, testResult)
        writer.flush()
        writer.close()
    }

    @Suppress("ComplexMethod")
    private fun generateXml(writer: XMLStreamWriter, testResult: TestResult) {
        @Suppress("MagicNumber")
        fun Long.toJUnitSeconds(): String = (TimeUnit.NANOSECONDS.toMillis(this) / 1000.0).toString()

        val test = testResult.test

        val failures = if (testResult.status == TestStatus.FAILURE) 1 else 0
        val ignored = if (testResult.status == TestStatus.IGNORED) 1 else 0

        val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(testResult.endTime))

        writer.document {
            element("testsuite") {
                attribute("name", "${test.clazz}.${test.method}")
                attribute("tests", "1")
                attribute("failures", "$failures")
                attribute("errors", "0")
                attribute("skipped", "$ignored")
                attribute("time", testResult.durationMillis().toJUnitSeconds())
                attribute("timestamp", formattedTimestamp)
                element("properties") {}
                element("testcase") {
                    attribute("classname", "${test.pkg}.${test.clazz}")
                    attribute("name", test.method)
                    attribute("time", testResult.durationMillis().toJUnitSeconds())
                    when (testResult.status) {
                        TestStatus.IGNORED -> {
                            element("skipped") {
                                testResult.stacktrace?.let {
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
    }
}

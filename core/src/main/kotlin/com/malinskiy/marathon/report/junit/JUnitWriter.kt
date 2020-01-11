package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import java.io.FileOutputStream
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

private const val JUNIT_REPORT = "Marathon_Junit_Report"

class JUnitWriter(private val fileManager: FileManager) {

    private val xmlWriterMap = hashMapOf<DevicePool, XMLStreamWriter>()

    fun prepareXMLReport(executionReport: ExecutionReport) {
        makeFile(executionReport)
        prepareReport(executionReport)
        finalize()
    }

    private fun makeFile(executionReport: ExecutionReport) {
        executionReport.testEvents.devicePools().forEach {
            val file = fileManager.createFile(FileType.TEST, it.devicePool, it.deviceInfo, JUNIT_REPORT)
            file.createNewFile()
            val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(file), "UTF-8")
            xmlWriterMap[it] = writer
        }
    }

    private fun prepareReport(executionReport: ExecutionReport) {
        val reportGenerator = JunitReportGenerator(executionReport.testEvents)
        reportGenerator.makeSuiteData()
        val junitReports = reportGenerator.junitReports
        junitReports.keys.forEach {
            generateXml(xmlWriterMap[it]!!, junitReports[it]!!)
        }
    }

    private fun finalize() {
        xmlWriterMap.values.forEach {
            it.flush()
            it.close()
        }
    }

    private fun generateXml(writer: XMLStreamWriter, junitReport: JUnitReport) {

        writer.document {
            element("testsuite") {
                attribute("name", junitReport.testSuiteData.name)
                attribute("tests", junitReport.testSuiteData.tests.toString())
                attribute("failures", junitReport.testSuiteData.failures.toString())
                attribute("errors", junitReport.testSuiteData.errors.toString())
                attribute("skipped", junitReport.testSuiteData.skipped.toString())
                attribute("time", junitReport.testSuiteData.time)
                attribute("timestamp", junitReport.testSuiteData.timeStamp)
                element("properties") {}

                junitReport.testCases.forEach {
                    element("testcase") {
                        attribute("classname", it.classname)
                        attribute("name", it.name)
                        attribute("time", it.time)
                        if (it.skipped?.isNotEmpty()!!) {
                            element("skipped", it.skipped)
                        }
                        if (it.failure?.isNotEmpty()!!) {
                            element("failure", it.failure)
                        }
                    }
                }
            }
        }
    }
}

data class DevicePool(
    val devicePool: DevicePoolId,
    val deviceInfo: DeviceInfo
)

package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.report.junit.model.JUnitReport
import com.malinskiy.marathon.report.junit.model.Pool
import org.testng.util.Strings
import java.io.FileOutputStream
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter


class JUnitWriter(private val fileManager: FileManager) {

    private  val reportName = "marathon_junit_report"

    private val xmlWriterMap = hashMapOf<Pool, XMLStreamWriter>()

    fun prepareXMLReport(executionReport: ExecutionReport) {
        makeFile(executionReport)
        prepareReport(executionReport)
        finalize()
    }

    private fun makeFile(executionReport: ExecutionReport) {
        executionReport.testEvents.map { Pool(it.poolId, it.device) }.distinct()
            .forEach {
            val file = fileManager.createFile(FileType.TEST, it.devicePoolId, it.deviceInfo, reportName)
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
            val xmlWriter = xmlWriterMap[it]
            val junitReport = junitReports[it]
            if (xmlWriter != null && junitReport != null)
                generateXml(xmlWriter, junitReport)
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
                        if (Strings.isNotNullAndNotEmpty(it.skipped)) element("skipped") { writeCData(it.skipped) }
                        if (Strings.isNotNullAndNotEmpty(it.failure)) element("failure") { writeCData(it.failure) }
                    }
                }
            }
        }
    }
}

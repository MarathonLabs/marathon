package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.io.FileType
import java.nio.file.Paths.get
import com.malinskiy.marathon.report.junit.model.JUnitReport
import com.malinskiy.marathon.report.junit.model.Pool
import java.io.File
import java.io.FileOutputStream
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

class JUnitWriter(private val outputDirectory: File) {

    private  val reportName = "marathon_junit_report"

    private val xmlWriterMap = hashMapOf<Pool, XMLStreamWriter>()

    fun prepareXMLReport(testEvents: List<TestEvent>) {
        makeFile(testEvents)
        prepareReport(testEvents)
        finalize()
    }

    private fun makeFile(testEvents: List<TestEvent>) {
        testEvents.map { Pool(it.poolId, it.device) }.distinct()
            .forEach {
                val reportDirectory = get(
                    outputDirectory.absolutePath,
                    FileType.TEST.dir,
                    it.devicePoolId.name,
                    it.deviceInfo.serialNumber)
                    .toFile()
                reportDirectory.mkdirs()
                val file = File(reportDirectory.absolutePath,reportName+"."+FileType.TEST.suffix)
                file.createNewFile()
                val writer = XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(file), "UTF-8")
                xmlWriterMap[it] = writer
        }
    }

    private fun prepareReport(testEvents: List<TestEvent>) {
        val reportGenerator = JunitReportGenerator(testEvents)
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
                        if(it.skipped.isError) element("skipped") {
                            if(it.skipped.stackTrace!=null){
                                writeCData(it.skipped.stackTrace)
                            }
                        }
                        if(it.failure.isError) element("failure") {
                            if (it.failure.stackTrace != null) {
                                writeCData(it.failure.stackTrace)
                            }
                        }
                    }
                }
            }
        }
    }
}

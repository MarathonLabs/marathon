package com.malinskiy.marathon.report.junit.serialize

import com.malinskiy.marathon.report.junit.attribute
import com.malinskiy.marathon.report.junit.document
import com.malinskiy.marathon.report.junit.element
import com.malinskiy.marathon.report.junit.model.JUnitReport
import com.malinskiy.marathon.report.junit.model.Rerun
import com.malinskiy.marathon.report.junit.model.TestSuite
import com.malinskiy.marathon.report.junit.writeCDataSafely
import java.io.File
import java.io.FileOutputStream
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

class JUnitReportSerializer {
    fun serialize(junitReport: JUnitReport, file: File) {
        XMLOutputFactory.newFactory().createXMLStreamWriter(FileOutputStream(file), "UTF-8").apply {
            document {
                when {
                    junitReport.testsuites.size == 1 -> writeTestSuite(junitReport.testsuites[0])
                    junitReport.testsuites.size > 1 -> {
                        element("testsuites") {
                            junitReport.name?.let { attribute("name", it) }
                            junitReport.time?.let { attribute("time", it) }
                            junitReport.tests?.let { attribute("tests", it.toString()) }
                            junitReport.failures?.let { attribute("failures", it.toString()) }
                            junitReport.disabled?.let { attribute("disabled", it.toString()) }
                            junitReport.errors?.let { attribute("errors", it.toString()) }
                            junitReport.testsuites.forEach {
                                writeTestSuite(it)
                            }
                        }
                    }
                }
            }
            flush()
            close()
        }
    }

    private fun XMLStreamWriter.writeTestSuite(testSuite: TestSuite) {
        element("testsuite") {
            attribute("name", testSuite.name)
            attribute("tests", testSuite.tests.toString())
            attribute("failures", testSuite.failures.toString())
            attribute("errors", testSuite.errors.toString())

            testSuite.group?.let { attribute("group", it) }
            testSuite.time?.let { attribute("time", it) }
            testSuite.skipped?.let { attribute("skipped", it.toString()) }
            testSuite.timestamp?.let { attribute("timestamp", it) }
            testSuite.hostname?.let { attribute("hostname", it) }
            testSuite.id?.let { attribute("id", it) }
            testSuite.pkg?.let { attribute("package", it) }
            testSuite.file?.let { attribute("file", it) }
            testSuite.log?.let { attribute("log", it) }
            testSuite.url?.let { attribute("url", it) }
            testSuite.version?.let { attribute("version", it) }

            testSuite.systemOut?.let { element("system-out") { writeCDataSafely(it) } }
            testSuite.systemErr?.let { element("system-err") { writeCDataSafely(it) } }

            if (testSuite.properties.isNotEmpty()) {
                element("properties") {
                    testSuite.properties.forEach { property ->
                        element("property") {
                            attribute("name", property.name)
                            attribute("value", property.value)
                        }
                    }
                }
            }

            testSuite.testcase.forEach { testCase ->
                element("testcase") {
                    attribute("name", testCase.name)
                    testCase.time?.let { attribute("time", it) }
                    attribute("classname", testCase.classname)
                    testCase.group?.let { attribute("group", it) }

                    testCase.skipped?.let {
                        element("skipped") {
                            if (!it.message.isNullOrEmpty()) {
                                attribute("message", it.message)
                            }
                            if (it.description.isNotEmpty()) {
                                writeCDataSafely(it.description)
                            }
                        }
                    }
                    testCase.error?.let {
                        element("error") {
                            if (!it.message.isNullOrEmpty()) {
                                attribute("message", it.message)
                            }
                            if (!it.type.isNullOrEmpty()) {
                                attribute("type", it.type)
                            }
                            if (it.description.isNotEmpty()) {
                                writeCDataSafely(it.description)
                            }
                        }
                    }
                    testCase.failure?.let {
                        element("failure") {
                            if (!it.message.isNullOrEmpty()) {
                                attribute("message", it.message)
                            }
                            if (!it.type.isNullOrEmpty()) {
                                attribute("type", it.type)
                            }
                            if (it.description.isNotEmpty()) {
                                writeCDataSafely(it.description)
                            }
                        }
                    }
                    writeReruns("flakyFailure", testCase.flakyFailure)
                    writeReruns("flakyError", testCase.flakyError)
                    writeReruns("rerunFailure", testCase.rerunFailure)
                    writeReruns("rerunError", testCase.rerunError)

                    testCase.systemOut?.let {
                        element("system-out") {
                            writeCDataSafely(it)
                        }
                    }
                    testCase.systemErr?.let {
                        element("system-err") {
                            writeCDataSafely(it)
                        }
                    }
                }
            }
        }
    }

    private fun XMLStreamWriter.writeReruns(elementName: String, reruns: List<Rerun>) {
        reruns.forEach {
            element(elementName) {
                it.message?.let { message -> attribute("message", message) }
                attribute("type", it.type)
                it.stackTrace?.let { stacktrace -> element("stackTrace") { writeCDataSafely(stacktrace) } }
                it.systemOut?.let { stdout -> element("system-out") { writeCDataSafely(stdout) } }
                it.systemErr?.let { stderr -> element("system-err") { writeCDataSafely(stderr) } }
            }
        }
    }
}

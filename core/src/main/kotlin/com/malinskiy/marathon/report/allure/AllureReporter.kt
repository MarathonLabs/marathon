package com.malinskiy.marathon.report.allure

import com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter
import com.google.common.collect.ImmutableMap
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import io.qameta.allure.AllureLifecycle
import io.qameta.allure.FileSystemResultsWriter
import io.qameta.allure.model.Attachment
import io.qameta.allure.model.Label
import io.qameta.allure.model.Status
import io.qameta.allure.model.StatusDetails
import io.qameta.allure.util.ResultsUtils
import java.io.File
import java.util.Locale
import java.util.UUID
import io.qameta.allure.Description as JavaDescription
import io.qameta.allure.Epic as JavaEpic
import io.qameta.allure.Feature as JavaFeature
import io.qameta.allure.Issue as JavaIssue
import io.qameta.allure.Owner as JavaOwner
import io.qameta.allure.Severity as JavaSeverity
import io.qameta.allure.SeverityLevel as JavaSeverityLevel
import io.qameta.allure.Story as JavaStory
import io.qameta.allure.TmsLink as JavaTmsLink
import io.qameta.allure.kotlin.Description as KotlinDescription
import io.qameta.allure.kotlin.Epic as KotlinEpic
import io.qameta.allure.kotlin.Feature as KotlinFeature
import io.qameta.allure.kotlin.Issue as KotlinIssue
import io.qameta.allure.kotlin.Owner as KotlinOwner
import io.qameta.allure.kotlin.Severity as KotlinSeverity
import io.qameta.allure.kotlin.SeverityLevel as KotlinSeverityLevel
import io.qameta.allure.kotlin.Story as KotlinStory
import io.qameta.allure.kotlin.TmsLink as KotlinTmsLink

class AllureReporter(val configuration: Configuration, private val outputDirectory: File) : Reporter {

    private val lifecycle: AllureLifecycle by lazy { AllureLifecycle(FileSystemResultsWriter(outputDirectory.toPath())) }

    override fun generate(executionReport: ExecutionReport) {
        executionReport.testEvents.forEach { testEvent ->
            val uuid = UUID.randomUUID().toString()
            val allureResults = createTestResult(uuid, testEvent.device, testEvent.testResult)
            lifecycle.scheduleTestCase(uuid, allureResults)
            lifecycle.writeTestCase(uuid)
        }

        val params = configuration.toMap()
        val builder = ImmutableMap.builder<String, String>()
        params.forEach {
            builder.put(it.key, it.value)
        }

        allureEnvironmentWriter(
            builder.build(), outputDirectory.absolutePath + File.separator
        )
    }

    private fun createTestResult(uuid: String, device: DeviceInfo, testResult: TestResult): io.qameta.allure.model.TestResult {
        val test = testResult.test
        val fullName = test.toSafeTestName()
        val testMethodName = test.method
        val suite = "${test.pkg}.${test.clazz}"

        val status: Status = when (testResult.status) {
            TestStatus.FAILURE -> Status.FAILED
            TestStatus.PASSED -> Status.PASSED
            TestStatus.INCOMPLETE -> Status.BROKEN
            TestStatus.ASSUMPTION_FAILURE -> Status.SKIPPED
            TestStatus.IGNORED -> Status.SKIPPED
        }

        val allureAttachments: List<Attachment> = testResult.attachments.map {
            Attachment()
                .setName(it.type.name.lowercase(Locale.ENGLISH)
                             .replaceFirstChar { cher -> if (cher.isLowerCase()) cher.titlecase(Locale.ENGLISH) else cher.toString() })
                .setSource(it.file.absolutePath)
                .setType(it.type.toMimeType())
        }

        val allureTestResult = io.qameta.allure.model.TestResult()
            .setUuid(uuid)
            .setFullName(fullName)
            .setName(testMethodName)
            .setHistoryId(getHistoryId(test))
            .setStatus(status)
            .setStart(testResult.startTime)
            .setStop(testResult.endTime)
            .setAttachments(allureAttachments)
            .setParameters(emptyList())
            .setLabels(
                mutableListOf(
                    ResultsUtils.createHostLabel().setValue(device.serialNumber),
                    ResultsUtils.createPackageLabel(test.pkg),
                    ResultsUtils.createTestClassLabel(suite),
                    ResultsUtils.createTestMethodLabel(test.method),
                    ResultsUtils.createSuiteLabel(suite)
                )
            )

        testResult.stacktrace?.let {
            allureTestResult.setStatusDetails(
                StatusDetails()
                    .setMessage(it.lines().first())
                    .setTrace(it)
            )
        }


        test.findValue<String>(JavaDescription::class.java.canonicalName)?.let { allureTestResult.setDescription(it) }
        test.findValue<String>(JavaIssue::class.java.canonicalName)?.let { allureTestResult.links.add(it.toLink()) }
        test.findValue<String>(JavaTmsLink::class.java.canonicalName)?.let { allureTestResult.links.add(it.toLink()) }

        test.findValue<String>(KotlinDescription::class.java.canonicalName)?.let { allureTestResult.setDescription(it) }
        test.findValue<String>(KotlinIssue::class.java.canonicalName)?.let { allureTestResult.links.add(it.toLink()) }
        test.findValue<String>(KotlinTmsLink::class.java.canonicalName)?.let { allureTestResult.links.add(it.toLink()) }


        allureTestResult.labels.addAll(test.getOptionalLabels())

        return allureTestResult
    }

    private fun getHistoryId(test: Test) =
        ResultsUtils.generateMethodSignatureHash(test.clazz, test.method, emptyList())

    private fun Test.getOptionalLabels(): Collection<Label> {
        val list = mutableListOf<Label>()

        findValue<String>(JavaEpic::class.java.canonicalName)?.let { list.add(ResultsUtils.createEpicLabel(it)) }
        findValue<String>(JavaFeature::class.java.canonicalName)?.let { list.add(ResultsUtils.createFeatureLabel(it)) }
        findValue<String>(JavaStory::class.java.canonicalName)?.let { list.add(ResultsUtils.createStoryLabel(it)) }
        findValue<JavaSeverityLevel>(JavaSeverity::class.java.canonicalName)?.let { list.add(ResultsUtils.createSeverityLabel(it)) }
        findValue<String>(JavaOwner::class.java.canonicalName)?.let { list.add(ResultsUtils.createOwnerLabel(it)) }

        findValue<String>(KotlinEpic::class.java.canonicalName)?.let { list.add(ResultsUtils.createEpicLabel(it)) }
        findValue<String>(KotlinFeature::class.java.canonicalName)?.let { list.add(ResultsUtils.createFeatureLabel(it)) }
        findValue<String>(KotlinStory::class.java.canonicalName)?.let { list.add(ResultsUtils.createStoryLabel(it)) }
        findValue<KotlinSeverityLevel>(KotlinSeverity::class.java.canonicalName)?.let {
            //Assuming that Java and Kotlin models are compatible
            list.add(ResultsUtils.createSeverityLabel(it.value))
        }
        findValue<String>(KotlinOwner::class.java.canonicalName)?.let { list.add(ResultsUtils.createOwnerLabel(it)) }

        return list
    }

    private fun String.toLink(): io.qameta.allure.model.Link {
        return io.qameta.allure.model.Link().also {
            it.name = "Issue"
            it.url = this
        }
    }

    private inline fun <reified T> Test.findValue(name: String): T? {
        metaProperties.find { it.name == name }?.let { property ->
            return property.values["value"] as? T
        }

        return null
    }
}

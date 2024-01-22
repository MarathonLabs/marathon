package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestIgnored
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunFailed
import com.malinskiy.adam.request.testrunner.TestRunFailing
import com.malinskiy.adam.request.testrunner.TestRunStartedEvent
import com.malinskiy.adam.request.testrunner.TestRunStopped
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.adam.request.testrunner.TestStarted
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.adam.event.TestAnnotationParser
import com.malinskiy.marathon.android.extension.testBundlesCompat
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.exceptions.TestParsingException
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.withTimeoutOrNull

private const val LISTENER_ARGUMENT = "listener"
private const val TEST_ANNOTATION_PRODUCER = "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer"

class AmInstrumentTestParser(
    private val configuration: Configuration,
    private val testBundleIdentifier: AndroidTestBundleIdentifier,
    private val vendorConfiguration: VendorConfiguration.AndroidConfiguration
) : RemoteTestParser<AdamDeviceProvider> {
    private val logger = MarathonLogging.logger {}
    private val testAnnotationParser = TestAnnotationParser()

    override suspend fun extract(device: Device): List<Test> {
        val testBundles = vendorConfiguration.testBundlesCompat()
        var blockListenerArgumentOverride = false
        return withRetry(3, 0) {
            try {
                val device = device as? AdamAndroidDevice ?: throw ConfigurationException("Unexpected device type for remote test parsing")
                return@withRetry parseTests(device, configuration, vendorConfiguration, testBundles, blockListenerArgumentOverride)
            } catch (e: CancellationException) {
                throw e
            } catch (e: TestAnnotationProducerNotFoundException) {
                logger.warn {
                    "Previous parsing attempt failed due to test parser misconfiguration: test annotation producer was not found. See https://docs.marathonlabs.io/runner/android/configure#test-parser. " +
                        "Next parsing attempt will remove overridden test run listener."
                }
                blockListenerArgumentOverride = true
                throw e
            } catch (throwable: Throwable) {
                logger.debug(throwable) { "Remote parsing failed. Retrying" }
                throw throwable
            }
        }
    }

    private suspend fun parseTests(
        device: AdamAndroidDevice,
        configuration: Configuration,
        vendorConfiguration: VendorConfiguration.AndroidConfiguration,
        testBundles: List<AndroidTestBundle>,
        blockListenerArgumentOverride: Boolean,
    ): List<Test> {
        val androidAppInstaller = AndroidAppInstaller(configuration)
        androidAppInstaller.prepareInstallation(device)

        return testBundles.flatMap { bundle ->
            val androidTestBundle =
                AndroidTestBundle(bundle.application, bundle.testApplication, bundle.extraApplications, bundle.splitApks)
            val instrumentationInfo = androidTestBundle.instrumentationInfo

            val testParserConfiguration = vendorConfiguration.testParserConfiguration
            val overrides: Map<String, String> = when {
                testParserConfiguration is TestParserConfiguration.RemoteTestParserConfiguration -> {
                    if (blockListenerArgumentOverride) testParserConfiguration.instrumentationArgs
                        .filterNot { it.key == LISTENER_ARGUMENT && it.value == TEST_ANNOTATION_PRODUCER }
                    else testParserConfiguration.instrumentationArgs
                }

                else -> emptyMap()
            }

            val runnerRequest = TestRunnerRequest(
                testPackage = instrumentationInfo.instrumentationPackage,
                runnerClass = instrumentationInfo.testRunnerClass,
                instrumentOptions = InstrumentOptions(
                    log = true,
                    overrides = overrides,
                ),
                supportedFeatures = device.supportedFeatures,
                coroutineScope = device,
            )
            val channel = device.executeTestRequest(runnerRequest)
            var observedAnnotations = false

            val tests = mutableListOf<Test>()
            while (!channel.isClosedForReceive && isActive) {
                val events: List<TestEvent>? = withTimeoutOrNull(configuration.testOutputTimeoutMillis) {
                    channel.receiveCatching().getOrNull() ?: emptyList()
                }
                if (events == null) {
                    throw TestParsingException("Unable to parse test list using ${device.serialNumber}")
                } else {
                    for (event in events) {
                        when (event) {
                            is TestRunStartedEvent -> Unit
                            is TestStarted -> Unit
                            is TestFailed -> Unit
                            is TestAssumptionFailed -> Unit
                            is TestIgnored -> Unit
                            is TestEnded -> {
                                val annotations = testAnnotationParser.extractAnnotations(event)
                                if (annotations.isNotEmpty()) {
                                    observedAnnotations = true
                                }
                                val test = TestIdentifier(event.id.className, event.id.testName).toTest(annotations)
                                tests.add(test)
                                testBundleIdentifier.put(test, androidTestBundle)
                            }

                            is TestRunFailing -> {
                                // Error message is stable, see https://github.com/android/android-test/blame/1ae53b93e02cc363311f6564a35edeea1b075103/runner/android_junit_runner/java/androidx/test/internal/runner/RunnerArgs.java#L624
                                if (event.error.contains("Could not find extra class $TEST_ANNOTATION_PRODUCER")) {
                                    throw TestAnnotationProducerNotFoundException()
                                }
                            }

                            is TestRunFailed -> Unit
                            is TestRunStopped -> Unit
                            is TestRunEnded -> Unit

                        }
                    }
                }
            }

            if (!observedAnnotations) {
                logger.warn {
                    "Bundle ${bundle.id} did not report any test annotations. If you need test annotations retrieval, remote test parser requires additional setup " +
                        "see https://docs.marathonlabs.io/runner/android/configure#test-parser"
                }
            }

            tests
        }
    }
}

private class TestAnnotationProducerNotFoundException : RuntimeException()

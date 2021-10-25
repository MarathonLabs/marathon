package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestIgnored
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunFailed
import com.malinskiy.adam.request.testrunner.TestRunStartedEvent
import com.malinskiy.adam.request.testrunner.TestRunStopped
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.adam.request.testrunner.TestStarted
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.extension.testBundlesCompat
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.android.TestParserConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock

class RemoteTestParser(
    private val configuration: Configuration,
    private val testBundleIdentifier: AndroidTestBundleIdentifier,
    private val vendorConfiguration: VendorConfiguration.AndroidConfiguration
) : TestParser {
    private val logger = MarathonLogging.logger {}

    override suspend fun extract(): List<Test> {
        val testBundles = vendorConfiguration.testBundlesCompat()

        val provider =
            AdamDeviceProvider(configuration, testBundleIdentifier, vendorConfiguration, Track(), SystemTimer(Clock.systemDefaultZone()))
        provider.initialize()
        val channel = provider.subscribe()

        try {
            for (update in channel) {
                if (update is DeviceProvider.DeviceEvent.DeviceConnected) {
                    val device = update.device as AdamAndroidDevice
                    return parseTests(device, configuration, vendorConfiguration, testBundles)
                }
            }
            throw RuntimeException("failed to parse")
        } finally {
            channel.close()
            provider.terminate()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun parseTests(
        device: AdamAndroidDevice,
        configuration: Configuration,
        vendorConfiguration: VendorConfiguration.AndroidConfiguration,
        testBundles: List<AndroidTestBundle>
    ): List<Test> {
        return testBundles.flatMap { bundle ->
            val androidTestBundle = AndroidTestBundle(bundle.application, bundle.testApplication)
            val instrumentationInfo = androidTestBundle.instrumentationInfo

            val testParserConfiguration = vendorConfiguration.testParserConfiguration
            val overrides: Map<String, String> = when {
                testParserConfiguration is TestParserConfiguration.RemoteTestParserConfiguration -> testParserConfiguration.instrumentationArgs
                else -> emptyMap()
            }

            val runnerRequest = TestRunnerRequest(
                testPackage = instrumentationInfo.instrumentationPackage,
                runnerClass = instrumentationInfo.testRunnerClass,
                instrumentOptions = InstrumentOptions(
                    log = true,
                    overrides = overrides
                ),
            )
            val androidAppInstaller = AndroidAppInstaller(configuration)
            androidAppInstaller.prepareInstallation(device)
            val channel = device.executeTestRequest(runnerRequest)

            val tests = mutableListOf<Test>()
            while (!channel.isClosedForReceive && isActive) {
                val events: List<TestEvent>? = withTimeoutOrNull(configuration.testOutputTimeoutMillis) {
                    channel.receiveCatching().getOrNull() ?: emptyList()
                }
                if (events == null) {
                    throw RuntimeException("Unable to parse test list using ${device.serialNumber}")
                } else {
                    for (event in events) {
                        when (event) {
                            is TestRunStartedEvent -> Unit
                            is TestStarted -> Unit
                            is TestFailed -> Unit
                            is TestAssumptionFailed -> Unit
                            is TestIgnored -> Unit
                            is TestEnded -> {
                                val annotations = extractAnnotations(event)
                                val test = TestIdentifier(event.id.className, event.id.testName).toTest(annotations)
                                tests.add(test)
                                testBundleIdentifier.put(test, androidTestBundle)
                            }
                            is TestRunFailed -> Unit
                            is TestRunStopped -> Unit
                            is TestRunEnded -> Unit
                        }
                    }
                }
            }
            tests
        }
    }

    private fun extractAnnotations(event: TestEnded): List<MetaProperty> {
        val v2 = event.metrics["com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v2"]
        return when {
            v2 != null -> {
                v2.removeSurrounding("[", "]").split(",").map { it.trim() }
                    .toList().map { serializedAnnotation ->
                        val index = serializedAnnotation.indexOfFirst { it == '(' }
                        val name = serializedAnnotation.substring(0 until index)
                        val parameters = serializedAnnotation.substring(index).removeSurrounding("(", ")").split(":")
                        val values = parameters.mapNotNull { parameter ->
                            val split = parameter.split("=")
                            if (split.size == 2) {
                                Pair(split[0], split[1])
                            } else {
                                null
                            }
                        }.toMap()
                        MetaProperty(name = name, values = values)
                    }
            }
            else -> emptyList()
        }
    }
}

package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.config.LogicalConfigurationValidator
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import java.util.*
import kotlin.coroutines.coroutineContext

private val log = MarathonLogging.logger {}

class Marathon(
    val configuration: Configuration,
    private val tracker: TrackerInternal,
    private val analytics: Analytics,
    private val progressReporter: ProgressReporter,
    private val track: Track
) {

    private val configurationValidator = LogicalConfigurationValidator()

    private fun configureLogging(vendorConfiguration: VendorConfiguration) {
        MarathonLogging.debug = configuration.debug

        vendorConfiguration.logConfigurator()?.configure(vendorConfiguration)
    }

    private suspend fun loadDeviceProvider(vendorConfiguration: VendorConfiguration): DeviceProvider {
        val vendorDeviceProvider = vendorConfiguration.deviceProvider()
            ?: ServiceLoader.load(DeviceProvider::class.java).first()

        vendorDeviceProvider.initialize(configuration.vendorConfiguration)
        return vendorDeviceProvider
    }

    private fun loadTestParser(vendorConfiguration: VendorConfiguration): TestParser {
        val vendorTestParser = vendorConfiguration.testParser()
        if (vendorTestParser != null) {
            return vendorTestParser
        }
        val loader = ServiceLoader.load(TestParser::class.java)
        return loader.first()
    }

    fun run() = runBlocking {
        try {
            runAsync()
        } catch (th: Throwable) {
            log.error(th.toString())

            when (th) {
                is NoDevicesException -> {
                    log.warn { "No devices found" }
                    false
                }
                else -> configuration.ignoreFailures
            }
        }
    }

    suspend fun runAsync(): Boolean {
        configureLogging(configuration.vendorConfiguration)
        trackAnalytics(configuration)

        val testParser = loadTestParser(configuration.vendorConfiguration)
        val deviceProvider = loadDeviceProvider(configuration.vendorConfiguration)

        configurationValidator.validate(configuration)

        val parsedTests = testParser.extract(configuration)
        val tests = applyTestFilters(parsedTests)
        val shard = prepareTestShard(tests, analytics)

        log.info("Scheduling ${tests.size} tests")
        log.debug(tests.joinToString(", ") { it.toTestName() })
        val currentCoroutineContext = coroutineContext
        val scheduler = Scheduler(
            deviceProvider,
            analytics,
            configuration,
            shard,
            progressReporter,
            track,
            currentCoroutineContext
        )

        if (configuration.outputDir.exists()) {
            log.info { "Output ${configuration.outputDir} already exists" }
            configuration.outputDir.deleteRecursively()
        }
        configuration.outputDir.mkdirs()

        val hook = installShutdownHook { onFinish(analytics, deviceProvider) }

        if (tests.isNotEmpty()) {
            scheduler.execute()
        }

        onFinish(analytics, deviceProvider)
        hook.uninstall()

        stopKoin()
        return progressReporter.aggregateResult()
    }

    private fun installShutdownHook(block: suspend () -> Unit): ShutdownHook {
        val shutdownHook = ShutdownHook(configuration) {
            runBlocking {
                block.invoke()
            }
        }
        shutdownHook.install()
        return shutdownHook
    }

    private suspend fun onFinish(analytics: Analytics, deviceProvider: DeviceProvider) {
        analytics.close()
        deviceProvider.terminate()
        tracker.close()
    }

    private fun applyTestFilters(parsedTests: List<Test>): List<Test> {
        var tests = parsedTests.filter { test ->
            configuration.testClassRegexes.all { it.matches(test.clazz) }
        }
        configuration.filteringConfiguration.whitelist.forEach { tests = it.filter(tests) }
        configuration.filteringConfiguration.blacklist.forEach { tests = it.filterNot(tests) }
        return tests
    }

    private fun prepareTestShard(tests: List<Test>, analytics: Analytics): TestShard {
        val shardingStrategy = configuration.shardingStrategy
        val flakinessShard = configuration.flakinessStrategy
        val shard = shardingStrategy.createShard(tests)
        return flakinessShard.process(shard, analytics)
    }

    private fun trackAnalytics(configuration: Configuration) {
        UsageAnalytics.USAGE_TRACKER.run {
            trackEvent(Event(TrackActionType.VendorConfiguration, configuration.vendorConfiguration.javaClass.name))
            trackEvent(Event(TrackActionType.PoolingStrategy, configuration.poolingStrategy.javaClass.name))
            trackEvent(Event(TrackActionType.ShardingStrategy, configuration.shardingStrategy.javaClass.name))
            trackEvent(Event(TrackActionType.SortingStrategy, configuration.sortingStrategy.javaClass.name))
            trackEvent(Event(TrackActionType.RetryStrategy, configuration.retryStrategy.javaClass.name))
            trackEvent(Event(TrackActionType.BatchingStrategy, configuration.batchingStrategy.javaClass.name))
            trackEvent(Event(TrackActionType.FlakinessStrategy, configuration.flakinessStrategy.javaClass.name))
        }
    }
}

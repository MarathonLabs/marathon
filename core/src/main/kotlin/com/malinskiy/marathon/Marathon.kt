package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.LogicalConfigurationValidator
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.exceptions.NoTestCasesFoundException
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.extension.toFlakinessStrategy
import com.malinskiy.marathon.extension.toShardingStrategy
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import kotlin.coroutines.coroutineContext

private val log = MarathonLogging.logger {}

class Marathon(
    private val configuration: Configuration,
    private val deviceProvider: DeviceProvider,
    private val testBundleIdentifier: TestBundleIdentifier?,
    private val testParser: TestParser,
    private val logConfigurator: MarathonLogConfigurator,
    private val tracker: TrackerInternal,
    private val analytics: Analytics,
    private val progressReporter: ProgressReporter,
    private val track: Track,
    private val timer: Timer
) {
    private val configurationValidator = LogicalConfigurationValidator()

    private fun configureLogging() {
        MarathonLogging.debug = configuration.debug
        logConfigurator.configure()
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
        configureLogging()
        trackAnalytics(configuration)

        logSystemInformation()

        deviceProvider.initialize()
        configurationValidator.validate(configuration)

        val parsedTests = testParser.extract()
        if (parsedTests.isEmpty()) throw NoTestCasesFoundException("No tests cases were found")
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
            timer,
            testBundleIdentifier,
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

        val result = onFinish(analytics, deviceProvider)
        hook.uninstall()

        stopKoin()
        return result
    }

    private fun logSystemInformation() {
        log.info { "System Information:" }

        val properties = System.getProperties()
        val systemProperties = properties.filterKeys { it.toString().startsWith("java") || it.toString().startsWith("os") }
        systemProperties.forEach {
            log.info { "${it.key}: ${it.value}" }
        }
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

    private suspend fun onFinish(analytics: Analytics, deviceProvider: DeviceProvider): Boolean {
        analytics.close()
        deviceProvider.terminate()
        tracker.close()
        return progressReporter.aggregateResult()
    }

    private fun applyTestFilters(parsedTests: List<Test>): List<Test> {
        var tests = parsedTests.filter { test ->
            configuration.testClassRegexes.all { it.matches(test.clazz) }
        }
        configuration.filteringConfiguration.allowlist.forEach { tests = it.toTestFilter().filter(tests) }
        configuration.filteringConfiguration.blocklist.forEach { tests = it.toTestFilter().filterNot(tests) }
        return tests
    }

    private fun prepareTestShard(tests: List<Test>, analytics: Analytics): TestShard {
        val shardingStrategy = configuration.shardingStrategy.toShardingStrategy()
        val flakinessStrategy = configuration.flakinessStrategy.toFlakinessStrategy()
        val shard = shardingStrategy.createShard(tests)
        return flakinessStrategy.process(shard, analytics)
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

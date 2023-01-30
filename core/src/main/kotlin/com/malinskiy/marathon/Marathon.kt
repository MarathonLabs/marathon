package com.malinskiy.marathon

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.ExecutionCommand
import com.malinskiy.marathon.config.LogicalConfigurationValidator
import com.malinskiy.marathon.config.MarathonRunCommand
import com.malinskiy.marathon.config.ParseCommand
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.exceptions.NoTestCasesFoundException
import com.malinskiy.marathon.execution.LocalTestParser
import com.malinskiy.marathon.execution.RemoteTestParser
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.command.parse.MarathonTestParseCommand
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.execution.withRetry
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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.context.stopKoin
import java.util.concurrent.atomic.AtomicBoolean
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
    private val timer: Timer,
    private val marathonTestParseCommand: MarathonTestParseCommand
) {
    private val configurationValidator = LogicalConfigurationValidator()
    private val cleanedUp = AtomicBoolean(false)

    private fun configureLogging() {
        MarathonLogging.debug = configuration.debug
        logConfigurator.configure()
    }

    fun run(executionCommand: ExecutionCommand = MarathonRunCommand) : Boolean = runBlocking {
        try {
            async {
                val hook = installShutdownHook { onFinish(analytics, deviceProvider) }
                val result = runAsync(executionCommand)
                hook.uninstall()
                result
            }.apply {
                invokeOnCompletion {
                    //Marathon will usually clean up correctly unless exception is thrown
                    if (it != null) {
                        runBlocking {
                            withContext(NonCancellable) {
                                onFinish(analytics, deviceProvider)
                            }
                        }
                    }
                }
            }.await()
        } catch (th: Throwable) {
            log.error(th) {}
            when (th) {
                is NoDevicesException -> {
                    log.warn { "No devices found" }
                    false
                }

                else -> configuration.ignoreFailures
            }
        }
    }

    suspend fun runAsync(executionCommand: ExecutionCommand = MarathonRunCommand): Boolean {
        configureLogging()
        trackAnalytics(configuration)

        logSystemInformation()
        configurationValidator.validate(configuration)

        deviceProvider.initialize()
        val parsedAllTests = when (testParser) {
            is LocalTestParser -> testParser.extract()
            is RemoteTestParser<*> -> {
                withRetry(3, 0) {
                    withTimeoutOrNull(configuration.deviceInitializationTimeoutMillis) {
                        val borrowedDevice = deviceProvider.borrow()
                        testParser.extract(borrowedDevice)
                    } ?: throw NoDevicesException("Timed out waiting for a temporary device for remote test parsing")
                }
            }

            else -> {
                throw ConfigurationException("unknown test parser type for ${testParser::class}, should inherit from either ${LocalTestParser::class.simpleName} or ${RemoteTestParser::class.simpleName}")
            }
        }

        if (parsedAllTests.isEmpty()) throw NoTestCasesFoundException("No tests cases were found")
        val parsedFilteredTests = applyTestFilters(parsedAllTests)
        if (executionCommand is ParseCommand) {
            marathonTestParseCommand.execute(
                tests = parsedFilteredTests,
                outputFileName = executionCommand.outputFileName
            )
            stopKoin()
            return true
        }

        val shard = prepareTestShard(parsedFilteredTests, analytics)

        log.info("Scheduling ${parsedFilteredTests.size} tests")
        log.debug(parsedFilteredTests.joinToString(", ") { it.toTestName() })
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

        if (parsedFilteredTests.isNotEmpty()) {
            scheduler.execute()
        }

        onFinish(analytics, deviceProvider)
        val result = progressReporter.aggregateResult()

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


    private suspend fun onFinish(analytics: Analytics, deviceProvider: DeviceProvider) {
        if (cleanedUp.compareAndSet(false, true)) {
            analytics.close()
            deviceProvider.terminate()
            tracker.close()
        }
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

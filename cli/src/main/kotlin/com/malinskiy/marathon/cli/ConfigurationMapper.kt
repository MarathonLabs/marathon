package com.malinskiy.marathon.cli

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.cli.schema.AnalyticsConfiguration
import com.malinskiy.marathon.cli.schema.Configuration
import com.malinskiy.marathon.cli.schema.FilteringConfiguration
import com.malinskiy.marathon.cli.schema.ScreenRecordingPolicy
import com.malinskiy.marathon.cli.schema.TestFilter
import com.malinskiy.marathon.cli.schema.VendorConfiguration
import com.malinskiy.marathon.cli.schema.android.AllureConfiguration
import com.malinskiy.marathon.cli.schema.android.ScreenRecordConfiguration
import com.malinskiy.marathon.cli.schema.android.ScreenshotConfiguration
import com.malinskiy.marathon.cli.schema.android.SerialStrategy
import com.malinskiy.marathon.cli.schema.android.TimeoutConfiguration
import com.malinskiy.marathon.cli.schema.android.VendorType
import com.malinskiy.marathon.cli.schema.android.VideoConfiguration
import com.malinskiy.marathon.cli.schema.common.DeviceFeature
import com.malinskiy.marathon.cli.schema.strategies.BatchingStrategy
import com.malinskiy.marathon.cli.schema.strategies.FlakinessStrategy
import com.malinskiy.marathon.cli.schema.strategies.PoolingStrategy
import com.malinskiy.marathon.cli.schema.strategies.RetryStrategy
import com.malinskiy.marathon.cli.schema.strategies.ShardingStrategy
import com.malinskiy.marathon.cli.schema.strategies.SortingStrategy
import com.malinskiy.marathon.execution.AnalyticsConfiguration.DisabledAnalytics
import com.malinskiy.marathon.execution.AnalyticsConfiguration.GraphiteConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.CompositionFilter
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ComboPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import com.malinskiy.marathon.ios.IOSConfiguration
import ddmlibModule
import com.malinskiy.marathon.execution.AnalyticsConfiguration as MarathonAnalyticsConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration as MarathonRetentionPolicyConfiguration
import com.malinskiy.marathon.execution.Configuration as MarathonConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration as MarathonFilteringConfiguration
import com.malinskiy.marathon.execution.TestFilter as MarathonTestFilter
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy as MarathonScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.BatchingStrategy as MarathonBatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy as MarathonFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy as MarathonPoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy as MarathonRetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy as MarathonShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy as MarathonSortingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration as MarathonVendorConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy as MarathonSerialStrategy
import com.malinskiy.marathon.android.ScreenRecordConfiguration as MarathonScreenRecordConfiguration
import com.malinskiy.marathon.device.DeviceFeature as MarathonDeviceFeature
import com.malinskiy.marathon.android.configuration.AllureConfiguration as MarathonAllureConfiguration
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration as MarathonTimeoutConfiguration
import com.malinskiy.marathon.execution.CompositionFilter.OPERATION as MarathonOperation
import com.malinskiy.marathon.android.VideoConfiguration as MarathonVideoConfiguration
import com.malinskiy.marathon.android.ScreenshotConfiguration as MarathonScreenshotConfiguration

class ConfigurationMapper {
    fun map(input: Configuration): MarathonConfiguration {
        return MarathonConfiguration(
            name = input.name,
            outputDir = input.outputDir,

            analyticsConfiguration = input.analyticsConfiguration.toMarathonAnalyticsConfig(),
            poolingStrategy = input.poolingStrategy.toMarathonPoolingStrategy(),
            shardingStrategy = input.shardingStrategy.toMarathonShardingStrategy(),
            sortingStrategy = input.sortingStrategy.toMarathonSortingStrategy(),
            batchingStrategy = input.batchingStrategy.toMarathonBatchingStrategy(),
            flakinessStrategy = input.flakinessStrategy.toMarathonFlakinessStrategy(),
            retryStrategy = input.retryStrategy.toMarathonRetryStrategy(),
            filteringConfiguration = input.filteringConfiguration.toMarathonFilteringConfiguration(),

            ignoreFailures = input.ignoreFailures,
            isCodeCoverageEnabled = input.isCodeCoverageEnabled,
            fallbackToScreenshots = input.fallbackToScreenshots,
            strictMode = input.strictMode,
            uncompletedTestRetryQuota = input.uncompletedTestRetryQuota,

            testClassRegexes = input.testClassRegexes,
            includeSerialRegexes = input.includeSerialRegexes,
            excludeSerialRegexes = input.excludeSerialRegexes,

            testBatchTimeoutMillis = input.testBatchTimeoutMillis,
            testOutputTimeoutMillis = input.testOutputTimeoutMillis,
            debug = input.debug,

            screenRecordingPolicy = input.screenRecordingPolicy.toMarathonScreenRecordingPolicy(),

            vendorConfiguration = input.vendorConfiguration.toMarathonVendorConfiguration(),

            analyticsTracking = input.analyticsTracking,
            deviceInitializationTimeoutMillis = input.deviceInitializationTimeoutMillis,
        )
    }

    private fun AnalyticsConfiguration.toMarathonAnalyticsConfig(): MarathonAnalyticsConfiguration {
        return when (this) {
            is AnalyticsConfiguration.Disabled -> DisabledAnalytics
            is AnalyticsConfiguration.Graphite -> GraphiteConfiguration(this.host, this.port, this.prefix)
            is AnalyticsConfiguration.InfluxDb -> InfluxDbConfiguration(
                this.url,
                this.user,
                this.password,
                this.dbName,
                this.retentionPolicy.toMarathonInfluxRetentionPolicy()
            )
        }
    }

    private fun AnalyticsConfiguration.InfluxDb.RetentionPolicyConfiguration.toMarathonInfluxRetentionPolicy(): MarathonRetentionPolicyConfiguration {
        return MarathonRetentionPolicyConfiguration(
            name,
            duration,
            shardDuration,
            replicationFactor,
            isDefault
        )
    }

    private fun PoolingStrategy.toMarathonPoolingStrategy(): MarathonPoolingStrategy {
        return when (this) {
            is PoolingStrategy.Omni -> OmniPoolingStrategy()
            is PoolingStrategy.ABI -> AbiPoolingStrategy()
            is PoolingStrategy.Combo -> ComboPoolingStrategy(list.map { it.toMarathonPoolingStrategy() })
            is PoolingStrategy.Manufacturer -> ManufacturerPoolingStrategy()
            is PoolingStrategy.Model -> ModelPoolingStrategy()
            is PoolingStrategy.OsVersion -> OperatingSystemVersionPoolingStrategy()
        }
    }

    private fun ShardingStrategy.toMarathonShardingStrategy(): MarathonShardingStrategy {
        return when (this) {
            is ShardingStrategy.Disabled -> ParallelShardingStrategy()
            is ShardingStrategy.Count -> CountShardingStrategy(count)
        }
    }

    private fun SortingStrategy.toMarathonSortingStrategy(): MarathonSortingStrategy {
        return when (this) {
            is SortingStrategy.Disabled -> NoSortingStrategy()
            is SortingStrategy.ExecutionTime -> ExecutionTimeSortingStrategy(percentile, timeLimit)
            is SortingStrategy.SuccessRate -> SuccessRateSortingStrategy(timeLimit, ascending)
        }
    }

    private fun BatchingStrategy.toMarathonBatchingStrategy(): MarathonBatchingStrategy {
        return when (this) {
            is BatchingStrategy.Disabled -> IsolateBatchingStrategy()
            is BatchingStrategy.FixedSize -> FixedSizeBatchingStrategy(fixedSize, durationMillis, percentile, timeLimit, lastMileLength)
        }
    }

    private fun FlakinessStrategy.toMarathonFlakinessStrategy(): MarathonFlakinessStrategy {
        return when (this) {
            is FlakinessStrategy.Disabled -> IgnoreFlakinessStrategy()
            is FlakinessStrategy.ProbabilityBased -> ProbabilityBasedFlakinessStrategy(minSuccessRate, maxCount, timeLimit)
        }
    }

    private fun RetryStrategy.toMarathonRetryStrategy(): MarathonRetryStrategy {
        return when (this) {
            is RetryStrategy.Disabled -> NoRetryStrategy()
            is RetryStrategy.FixedQuota -> FixedQuotaRetryStrategy(totalAllowedRetryQuota, retryPerTestQuota)
        }
    }

    private fun FilteringConfiguration.toMarathonFilteringConfiguration(): MarathonFilteringConfiguration {
        return MarathonFilteringConfiguration(
            allowList.map { it.toMarathonFilter() },
            blockList.map { it.toMarathonFilter() }
        )
    }

    private fun TestFilter.toMarathonFilter(): MarathonTestFilter {
        return when (this) {
            is TestFilter.Annotation -> AnnotationFilter(annotation)
            is TestFilter.FullyQualifiedClassname -> FullyQualifiedClassnameFilter(fullyQualifiedClassname)
            is TestFilter.SimpleClassname -> SimpleClassnameFilter(simpleClassname)
            is TestFilter.TestMethod -> TestMethodFilter(method)
            is TestFilter.TestPackage -> TestPackageFilter(`package`)
            is TestFilter.Composition -> CompositionFilter(composition.map { it.toMarathonFilter() }, op.toMarathonOperation())
        }
    }

    private fun TestFilter.OPERATION.toMarathonOperation(): MarathonOperation {
        return when (this) {
            TestFilter.OPERATION.INTERSECTION -> MarathonOperation.INTERSECTION
            TestFilter.OPERATION.SUBTRACT -> MarathonOperation.SUBTRACT
            TestFilter.OPERATION.UNION -> MarathonOperation.UNION
        }
    }

    private fun ScreenRecordingPolicy.toMarathonScreenRecordingPolicy(): MarathonScreenRecordingPolicy {
        return when (this) {
            ScreenRecordingPolicy.ON_FAILURE -> MarathonScreenRecordingPolicy.ON_FAILURE
            ScreenRecordingPolicy.ON_ANY -> MarathonScreenRecordingPolicy.ON_ANY
        }
    }

    private fun implementationModules(vendor: VendorType) = when (vendor) {
        VendorType.ADAM -> listOf(adamModule)
        VendorType.DDMLIB -> listOf(ddmlibModule)
    }

    private fun VendorConfiguration.toMarathonVendorConfiguration(): MarathonVendorConfiguration {
        return when (this) {
            is VendorConfiguration.Android -> AndroidConfiguration(
                androidSdk = androidSdk,
                applicationOutput = applicationApk,
                testApplicationOutput = testApplicationApk,
                implementationModules = implementationModules(vendor),
                autoGrantPermission = autoGrantPermission,
                instrumentationArgs = instrumentationArgs,
                applicationPmClear = applicationPmClear,
                testApplicationPmClear = testApplicationPmClear,
                adbInitTimeoutMillis = adbInitTimeoutMillis,
                installOptions = installOptions,
                serialStrategy = serialStrategy.toMarathonSerialStrategy(),
                screenRecordConfiguration = screenRecordConfiguration.toMarathonScreenRecordConfiguration(),
                waitForDevicesTimeoutMillis = waitForDevicesTimeoutMillis,
                allureConfiguration = allureConfiguration.toMarathonAllureConfiguration(),
                timeoutConfiguration = timeoutConfiguration.toMarathonTimeoutConfiguration(),
            )
            is VendorConfiguration.IOS -> IOSConfiguration(
                derivedDataDir = derivedDataDir,
                xctestrunPath = xctestrunPath,
                remoteUsername = remoteUsername,
                remotePrivateKey = remotePrivateKey,
                knownHostsPath = knownHostsPath,
                remoteRsyncPath = remoteRsyncPath,
                debugSsh = debugSsh,
                alwaysEraseSimulators = alwaysEraseSimulators,
                hideRunnerOutput = hideRunnerOutput,
                compactOutput = compactOutput,
                keepAliveIntervalMillis = keepAliveIntervalMillis,
                devicesFile = devicesFile,
                sourceRoot = sourceRoot
            )
        }
    }

    private fun SerialStrategy.toMarathonSerialStrategy(): MarathonSerialStrategy {
        return when (this) {
            SerialStrategy.AUTOMATIC -> MarathonSerialStrategy.AUTOMATIC
            SerialStrategy.BOOT_PROPERTY -> MarathonSerialStrategy.BOOT_PROPERTY
            SerialStrategy.DDMS -> MarathonSerialStrategy.DDMS
            SerialStrategy.HOSTNAME -> MarathonSerialStrategy.HOSTNAME
            SerialStrategy.MARATHON_PROPERTY -> MarathonSerialStrategy.MARATHON_PROPERTY
        }
    }

    private fun ScreenRecordConfiguration.toMarathonScreenRecordConfiguration(): MarathonScreenRecordConfiguration {
        return MarathonScreenRecordConfiguration(
            preferableRecorderType = preferableRecorderType?.toMarathonDeviceFeature(),
            videoConfiguration = videoConfiguration.toMarathonVideoConfiguration(),
            screenshotConfiguration = screenshotConfiguration.toMarathonScreenshotConfiguration()
        )
    }

    private fun DeviceFeature.toMarathonDeviceFeature(): MarathonDeviceFeature {
        return when (this) {
            DeviceFeature.SCREENSHOT -> MarathonDeviceFeature.SCREENSHOT
            DeviceFeature.VIDEO -> MarathonDeviceFeature.VIDEO
        }
    }

    private fun VideoConfiguration.toMarathonVideoConfiguration(): MarathonVideoConfiguration {
        return MarathonVideoConfiguration(enabled, width, height, bitrateMbps, timeLimit, timeLimitUnits)
    }

    private fun ScreenshotConfiguration.toMarathonScreenshotConfiguration(): MarathonScreenshotConfiguration {
        return MarathonScreenshotConfiguration(enabled, width, height, delayMs)
    }

    private fun AllureConfiguration.toMarathonAllureConfiguration(): MarathonAllureConfiguration {
        return MarathonAllureConfiguration(enabled, resultsDirectory)
    }

    private fun TimeoutConfiguration.toMarathonTimeoutConfiguration(): MarathonTimeoutConfiguration {
        return MarathonTimeoutConfiguration(shell, listFiles, pushFile, pullFile, uninstall, install, screenrecorder, screencapturer)
    }
}

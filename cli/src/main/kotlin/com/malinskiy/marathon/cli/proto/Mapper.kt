package com.malinskiy.marathon.cli.proto

import com.google.protobuf.Timestamp
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.ScreenshotConfiguration
import com.malinskiy.marathon.android.VideoConfiguration
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.AggregationMode
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncEntry
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.ThreadingConfiguration
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.VendorType.ADAM
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.VendorType.DDMLIB
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.CompositionFilter
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
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
import com.malinskiy.marathon.vendor.VendorConfiguration
import ddmlibModule
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration as ProtoAnalyticsConfiguration
import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration.Graphite as ProtoGraphite
import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration.InfluxDb as ProtoInfluxDb
import com.malinskiy.marathon.cliconfig.proto.AnalyticsConfiguration.InfluxDb.RetentionPolicy as ProtoRetentionPolicy
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration as ProtoAndroidConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.AllureConfiguration as ProtoAllureConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.FileSyncConfiguration as ProtoFileSyncConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.ScreenRecordConfiguration as ProtoScreenRecordConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.ScreenRecordConfiguration.ScreenshotConfiguration as ProtoScreenshotConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.ScreenRecordConfiguration.VideoConfiguration as ProtoVideoConfiguration
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.SerialStrategy as ProtoSerialStrategy
import com.malinskiy.marathon.cliconfig.proto.AndroidConfiguration.TimeoutConfiguration as ProtoTimeoutConfiguration
import com.malinskiy.marathon.cliconfig.proto.BatchingStrategy as ProtoBatchingStrategy
import com.malinskiy.marathon.cliconfig.proto.Configuration as ProtoConfig
import com.malinskiy.marathon.cliconfig.proto.DeviceFeature as ProtoDeviceFeature
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration as ProtoFilteringConfiguration
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration.TestFilter as ProtoTestFilter
import com.malinskiy.marathon.cliconfig.proto.FilteringConfiguration.TestFilter.Composition.Operation as ProtoTestFilterOperation
import com.malinskiy.marathon.cliconfig.proto.FlakinessStrategy as ProtoFlakinessStrategy
import com.malinskiy.marathon.cliconfig.proto.IosConfiguration as ProtoIosConfiguration
import com.malinskiy.marathon.cliconfig.proto.PoolingStrategy as ProtoPoolingStrategy
import com.malinskiy.marathon.cliconfig.proto.RetryStrategy as ProtoRetryStrategy
import com.malinskiy.marathon.cliconfig.proto.ScreenRecordingPolicy as ProtoScreenRecordingPolicy
import com.malinskiy.marathon.cliconfig.proto.ShardingStrategy as ProtoShardingStrategy
import com.malinskiy.marathon.cliconfig.proto.SortingStrategy as ProtoSortingStrategy
import com.malinskiy.marathon.cliconfig.proto.VendorConfiguration as ProtoVendorConfiguration

fun <T, O> O.get(check: O.() -> Boolean, get: O.() -> T): T? {
    if (check(this)) {
        return get(this)
    }
    return null
}

class Mapper {
    private fun convert(protoConfig: ProtoConfig): Configuration {
        return Configuration(
            name = protoConfig.name,
            outputDir = File(protoConfig.outputDir),
            analyticsConfiguration = protoConfig.get({ hasAnalyticsConfiguration() }, { analyticsConfiguration })?.convert(),
            poolingStrategy = protoConfig.get({ hasPoolingStrategy() }, { poolingStrategy })?.convert(),
            shardingStrategy = protoConfig.get({ hasShardingStrategy() }, { shardingStrategy })?.convert(),
            sortingStrategy = protoConfig.get({ hasSortingStrategy() }, { sortingStrategy })?.convert(),
            batchingStrategy = protoConfig.get({ hasBatchingStrategy() }, { batchingStrategy })?.convert(),
            flakinessStrategy = protoConfig.get({ hasFlakinessStrategy() }, { flakinessStrategy })?.convert(),
            retryStrategy = protoConfig.get({ hasRetryStrategy() }, { retryStrategy })?.convert(),
            filteringConfiguration = protoConfig.get({ hasFilteringConfiguration() }, { filteringConfiguration })?.convert(),

            ignoreFailures = protoConfig.get({ hasIgnoreFailures() }, { ignoreFailures }),
            isCodeCoverageEnabled = protoConfig.get({ hasIsCodeCoverageEnabled() }, { isCodeCoverageEnabled }),
            fallbackToScreenshots = protoConfig.get({ hasFallbackToScreenshots() }, { fallbackToScreenshots }),
            strictMode = protoConfig.get({ hasStrictMode() }, { strictMode }),
            uncompletedTestRetryQuota = protoConfig.get({ hasUncompletedTestRetryQuota() }, { uncompletedTestRetryQuota }),

            testClassRegexes = protoConfig.get(
                { hasTestClassRegexes() },
                { testClassRegexes }
            )?.valuesList?.map { it.toRegex() },
            includeSerialRegexes = protoConfig.get(
                { hasIncludeSerialRegexes() },
                { includeSerialRegexes }
            )?.valuesList?.map { it.toRegex() },
            excludeSerialRegexes = protoConfig.get(
                { hasExcludeSerialRegexes() },
                { excludeSerialRegexes }
            )?.valuesList?.map { it.toRegex() },

            testBatchTimeoutMillis = protoConfig.get({ hasTestBatchTimeoutMillis() }, { testBatchTimeoutMillis }),
            testOutputTimeoutMillis = protoConfig.get({ hasTestOutputTimeoutMillis() }, { testOutputTimeoutMillis }),
            debug = protoConfig.get({ hasDebug() }, { debug }),

            screenRecordingPolicy = protoConfig.get({ hasScreenRecordingPolicy() }, { screenRecordingPolicy })?.convert(),
            vendorConfiguration = protoConfig.vendorConfiguration.convert(),
            analyticsTracking = protoConfig.get({ hasAnalyticsTracking() }, { analyticsTracking }),
            deviceInitializationTimeoutMillis = protoConfig.get({ hasDeviceInitializationTimeoutMillis() },
                                                                { deviceInitializationTimeoutMillis })
        )
    }

    fun parse(marathonProto: File): Configuration {
        return convert(ProtoConfig.parseFrom(marathonProto.inputStream()))
    }
}


fun ProtoAnalyticsConfiguration.convert(): AnalyticsConfiguration {
    return when {
        hasDisabled() -> AnalyticsConfiguration.DisabledAnalytics
        hasInflux() -> influx.convert()
        hasGraphite() -> graphite.convert()
        else -> throw fail("AnalyticsConfiguration")
    }
}

fun ProtoInfluxDb.convert(): AnalyticsConfiguration.InfluxDbConfiguration =
    AnalyticsConfiguration.InfluxDbConfiguration(url, user, password, dbName, retentionPolicy.convert())

fun ProtoRetentionPolicy.convert(): AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration {
    return AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration(
        name,
        duration,
        shardDuration,
        replicationFactor,
        isDefault
    )
}

fun ProtoGraphite.convert(): AnalyticsConfiguration.GraphiteConfiguration = AnalyticsConfiguration.GraphiteConfiguration(host, port, prefix)

fun ProtoPoolingStrategy.convert(): PoolingStrategy {
    return when {
        hasAbi() -> AbiPoolingStrategy()
        hasCombo() -> ComboPoolingStrategy(combo.listList.map { it.convert() })
        hasManufacturer() -> ManufacturerPoolingStrategy()
        hasModel() -> ModelPoolingStrategy()
        hasOmni() -> OmniPoolingStrategy()
        hasOsVersion() -> OperatingSystemVersionPoolingStrategy()
        else -> throw fail("PoolingStrategy")
    }
}

fun ProtoShardingStrategy.convert(): ShardingStrategy {
    return when {
        hasDisabled() -> ParallelShardingStrategy()
        hasCount() -> CountShardingStrategy(count.count)
        else -> throw fail("ShardingStrategy")
    }
}

fun ProtoSortingStrategy.convert(): SortingStrategy {
    return when {
        hasDisabled() -> NoSortingStrategy()
        hasExecutionTime() -> ExecutionTimeSortingStrategy(
            executionTime.percentile,
            executionTime.timeLimit.toInstant()
        )
        hasSuccessRate() -> SuccessRateSortingStrategy(
            successRate.timeLimit.toInstant(),
            successRate.ascending
        )
        else -> throw fail("SortingStrategy")
    }
}

fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(seconds, nanos.toLong())
}

fun ProtoBatchingStrategy.convert(): BatchingStrategy {
    return when {
        hasDisabled() -> IsolateBatchingStrategy()
        hasFixedSize() -> FixedSizeBatchingStrategy(
            fixedSize.fixedSize,
            fixedSize.durationMillis,
            fixedSize.percentile,
            fixedSize.timeLimit.toInstant(),
            fixedSize.lastMileLength.toInt(),
        )
        else -> throw fail("BatchingStrategy")
    }
}

fun ProtoFlakinessStrategy.convert(): FlakinessStrategy {
    return when {
        hasDisabled() -> IgnoreFlakinessStrategy()
        hasProbabilityBased() -> ProbabilityBasedFlakinessStrategy(
            probabilityBased.minSuccessRate,
            probabilityBased.maxCount,
            probabilityBased.timeLimit.toInstant()
        )
        else -> throw fail("BatchingStrategy")
    }
}

fun ProtoRetryStrategy.convert(): RetryStrategy {
    return when {
        hasDisabled() -> NoRetryStrategy()
        hasFixedQuota() -> FixedQuotaRetryStrategy(fixedQuota.totalAllowedRetryQuota, fixedQuota.retryPerTestQuota)
        else -> throw fail("RetryStrategy")
    }
}

fun ProtoFilteringConfiguration.convert(): FilteringConfiguration {
    return FilteringConfiguration(
        allowlist = allowListList.map { it.convert() },
        blocklist = blockListList.map { it.convert() },
    )
}

fun ProtoTestFilter.convert(): TestFilter {
    return when {
        hasAnnotation() -> AnnotationFilter(annotation.annotation.toRegex())
        hasComposition() -> CompositionFilter(
            filters = composition.filtersList.map { it.convert() },
            op = composition.operation.convert()
        )
        hasTestMethod() -> TestMethodFilter(testMethod.method.toRegex())
        hasTestPackage() -> TestPackageFilter(testPackage.`package`.toRegex())
        hasSimpleClassname() -> SimpleClassnameFilter(simpleClassname.simpleClassname.toRegex())
        hasFullyQualifiedClassname() -> FullyQualifiedClassnameFilter(fullyQualifiedClassname.fullyQualifiedClassname.toRegex())
        else -> throw fail("TestFilter")
    }
}

fun ProtoTestFilterOperation.convert(): CompositionFilter.OPERATION {
    return when (this) {
        ProtoTestFilterOperation.UNION -> CompositionFilter.OPERATION.UNION
        ProtoTestFilterOperation.SUBTRACT -> CompositionFilter.OPERATION.SUBTRACT
        ProtoTestFilterOperation.INTERSECTION -> CompositionFilter.OPERATION.INTERSECTION
        ProtoTestFilterOperation.UNRECOGNIZED -> throw fail("CompositionFilter.OPERATION")
    }
}

fun ProtoScreenRecordingPolicy.convert(): ScreenRecordingPolicy {
    return when (this) {
        ProtoScreenRecordingPolicy.ON_ANY -> ScreenRecordingPolicy.ON_ANY
        ProtoScreenRecordingPolicy.ON_FAILURE -> ScreenRecordingPolicy.ON_FAILURE
        else -> throw fail("ScreenRecordingPolicy")
    }
}

fun ProtoVendorConfiguration.convert(): VendorConfiguration {
    return when {
        hasAndroid() -> android.convert()
        hasIos() -> ios.convert()
        else -> throw fail("VendorConfiguration")
    }
}

fun ProtoAndroidConfiguration.convert(): AndroidConfiguration {
    val implementationModules = when (vendor) {
        ADAM -> listOf(adamModule)
        DDMLIB -> listOf(ddmlibModule)
        else -> throw fail("VendorType")
    }
    with(this) {
        get({ hasAutoGrantPermission() }, { autoGrantPermission })
    }
    return AndroidConfiguration(
        androidSdk = File(androidSdk),
        applicationOutput = File(applicationApk),
        testApplicationOutput = File(testApplicationApk),
        implementationModules = implementationModules,
        autoGrantPermission = get({ hasAutoGrantPermission() }, { autoGrantPermission }),
        instrumentationArgs = this.instrumentationArgsMap,
        applicationPmClear = get({ hasApplicationPmClear() }, { applicationPmClear }),
        testApplicationPmClear = get({ hasTestApplicationPmClear() }, { testApplicationPmClear }),
        adbInitTimeoutMillis = get({ hasAdbInitTimeoutMillis() }, { adbInitTimeoutMillis }),
        installOptions = get({ hasInstallOptions() }, { installOptions }),
        serialStrategy = get({ hasSerialStrategy() }, { serialStrategy })?.convert(),
        screenRecordConfiguration = get({ hasScreenRecordConfiguration() }, { screenRecordConfiguration })?.convert(),
        waitForDevicesTimeoutMillis = get({ hasWaitForDevicesTimeoutMillis() }, { waitForDevicesTimeoutMillis }),
        allureConfiguration = get({ hasAllureConfiguration() }, { allureConfiguration })?.convert(),
        timeoutConfiguration = get({ hasTimeoutConfiguration() }, { timeoutConfiguration })?.convert(),
        fileSyncConfiguration = get({ hasFileSyncConfiguration() }, { fileSyncConfiguration })?.convert(),
        threadingConfiguration = get({ hasThreadingConfiguration() }, { threadingConfiguration })?.convert(),
    )
}

private fun ProtoAndroidConfiguration.ThreadingConfiguration.convert(): ThreadingConfiguration {
    return ThreadingConfiguration(bootWaitingThreads, adbIoThreads)
}

private fun ProtoFileSyncConfiguration.convert(): FileSyncConfiguration {
    return FileSyncConfiguration(pullList.map { it.convert() }.toMutableList())
}

private fun ProtoFileSyncConfiguration.FileSyncEntry.convert(): FileSyncEntry {
    return FileSyncEntry(relativePath, aggregationMode.convert())
}

private fun ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.convert(): AggregationMode {
    return when (this) {
        ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.DEVICE -> AggregationMode.DEVICE
        ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.DEVICE_AND_POOL -> AggregationMode.DEVICE_AND_POOL
        ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.POOL -> AggregationMode.POOL
        ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.TEST_RUN -> AggregationMode.TEST_RUN
        ProtoFileSyncConfiguration.FileSyncEntry.AggregationMode.UNRECOGNIZED -> throw fail("AggregationMode")
    }
}

private fun ProtoAllureConfiguration.convert(): AllureConfiguration {
    return AllureConfiguration(enabled, resultsDirectory)
}

private fun ProtoTimeoutConfiguration.convert(): TimeoutConfiguration {
    return TimeoutConfiguration(
        shell.convert(),
        listFiles.convert(),
        pushFile.convert(),
        pullFile.convert(),
        uninstall.convert(),
        install.convert(),
        screenRecorder.convert(),
        screenCapturer.convert()
    )
}

private fun com.google.protobuf.Duration.convert(): Duration {
    return Duration.ofSeconds(seconds, nanos.toLong())
}

private fun ProtoScreenRecordConfiguration.convert(): ScreenRecordConfiguration {
    return ScreenRecordConfiguration(
        preferableRecorderType = preferableRecorderType.convert(),
        videoConfiguration = videoConfiguration.convert(),
        screenshotConfiguration = screenshotConfiguration.convert(),
    )
}

private fun ProtoScreenshotConfiguration.convert(): ScreenshotConfiguration {
    return ScreenshotConfiguration(enabled, width, height, delayMs)
}

private fun ProtoVideoConfiguration.convert(): VideoConfiguration {
    return VideoConfiguration(enabled, width, height, bitmapMbps, duration.seconds, TimeUnit.SECONDS)
}

private fun ProtoDeviceFeature.convert(): DeviceFeature {
    return when (this) {
        ProtoDeviceFeature.SCREENSHOT -> DeviceFeature.SCREENSHOT
        ProtoDeviceFeature.VIDEO -> DeviceFeature.VIDEO
        ProtoDeviceFeature.UNRECOGNIZED -> throw fail("DeviceFeature")
    }
}

private fun ProtoSerialStrategy.convert(): SerialStrategy {
    return when (this) {
        ProtoSerialStrategy.AUTOMATIC -> SerialStrategy.AUTOMATIC
        ProtoSerialStrategy.BOOT_PROPERTY -> SerialStrategy.BOOT_PROPERTY
        ProtoSerialStrategy.DDMS -> SerialStrategy.DDMS
        ProtoSerialStrategy.HOSTNAME -> SerialStrategy.HOSTNAME
        ProtoSerialStrategy.MARATHON_PROPERTY -> SerialStrategy.MARATHON_PROPERTY
        ProtoSerialStrategy.UNRECOGNIZED -> throw fail("SerialStrategy")
    }
}

fun ProtoIosConfiguration.convert(): IOSConfiguration {
    TODO("Not implemented yet")
}


fun fail(type: String) = RuntimeException("Cannot map config. Failed to map $type")


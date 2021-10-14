package com.malinskiy.marathon.config.serialization.yaml

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.environment.EnvironmentReader
import com.malinskiy.marathon.config.serialization.time.InstantTimeProvider
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import java.io.File

class DeserializeModule(
    instantTimeProvider: InstantTimeProvider,
    environmentReader: EnvironmentReader,
    marathonfileDir: File,
    fileListProvider: FileListProvider
) :
    SimpleModule() {
    init {
        addDeserializer(AnalyticsConfiguration::class.java, AnalyticsConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.InfluxDbConfiguration::class.java, InfluxDbConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.GraphiteConfiguration::class.java, GraphiteConfigurationDeserializer())
        addDeserializer(
            AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java,
            RetentionPolicyConfigurationDeserializer()
        )
        addDeserializer(PoolingStrategyConfiguration::class.java, PoolingStrategyConfigurationDeserializer())
        addDeserializer(ShardingStrategyConfiguration::class.java, ShardingStrategyConfigurationDeserializer())
        addDeserializer(SortingStrategyConfiguration::class.java, SortingStrategyConfigurationDeserializer())
        addDeserializer(
            SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration::class.java,
            ExecutionTimeSortingStrategyConfigurationDeserializer(instantTimeProvider)
        )
        addDeserializer(BatchingStrategyConfiguration::class.java, BatchingStrategyConfigurationDeserializer())
        addDeserializer(FlakinessStrategyConfiguration::class.java, FlakinessStrategyConfigurationDeserializer())
        addDeserializer(
            FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration::class.java,
            ProbabilityBasedFlakinessStrategyConfigurationDeserializer(instantTimeProvider)
        )
        addDeserializer(
            BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration::class.java,
            FixedSizeBatchingStrategyConfigurationDeserializer(instantTimeProvider)
        )
        addDeserializer(RetryStrategyConfiguration::class.java, RetryStrategyConfigurationDeserializer())
        addDeserializer(TestFilterConfiguration::class.java, TestFilterConfigurationDeserializer())
        addDeserializer(
            VendorConfiguration::class.java,
            VendorConfigurationDeserializer(marathonfileDir, environmentReader, fileListProvider)
        )
        addDeserializer(ScreenRecordingPolicy::class.java, ScreenRecordingPolicyDeserializer())
    }
}

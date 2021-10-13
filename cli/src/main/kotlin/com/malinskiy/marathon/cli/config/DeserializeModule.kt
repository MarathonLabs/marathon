package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.cli.args.FileVendorConfiguration
import com.malinskiy.marathon.cli.config.deserialize.AnalyticsConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.BatchingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ExecutionTimeSortingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FileVendorConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FixedSizeBatchingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FlakinessStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.GraphiteConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.InfluxDbConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.PoolingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ProbabilityBasedFlakinessStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.RetentionPolicyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.RetryStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ScreenRecordingPolicyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ShardingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.SortingStrategyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.TestFilterConfigurationDeserializer
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.ScreenRecordingPolicy
import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration

class DeserializeModule(instantTimeProvider: InstantTimeProvider) : SimpleModule() {
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
        addDeserializer(FileVendorConfiguration::class.java, FileVendorConfigurationDeserializer())
        addDeserializer(ScreenRecordingPolicy::class.java, ScreenRecordingPolicyDeserializer())
    }
}

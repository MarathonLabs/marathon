package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.cli.config.deserialize.AnalyticsConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.BatchingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FlakinessStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.InfluxDbConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.PoolingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.RetryStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ShardingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.SortingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.TestFilterDeserializer
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy

class DeserializeModule: SimpleModule() {
    init {
        addDeserializer(AnalyticsConfiguration::class.java, AnalyticsConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.InfluxDbConfiguration::class.java, InfluxDbConfigurationDeserializer())
        addDeserializer(PoolingStrategy::class.java, PoolingStrategyDeserializer())
        addDeserializer(ShardingStrategy::class.java, ShardingStrategyDeserializer())
        addDeserializer(SortingStrategy::class.java, SortingStrategyDeserializer())
        addDeserializer(BatchingStrategy::class.java, BatchingStrategyDeserializer())
        addDeserializer(FlakinessStrategy::class.java, FlakinessStrategyDeserializer())
        addDeserializer(RetryStrategy::class.java, RetryStrategyDeserializer())
        addDeserializer(TestFilter::class.java, TestFilterDeserializer())
    }
}

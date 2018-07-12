package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.cli.config.deserialize.*
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.strategy.*

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
//        addDeserializer(FilteringConfiguration::class.java, FilteringConfigurationDeserializer())
        addDeserializer(TestFilter::class.java, TestFilterDeserializer())
    }
}
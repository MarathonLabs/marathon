package com.malinskiy.marathon.lite.configuration

import com.malinskiy.marathon.lite.configuration.strategies.BatchingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.FlakinessStrategy
import com.malinskiy.marathon.lite.configuration.strategies.PoolingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.RetryStrategy
import com.malinskiy.marathon.lite.configuration.strategies.ShardingStrategy
import com.malinskiy.marathon.lite.configuration.strategies.SortingStrategy

val DEFAULT_ANALYTICS_CONFIGURATION = AnalyticsConfiguration.Disabled
val DEFAULT_POOLING_STRATEGY: PoolingStrategy = PoolingStrategy.Omni
val DEFAULT_SHARDING_STRATEGY: ShardingStrategy = ShardingStrategy.Disabled
val DEFAULT_SORTING_STRATEGY: SortingStrategy = SortingStrategy.Disabled
val DEFAULT_BATCHING_STRATEGY: BatchingStrategy = BatchingStrategy.Disabled
val DEFAULT_FLAKINESS_STRATEGY: FlakinessStrategy = FlakinessStrategy.Disabled
val DEFAULT_RETRY_STRATEGY: RetryStrategy = RetryStrategy.Disabled
val DEFAULT_FILTERING_CONFIGURATION: FilteringConfiguration = FilteringConfiguration(emptyList(), emptyList())
const val DEFAULT_INIT_TIMEOUT_MILLIS = 30_000

const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false
const val DEFAULT_INSTALL_OPTIONS = ""
const val DEFAULT_WAIT_FOR_DEVICES_TIMEOUT = 30000L

const val DEFAULT_IGNORE_FAILURES = false
const val DEFAULT_IS_CODE_COVERAGE_ENABLED = false
const val DEFAULT_FALLBACK_TO_SCREENSHOTS = false
const val DEFAULT_STRICT_MODE = false
const val DEFAULT_UNCOMPLETED_TEST_RETRY_QUOTA = Integer.MAX_VALUE
val DEFAULT_INCLUDES_SERIAL_REGEXES = emptyList<String>()
val DEFAULT_EXCLUDES_SERIAL_REGEXES = emptyList<String>()
val DEFAULT_TEST_CLASS_REGEXES = listOf("^((?!Abstract).)*Test[s]*$")
const val DEFAULT_EXECUTION_TIMEOUT_MILLIS: Long = 900_000
const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 60_000
const val DEFAULT_DEBUG = true
const val DEFAULT_ANALYTICS_TRACKING = false
const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L


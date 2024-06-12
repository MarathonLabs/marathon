package com.malinskiy.marathon.extension

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.FlakinessStrategyConfiguration
import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.config.strategy.ShardingStrategyConfiguration
import com.malinskiy.marathon.config.strategy.SortingStrategyConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.filter.AllureTestFilter
import com.malinskiy.marathon.execution.filter.AnnotationDataFilter
import com.malinskiy.marathon.execution.filter.AnnotationFilter
import com.malinskiy.marathon.execution.filter.CompositionFilter
import com.malinskiy.marathon.execution.filter.FragmentationFilter
import com.malinskiy.marathon.execution.filter.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.filter.FullyQualifiedTestnameFilter
import com.malinskiy.marathon.execution.filter.SimpleClassnameFilter
import com.malinskiy.marathon.execution.filter.SimpleTestnameFilter
import com.malinskiy.marathon.execution.filter.TestMethodFilter
import com.malinskiy.marathon.execution.filter.TestPackageFilter
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.ClassNameBatchingStrategy
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
import com.malinskiy.marathon.execution.strategy.impl.sorting.RandomOrderSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy

fun ShardingStrategyConfiguration.toShardingStrategy(): ShardingStrategy {
    return when (this) {
        is ShardingStrategyConfiguration.CountShardingStrategyConfiguration -> CountShardingStrategy(cnf = this)
        ShardingStrategyConfiguration.ParallelShardingStrategyConfiguration -> ParallelShardingStrategy()
    }
}

fun FlakinessStrategyConfiguration.toFlakinessStrategy(): FlakinessStrategy {
    return when (this) {
        FlakinessStrategyConfiguration.IgnoreFlakinessStrategyConfiguration -> IgnoreFlakinessStrategy()
        is FlakinessStrategyConfiguration.ProbabilityBasedFlakinessStrategyConfiguration -> ProbabilityBasedFlakinessStrategy(this)
    }
}

fun TestFilterConfiguration.toTestFilter(): TestFilter {
    return when (this) {
        is TestFilterConfiguration.AnnotationDataFilterConfiguration -> AnnotationDataFilter(this)
        is TestFilterConfiguration.AnnotationFilterConfiguration -> AnnotationFilter(this)
        is TestFilterConfiguration.CompositionFilterConfiguration -> CompositionFilter(this.filters.map { it.toTestFilter() }, this.op, this.enabled)
        is TestFilterConfiguration.FragmentationFilterConfiguration -> FragmentationFilter(this)
        is TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration -> FullyQualifiedClassnameFilter(this)
        is TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration -> FullyQualifiedTestnameFilter(this)
        is TestFilterConfiguration.SimpleClassnameFilterConfiguration -> SimpleClassnameFilter(this)
        is TestFilterConfiguration.SimpleTestnameFilterConfiguration -> SimpleTestnameFilter(this)
        is TestFilterConfiguration.TestMethodFilterConfiguration -> TestMethodFilter(this)
        is TestFilterConfiguration.TestPackageFilterConfiguration -> TestPackageFilter(this)
        is TestFilterConfiguration.AllureFilterConfiguration -> AllureTestFilter(this)
    }
}

fun PoolingStrategyConfiguration.toPoolingStrategy(): PoolingStrategy {
    return when (this) {
        PoolingStrategyConfiguration.AbiPoolingStrategyConfiguration -> AbiPoolingStrategy()
        is PoolingStrategyConfiguration.ComboPoolingStrategyConfiguration -> ComboPoolingStrategy(this.list.map { it.toPoolingStrategy() })
        PoolingStrategyConfiguration.ManufacturerPoolingStrategyConfiguration -> ManufacturerPoolingStrategy()
        PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration -> ModelPoolingStrategy()
        PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration -> OmniPoolingStrategy()
        PoolingStrategyConfiguration.OperatingSystemVersionPoolingStrategyConfiguration -> OperatingSystemVersionPoolingStrategy()
    }
}

fun SortingStrategyConfiguration.toSortingStrategy(): SortingStrategy {
    return when (this) {
        is SortingStrategyConfiguration.ExecutionTimeSortingStrategyConfiguration -> ExecutionTimeSortingStrategy(this)
        SortingStrategyConfiguration.NoSortingStrategyConfiguration -> NoSortingStrategy()
        SortingStrategyConfiguration.RandomOrderStrategyConfiguration -> RandomOrderSortingStrategy()
        is SortingStrategyConfiguration.SuccessRateSortingStrategyConfiguration -> SuccessRateSortingStrategy(this)
    }
}

fun RetryStrategyConfiguration.toRetryStrategy(): RetryStrategy {
    return when (this) {
        is RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration -> FixedQuotaRetryStrategy(this)
        RetryStrategyConfiguration.NoRetryStrategyConfiguration -> NoRetryStrategy()
    }
}

fun BatchingStrategyConfiguration.toBatchingStrategy(): BatchingStrategy {
    return when (this) {
        is BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration -> FixedSizeBatchingStrategy(this)
        is BatchingStrategyConfiguration.ClassNameBatchingStrategyConfiguration -> ClassNameBatchingStrategy()
        BatchingStrategyConfiguration.IsolateBatchingStrategyConfiguration -> IsolateBatchingStrategy()
    }
}

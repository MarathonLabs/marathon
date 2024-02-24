package com.malinskiy.marathon.analytics.external

import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.Instant

private data class MeasurementKey(val key: Double = Double.NaN, val limit: Instant)

private class MeasurementValues(private val values: Map<String, Double>) {
    fun get(testName: String): Double? {
        return values[testName]
    }
}

private const val RETRY_DELAY: Long = 50L


class MetricsProviderImpl(
    private val remoteDataStore: RemoteDataSource,
    private val defaultSuccessRate: Double,
    private val defaultDuration: Duration
) : MetricsProvider {

    private val logger = MarathonLogging.logger(MetricsProviderImpl::class.java.simpleName)

    private val successRateMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()
    private val executionTimeMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()

    private fun emptyMeasurementValues() = MeasurementValues(emptyMap())

    override fun successRate(test: Test, limit: Instant): Double {
        val key = MeasurementKey(limit = limit)

        if (!successRateMeasurements.containsKey(key)) {
            successRateMeasurements[key] = runCatching {
                fetchSuccessRateData(limit)
            }.onFailure {
                logger.warn(it) { "Cannot fetch success rate from database" }
            }.fold({ list ->
                       MeasurementValues(list.associateBy({ it.testName }, { it.mean }))
                   }, {
                       emptyMeasurementValues()
                   })
        }

        val testName = test.toSafeTestName()
        return successRateMeasurements[key]?.get(testName) ?: run {
            if (!warningSuccessRateTimeReported.contains(testName)) {
                logger.warn { "No success rate found for $testName. Using $defaultSuccessRate" }
                warningSuccessRateTimeReported.add(testName)
            }
            defaultSuccessRate
        }
    }

    private fun fetchSuccessRateData(limit: Instant) = runBlocking {
        withRetry(3, RETRY_DELAY) {
            remoteDataStore.requestAllSuccessRates(limit)
        }
    }


    override fun executionTime(
        test: Test,
        percentile: Double,
        limit: Instant
    ): Double {
        val key = MeasurementKey(percentile, limit)
        if (!executionTimeMeasurements.containsKey(key)) {
            executionTimeMeasurements[key] = runCatching {
                fetchExecutionTime(percentile, limit)
            }.onFailure {
                logger.warn(it) { "Cannot fetch execution time from database" }
            }.fold({ list ->
                       logger.warn { list }
                       MeasurementValues(list.associateBy({ it.testName }, { it.percentile }))
                   }, {
                       emptyMeasurementValues()
                   })
        }
        val testName = test.toSafeTestName()
        return executionTimeMeasurements[key]?.get(testName) ?: run {
            val default = defaultDuration.toMillis()
            if (!warningExecutionTimeReported.contains(testName)) {
                logger.warn { "No execution time found for $testName. Using $default seconds" }
                warningExecutionTimeReported.add(testName)
            }
            default.toDouble()
        }
    }

    private val warningExecutionTimeReported = hashSetOf<String>()
    private val warningSuccessRateTimeReported = hashSetOf<String>()

    private fun fetchExecutionTime(percentile: Double, limit: Instant) = runBlocking {
        withRetry(3, RETRY_DELAY) {
            remoteDataStore.requestAllExecutionTimes(percentile, limit)
        }
    }

    override fun close() {
        remoteDataStore.close()
    }
}


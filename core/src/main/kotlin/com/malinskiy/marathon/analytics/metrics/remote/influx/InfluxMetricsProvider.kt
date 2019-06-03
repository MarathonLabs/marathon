package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.tracker.remote.influx.InfluxDbProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import kotlinx.coroutines.runBlocking
import java.time.Instant

private data class MeasurementKey(val key: Double = Double.NaN, val limit: Instant)

private class MeasurementValues(private val values: Map<String, Double>) {
    fun get(testName: String): Double? {
        return values[testName]
    }
}

private const val RETRY_DELAY: Long = 50L


class InfluxMetricsProvider(private val remoteDataStore: RemoteDataSource) : MetricsProvider {

    private val logger = MarathonLogging.logger(InfluxMetricsProvider::class.java.simpleName)

    private val successRateMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()
    private val executionTimeMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()

    private fun emptyMeasurementValues() = MeasurementValues(emptyMap())

    override fun successRate(test: Test, limit: Instant): Double {
        val key = MeasurementKey(limit = limit)

        if (!successRateMeasurements.containsKey(key)) {
            successRateMeasurements[key] = runCatching {
                fetchSuccessRateData(limit)
            }.onFailure {
                logger.warn { "Cannot fetch success rate from database" }
            }.fold({ list ->
                MeasurementValues(list.associateBy({ it.testName }, { it.mean }))
            }, {
                emptyMeasurementValues()
            })
        }

        val testName = test.toSafeTestName()
        return successRateMeasurements[key]?.get(testName) ?: {
            logger.warn { "No success rate found for $testName. Using 0 i.e. fails all the time" }
            0.0
        }()
    }

    private fun fetchSuccessRateData(limit: Instant) = runBlocking {
        withRetry(3, RETRY_DELAY) {
            remoteDataStore.requestAllSuccessRates(limit)
        }
    }


    override fun executionTime(test: Test,
                               percentile: Double,
                               limit: Instant): Double {
        val key = MeasurementKey(percentile, limit)
        if (!executionTimeMeasurements.containsKey(key)) {
            executionTimeMeasurements[key] = runCatching {
                fetchExecutionTime(percentile, limit)
            }.onFailure {
                logger.warn { "Cannot fetch execution time from database" }
            }.fold({ list ->
                logger.warn { list }
                MeasurementValues(list.associateBy({ it.testName }, { it.percentile }))
            }, {
                emptyMeasurementValues()
            })
        }
        val testName = test.toSafeTestName()
        return executionTimeMeasurements[key]?.get(testName) ?: {
            logger.warn { "No execution time found for $testName. Using 300_000 seconds i.e. long test" }
            300_000.0
        }()
    }

    private fun fetchExecutionTime(percentile: Double, limit: Instant) = runBlocking {
        withRetry(3, RETRY_DELAY) {
            remoteDataStore.requestAllExecutionTimes(percentile, limit)
        }
    }

    override fun close() {
        remoteDataStore.close()
    }

    companion object {
        fun createWithFallback(configuration: AnalyticsConfiguration.InfluxDbConfiguration, fallback: MetricsProvider): MetricsProvider {
            return try {
                val db = InfluxDbProvider(configuration).createDb()
                val dataSource = InfluxDBDataSource(db, configuration.dbName)
                InfluxMetricsProvider(dataSource)
            } catch (e: Exception) {
                fallback
            }
        }
    }
}


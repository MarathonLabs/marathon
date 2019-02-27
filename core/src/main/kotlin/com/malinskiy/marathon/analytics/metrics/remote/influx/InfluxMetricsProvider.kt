package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.analytics.tracker.remote.influx.InfluxDbProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import com.sun.org.apache.xpath.internal.operations.Bool
import org.influxdb.InfluxDB
import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement
import org.influxdb.dto.Query
import org.influxdb.impl.InfluxDBResultMapper
import java.time.Instant

@Measurement(name = "tests")
class ExecutionTime(@Column(name = "testname", tag = true) var testName: String? = null,
                    @Column(name = "percentile") var percentile: Double? = null)

@Measurement(name = "tests")
class SuccessRate(@Column(name = "testname", tag = true) var testName: String? = null,
                  @Column(name = "mean") var mean: Double? = null)

private data class MeasurementKey(val key: Double = Double.NaN, val limit: Instant)
private class MeasurementValues(pairs: List<Pair<String, Double>>) {
    private val values = mutableMapOf<String, Double>()
    init {
        values.putAll(pairs = pairs)
    }
    fun get(testname: String): Double? {
        return values[testname]
    }
}

class InfluxMetricsProvider(private val influxDb: InfluxDB,
                            private val dbName: String) : MetricsProvider {

    private val logger = MarathonLogging.logger(InfluxMetricsProvider::class.java.simpleName)
    private val mapper = InfluxDBResultMapper()

    private val successRateMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()
    private val executionTimeMeasurements = mutableMapOf<MeasurementKey, MeasurementValues>()

    private var successRateInitialized = false

    override fun successRate(test: Test, limit: Instant): Double {
        val key = MeasurementKey(limit = limit)

        var measurements = successRateMeasurements[key]
        if (measurements == null) {
            maybeQuery { requestAllSuccessRates(limit) }
                    ?.let { result ->
                        measurements = MeasurementValues(pairs = result.mapNotNull { it.toPairOrNull() })
                                .also { successRateMeasurements[key] = it }
                    }
        }

        val successRate = measurements?.get(test.toSafeTestName())
        return if (successRate == null) {
            logger.warn { "No success rate found for ${test.toSafeTestName()}. Using 0 i.e. fails all the time" }
            0.0
        } else {
            successRate
        }
    }

    private fun requestAllSuccessRates(limit: Instant): List<SuccessRate> {
        val results = influxDb.query(Query("""
            SELECT MEAN("success")
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        return mapper.toPOJO(results, SuccessRate::class.java)
    }

    override fun executionTime(test: Test,
                               percentile: Double,
                               limit: Instant): Double {
        val key = MeasurementKey(percentile, limit)
        var measurements = executionTimeMeasurements[key]
        if (measurements == null) {
            maybeQuery { requestAllExecutionTimes(percentile, limit) }
                ?.let { result ->
                    measurements = MeasurementValues(pairs = result.mapNotNull { it.toPairOrNull() })
                            .also { executionTimeMeasurements[key] = it }
                }
        }

        val executionTime = measurements?.get(test.toSafeTestName())
        return if (executionTime == null) {
            logger.warn { "No execution time found for ${test.toSafeTestName()}. Using 300_000 seconds i.e. long test" }
            300_000.0
        } else {
            executionTime
        }
    }

    private fun requestAllExecutionTimes(percentile: Double,
                                         limit: Instant): List<ExecutionTime> {
        val results = influxDb.query(Query("""
            SELECT PERCENTILE("duration",$percentile)
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        return mapper.toPOJO(results, ExecutionTime::class.java)
    }

    override fun close() {
        influxDb.close()
    }

    companion object {
        fun createWithFallback(configuration: AnalyticsConfiguration.InfluxDbConfiguration, fallback: MetricsProvider): MetricsProvider {
            return try {
                val db = InfluxDbProvider(configuration).createDb()
                InfluxMetricsProvider(db, configuration.dbName)
            } catch (e: Exception) {
                fallback
            }
        }
    }
}

private fun <R> maybeQuery(query: () -> R): R? = QueryCounter.maybeQuery(query)

private object QueryCounter {
    private const val MAX_QUERY_RETRY_COUNT = 8
    private var failureCount: Int = 0
    val shouldRetryFailedQuery: Boolean
        get() = failureCount < MAX_QUERY_RETRY_COUNT
    fun queryDidFail() {
        failureCount++
    }
    fun <R> maybeQuery(query: () -> R): R? {
        if (!QueryCounter.shouldRetryFailedQuery) {
            return null
        }
        val result = runCatching {
            query()
        }
        result.exceptionOrNull()?.let { e ->
            val logger = MarathonLogging.logger(InfluxMetricsProvider::class.java.simpleName)
            logger.warn("Caught an exception querying data: $e")
            QueryCounter.queryDidFail()
        }
        return result.getOrNull()
    }
}

private fun ExecutionTime.toPairOrNull(): Pair<String, Double>? {
    val testName = this.testName
    val percentile = this.percentile
    return when {
        testName != null && percentile != null -> testName to percentile
        else -> null
    }
}

private fun SuccessRate.toPairOrNull(): Pair<String, Double>? {
    val testName = this.testName
    val mean = this.mean
    return when {
        testName != null && mean != null -> testName to mean
        else -> null
    }
}
package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.analytics.tracker.remote.influx.InfluxDbProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBException
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

private const val MAX_QUERY_RETRIES = 8

class InfluxMetricsProvider(private val influxDb: InfluxDB,
                            private val dbName: String) : MetricsProvider {

    private val logger = MarathonLogging.logger(InfluxMetricsProvider::class.java.simpleName)
    private val mapper = InfluxDBResultMapper()

    private val successRate = mutableMapOf<String, Double>()
    private val executionTime = mutableMapOf<String, Double>()

    override fun successRate(test: Test, limit: Instant): Double {
        requestAllSuccessRatesIfNeeded(limit)

        val successRate = successRate[test.toSafeTestName()]
        return if (successRate == null) {
            logger.warn { "No success rate found for ${test.toSafeTestName()}. Using 0 i.e. fails all the time" }
            0.0
        } else {
            successRate
        }
    }

    private var successRateInitialized = false
    private var successRateQueryCount = 0
    private fun requestAllSuccessRatesIfNeeded(limit: Instant) {
        if (successRateInitialized) {
            return
        }

        if (successRateQueryCount == MAX_QUERY_RETRIES) {
            successRateInitialized = true
            return
        }

        val successRates = requestAllSuccessRates(limit)
        if (successRates == null) {
            successRateQueryCount++
            return
        }

        for (rate in successRates) {
            val testName = rate.testName
            val mean = rate.mean
            if (testName != null && mean != null) {
                successRate[testName] = mean
            }
        }
        successRateInitialized = true
    }

    private fun requestAllSuccessRates(limit: Instant): List<SuccessRate>? = try {
        val results = influxDb.query(Query("""
            SELECT MEAN("success")
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        mapper.toPOJO(results, SuccessRate::class.java)
    } catch (e: InfluxDBException) {
        logger.warn("Exception querying success rates: $e")
        null
    }

    private var executionTimeInitialized = false
    private var executionTimeQueryCount = 0
    override fun executionTime(test: Test,
                               percentile: Double,
                               limit: Instant): Double {
        requestAllExecutionTimesIfNeeded(percentile, limit)

        val executionTime = executionTime[test.toSafeTestName()]
        return if (executionTime == null) {
            logger.warn { "No execution time found for ${test.toSafeTestName()}. Using 300_000 seconds i.e. long test" }
            300_000.0
        } else {
            executionTime
        }
    }

    private fun requestAllExecutionTimesIfNeeded(percentile: Double, limit: Instant) {
        if (executionTimeInitialized) {
            return
        }

        if (executionTimeQueryCount >= MAX_QUERY_RETRIES) {
            executionTimeInitialized = true
            return
        }

        val executionTimes = requestAllExecutionTimes(percentile, limit)
        if (executionTimes == null) {
            executionTimeQueryCount++
            return
        }

        for (time in executionTimes) {
            val testName = time.testName
            val percentile = time.percentile
            if (testName != null && percentile != null) {
                executionTime[testName] = percentile
            }
        }
        executionTimeInitialized = true
    }

    private fun requestAllExecutionTimes(percentile: Double,
                                         limit: Instant): List<ExecutionTime>? = try {
        val results = influxDb.query(Query("""
            SELECT PERCENTILE("duration",$percentile)
            FROM "tests"
            WHERE time >= '$limit'
            GROUP BY "testname"
        """, dbName))
        mapper.toPOJO(results, ExecutionTime::class.java)
    } catch (e: InfluxDBException) {
        logger.warn("Exception querying execution times")
        logger.warn("$e")
        null
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

package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.metrics.MetricsProvider
import com.malinskiy.marathon.analytics.tracker.remote.influx.InfluxDbProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBIOException
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

class InfluxMetricsProvider(private val influxDb: InfluxDB,
                            private val dbName: String) : MetricsProvider {

    private val logger = MarathonLogging.logger(InfluxMetricsProvider::class.java.simpleName)
    private val mapper = InfluxDBResultMapper()

    private val successRate = mutableMapOf<String, Double>()
    private val executionTime = mutableMapOf<String, Double>()

    private var successRateInitialized = false

    override fun successRate(test: Test, limit: Instant): Double {
        if (!successRateInitialized) {
            successRateInitialized = safeInfluxCall {
                requestAllSuccessRates(limit).forEach {
                    val testName = it.testName
                    val mean = it.mean
                    if (testName != null && mean != null) {
                        successRate[testName] = mean
                    }
                }
            }
        }

        val successRate = successRate[test.toSafeTestName()]
        return if (successRate == null) {
            logger.warn { "No success rate found for ${test.toSafeTestName()}. Using 0 i.e. fails all the time" }
            0.0
        } else {
            successRate
        }
    }

    /**
     * @return true if successfully executed
     */
    private fun safeInfluxCall(block: () -> Unit): Boolean {
        return try {
            block.invoke()
            true
        } catch (e: InfluxDBIOException) {
            false
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

    var executionTimeInitialized = false

    override fun executionTime(test: Test,
                               percentile: Double,
                               limit: Instant): Double {
        if (!executionTimeInitialized) {
            executionTimeInitialized = safeInfluxCall {
                requestAllExecutionTimes(percentile, limit).forEach {
                    executionTime[it.testName!!] = it.percentile!!
                }
            }
        }

        val executionTime = executionTime[test.toSafeTestName()]
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

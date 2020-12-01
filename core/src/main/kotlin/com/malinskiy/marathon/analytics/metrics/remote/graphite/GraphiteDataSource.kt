package com.malinskiy.marathon.analytics.metrics.remote.graphite

import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import com.malinskiy.marathon.extension.withPrefix
import java.time.Instant

class GraphiteDataSource(
    private val graphite: QueryableGraphiteClient,
    private val prefix: String?
) : RemoteDataSource {

    override fun requestAllSuccessRates(limit: Instant): List<SuccessRate> {
        val path = "tests.*.*.*.*.*.success".withPrefix(prefix)
        val testNameNodeNum = 1.adjustNodeNum(prefix)
        val lines = graphite.query(
            """
            aliasByNode(
                asPercent(
                    summarize($path, '10years', 'sum', true),
                    summarize($path, '10years', 'count', true)
                ),
                $testNameNodeNum
            )
            """.replace("\\s".toRegex(), ""),
            limit
        )
        return lines.map { line ->
            val testName = line.substringBefore(',').decodeTestName()
            val mean = line.substringAfter('|').toDouble() / 100
            SuccessRate(testName, mean)
        }
    }

    override fun requestAllExecutionTimes(percentile: Double, limit: Instant): List<ExecutionTime> {
        val path = "tests.*.*.*.*.*.duration".withPrefix(prefix)
        val testNameNodeNum = 1.adjustNodeNum(prefix)
        val lines = graphite.query(
            "aliasByNode(summarize(nPercentile($path, $percentile), '10years', 'average', true), $testNameNodeNum)",
            limit
        )
        return lines.map { line ->
            val testName = line.substringBefore(',').decodeTestName()
            val duration = line.substringAfter('|').toDouble()
            ExecutionTime(testName, duration)
        }
    }

    override fun close() {
        // nop
    }

    private fun Int.adjustNodeNum(prefix: String?): Int {
        return this + if (prefix.isNullOrEmpty()) 0 else prefix.count { it == '.' } + 1
    }

    private fun String.decodeTestName(): String = replace("--", ".")
}

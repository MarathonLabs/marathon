package com.malinskiy.marathon.analytics.metrics.remote.graphite

import com.malinskiy.marathon.analytics.external.graphite.withPrefix
import com.malinskiy.marathon.analytics.metrics.remote.ExecutionTime
import com.malinskiy.marathon.analytics.metrics.remote.RemoteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.SuccessRate
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class GraphiteDataSource(
    private val host: String,
    private val prefix: String?
) : RemoteDataSource {

    private val fromFormatter = DateTimeFormatter.ofPattern("HH:mm_yyyyMMdd").withZone(ZoneId.systemDefault())

    override fun requestAllSuccessRates(limit: Instant): List<SuccessRate> {
        val path = "tests.*.*.*.*.success".withPrefix(prefix)
        val testNameNodeNum = 1.adjustNodeNum(prefix)
        val lines = query(
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
        val path = "tests.*.*.*.*.duration".withPrefix(prefix)
        val testNameNodeNum = 1.adjustNodeNum(prefix)
        val lines = query(
            "aliasByNode(summarize(nPercentile($path, $percentile), '10years', 'average', true), $testNameNodeNum)",
            limit
        )
        return lines.map { line ->
            val testName = line.substringBefore(',').decodeTestName()
            val duration = line.substringAfter('|').toDouble()
            ExecutionTime(testName, duration)
        }
    }

    private fun query(target: String, from: Instant): List<String> {
        val encodedTarget = URLEncoder.encode(target, StandardCharsets.UTF_8.name())
        val formattedFrom = fromFormatter.format(from)
        val url = URL("http://${host}/render?target=$encodedTarget&format=raw&from=$formattedFrom")
        val connection = url.openConnection() as HttpURLConnection
        val scanner = Scanner(connection.inputStream, StandardCharsets.UTF_8.name())
        val lines = mutableListOf<String>()
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine())
        }
        return lines
    }

    override fun close() {
        // nop
    }

    private fun Int.adjustNodeNum(prefix: String?): Int {
        return this + if (prefix.isNullOrEmpty()) 0 else prefix.count { it == '.' } + 1
    }

    private fun String.decodeTestName(): String = replace("--", ".")
}

package com.malinskiy.marathon.analytics.external.graphite

import com.malinskiy.marathon.execution.AnalyticsConfiguration.GraphiteConfiguration
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException
import java.time.Instant

data class GraphiteMetric(
    val metricName: String,
    val metricValue: String,
    val timestamp: Long = Instant.now().toEpochMilli()
)

interface GraphiteClient {
    fun send(graphiteMetric: GraphiteMetric)
    fun send(graphiteMetrics: Collection<GraphiteMetric>)
}

interface DataSender {
    fun sendData(host: String = "", port: Int = 0, callback: (PrintWriter) -> Unit = {})
}

class SocketSender : DataSender {
    override fun sendData(host: String, port: Int, callback: (PrintWriter) -> Unit) {
        Socket(host, port).getOutputStream().use {
            PrintWriter(it, true).use { callback(it) }
        }
    }
}

class BasicGraphiteClient(
    private val configuration: GraphiteConfiguration,
    private val dataSender: DataSender = SocketSender()
): GraphiteClient {
    override fun send(graphiteMetric: GraphiteMetric) {
        sender { writer ->
            val message = convertToSting(graphiteMetric)
            writer.print(message)
        }
    }

    override fun send(graphiteMetrics: Collection<GraphiteMetric>) {
        if (graphiteMetrics.isNotEmpty()) {
            sender { writer -> graphiteMetrics
                .map {
                    val modified = convertToSting(it)
                    modified
                }
                .forEach { writer.print(it) }
            }
        }
    }

    private fun sender(callback: (PrintWriter) -> Unit) {
        try {
            dataSender.sendData(configuration.host, configuration.port, callback)
        } catch (e: UnknownHostException) {
            throw GraphiteException("Unknown host: ${configuration.host}")
        } catch (e: IOException) {
            throw GraphiteException("Error while writing data to graphite: ${e.message}")
        }
    }

    private fun convertToSting(metric: GraphiteMetric) =
        "${metric.metricName.withPrefix(configuration.prefix)} ${metric.metricValue} ${metric.timestamp / 1000}\n"
}

class GraphiteException(message: String) : Exception(message)

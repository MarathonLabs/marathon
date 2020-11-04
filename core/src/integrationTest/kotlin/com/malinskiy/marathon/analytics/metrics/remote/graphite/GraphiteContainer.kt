package com.malinskiy.marathon.analytics.metrics.remote.graphite

import org.testcontainers.containers.GenericContainer

class GraphiteContainer : GenericContainer<GraphiteContainer>("graphiteapp/graphite-statsd:1.1.7-6")

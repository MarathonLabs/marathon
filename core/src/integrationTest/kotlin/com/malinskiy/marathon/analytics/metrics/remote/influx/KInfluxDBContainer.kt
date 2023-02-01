package com.malinskiy.marathon.analytics.metrics.remote.influx

import org.testcontainers.containers.InfluxDBContainer

typealias InfluxV1Container = InfluxDBContainer<KInfluxDBContainer>

class KInfluxDBContainer : InfluxDBContainer<KInfluxDBContainer>()

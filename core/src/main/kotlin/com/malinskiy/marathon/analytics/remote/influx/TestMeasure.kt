package com.malinskiy.marathon.analytics.remote.influx

import org.influxdb.annotation.Column
import org.influxdb.annotation.Measurement
import java.time.Instant


@Measurement(name = "test")
class TestMeasure {
    @Column(name = "testname")
    val name: String? = null
    @Column(name = "time")
    val time: Instant? = null
    @Column(name = "success")
    val success: Int? = null
}
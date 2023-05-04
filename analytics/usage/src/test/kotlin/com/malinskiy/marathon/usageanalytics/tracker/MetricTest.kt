package com.malinskiy.marathon.usageanalytics.tracker

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test


class MetricTest {

    @Test
    fun toJson() {
        val metric = Metric(
            name = "testing.count",
            interval = 1,
            value = 42,
            time = 1000,
            tags = mapOf("id" to "123")
        )
        metric.toJson().`should be equal to`("""{"name":"testing.count","interval":1,"value":42,"time":1000,"tags":["id=123"]}""")
    }
}

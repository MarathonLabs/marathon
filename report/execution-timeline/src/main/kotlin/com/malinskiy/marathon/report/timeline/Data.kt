package com.malinskiy.marathon.report.timeline

data class Data(
    val testName: String,
    val metricType: MetricType,
    val startDate: Long,
    val endDate: Long,
    val expectedValue: Double,
    val variance: Double,
    /** Test batch that produced this bar; null for non-test bars (device init / prepare). */
    val batchId: String? = null,
    /** 0-based attempt index within the logical test; null for non-test bars. */
    val attemptIndex: Int? = null,
    /**
     * Pool that owns this test. Populated only for test bars. Combined with
     * [testFilename] + measure device serial, the UI can construct the same
     * relative href the pool list uses to jump to the test detail page.
     */
    val poolId: String? = null,
    /**
     * File name of the emitted test HTML (e.g. `Foo.method.html`) — mirrors
     * what `HtmlSummaryReporter` writes for the pool row. Null on non-test bars.
     */
    val testFilename: String? = null,
    /** Serialized safe device id (escape()'d) so the URL construction is a pure join. */
    val deviceSerial: String? = null,
)

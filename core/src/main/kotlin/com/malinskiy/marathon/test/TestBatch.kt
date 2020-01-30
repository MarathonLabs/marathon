package com.malinskiy.marathon.test

data class TestBatch(
    val tests: List<Test>,
    val maxExpectedTestDurationMs: Long = 0,
    val expectedBatchDurationMs: Long = 0
)

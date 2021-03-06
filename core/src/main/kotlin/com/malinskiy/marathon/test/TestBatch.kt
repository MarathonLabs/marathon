package com.malinskiy.marathon.test

import java.util.*

data class TestBatch(val tests: List<Test>, val id: String = UUID.randomUUID().toString())

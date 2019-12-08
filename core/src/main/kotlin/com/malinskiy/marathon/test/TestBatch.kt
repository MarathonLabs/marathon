package com.malinskiy.marathon.test

import java.util.UUID

data class TestBatch(val tests: List<Test>,  val id: String = UUID.randomUUID().toString())

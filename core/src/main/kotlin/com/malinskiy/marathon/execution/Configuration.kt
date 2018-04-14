package com.malinskiy.marathon.execution

data class Configuration @JvmOverloads constructor(val name: String,
                         var test: String = "",
                         var tests: String = "")
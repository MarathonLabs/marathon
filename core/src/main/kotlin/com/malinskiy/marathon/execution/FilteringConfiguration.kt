package com.malinskiy.marathon.execution

data class FilteringConfiguration(val whitelist: Collection<TestFilter>, val blacklist: Collection<TestFilter>)
package com.malinskiy.marathon

import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.TestFilter
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    var whitelist: Collection<TestFilter> = mutableListOf()
    var blacklist: Collection<TestFilter> = mutableListOf()

    fun whitelist(closure: Closure<*>) {
        closure.delegate = whitelist
        closure.call()
    }

    fun blacklist(closure: Closure<*>) {
        closure.delegate = blacklist
        closure.call()
    }
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    return FilteringConfiguration(whitelist, blacklist)
}

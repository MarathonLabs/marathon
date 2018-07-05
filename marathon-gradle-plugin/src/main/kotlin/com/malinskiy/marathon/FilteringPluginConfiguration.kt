package com.malinskiy.marathon

import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.TestFilter
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    var whitelist: MutableCollection<TestFilter> = mutableListOf()
    var blacklist: MutableCollection<TestFilter> = mutableListOf()

    fun whitelist(closure: Closure<*>) {
        closure.delegate = whitelist
        closure.call()
    }

    fun blacklist(closure: Closure<*>) {
        closure.delegate = blacklist
        closure.call()
    }

    fun whitelist(block: MutableCollection<TestFilter>.() -> Unit) {
        whitelist.also(block)
    }

    fun blacklist(block: MutableCollection<TestFilter>.() -> Unit) {
        blacklist.also(block)
    }
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    return FilteringConfiguration(whitelist, blacklist)
}

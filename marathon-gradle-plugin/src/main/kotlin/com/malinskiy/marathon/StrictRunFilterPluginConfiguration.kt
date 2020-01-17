package com.malinskiy.marathon

import com.malinskiy.marathon.execution.StrictRunFilterConfiguration
import com.malinskiy.marathon.execution.TestFilter
import groovy.lang.Closure

open class StrictRunFilterPluginConfiguration {
    //groovy
    var groovyFilter: FilterWrapper? = null
    var runs: Int = 1

    fun filter(closure: Closure<*>) {
        groovyFilter = FilterWrapper()
        closure.delegate = groovyFilter
        closure.call()
    }

    //kts
    var filter: MutableCollection<TestFilter> = mutableListOf()
    fun filter(block: MutableCollection<TestFilter>.() -> Unit) {
        filter.also(block)
    }
}

fun StrictRunFilterPluginConfiguration.toStrictRunFilterConfiguration(): StrictRunFilterConfiguration {
    if (groovyFilter != null) {
        val filter = groovyFilter?.toList() ?: emptyList()
        return StrictRunFilterConfiguration(filter, runs)
    }
    return StrictRunFilterConfiguration(filter, runs)
}

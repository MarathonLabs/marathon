package com.malinskiy.marathon

import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.TestFilter
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    //groovy
    var groovyWhiteList: FilterWrapper? = null
    var groovyBlackList: FilterWrapper? = null

    fun whitelist(closure: Closure<*>) {
        groovyWhiteList = FilterWrapper()
        closure.delegate = groovyWhiteList
        closure.call()
    }

    fun blacklist(closure: Closure<*>) {
        groovyBlackList = FilterWrapper()
        closure.delegate = groovyBlackList
        closure.call()
    }

    //kts
    var whitelist: MutableCollection<TestFilter> = mutableListOf()
    var blacklist: MutableCollection<TestFilter> = mutableListOf()
    fun whitelist(block: MutableCollection<TestFilter>.() -> Unit) {
        whitelist.also(block)
    }

    fun blacklist(block: MutableCollection<TestFilter>.() -> Unit) {
        blacklist.also(block)
    }
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    if (groovyWhiteList != null || groovyBlackList != null) {
        val white = groovyWhiteList?.toList() ?: emptyList()

        val black = groovyBlackList?.toList() ?: emptyList()
        return FilteringConfiguration(white, black)
    }
    return FilteringConfiguration(whitelist, blacklist)
}

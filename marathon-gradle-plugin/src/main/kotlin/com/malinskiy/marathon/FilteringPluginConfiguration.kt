package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    //groovy
    var groovyWhiteList: Wrapper? = null
    var groovyBlackList: Wrapper? = null

    fun whitelist(closure: Closure<*>) {
        groovyWhiteList = Wrapper()
        closure.delegate = groovyWhiteList
        closure.call()
    }

    fun blacklist(closure: Closure<*>) {
        groovyBlackList = Wrapper()
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

open class Wrapper {
    open var simpleClassNameFilter: ArrayList<String>? = null
    open var fullyQualifiedClassnameFilter: ArrayList<String>? = null
    open var testPackageFilter: ArrayList<String>? = null
    open var annotationFilter: ArrayList<String>? = null
}

fun Wrapper.toList(): List<TestFilter> {
    val mutableList = mutableListOf<TestFilter>()
    this.annotationFilter?.map { AnnotationFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.fullyQualifiedClassnameFilter?.map { FullyQualifiedClassnameFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.testPackageFilter?.map { TestPackageFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.simpleClassNameFilter?.map { SimpleClassnameFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    return mutableList
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    if (groovyWhiteList != null || groovyBlackList != null) {
        val white = groovyWhiteList?.toList() ?: emptyList()

        val black = groovyBlackList?.toList() ?: emptyList()
        return FilteringConfiguration(white, black)
    }
    return FilteringConfiguration(whitelist, blacklist)
}


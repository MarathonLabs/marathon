package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnnotationDataFilter
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.FilteringConfiguration
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import org.gradle.api.Action
import java.io.Serializable

open class FilteringPluginConfiguration : Serializable {
    //groovy
    var groovyAllowList: Wrapper? = null
    var groovyBlockList: Wrapper? = null

    fun allowlist(action: Action<Wrapper>) {
        groovyAllowList = Wrapper().also(action::execute)
    }

    fun blocklist(action: Action<Wrapper>) {
        groovyBlockList = Wrapper().also(action::execute)
    }

//    //kts
//    var allowlist: MutableCollection<TestFilter> = mutableListOf()
//    var blocklist: MutableCollection<TestFilter> = mutableListOf()
//    fun allowlist(action: Action<MutableCollection<TestFilter>>) {
//        allowlist.also(action::execute)
//    }
//
//    fun blocklist(action: Action<MutableCollection<TestFilter>>) {
//        blocklist.also(action::execute)
//    }
}

open class Wrapper : Serializable {
    open var simpleClassNameFilter: ArrayList<String>? = null
    open var fullyQualifiedClassnameFilter: ArrayList<String>? = null
    open var testPackageFilter: ArrayList<String>? = null
    open var testMethodFilter: ArrayList<String>? = null
    open var annotationFilter: ArrayList<String>? = null
    open var annotationDataFilter: ArrayList<String>? = null
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
    this.testMethodFilter?.map { TestMethodFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.simpleClassNameFilter?.map { SimpleClassnameFilter(it.toRegex()) }?.let {
        mutableList.addAll(it)
    }
    this.annotationDataFilter?.map {
        val currentData = it.split(",")
        AnnotationDataFilter(currentData.first().toRegex(), currentData[1].toRegex())
    }?.let {
        mutableList.addAll(it)
    }
    return mutableList
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
//    if (groovyAllowList != null || groovyBlockList != null) {
        val allow = groovyAllowList?.toList() ?: emptyList()

        val block = groovyBlockList?.toList() ?: emptyList()
        return FilteringConfiguration(allow, block)
//    }
//    return FilteringConfiguration(allowlist, blocklist)
}


package com.malinskiy.marathon

import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.TestPackageFilter

open class FilterWrapper {
    open var simpleClassNameFilter: ArrayList<String>? = null
    open var fullyQualifiedClassnameFilter: ArrayList<String>? = null
    open var testPackageFilter: ArrayList<String>? = null
    open var annotationFilter: ArrayList<String>? = null
}

fun FilterWrapper.toList(): List<TestFilter> {
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

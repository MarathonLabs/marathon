package com.malinskiy.marathon.vendor.junit4.extensions

import com.malinskiy.marathon.test.Test

const val JUNIT_IGNORE_META_PROPERTY_NAME = "org.junit.Ignore"
private val ignoredMetaProperties = setOf(JUNIT_IGNORE_META_PROPERTY_NAME)

fun Test.isIgnored() = metaProperties.map { it.name }.intersect(ignoredMetaProperties).isNotEmpty()

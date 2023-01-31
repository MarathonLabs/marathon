package com.malinskiy.marathon.android.extension

import com.malinskiy.marathon.test.Test

const val JUNIT_IGNORE_META_PROPERTY_NAME = "org.junit.Ignore"
const val ANDROID_SUPPORT_SUPPRESS_META_PROPERTY_NAME = "android.support.test.filters.Suppress"
const val ANDROID_TEST_SUPPRESS_PROPERTY_NAME = "android.test.suitebuilder.annotation.Suppress"
private val ignoredMetaProperties =
    setOf(JUNIT_IGNORE_META_PROPERTY_NAME, ANDROID_SUPPORT_SUPPRESS_META_PROPERTY_NAME, ANDROID_TEST_SUPPRESS_PROPERTY_NAME)

fun Test.isIgnored() = metaProperties.map { it.name }.intersect(ignoredMetaProperties).isNotEmpty()

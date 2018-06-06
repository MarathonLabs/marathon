package com.malinskiy.marathon.test

data class Test(val pkg: String,
                val clazz: String,
                val method: String,
                val annotations: Collection<String>)

fun Test.toTestName(): String = "$pkg.$clazz#$method"
fun Test.toSafeTestName(): String = "$pkg.$clazz.$method"

package com.malinskiy.marathon.test

import java.util.Objects

data class Test(
    val pkg: String,
    val clazz: String,
    val method: String,
    val metaProperties: Collection<MetaProperty>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val test = other as Test
        return pkg == test.pkg &&
                clazz == test.clazz &&
                method == test.method
    }

    override fun hashCode(): Int {
        return Objects.hash(pkg, clazz, method)
    }
}

fun Test.toTestName(): String = toHumanReadableTestName()
fun Test.toSimpleSafeTestName(): String = "$clazz.$method"
fun Test.toSafeTestName() = toHumanReadableTestName(separator = '.')

fun Test.toHumanReadableClassName(): String {
    return StringBuilder().apply {
        if (pkg.isNotBlank()) {
            append("$pkg.")
        }
        append(clazz)
    }.toString()
}

fun Test.toHumanReadableTestName(separator: Char = '#'): String {
    return StringBuilder().apply {
        append(toHumanReadableClassName())
        append(separator)
        append(method)
    }.toString()
}

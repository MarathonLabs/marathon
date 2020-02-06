package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.test.Test
import java.util.*

class TestNameRegexTestMatcher(
    val pkg: String? = null,
    val clazz: String? = null,
    val method: String
) : TestMatcher {
    private val pkgRegex: Regex
        get() = (pkg ?: "^.*\$").toRegex()
    private val clazzRegex: Regex
        get() = (clazz ?: "^.*\$").toRegex()
    private val methodRegex: Regex
        get() = method.toRegex()

    override fun matches(test: Test): Boolean =
        pkgRegex.matches(test.pkg) &&
                clazzRegex.matches(test.clazz) &&
                methodRegex.matches(test.method)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val test = other as TestNameRegexTestMatcher
        return pkg == test.pkg &&
                clazz == test.clazz &&
                method == test.method
    }

    override fun hashCode(): Int {
        return Objects.hash(pkg, clazz, method)
    }

    override fun toString(): String {
        return "PkgRegex=\'$pkgRegex\'; ClazzRegex=\'$clazzRegex\'; MethodRegex=\'$methodRegex\'."
    }
}

fun Test.toTestMatcher(): TestNameRegexTestMatcher = TestNameRegexTestMatcher(
    pkg = "^$pkg\$",
    clazz = "^$clazz\$",
    method = "^$method\$"
)
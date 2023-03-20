package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.test.Test
import java.io.File

open class SingleValueTestFilter(
    val regex: Regex?,
    val values: List<String>?,
    val file: File?,
    val predicate: SingleValueTestFilter.(test: Test, values: List<String>?) -> Boolean,
) : TestFilter {
    private val fileValuesCache: List<String>? by lazy {
        file?.let { valuesFile ->
            if (valuesFile.exists()) {
                valuesFile.readLines().filter { it.isNotBlank() }
            } else {
                emptyList()
            }
        }
    }

    private fun readValues(): List<String>? {
        return values ?: fileValuesCache
    }

    override fun filter(tests: List<Test>): List<Test> = with(tests) {
        filter { predicate(this@SingleValueTestFilter, it, readValues()) }
    }

    override fun filterNot(tests: List<Test>): List<Test> = with(tests) {
        filterNot { predicate(this@SingleValueTestFilter, it, readValues()) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SingleValueTestFilter

        if (!regex.toString().contentEquals(other.regex.toString())) return false
        if (values != other.values) return false
        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        var result = regex?.hashCode() ?: 0
        result = 31 * result + (values?.hashCode() ?: 0)
        result = 31 * result + (file?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "${this::class.simpleName}(regex=$regex, values=$values, file=$file)"
    }
}

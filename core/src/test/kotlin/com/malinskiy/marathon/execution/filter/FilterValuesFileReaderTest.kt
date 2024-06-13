package com.malinskiy.marathon.execution.filter

import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.Test
import java.io.File

class FilterValuesFileReaderTest {

    private val valueFile = File(FilterValuesFileReaderTest::class.java.getResource("/testfilters/valuelist_1").file)
    private val valuesFileReader = FilterValuesFileReader()

    @Test
    fun shouldIgnoreComments() {
        val expectedValueList = listOf(
            "com.example.test1",
            "com.example.test2",
            "com.example",
            "ClassName#test4",
            "SimpleTestName",
            "SimpleTestName2"
        )
        val values = valuesFileReader.readValues(valueFile)

        values?.shouldHaveSize(expectedValueList.size)
        values?.shouldContainAll(expectedValueList)
    }
}

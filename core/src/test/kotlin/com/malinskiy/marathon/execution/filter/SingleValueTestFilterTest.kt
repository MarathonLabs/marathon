package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.Test as MarathonTest
import java.io.File
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class SingleValueTestFilterTest {
    @Test
    fun `if empty block list file all tests should be allowed`() {
        val inputTests = listOf(
            MarathonTest("pkg", "clazz", "method", emptyList()),
            MarathonTest("pkg", "clazz", "method2", emptyList())
        )

        val blocklistEmptyFileFilter = FullyQualifiedClassnameFilter(
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(
                regex = null,
                values = null,
                file = File(SingleValueTestFilterTest::class.java.getResource("/testfilters/emptylist").file)
            )
        )

        val filteredTests = blocklistEmptyFileFilter.filterNot(inputTests)

        assertEquals(inputTests, filteredTests)
    }

    @Test
    fun `if block list file is not exist all tests should be allowed`() {
        val inputTests = listOf(
            MarathonTest("pkg", "clazz", "method", emptyList()),
            MarathonTest("pkg", "clazz", "method2", emptyList())
        )

        val blocklistEmptyFileFilter = FullyQualifiedClassnameFilter(
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(
                regex = null,
                values = null,
                file = File("notExistFile")
            )
        )

        val filteredTests = blocklistEmptyFileFilter.filterNot(inputTests)

        assertEquals(inputTests, filteredTests)
    }

    @Test
    fun `if empty allow list list file all tests should be blocked`() {
        val inputTests = listOf(
            MarathonTest("pkg", "clazz", "method", emptyList()),
            MarathonTest("pkg", "clazz", "method2", emptyList())
        )

        val allowlistEmptyFileFilter = FullyQualifiedClassnameFilter(
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(
                regex = null,
                values = null,
                file = File(SingleValueTestFilterTest::class.java.getResource("/testfilters/emptylist").file)
            )
        )

        val filteredTests = allowlistEmptyFileFilter.filter(inputTests)

        assertEquals(emptyList(), filteredTests)
    }

    @Test
    fun `if allow list file is not exist all tests should be blocked`() {
        val inputTests = listOf(
            MarathonTest("pkg", "clazz", "method", emptyList()),
            MarathonTest("pkg", "clazz", "method2", emptyList())
        )

        val allowlistEmptyFileFilter = FullyQualifiedClassnameFilter(
            TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(
                regex = null,
                values = null,
                file = File("notExistFile")
            )
        )

        val filteredTests = allowlistEmptyFileFilter.filter(inputTests)

        assertEquals(emptyList(), filteredTests)
    }
}

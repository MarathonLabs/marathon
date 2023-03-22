package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.toTestName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import com.malinskiy.marathon.test.Test as MarathonTest

class TestFilterHelperKtTest {

    private val test1 = MarathonTest("com.example", "Test1", "test1", emptyList())
    private val test2 = MarathonTest("com.example", "Test2", "test1", emptyList())
    private val test3 = MarathonTest("com.example", "Test3", "test1", emptyList())
    private val test4 = MarathonTest("com.example", "Test4", "test1", emptyList())

    @Test
    fun `all filters is empty`() {
        val expected = listOf(test1, test2, test3, test4)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            emptyList(),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test single allow list`() {
        val expected = listOf(test1)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                )
            ),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test multiple allow list`() {
        val expected = listOf(test1, test2)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            ),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test single block list`() {
        val expected = listOf(test2, test3, test4)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                )
            )
        )

        assertEquals(expected, result)
    }
    @Test
    fun `test multiple block list`() {
        val expected = listOf(test3, test4)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test allow block list intersection`() {
        val expected = emptyList<MarathonTest>()

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            ),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test allow block list difference`() {
        val expected =  listOf(test1, test2)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            emptyList(),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            ),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test3.toTestName())
                ),
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test4.toTestName())
                )
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test single class regex filtering`() {
        val expected = listOf(test1, test2)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            listOf("""Test[1-2]""".toRegex()),
            emptyList(),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test multiple class regex filtering`() {
        val expected = listOf(test1, test2, test3, test4)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            listOf("""Test[1-2]""".toRegex(),
                   """Test[3-4]""".toRegex()),
            emptyList(),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test single class regex exclude filtering`() {
        val expected = emptyList<MarathonTest>()

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            listOf("""Test[5-9]""".toRegex()),
            emptyList(),
            emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test multiple single regex, with allow and blocking list filtering`() {
        val expected = listOf(test1)

        val result = applyTestFilters(
            listOf(test1, test2, test3, test4),
            listOf("""Test[1-3]""".toRegex()),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test1.toTestName(), test2.toTestName())
                )
            ),
            listOf(
                TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(
                    values = listOf(test2.toTestName())
                )
            )
        )

        assertEquals(expected, result)
    }
}

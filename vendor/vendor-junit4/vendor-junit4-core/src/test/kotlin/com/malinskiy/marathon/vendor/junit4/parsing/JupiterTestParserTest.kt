package com.malinskiy.marathon.vendor.junit4.parsing

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.vendor.junit4.rule.IntegrationTestRule
import com.malinskiy.marathon.vendor.junit4.rule.ParserRule
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should contain all`
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import com.malinskiy.marathon.test.Test as MarathonTest

@RunWith(Parameterized::class)
class JupiterTestParserTest(private val name: String, private val expected: List<MarathonTest>) {
    @get:Rule
    val temp = TemporaryFolder()

    @get:Rule
    val testParserRule = ParserRule()

    @get:Rule
    val integrationTestRule = IntegrationTestRule(temp)

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>?> {
            return listOf(
                arrayOf(
                    "SimpleTest", listOf(
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "SimpleTest",
                            "testSucceeds",
                            emptyList()
                        ),
                        MarathonTest("com.malinskiy.marathon.vendor.junit4.integrationtests", "SimpleTest", "testFails", emptyList()),
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "SimpleTest",
                            "testAssumptionFails",
                            emptyList()
                        ),
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "SimpleTest",
                            "testFailsWithNoMessage",
                            emptyList()
                        ),
                        MarathonTest("com.malinskiy.marathon.vendor.junit4.integrationtests", "SimpleTest", "testIgnored", emptyList()),
                    )
                ),
                arrayOf(
                    "CustomParameterizedTest", listOf(
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "CustomParameterizedTest",
                            "testcase1 raw",
                            emptyList()
                        ),
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "CustomParameterizedTest",
                            "testcase2 raw",
                            emptyList()
                        ),
                    )
                ),
                arrayOf(
                    "ParameterizedTest", listOf(
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "ParameterizedTest",
                            "testShouldCapitalize[a -> A]",
                            emptyList()
                        ),
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests",
                            "ParameterizedTest",
                            "testShouldCapitalize[b -> B]",
                            emptyList()
                        ),
                    )
                ),
                arrayOf(
                    "IgnoredTest", listOf(
                        MarathonTest(
                            "com.malinskiy.marathon.vendor.junit4.integrationtests", "IgnoredTest", "testIgnoredTest", listOf(
                                MetaProperty(name = "org.junit.Ignore")
                            )
                        ),
                    )
                ),
            )
        }
    }

    @Test
    fun testParsing() {
        runBlocking {
            val tests = testParserRule.testParser.extract(integrationTestRule.configuration)
            tests `should contain all` expected
        }
    }
}

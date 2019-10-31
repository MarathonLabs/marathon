package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.IOSComponentInfo
import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.ios.logparser.parser.TestRunProgressParser
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.itAnswers
import org.amshove.kluent.itReturns
import org.amshove.kluent.mock
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
import org.amshove.kluent.withFirstArg
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class ProgressParserSpek : Spek(
    {
        describe("TestRunProgressParser") {
            val mockFormatter = mock(PackageNameFormatter::class)
            val mockListener = mock(TestRunListener::class)
            val mockTimer = mock(Timer::class)
            val mockedTimeMillis = 1537187696000L
            val componentInfo = IOSComponentInfo(File("."), File("."), File("."))
            When calling mockTimer.currentTimeMillis() itReturns mockedTimeMillis

            val progressParser = TestRunProgressParser(mockTimer, mockFormatter, componentInfo, listOf(mockListener))

            beforeEachTest { When calling mockFormatter.format(any()) itAnswers withFirstArg() }
            afterEachTest { reset(mockListener, mockFormatter) }

            on("parsing testing output") {
                val testOutputFile =
                    File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)

                it("should apply package name formatter") {
                    testOutputFile.readLines().forEach {
                        progressParser.onLine(it)
                    }

                    verify(
                        mockFormatter,
                        atLeastOnce()
                    ) that mockFormatter.format("sample_appUITests") was called
                }
            }

            on("parsing single success output") {
                val testOutputFile =
                    File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)

                it("should report single start and success") {
                    testOutputFile.readLines().forEach {
                        progressParser.onLine(it)
                    }

                    Verify on mockListener that mockListener.testStarted(
                        Test(
                            "sample_appUITests",
                            "MoreTests",
                            "testPresentModal",
                            emptyList(),
                            componentInfo
                        )
                    ) was called
                    Verify on mockListener that mockListener.testPassed(
                        Test("sample_appUITests", "MoreTests", "testPresentModal", emptyList(), componentInfo),
                        mockedTimeMillis - 5315,
                        mockedTimeMillis
                    ) was called
                }
            }

            on("parsing multiple success output") {
                val testOutputFile =
                    File(javaClass.classLoader.getResource("fixtures/test_output/success_multiple_0.log").file)

                it("should report multiple starts and successes") {
                    testOutputFile.readLines().forEach {
                        progressParser.onLine(it)
                    }

                    Verify on mockListener that mockListener.testStarted(
                        Test(
                            "sample_appUITests",
                            "FlakyTests",
                            "testTextFlaky1",
                            emptyList(),
                            componentInfo
                        )
                    ) was called
                    Verify on mockListener that mockListener.testStarted(
                        Test(
                            "sample_appUITests",
                            "FlakyTests",
                            "testTextFlaky2",
                            emptyList(),
                            componentInfo
                        )
                    ) was called

                    Verify on mockListener that mockListener.testPassed(
                        Test("sample_appUITests", "FlakyTests", "testTextFlaky1", emptyList(), componentInfo),
                        mockedTimeMillis - 4415,
                        mockedTimeMillis
                    ) was called
                    Verify on mockListener that mockListener.testPassed(
                        Test("sample_appUITests", "FlakyTests", "testTextFlaky2", emptyList(), componentInfo),
                        mockedTimeMillis - 4118,
                        mockedTimeMillis
                    ) was called
                }
            }
        }
    })

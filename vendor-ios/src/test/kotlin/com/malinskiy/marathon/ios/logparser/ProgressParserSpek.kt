package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.verify
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

class ProgressParserSpek : Spek({
    describe("TestRunProgressParser") {
//        val commandMock = mock(Session.Command::class)
//        When calling Mocks.CommandExecutor.DEFAULT.startSession().exec("/Applications/Xcode.app/Contents/Developer/usr/bin/simctl list --json") itReturns commandMock
//        When calling commandMock.inputStream itReturns javaClass.classLoader.getResource("fixtures/simctl/list_output.json").openStream()

        val mockFormatter = mock(PackageNameFormatter::class)
        When calling mockFormatter.format(any()) itAnswers withFirstArg()
        val mockListener = mock(TestRunListener::class)
        val mockTimer = mock(Timer::class)
        val mockedTimeMillis = 1537187696000L
        When calling mockTimer.currentTimeMillis() itReturns mockedTimeMillis

        val progressParser = TestRunProgressParser(mockTimer, listOf(mockListener), mockFormatter)

        on("parsing testing output") {
            val testOutputFile = File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)

            it("should apply package name formatter") {
                testOutputFile.readLines().forEach {
                    progressParser.onLine(it)
                }

                verify(mockFormatter, atLeastOnce()) that mockFormatter.format("sample_appUITests") was called
            }
        }

        on("parsing single success output") {
            val testOutputFile = File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)

            it("should report single start and success") {
                testOutputFile.readLines().forEach {
                    progressParser.onLine(it)
                }

                Verify on mockListener that mockListener.testStarted(Test("sample-appUITests", "MoreTests", "testPresentModal", emptyList())) was called
                Verify on mockListener that mockListener.testPassed(Test("sample-appUITests", "MoreTests", "testPresentModal", emptyList()),
                        mockedTimeMillis - 5315,
                        mockedTimeMillis) was called
            }
        }

        on("parsing multiple success output") {
            val testOutputFile = File(javaClass.classLoader.getResource("fixtures/test_output/success_multiple_0.log").file)

            it("should report multiple starts and successes") {
                testOutputFile.readLines().forEach {
                    progressParser.onLine(it)
                }

                Verify on mockListener that mockListener.testStarted(Test("sample-appUITests", "FlakyTests", "testTextFlaky1", emptyList())) was called
                Verify on mockListener that mockListener.testStarted(Test("sample-appUITests", "FlakyTests", "testTextFlaky2", emptyList())) was called

                Verify on mockListener that mockListener.testPassed(Test("sample-appUITests", "FlakyTests", "testTextFlaky1", emptyList()),
                        mockedTimeMillis - 4415,
                        mockedTimeMillis) was called
                Verify on mockListener that mockListener.testPassed(Test("sample-appUITests", "FlakyTests", "testTextFlaky2", emptyList()),
                        mockedTimeMillis - 4118,
                        mockedTimeMillis) was called
            }
        }
    }
})

package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.ios.logparser.parser.TestRunProgressParser
import com.malinskiy.marathon.test.Test as MarathonTest
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class ProgressParserTest {
    private val mockFormatter = mock(PackageNameFormatter::class)
    private val mockListener = mock(TestRunListener::class)
    private val mockTimer = mock(Timer::class)
    private val mockedTimeMillis = 1537187696000L
    private val progressParser = TestRunProgressParser(mockTimer, mockFormatter, listOf(mockListener))

    @BeforeEach
    fun `setup mocks`() {
        reset(mockTimer, mockFormatter, mockListener)
        When calling mockTimer.currentTimeMillis() itReturns mockedTimeMillis
        When calling mockFormatter.format(any()) itAnswers withFirstArg()
    }

    @Test
    fun `parsing testing output should apply package name formatter`() {
        val testOutputFile =
            File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)
        testOutputFile.readLines().forEach {
            progressParser.onLine(it)
        }

        verify(
            mockFormatter,
            atLeastOnce()
        ) that mockFormatter.format("sample_appUITests") was called
    }

    @Test
    fun `parsing single success output should report single start and success`() {
        val testOutputFile =
            File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)
        testOutputFile.readLines().forEach {
            progressParser.onLine(it)
        }

        Verify on mockListener that mockListener.testStarted(
            MarathonTest(
                "sample_appUITests",
                "MoreTests",
                "testPresentModal",
                emptyList()
            )
        ) was called
        Verify on mockListener that mockListener.testPassed(
            MarathonTest("sample_appUITests", "MoreTests", "testPresentModal", emptyList()),
            mockedTimeMillis - 5315,
            mockedTimeMillis
        ) was called
    }

    @Test
    fun `parsing multiple success output should report multiple starts and successes`() {
        val testOutputFile =
            File(javaClass.classLoader.getResource("fixtures/test_output/success_multiple_0.log").file)
        testOutputFile.readLines().forEach {
            progressParser.onLine(it)
        }

        Verify on mockListener that mockListener.testStarted(
            MarathonTest(
                "sample_appUITests",
                "FlakyTests",
                "testTextFlaky1",
                emptyList()
            )
        ) was called
        Verify on mockListener that mockListener.testStarted(
            MarathonTest(
                "sample_appUITests",
                "FlakyTests",
                "testTextFlaky2",
                emptyList()
            )
        ) was called

        Verify on mockListener that mockListener.testPassed(
            MarathonTest("sample_appUITests", "FlakyTests", "testTextFlaky1", emptyList()),
            mockedTimeMillis - 4415,
            mockedTimeMillis
        ) was called
        Verify on mockListener that mockListener.testPassed(
            MarathonTest("sample_appUITests", "FlakyTests", "testTextFlaky2", emptyList()),
            mockedTimeMillis - 4118,
            mockedTimeMillis
        ) was called
    }
}

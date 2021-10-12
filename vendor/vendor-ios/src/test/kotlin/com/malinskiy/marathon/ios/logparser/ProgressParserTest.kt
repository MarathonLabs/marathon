package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.TestRunListener
import com.malinskiy.marathon.ios.logparser.parser.TestRunProgressParser
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class ProgressParserTest {
    private val mockFormatter = mock<PackageNameFormatter>()
    private val mockListener = mock<TestRunListener>()
    private val mockTimer = mock<Timer>()
    private val mockedTimeMillis = 1537187696000L
    private val progressParser = TestRunProgressParser(mockTimer, mockFormatter, listOf(mockListener))

    @BeforeEach
    fun `setup mocks`() {
        reset(mockTimer, mockFormatter, mockListener)
        whenever(mockTimer.currentTimeMillis()).thenReturn(mockedTimeMillis)
        whenever(mockFormatter.format(any())).thenAnswer { it.arguments.first() }
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
        ).format("sample_appUITests")
    }

    @Test
    fun `parsing single success output should report single start and success`() {
        val testOutputFile =
            File(javaClass.classLoader.getResource("fixtures/test_output/success_0.log").file)
        testOutputFile.readLines().forEach {
            progressParser.onLine(it)
        }

        verify(mockListener).testStarted(
            MarathonTest(
                "sample_appUITests",
                "MoreTests",
                "testPresentModal",
                emptyList()
            )
        )
        verify(mockListener).testPassed(
            MarathonTest("sample_appUITests", "MoreTests", "testPresentModal", emptyList()),
            mockedTimeMillis - 5315,
            mockedTimeMillis
        )
    }

    @Test
    fun `parsing multiple success output should report multiple starts and successes`() {
        val testOutputFile =
            File(javaClass.classLoader.getResource("fixtures/test_output/success_multiple_0.log").file)
        testOutputFile.readLines().forEach {
            progressParser.onLine(it)
        }

        verify(mockListener).testStarted(
            MarathonTest(
                "sample_appUITests",
                "FlakyTests",
                "testTextFlaky1",
                emptyList()
            )
        )
        verify(mockListener).testStarted(
            MarathonTest(
                "sample_appUITests",
                "FlakyTests",
                "testTextFlaky2",
                emptyList()
            )
        )

        verify(mockListener).testPassed(
            MarathonTest("sample_appUITests", "FlakyTests", "testTextFlaky1", emptyList()),
            mockedTimeMillis - 4415,
            mockedTimeMillis
        )
        verify(mockListener).testPassed(
            MarathonTest("sample_appUITests", "FlakyTests", "testTextFlaky2", emptyList()),
            mockedTimeMillis - 4118,
            mockedTimeMillis
        )
    }
}

package com.malinskiy.marathon.ios.logparser.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.ios.logparser.formatter.NoopPackageNameFormatter
import com.malinskiy.marathon.ios.test.TestEvent
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.reset

class TestRunProgressParserTest {
    private val mockTimer = mock<Timer>()
    private val mockedTimeMillis = 1537187696000L

    @BeforeEach
    fun `setup mocks`() {
        reset(mockTimer)
        whenever(mockTimer.currentTimeMillis()).thenReturn(mockedTimeMillis)
    }

    @Test
    fun testSample1() {
        val parser = TestRunProgressParser(mockTimer, NoopPackageNameFormatter)

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/success_0.log").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/success_0.expected").reader().readText().trimEnd())
    }

    @Test
    fun testSample2() {
        val parser = TestRunProgressParser(mockTimer, NoopPackageNameFormatter)

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/patrol.log").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/patrol.expected").reader().readText().trimEnd())
    }

    @Test
    fun testSample3() {
        val parser = TestRunProgressParser(mockTimer, NoopPackageNameFormatter)

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/success_multiple_0.log").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/success_multiple_0.expected").reader().readText().trimEnd())
    }

    @Test
    fun testX() {
        val parser = TestRunProgressParser(mockTimer, NoopPackageNameFormatter)

//        """Test Case '-[sample_appUITests.MoreTests testPresentModal]' started."""
        parser.process("""Test Case '-[RunnerUITests androidAppTest___tapsAround]' started.""")
    }
}

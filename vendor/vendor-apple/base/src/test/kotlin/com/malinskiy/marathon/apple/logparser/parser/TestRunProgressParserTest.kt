package com.malinskiy.marathon.apple.logparser.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.time.Timer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.reset
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        val parser = TestRunProgressParser(mockTimer, "")

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/success_0.log.input").bufferedReader().use {
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
        val parser = TestRunProgressParser(mockTimer, "testTarget")

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/patrol_0.log.input").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/patrol_0.expected").reader().readText().trimEnd())
    }

    @Test
    fun testSample3() {
        val parser = TestRunProgressParser(mockTimer, "")

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/success_multiple_0.log.input").bufferedReader().use {
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
    fun testSample3WithTargetOverride() {
        val parser = TestRunProgressParser(mockTimer, "testTarget")

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/success_multiple_0.log.input").bufferedReader().use {
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
    fun testSample4() {
        val parser = TestRunProgressParser(mockTimer, "testTarget")

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/patrol_1.log.input").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/patrol_1.expected").reader().readText().trimEnd())
    }
}

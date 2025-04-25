package com.malinskiy.marathon.apple.logparser.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.malinskiy.marathon.apple.test.TestEvent
import com.malinskiy.marathon.time.Timer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito.reset
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.stream.Stream

@RunWith(Parameterized::class)
class TestRunProgressParserTest {

    private val mockTimer = mock<Timer>()
    private val mockedTimeMillis = 1537187696000L

    @BeforeEach
    fun `setup mocks`() {
        reset(mockTimer)
        whenever(mockTimer.currentTimeMillis()).thenReturn(mockedTimeMillis)
    }

    data class TestData(val caseName: String, val targetName: String)

    class TestDataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments?>? {
            return Stream.of(
                Arguments.of(TestData(caseName = "success_0", targetName = "")),
                Arguments.of(TestData(caseName = "patrol_0", targetName = "testTarget")),
                Arguments.of(TestData(caseName = "success_multiple_0", targetName = "")),
                Arguments.of(TestData(caseName = "success_multiple_0", targetName = "testTarget")),
                Arguments.of(TestData(caseName = "patrol_1", targetName = "testTarget")),
                Arguments.of(TestData(caseName = "timeout_0", targetName = "")),
                Arguments.of(TestData(caseName = "timeout_1", targetName = "")),
                Arguments.of(TestData(caseName = "failure_0", targetName = "")),
                Arguments.of(TestData(caseName = "failure_1", targetName = "")),
            )
        }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestDataProvider::class)
    fun test(testData: TestData) {
        val parser = TestRunProgressParser(mockTimer, testData.targetName)

        val events = mutableListOf<TestEvent>()
        javaClass.getResourceAsStream("/fixtures/test_output/${testData.caseName}.log.input").bufferedReader().use {
            it.lines().forEach { line ->
                parser.process(line)?.let {
                    events.addAll(it)
                }
            }
        }

        assertThat(events.map { it.toString() }.reduce { acc, s -> acc + "\n" + s })
            .isEqualTo(javaClass.getResourceAsStream("/fixtures/test_output/${testData.caseName}.expected").reader().readText().trimEnd())
    }
}

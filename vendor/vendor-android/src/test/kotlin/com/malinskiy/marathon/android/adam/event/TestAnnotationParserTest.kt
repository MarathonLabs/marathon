package com.malinskiy.marathon.android.adam.event

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.adam.request.testrunner.TestIdentifier
import com.malinskiy.marathon.test.MetaProperty
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class TestAnnotationParserTest {
    @ParameterizedTest
    @MethodSource
    fun testExtractionV2(value: String, expected: List<MetaProperty>) {
        val testEnded = TestEnded(
            TestIdentifier("class", "method"), metrics =
            mapOf(
                "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v2" to value,
            )
        )

        val parser = TestAnnotationParser()
        val annotations = parser.extractAnnotations(testEnded)
        assertThat(annotations).containsExactlyInAnyOrder(*expected.toTypedArray())
    }

    @ParameterizedTest
    @MethodSource
    fun testExtractionV4(value: String, expected: List<MetaProperty>) {
        val testEnded = TestEnded(
            TestIdentifier("class", "method"), metrics =
            mapOf(
                "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v4" to value,
            )
        )

        val parser = TestAnnotationParser()
        val annotations = parser.extractAnnotations(testEnded)
        assertThat(annotations).containsExactlyInAnyOrder(*expected.toTypedArray())
    }
    
    companion object {
        @JvmStatic
        fun testExtractionV2(): Stream<Arguments> {
            return Stream.of(
                arguments("[]", listOf<MetaProperty>()),
                arguments(
                    "[io.qameta.allure.kotlin.Description(useJavaDoc=false:value=Some description, but with quotes,), io.qameta.allure.kotlin.TmsLink(value=TEST-666)]",
                    listOf(
                        MetaProperty(
                            name = "io.qameta.allure.kotlin.Description", values = mapOf(
                                "useJavaDoc" to "false",
                                "value" to "Some description, but with quotes,",
                            )
                        ),
                        MetaProperty(
                            name = "io.qameta.allure.kotlin.TmsLink", values = mapOf(
                                "value" to "TEST-666",
                            )
                        )
                    )
                ),
            )
        }
        @JvmStatic
        fun testExtractionV4(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    "[64Lorg.junit.Test(34Lexpected=class org.junit.Test\$None9Ltimeout=0)]",
                    listOf(
                        MetaProperty(
                            name = "org.junit.Test", values = mapOf(
                                "expected" to "class org.junit.Test\$None",
                                "timeout" to "0",
                            )
                        ),
                    )
                ),
                arguments(
                    "[87Lorg.junit.Test(68Lvalue=something is\nbound to happen (when you're dealing with parsin))]",
                    listOf(
                        MetaProperty(
                            name = "org.junit.Test", values = mapOf(
                                "value" to "something is\nbound to happen (when you're dealing with parsin)",
                            )
                        ),
                    )
                )
            )
        }

    }
}

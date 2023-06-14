package com.malinskiy.marathon.android.adam.event

import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.marathon.test.MetaProperty

class TestAnnotationParser {
    fun extractAnnotations(event: TestEnded): List<MetaProperty> {
        val v2 = event.metrics[METRIC_ANNOTATION_V2]
        return when {
            v2 != null -> {
                val removeSurrounding = v2.removeSurrounding("[", "]")
                METRIC_ANNOTATION_V2_REGEX.findAll(removeSurrounding)
                    .map { it.value.trim() }
                    .filter { !it.isNullOrEmpty() }
                    .toList().map { serializedAnnotation ->
                        val index = serializedAnnotation.indexOfFirst { it == '(' }
                        val name = serializedAnnotation.substring(0 until index)
                        val parameters = serializedAnnotation.substring(index).removeSurrounding("(", ")").split(":")
                        val values = parameters.mapNotNull { parameter ->
                            val split = parameter.split("=")
                            if (split.size == 2) {
                                Pair(split[0], split[1])
                            } else {
                                null
                            }
                        }.toMap()
                        MetaProperty(name = name, values = values)
                    }
            }

            else -> emptyList()
        }
    }

    companion object {
        private const val METRIC_ANNOTATION_V2 = "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v2"
        private val METRIC_ANNOTATION_V2_REGEX = "\\p{Graph}+\\([^\\)]*\\)".toRegex()
    }
}

package com.malinskiy.marathon.android.adam.event

import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.marathon.test.MetaProperty

class TestAnnotationParser {
    fun extractAnnotations(event: TestEnded): List<MetaProperty> {
        val v2 = event.metrics[METRIC_ANNOTATION_V2]
        val v3 = event.metrics[METRIC_ANNOTATION_V3]
        return when {
            v3 != null -> {
                val removeSurrounding = v3.removeSurrounding("[", "]")
                METRIC_ANNOTATION_V2_REGEX.findAll(removeSurrounding)
                    .map { it.value.trim() }
                    .filter { !it.isNullOrEmpty() }
                    .toList().map { serializedAnnotation ->
                        val index = serializedAnnotation.indexOfFirst { it == '(' }
                        val name = serializedAnnotation.substring(0 until index)
                        var serializedParameters = serializedAnnotation.substring(index).removeSurrounding("(", ")")

                        val values = mutableListOf<Pair<String, String>>()
                        while (true) {
                            val splitPoint = serializedParameters.indexOfFirst { it == 'L' }
                            if (splitPoint == -1) break
                            val length = serializedParameters.substring(0 until splitPoint).toIntOrNull() ?: break
                            serializedParameters = serializedParameters.removeRange(0..splitPoint)
                            val parameter = serializedParameters.substring(0 until length)
                            serializedParameters = serializedParameters.removeRange(0 until length)
                            if (parameter.length > 1 && parameter.contains("=")) {
                                values.add(Pair(parameter.substringBefore("="), parameter.substringAfter("=")))
                            }
                        }

                        MetaProperty(name = name, values = values.toMap())
                    }
            }

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
        private const val METRIC_ANNOTATION_V3 = "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v3"
        private val METRIC_ANNOTATION_V2_REGEX = "\\p{Graph}+\\([^\\)]*\\)".toRegex()
    }
}

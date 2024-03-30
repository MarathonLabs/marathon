package com.malinskiy.marathon.android.adam.event

import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.marathon.test.MetaProperty

class TestAnnotationParser {
    fun extractAnnotations(event: TestEnded): List<MetaProperty> {
        val v2 = event.metrics[METRIC_ANNOTATION_V2]
        val v3 = event.metrics[METRIC_ANNOTATION_V3]
        val v4 = event.metrics[METRIC_ANNOTATION_V4]
        return when {
            v4 != null -> {
                var body = v4.removeSurrounding("[", "]")
                val result = mutableListOf<MetaProperty>()

                val annotations = generateSequence {
                    val splitPoint = body.indexOfFirst { it == 'L' }
                    if (splitPoint == -1) return@generateSequence null

                    val length = body.substring(0 until splitPoint).toIntOrNull() ?: return@generateSequence null
                    body = body.removeRange(0..splitPoint)
                    var annotation = body.substring(0 until length)
                    body = body.removeRange(0 until length)
                    body = body.removePrefix(",")
                    body = body.trimStart()

                    return@generateSequence annotation
                }

                for (annotation in annotations) {
                    val splitPoint = annotation.indexOfFirst { it == '(' }
                    if (splitPoint == -1) continue

                    val name = annotation.substring(0 until splitPoint)
                    var annotation = annotation.substring(splitPoint + 1)
                    val values = mutableListOf<Pair<String, String>>()
                    while (true) {
                        val splitPoint = annotation.indexOfFirst { it == 'L' }
                        if (splitPoint == -1) break
                        val length = annotation.substring(0 until splitPoint).toIntOrNull() ?: break
                        annotation = annotation.removeRange(0..splitPoint)
                        val parameter = annotation.substring(0 until length)
                        annotation = annotation.removeRange(0 until length)
                        if (parameter.length > 1 && parameter.contains("=")) {
                            values.add(Pair(parameter.substringBefore("="), parameter.substringAfter("=")))
                        }
                    }

                    result.add(MetaProperty(name = name, values = values.toMap()))
                }

                result
            }

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
        private const val METRIC_ANNOTATION_V4 = "com.malinskiy.adam.junit4.android.listener.TestAnnotationProducer.v4"
        private val METRIC_ANNOTATION_V2_REGEX = "\\p{Graph}+\\([^\\)]*\\)".toRegex()
    }
}

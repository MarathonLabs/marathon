package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

class AnnotationFilter(
    @JsonProperty("regex") regex: Regex? = null,
    @JsonProperty("values") values: List<String>? = null,
    @JsonProperty("file") file: File? = null,
) : SingleValueTestFilter(regex, values, file, { test, filterValues ->
    when {
        regex != null -> {
            test.metaProperties.map { it.name }.any(regex::matches)
        }
        filterValues != null -> {
            test.metaProperties.map { it.name }.intersect(filterValues).isNotEmpty()
        }
        else -> {
            true
        }
    }
})

package com.malinskiy.marathon.execution.filter

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.execution.SingleValueTestFilter
import com.malinskiy.marathon.test.toHumanReadableTestName
import java.io.File

class FullyQualifiedTestnameFilter(
    @JsonProperty("regex") regex: Regex? = null,
    @JsonProperty("values") values: List<String>? = null,
    @JsonProperty("file") file: File? = null,
) : SingleValueTestFilter(regex, values, file, { test, filterValues ->
    (regex?.matches(test.toHumanReadableTestName()) ?: true) && (filterValues?.contains(test.toHumanReadableTestName()) ?: true)
})

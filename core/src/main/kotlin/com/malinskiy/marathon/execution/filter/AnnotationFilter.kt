package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class AnnotationFilter(cnf: TestFilterConfiguration.AnnotationFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        when {
            regex != null -> {
                test.metaProperties.map { it.name }.any(regex::matches)
            }
            values != null -> {
                test.metaProperties.map { it.name }.intersect(values).isNotEmpty()
            }
            else -> {
                true
            }
        }
    })

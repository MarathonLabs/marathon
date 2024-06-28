package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.toSimpleSafeTestName

class SimpleTestnameFilter(cnf: TestFilterConfiguration.SimpleTestnameFilterConfiguration) :
    SingleValueTestFilter(
        cnf.regex,
        cnf.values,
        cnf.file,
        cnf.enabled,
        { test, values ->
            val simpleSafeTestName = test.toSimpleSafeTestName(methodSeparator = '#')
            (regex?.matches(simpleSafeTestName) ?: true) && (values?.contains(simpleSafeTestName) ?: true)
        },
    )

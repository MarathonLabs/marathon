package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.toHumanReadableTestName

class FullyQualifiedTestnameFilter(cnf: TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        (regex?.matches(test.toHumanReadableTestName()) ?: true) && (values?.contains(test.toHumanReadableTestName()) ?: true)
    })

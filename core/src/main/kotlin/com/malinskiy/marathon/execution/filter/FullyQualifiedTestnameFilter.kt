package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.toTestName

class FullyQualifiedTestnameFilter(cnf: TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration) :
    SingleValueTestFilter(
        cnf.regex,
        cnf.values,
        cnf.file,
        cnf.enabled,
        { test, values ->
            (regex?.matches(test.toTestName()) ?: true) && (values?.contains(test.toTestName()) ?: true)
        },
    )

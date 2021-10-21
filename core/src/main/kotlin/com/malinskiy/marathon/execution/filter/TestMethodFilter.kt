package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class TestMethodFilter(cnf: TestFilterConfiguration.TestMethodFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        (regex?.matches(test.method) ?: true) && (values?.contains(test.method) ?: true)
    })

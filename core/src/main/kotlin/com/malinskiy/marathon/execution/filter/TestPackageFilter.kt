package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class TestPackageFilter(cnf: TestFilterConfiguration.TestPackageFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        (regex?.matches(test.pkg) ?: true) && (values?.contains(test.pkg) ?: true)
    })

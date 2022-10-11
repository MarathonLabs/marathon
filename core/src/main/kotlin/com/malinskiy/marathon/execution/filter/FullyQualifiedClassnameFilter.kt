package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.toClassName

class FullyQualifiedClassnameFilter(cnf: TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        (regex?.matches(test.toClassName()) ?: true) && (values?.contains(test.toClassName()) ?: true)
    })

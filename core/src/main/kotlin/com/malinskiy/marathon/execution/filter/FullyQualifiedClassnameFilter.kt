package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class FullyQualifiedClassnameFilter(cnf: TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration) :
    SingleValueTestFilter(cnf.regex, cnf.values, cnf.file, { test, values ->
        (regex?.matches("${test.pkg}.${test.clazz}") ?: true) && (values?.contains("${test.pkg}.${test.clazz}") ?: true)
    })

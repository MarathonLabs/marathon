package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class SimpleClassnameFilter(cnf: TestFilterConfiguration.SimpleClassnameFilterConfiguration) :
    SingleValueTestFilter(cnf.regex,
                          cnf.values,
                          cnf.file,
                          cnf.enabled,
                          { test, values ->
                              (regex?.matches(test.clazz) ?: true) && (values?.contains(test.clazz) ?: true)
                          },
    )

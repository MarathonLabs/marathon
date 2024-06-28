package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration

class TestPackageFilter(cnf: TestFilterConfiguration.TestPackageFilterConfiguration) :
    SingleValueTestFilter(
        cnf.regex,
        cnf.values,
        cnf.file,
        cnf.enabled,
        { test, values ->
            (regex?.matches(test.pkg) ?: true) && when (cnf.subpackages) {
                true -> (values?.any {
                    test.pkg == it ||
                        test.pkg.startsWith("$it$PACKAGE_SEPARATOR")
                } ?: true)

                false -> (values?.contains(test.pkg) ?: true)
            }
        },
    ) {
    companion object {
        const val PACKAGE_SEPARATOR = '.'
    }
}

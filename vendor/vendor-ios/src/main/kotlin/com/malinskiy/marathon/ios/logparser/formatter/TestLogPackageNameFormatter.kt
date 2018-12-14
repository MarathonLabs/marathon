package com.malinskiy.marathon.ios.logparser.formatter

/**
 * When applied, replaces module name reported in xcodebuild log with testing target name in order
 * to unify test representation.
 */
class TestLogPackageNameFormatter(private val productModuleName: String,
                                  private val targetName: String): PackageNameFormatter {
    override fun format(name: String?): String? {
        return name?.replace(productModuleName, targetName)
    }
}

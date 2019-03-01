package com.malinskiy.marathon.ios.logparser.formatter

/**
 * When applied, replaces module name reported in xcodebuild log with testing target name in order
 * to unify test representation.
 *
 * @param targetNameMappings a map with target names as keys and product module names as corresponding values.
 */
class TestLogPackageNameFormatter(private val poductModuleNameMap: Map<String, String>): PackageNameFormatter {
    override fun format(name: String?): String? {
        if (name == null) { return null }

        val result = poductModuleNameMap.entries
                .firstOrNull { name.contains(it.key) }
                ?.let { name.replace(it.key, it.value) }
        return result
    }
}

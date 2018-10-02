package com.malinskiy.marathon.ios.xctestrun

sealed class PropertyListKey(private val value: String) {
    class ModuleName(val moduleName: String): PropertyListKey(moduleName)

    object ProductModuleName: PropertyListKey("ProductModuleName")
    object IsUITestBundle: PropertyListKey("IsUITestBundle")
    object SkipTestIdentifiers: PropertyListKey("SkipTestIdentifiers")
    object EnvironmentVariables: PropertyListKey("EnvironmentVariables")
    object TestingEnvironmentVariables: PropertyListKey("TestingEnvironmentVariables")

    fun toKeyString(): String {
        return value
    }
}

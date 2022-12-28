package com.malinskiy.marathon.ios.plist

sealed class PropertyListKey(private val value: String) {
    object __xctestrun_metadata__ : PropertyListKey("__xctestrun_metadata__")
    class TargetName(targetName: String) : PropertyListKey(targetName)

    object ProductModuleName : PropertyListKey("ProductModuleName")
    object IsUITestBundle : PropertyListKey("IsUITestBundle")
    object SkipTestIdentifiers : PropertyListKey("SkipTestIdentifiers")
    object EnvironmentVariables : PropertyListKey("EnvironmentVariables")
    object TestingEnvironmentVariables : PropertyListKey("TestingEnvironmentVariables")
    object SystemAttachmentLifetime : PropertyListKey("SystemAttachmentLifetime")

    /**
     * @see <a href="https://developer.apple.com/documentation/bundleresources/information_property_list/cfbundleidentifier">Apple documentation</a>
     */
    object CFBundleIdentifier : PropertyListKey("CFBundleIdentifier")
    /**
     * @see <a href="https://developer.apple.com/documentation/bundleresources/information_property_list/minimumosversion">Apple documentation</a>
     */
    object MinimumOSVersion : PropertyListKey("MinimumOSVersion")
    
    fun toKeyString(): String {
        return value
    }
}

package com.malinskiy.marathon.ios.xctestrun

sealed class XctestrunKey(private val value: String): PropertyListKey {
    object __xctestrun_metadata__: XctestrunKey("__xctestrun_metadata__")
    class TargetName(targetName: String): XctestrunKey(targetName)

    object ProductModuleName: XctestrunKey("ProductModuleName")
    object IsUITestBundle: XctestrunKey("IsUITestBundle")
    object SkipTestIdentifiers: XctestrunKey("SkipTestIdentifiers")
    object EnvironmentVariables: XctestrunKey("EnvironmentVariables")
    object TestingEnvironmentVariables: XctestrunKey("TestingEnvironmentVariables")
    object TestHostPath: XctestrunKey("TestHostPath") // __TESTROOT__/Debug-iphonesimulator/Agoda.ConsumerUITests-Runner.app
    object TestBundlePath: XctestrunKey("TestBundlePath") // __TESTHOST__/PlugIns/Agoda.ConsumerUITests.xctest

    override fun toKeyString(): String = value
}
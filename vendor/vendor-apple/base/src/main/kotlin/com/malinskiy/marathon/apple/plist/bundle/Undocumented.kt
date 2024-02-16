package com.malinskiy.marathon.apple.plist.bundle

import com.dd.plist.NSDictionary
import com.malinskiy.marathon.apple.plist.arrayDelegateFor
import com.malinskiy.marathon.apple.plist.delegateFor

class Undocumented(delegate: NSDictionary) {
    val bundleSupportedPlatforms: Array<String> by delegate.arrayDelegateFor("CFBundleSupportedPlatforms", optional = true)
    val buildMachineOSBuild: String? by delegate.delegateFor("BuildMachineOSBuild")
    val containsUITests: Boolean? by delegate.delegateFor("XCTContainsUITests")
    val dtCompiler: String? by delegate.delegateFor("DTCompiler")
    val dtPlatformBuild: String? by delegate.delegateFor("DTPlatformBuild")
    val dtPlatformName: String? by delegate.delegateFor("DTPlatformName")
    val dtPlatformVersion: String? by delegate.delegateFor("DTPlatformVersion")
    val dtSDKBuild: String? by delegate.delegateFor("DTSDKBuild")
    val dtSDKName: String? by delegate.delegateFor("DTSDKName")
    val dtXcode: String? by delegate.delegateFor("DTXcode")
    val dtXcodeBuild: String? by delegate.delegateFor("DTXcodeBuild")
    val uiDeviceFamily: Array<Int> by delegate.arrayDelegateFor("UIDeviceFamily", true)
}
